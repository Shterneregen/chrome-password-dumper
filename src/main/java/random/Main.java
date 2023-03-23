package random;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;
import random.chrome.ChromeAccountEntry;
import random.chrome.ChromeService;
import random.util.Dumper;
import random.util.Encryption;
import random.util.Utils;

import java.io.FileReader;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Command(name = "", subcommands = {
        Main.Dump.class,
        Main.Show.class,
        Main.GenerateKeyPair.class,
        Main.DecryptString.class,
        Main.DecryptFile.class
})
public class Main implements Runnable {
    private static final Logger LOG = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    private static final ChromeService chromeService = new ChromeService();
    private static final Dumper dumper = new Dumper();

    @Spec
    CommandSpec spec;

    @Override
    public void run() {
        throw new ParameterException(spec.commandLine(), "Missing required subcommand");
    }

    @Command(name = "show")
    static class Show implements Runnable {
        @Override
        public void run() {
            try {
                Map<String, List<ChromeAccountEntry>> profiles = chromeService.getProfiles();
                dumper.showAccountsInfo(profiles);
            } catch (Exception e) {
                LOG.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    @Command(name = "dump")
    static class Dump implements Runnable {
        @Option(names = "--pub")
        String pubKeyPath;
        @Option(names = "--file")
        String filePath;

        @Override
        public void run() {
            try {
                Map<String, List<ChromeAccountEntry>> profiles = chromeService.getProfiles();
                dumper.saveAccountsInfoToFile(profiles, filePath, pubKeyPath);
            } catch (Exception e) {
                LOG.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    @Command(name = "keys")
    static class GenerateKeyPair implements Runnable {
        @Option(names = "--name")
        String keysName;
        @Option(names = "--path")
        String keysPath;

        @Override
        public void run() {
            Encryption.saveKeyPairBase64(
                    keysName == null ? "key" : keysName,
                    keysPath == null ? ".\\" : keysPath
            );
        }
    }

    @Command(name = "decrypt-str")
    static class DecryptString implements Runnable {
        @Option(names = "--pk", required = true)
        String privateKeyPath;
        @Option(names = "--str", required = true)
        String encryptedStr;

        @Override
        public void run() {
            System.out.println(Encryption.decrypt(privateKeyPath, encryptedStr));
        }
    }

    @Command(name = "decrypt-file")
    static class DecryptFile implements Runnable {
        @Option(names = "--pk", required = true)
        String privateKeyPath;
        @Option(names = "--file", required = true)
        String filePath;

        @Override
        public void run() {
            try {
                Utils.saveToFile(Encryption.decrypt(privateKeyPath, Utils.getStringFromReader(new FileReader(filePath))));
            } catch (IOException e) {
                LOG.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    public static void main(final String[] args) {
        System.exit(new CommandLine(new Main()).execute(args));
    }
}
