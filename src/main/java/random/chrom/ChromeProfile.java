package random.chrom;

public class ChromeProfile {

    private final int id;
    private final String name;
    private final String gaiaName;

    public ChromeProfile(final int id, final String name, String gaiaName) {
        this.id = id;
        this.name = name;
        this.gaiaName = gaiaName;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return id > 0 ? "Profile " + id : "Default";
    }

    public String getGaiaName() {
        return gaiaName;
    }
}
