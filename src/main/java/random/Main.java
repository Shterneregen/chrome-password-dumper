package random;

import random.util.Dumper;
import random.util.Encryption;
import random.util.Utils;

import java.io.FileReader;
import java.lang.invoke.MethodHandles;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOG = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    public static void main(final String[] args) {

        final String mode = args.length > 0 ? args[0] : "";
        final String newFiePath = args.length > 1 ? args[1] : "";
        final String key = args.length > 2 ? args[2] : "";
        try {
            final Dumper d = Dumper.dumpAccounts();
            if (mode.equals("")) {
                d.saveToFile(newFiePath, key);
            } else if (mode.equals("-dump")) { // dump
                d.saveToFile(newFiePath, key);
            } else if (mode.equals("-sh")) { // show
                d.show();
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
