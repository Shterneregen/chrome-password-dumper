package random.chrom;

public class ChromeProfile {

    private final String name;
    private final String gaiaName;
    private final String profileName;

    public ChromeProfile(String name, String gaiaName, String profileName) {
        this.name = name;
        this.gaiaName = gaiaName;
        this.profileName = profileName;
    }

    public String getName() {
        return name;
    }

    public String getProfileName() {
        return profileName;
    }

    public String getGaiaName() {
        return gaiaName;
    }
}
