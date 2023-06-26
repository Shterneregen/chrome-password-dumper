package random.util;

import random.Main;

import java.io.File;

public enum OperatingSystem {

    WINDOWS(buildPath("AppData\\Local\\Google\\Chrome\\User Data\\"), buildRunningPath()),
    MAC(buildPath("Library/Application Support/Google/Chrome/"), buildRunningPath()),
    LINUX(buildPath(".config/google-chrome/"), buildRunningPath()),
    UNKNOWN("", "");

    public static OperatingSystem getOperatingSystem() {
        final String os = System.getProperty("os.name");
        if (os.contains("Mac")) {
            return MAC;
        } else if (os.contains("Windows")) {
            return WINDOWS;
        } else if (os.contains("Linux")) {
            return LINUX;
        } else {
            return UNKNOWN;
        }
    }

    private final String chromePath;

    private final String runningPath;

    OperatingSystem(final String chromePath, final String runningPath) {
        this.chromePath = chromePath;
        this.runningPath = runningPath;
    }

    public String getChromePath() {
        return chromePath;
    }

    public String getSavePath() {
        return runningPath.substring(0, runningPath.lastIndexOf(File.separatorChar) + 1) + "Accounts";
    }

    private static String buildPath(String chromePath) {
        return System.getProperty("user.home") + File.separator + chromePath;
    }

    private static String buildRunningPath() {
        return Main.class.getProtectionDomain().getCodeSource().getLocation().toString()
                .replace("%20", " ").replace("file:", "").replace("/", File.separator);
    }
}
