package random.util;

import random.chrome.ChromeAccount;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

public class AccountFormatterService {
    private static final TableGenerator tableGenerator = new TableGenerator();
    private static final List<String> headersList = List.of(
            "#",
            "Username",
            "Password",
            "Element",
            "Display name",
            "Created",
            "Last Used",
            "Password Modified",
            "Times used",
            "Action URL",
            "Origin URL"
    );

    public void saveAccountsInfoToFile(Map<String, List<ChromeAccount>> profiles,
                                       String newFiePath, String pubKeyPath) throws IOException {

        final List<String> lines = getAccountsInfo(profiles);

        String savePath = newFiePath != null && !newFiePath.equals("") ? newFiePath : ".";
        File file = new File(savePath + System.getProperty("file.separator"),
                "%s-%s.txt".formatted(Utils.getCurrentTime(), System.getProperty("user.name")));

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

    public void showAccountsInfo(Map<String, List<ChromeAccount>> profiles) {
        List<String> lines = getAccountsInfo(profiles);
        lines.forEach(System.out::println);
    }

    private List<String> getAccountsInfo(Map<String, List<ChromeAccount>> profiles) {
        final List<String> lines = new ArrayList<>();
        for (Entry<String, List<ChromeAccount>> profile : profiles.entrySet()) {
            lines.add("==================================================");
            lines.add(profile.getKey());
            lines.add("==================================================");

            AtomicInteger counter = new AtomicInteger();
            List<List<String>> rowsList = profile.getValue().stream()
                    .map(account -> List.of(
                            String.valueOf(counter.incrementAndGet()),
                            account.usernameValue(),
                            account.password(),
                            account.usernameElement(),
                            account.displayName(),
                            account.dateCreated(),
                            account.dateLastUsed(),
                            account.datePasswordModified(),
                            String.valueOf(account.timesUsed()),
                            account.actionUrl(),
                            account.originUrl()
                    )).toList();
            lines.add(tableGenerator.generateTable(headersList, rowsList));
        }
        return lines;
    }
}
