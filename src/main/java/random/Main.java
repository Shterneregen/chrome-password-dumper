package random;

import random.chrom.ChromeAccountEntry;
import random.chrom.ChromeDatabase;
import random.util.Dumper;
import random.util.Encryption;
import random.util.Utils;

import java.io.FileReader;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOG = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    private static final ChromeDatabase database = new ChromeDatabase();
    private static final Dumper dumper = new Dumper();

    public static void main(final String[] args) {

        final String mode = args.length > 0 ? args[0] : "";
        final String newFiePath = args.length > 1 ? args[1] : "";
        final String key = args.length > 2 ? args[2] : "";
        try {
            Map<String, List<ChromeAccountEntry>> profiles = database.getProfiles();
            if (mode.equals("-dump") || mode.isEmpty()) { // dump
                dumper.saveAccountsInfoToFile(profiles, newFiePath, key);
            } else if (mode.equals("-sh")) { // show
                dumper.showAccountsInfo(profiles);
            } else if (mode.equals("-g")) { // generate
                Encryption.saveKeyPairBase64(args.length > 1 ? args[1] : ".\\");
            } else if (mode.equals("-d") && args.length > 2) { // decrypt
                String privateKeyPath = args[1];
                String encryptedStr = args[2];
                System.out.println(Encryption.decrypt(privateKeyPath, encryptedStr));
            } else if (args[0].equals("-df") && args.length > 2) {
                String privateKeyPath = args[1];
                String filePath = args[2];
                Utils.saveToFile(Encryption.decrypt(privateKeyPath, Utils.getStringFromReader(new FileReader(filePath))));
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
