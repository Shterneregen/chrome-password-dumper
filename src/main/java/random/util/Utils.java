package random.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    public static void get(String url) {
        BufferedReader in = null;
        InputStreamReader isr = null;
        try {
            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

            connection.setRequestMethod("GET");

            isr = new InputStreamReader(connection.getInputStream());
            in = new BufferedReader(isr);
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            System.out.println(response.toString());
        } catch (IOException e) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            close(in);
            close(isr);
        }

    }

    private static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
