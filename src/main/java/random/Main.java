package random;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;
import random.services.AccountService;

import java.io.FileReader;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.logging.Level;
import java.util.logging.Logger;

import static random.services.EncryptionService.decrypt;
import static random.services.EncryptionService.createKeyPairBase64;
import static random.util.Utils.getStringFromReader;
import static random.util.Utils.saveToFile;

@Command(name = "", subcommands = {
        Main.Dump.class,
        Main.Show.class,
        Main.GenerateKeyPair.class,
        Main.DecryptString.class,
        Main.DecryptFile.class
})
public class Main implements Runnable {
    private static final Logger LOG = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    private static final AccountService accountService = new AccountService();

    @Spec
    CommandSpec spec;

    @Override
    public void run() {
        throw new ParameterException(spec.commandLine(), "Missing required subcommand");
    }

    @Command(name = "show", description = "Show passwords")
    static class Show implements Runnable {
        @Override
        public void run() {
            accountService.showAccountsInfo();
        }
    }

    @Command(name = "dump", description = "Dump passwords")
    static class Dump implements Runnable {
        @Option(names = "--pub", description = "Path to public key to encrypt file")
        String pubKeyPath;
        @Option(names = "--path", description = "Path to save file with passwords")
        String filePath;

        @Override
        public void run() {
            accountService.saveAccountsInfoToFile(filePath, pubKeyPath);
        }
    }

    @Command(name = "keys", description = "Generate new keypair")
    static class GenerateKeyPair implements Runnable {
        @Option(names = "--name", description = "Name of new keypair")
        String keysName;
        @Option(names = "--path", description = "Path to save new keypair")
        String keysPath;

        @Override
        public void run() {
            createKeyPairBase64(
                    keysName == null ? "key" : keysName,
                    keysPath == null ? "." : keysPath
            );
        }
    }

    @Command(name = "decrypt-str", description = "Decrypt string")
    static class DecryptString implements Runnable {
        @Option(names = "--pk", required = true, description = "Path to private key")
        String privateKeyPath;
        @Option(names = "--str", required = true, description = "Encrypted string")
        String encryptedStr;

        @Override
        public void run() {
            System.out.println(decrypt(privateKeyPath, encryptedStr));
        }
    }

    @Command(name = "decrypt-file", description = "Decrypt file")
    static class DecryptFile implements Runnable {
        @Option(names = "--pk", required = true, description = "Path to private key")
        String privateKeyPath;
        @Option(names = "--file", required = true, description = "Path ot encrypted file")
        String filePath;

        @Override
        public void run() {
            try {
                saveToFile(decrypt(privateKeyPath, getStringFromReader(new FileReader(filePath))));
            } catch (IOException e) {
                LOG.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    public static void main(final String[] args) {
        System.exit(new CommandLine(new Main()).execute(args));
    }
}
