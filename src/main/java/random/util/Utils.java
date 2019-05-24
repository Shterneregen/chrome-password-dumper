package random.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

public class Utils {

    public static void saveToFile(String str) throws IOException {
        String[] lines = str.split(",");
        File file = new File(getCurrentTime() + ".txt");
        file.createNewFile();
//        Files.write(file.toPath(), str.getBytes("UTF-8"));
        Files.write(file.toPath(), Arrays.asList(lines));
    }

    public static String getStringFromReader(Reader reader) throws IOException {
        final int BUFFER_SIZE = 4096;
        char[] buffer = new char[BUFFER_SIZE];
        Reader bufferedReader = new BufferedReader(reader, BUFFER_SIZE);
        StringBuilder builder = new StringBuilder();
        int length = 0;
        while ((length = bufferedReader.read(buffer, 0, BUFFER_SIZE)) != -1) {
            builder.append(buffer, 0, length);
        }
        reader.close();

        return builder.toString();
    }

    private static String getCurrentTime() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("dd-MM-yy-HH-mm-ss");
        return currentTime.format(cal.getTime());
    }

}
