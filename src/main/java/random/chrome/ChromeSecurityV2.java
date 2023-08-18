package random.chrome;

import com.sun.jna.platform.win32.Crypt32Util;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChromeSecurityV2 {
    private static final Logger LOG = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    private static final int GCM_TAG_LENGTH = 16;
    private final static int GCM_IV_LENGTH = 12;

    private ChromeSecurityV2() {
    }

    // https://stackoverflow.com/questions/65939796/java-how-do-i-decrypt-chrome-cookies
    public static String decryptChromeSecret(byte[] cipherText, String encryptedMasterKeyWithPrefixB64) {
        byte[] masterKey = getMasterKey(encryptedMasterKeyWithPrefixB64);
        return decryptChromeSecret(cipherText, masterKey);
    }

    public static String decryptChromeSecret(byte[] cipherText, byte[] masterKey) {
        try {
            // Separate prefix (v10), nonce and ciphertext/tag
            byte[] nonce = Arrays.copyOfRange(cipherText, 3, 3 + GCM_IV_LENGTH);
            byte[] ciphertextTag = Arrays.copyOfRange(cipherText, 3 + GCM_IV_LENGTH, cipherText.length);

            // Decrypt
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce);
            SecretKeySpec keySpec = new SecretKeySpec(masterKey, "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
            byte[] decryptedText = cipher.doFinal(ciphertextTag);

            return new String(decryptedText);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
    }

    public static byte[] getMasterKey(String encryptedMasterKeyWithPrefixB64) {
        // Remove prefix (DPAPI)
        byte[] encryptedMasterKeyWithPrefix = Base64.getDecoder().decode(encryptedMasterKeyWithPrefixB64);
        byte[] encryptedMasterKey = Arrays.copyOfRange(encryptedMasterKeyWithPrefix, 5, encryptedMasterKeyWithPrefix.length);
        return Crypt32Util.cryptUnprotectData(encryptedMasterKey);
    }
}
