package random;

import random.util.Dumper;

public class Main {
    public static void main(final String[] args) {

//        Extractor.test();
//        Extractor.getListOfSSIDs();
//        Extractor.isConnected();
//        Extractor.getConnectedSSID();

//        System.out.println(Extractor.getConnectedSSID());
//        System.out.println(Extractor.getBroadcast());
//        Extractor.getConnectedSSID();

        final String newFiePath = args.length > 0 ? args[0] : "";
        final String key = args.length > 1 ? args[1] : "";
        try {
            final Dumper d = Dumper.dumpAccounts(newFiePath);
            d.saveToFile(key);
        } catch (Exception ignored) {
        }
    }
}
