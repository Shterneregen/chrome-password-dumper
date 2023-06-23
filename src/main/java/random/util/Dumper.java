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

import static java.util.Optional.ofNullable;

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
        TableGenerator tableGenerator = new TableGenerator();
        List<String> headersList = new ArrayList<>();
        headersList.add("#");
        headersList.add("Username");
        headersList.add("Element");
        headersList.add("Display name");
        headersList.add("Password");
        headersList.add("date_created");
        headersList.add("date_last_used");
        headersList.add("date_password_modified");
        headersList.add("Times used");
        headersList.add("Action URL");
        headersList.add("Origin URL");

        final List<String> lines = new ArrayList<>();
        for (Map.Entry<String, List<ChromeAccountEntry>> profile : profiles.entrySet()) {
            String profileName = profile.getKey();
            List<ChromeAccountEntry> profileEntries = profile.getValue();
            lines.add("==================================================");
            lines.add("%s (%d)".formatted(profileName, profileEntries.size()));
            lines.add("==================================================");

            if (profileEntries.size() > 0) {
                int counter = 0;
                List<List<String>> rowsList = new ArrayList<>();

                for (final ChromeAccountEntry account : profileEntries) {
                    boolean returnInfo = onlyWithNotEmptyPsw
                            ? !account.password().equals("")
                            : !account.actionUrl().equals("") || !account.usernameValue().equals("") || !account.password().equals("");

                    if (returnInfo) {
                        List<String> row = new ArrayList<>();
                        row.add(String.valueOf(++counter));
                        row.add(account.usernameValue());
                        row.add(account.usernameElement());
                        row.add(account.displayName());
                        row.add(account.password());
                        row.add(account.dateCreated());
                        row.add(account.dateLastUsed());
                        row.add(account.datePasswordModified());
                        row.add(ofNullable(account.timesUsed()).map(Object::toString).orElse(null));
                        row.add(account.actionUrl());
                        row.add(account.originUrl());
                        rowsList.add(row);
                    }
                }
                lines.add(tableGenerator.generateTable(headersList, rowsList));
            }
        }
        return lines;
    }
}
