package random.services;

import random.chrome.ChromeAccount;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static random.services.EncryptionService.encrypt;
import static random.util.Utils.getCurrentTime;

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

        final List<String> accountsInfo = getAccountsInfoAsTable(profiles);

        String savePath = newFiePath != null && !newFiePath.equals("") ? newFiePath : ".";
        File file = new File(savePath + System.getProperty("file.separator"),
                "%s-%s.txt".formatted(getCurrentTime(), System.getProperty("user.name")));

        if (file.exists()) {
            file.delete();
        }
        file.getParentFile().mkdirs();
        file.createNewFile();

        List<String> lines = pubKeyPath != null && !pubKeyPath.isEmpty()
                ? List.of(encrypt(pubKeyPath, accountsInfo.toString()))
                : accountsInfo;
        Files.write(file.toPath(), lines, UTF_8);
    }

    public void showAccountsInfo(Map<String, List<ChromeAccount>> profiles) {
        getAccountsInfoAsTable(profiles).forEach(System.out::println);
    }

    public void showShortAccountsInfo(Map<String, List<ChromeAccount>> profiles) {
        getShortAccountsInfo(profiles).forEach(System.out::println);
    }

    private List<String> getAccountsInfoAsTable(Map<String, List<ChromeAccount>> profiles) {
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

    private List<String> getShortAccountsInfo(Map<String, List<ChromeAccount>> profiles) {
        final List<String> lines = new ArrayList<>();
        for (Entry<String, List<ChromeAccount>> profile : profiles.entrySet()) {
            lines.add("==================================================");
            lines.add(profile.getKey());
            lines.add("==================================================");

            int maxUserNameLen = profile.getValue().stream()
                    .map(ChromeAccount::usernameValue)
                    .map(String::length)
                    .max(naturalOrder())
                    .orElse(0);

            int maxPswLen = profile.getValue().stream()
                    .map(ChromeAccount::password)
                    .map(String::length)
                    .max(naturalOrder())
                    .orElse(0);

            AtomicInteger counter = new AtomicInteger();
            String pattern = "%-3d | %-" + maxUserNameLen + "s | %-" + maxPswLen + "s | %s";

            List<String> data = profile.getValue().stream()
                    .filter(acc -> !acc.usernameValue().isBlank() || !acc.password().isBlank())
                    .sorted(comparing(ChromeAccount::usernameValue))
                    .map(account -> pattern.formatted(
                            counter.incrementAndGet(),
                            account.usernameValue(),
                            account.password(),
                            account.originUrl()
                    ))
                    .toList();
            lines.addAll(data);
        }
        return lines;
    }
}
