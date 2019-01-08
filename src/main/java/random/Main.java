package random;

import random.util.Dumper;

public class Main {
    public static void main(final String[] args) {

        final String mode = args.length > 0 ? args[0] : "";
        final String newFiePath = args.length > 1 ? args[1] : "";
        final String key = args.length > 2 ? args[2] : "";
        try {
            final Dumper d = Dumper.dumpAccounts();
            if (mode.equals("")) {
                d.saveToFile(newFiePath, key);
            } else if (mode.equals("-d")) { // dump
                d.saveToFile(newFiePath, key);
            } else if (mode.equals("-sh")) { // show
                d.show();
            }
        } catch (Exception ignored) {
            System.out.println(ignored);
        }
    }
}
