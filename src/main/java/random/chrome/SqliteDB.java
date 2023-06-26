package random.chrome;

import org.sqlite.SQLiteConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

public class SqliteDB {

    public Connection connectToTempDB(File dbFile, String tempDbName) throws IOException {
        try {
            Path tempDbPath = makeDbCopy(dbFile, tempDbName);

            SQLiteConfig config = new SQLiteConfig();
            config.setReadOnly(true);
            config.setTransactionMode(SQLiteConfig.TransactionMode.EXCLUSIVE);
            Connection connection = config.createConnection("jdbc:sqlite:%s".formatted(tempDbPath));
            connection.setAutoCommit(true);
            return connection;
        } catch (final IOException e) {
            throw new IOException("Error copying database! Does the login file exist?");
        } catch (final SQLException e) {
            throw new IOException("Error connecting to database! Is database corrupted?");
        }
    }

    private Path makeDbCopy(File dbFile, String tempDbName) throws IOException {
        Path tempDB = Files.createTempFile(tempDbName, null);
        FileOutputStream out = new FileOutputStream(tempDB.toFile());
        Files.copy(Paths.get(dbFile.getPath()), out);
        out.close();
        tempDB.toFile().deleteOnExit();
        return tempDB;
    }
}
