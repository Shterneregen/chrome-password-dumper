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

    private String path;

    private String runningPath;

    OperatingSystem(final String path, final String runningPath) {
        this.path = path;
        this.runningPath = runningPath;
    }

    public String getChromePath() {
        return path;
    }

    public String getSavePath() {
        return runningPath.substring(0, runningPath.lastIndexOf(File.separatorChar) + 1) + "Accounts";
    }

    private static String buildPath(String chromPath) {
        return System.getProperty("user.home") + File.separator + chromPath;
    }

    private static String buildRunningPath() {
        return Main.class.getProtectionDomain().getCodeSource().getLocation().toString()
                .replace("%20", " ").replace("file:", "").replace("/", File.separator);
    }
}
