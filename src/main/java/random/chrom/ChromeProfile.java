package random.chrom;

public class ChromeProfile {

    private static int profileCount = 0;

    private final int id;
    private final String name;
    private final String gaiaName;

    public ChromeProfile(final int id, final String name, String gaiaName) {
        this.id = id;
        this.name = name;
        this.gaiaName = gaiaName;
        profileCount++;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        if (profileCount == 1) {
            return "Profile " + id + 1;
        }
        return id == 0 ? "Default" : "Profile " + id;
    }

    public String getGaiaName() {
        return gaiaName;
    }
}
