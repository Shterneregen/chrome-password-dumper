package random;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TestUtils {
    public static final Path TEST_RESOURCES = Paths.get("src", "test", "resources");

    public static Path getTestResource(String fileName) {
        return Paths.get(TEST_RESOURCES.toString(), fileName);
    }
}
