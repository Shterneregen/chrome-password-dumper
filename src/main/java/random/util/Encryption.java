package random.util;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Encryption {

    private static String RSA = "RSA";

    private static String EXT_PUBLIC = "pub";
    private static String EXT_PRIVATE = "pr";

    private KeyPair keypair;
    private Cipher cipher;

    Encryption() {
    }

    Encryption(KeyPair keypair) {
        this.keypair = keypair;
        try {
            this.cipher = Cipher.getInstance(RSA);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            Logger.getLogger(Encryption.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public static void generateAndSaveKeyPair(String path) {
        Encryption encryption = new Encryption();
        encryption.createPair();
        KeyPair kp = encryption.getKeypair();
        try {
            encryption.saveKeyPair(path, kp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String encrypt(String pubKeyPath, String originalStr) {
        Encryption encryption = new Encryption();
        try {
            return encryption.encrypt(originalStr, loadPublic(pubKeyPath, RSA));
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String decrypt(String keyPairPath, String encryptedStr) {
        try {
            KeyPair serverKeyPair = Encryption.loadKeyPair(keyPairPath, RSA);
            Encryption encryption = new Encryption(serverKeyPair);
            return encryption.decrypt(encryptedStr);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return "no decrypt result";
    }

    //<editor-fold desc="get-set">
    KeyPair getKeypair() {
        return keypair;
    }

    /**
     * Возвращает открытый ключ из существующей пары открытый/закрытый ключ
     *
     * @return открытый ключ
     */
    public PublicKey getPublic() {
        return keypair.getPublic();
    }
    //</editor-fold>

    /**
     * Формирует пару открытый/закрытый ключ по заданному открытому ключу
     *
     * @param publicKey открытый ключ
     */
    private void createPair(PublicKey publicKey) {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(RSA);
            kpg.initialize(1024);
            KeyPair tempKeypair = kpg.generateKeyPair();
            this.keypair = publicKey == null
                    ? kpg.generateKeyPair()
                    : new KeyPair(publicKey, tempKeypair.getPrivate());
            this.cipher = Cipher.getInstance(RSA);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
            Logger.getLogger(Encryption.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Формирует пару открытый/закрытый ключ
     */
    void createPair() {
        createPair(null);
    }

    /**
     * Шифрует сообщение открытым ключом
     *
     * @param plaintext исходная строка
     * @return зашифрованная строка
     */
    String encrypt(String plaintext) {
        return encrypt(plaintext, (PublicKey) null);
    }

    private String encrypt(String plaintext, PublicKey publicKey) {
        try {
            if (publicKey == null) {
                this.cipher.init(Cipher.ENCRYPT_MODE, this.keypair.getPublic());

            } else {
                this.cipher = Cipher.getInstance(RSA);
                this.cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            }
            byte[] bytes = plaintext.getBytes("UTF-8");
            byte[] encrypted = blockCipher(bytes, Cipher.ENCRYPT_MODE);
            //	encryptedTranspherable = Hex.encodeHex(encrypted);
            return byte2Hex(encrypted);
        } catch (Exception ex) {
            Logger.getLogger(Encryption.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "no encrypt result";
    }

    /**
     * Расшифровывает строку закрытым ключом
     *
     * @param encryptedStr зашифрованая строка
     * @return расшифрованная строка
     */
    String decrypt(String encryptedStr) {
        try {
            this.cipher.init(Cipher.DECRYPT_MODE, this.keypair.getPrivate());
            byte[] bts = hex2Byte(encryptedStr);
            byte[] decrypted = blockCipher(bts, Cipher.DECRYPT_MODE);
            String resStr = new String(decrypted, "UTF-8");
            return removeTheTrash(resStr);
        } catch (Exception ex) {
            Logger.getLogger(Encryption.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "no decrypt result";
    }

    //<editor-fold desc="Encryption logic">
    private byte[] blockCipher(byte[] bytes, int mode) throws IllegalBlockSizeException, BadPaddingException {
        // string initialize 2 buffers.
        // scrambled will hold intermediate results
        byte[] scrambled = new byte[0];

        // toReturn will hold the total result
        byte[] toReturn = new byte[0];
        // if we encrypt we use 100 byte long blocks. Decryption requires 128 byte long blocks (because of RSA)
        int length = (mode == Cipher.ENCRYPT_MODE) ? 100 : 128;

        // another buffer. this one will hold the bytes that have to be modified in this step
        byte[] buffer = new byte[length];

        for (int i = 0; i < bytes.length; i++) {

            // if we filled our buffer array we have our block ready for de- or encryption
            if ((i > 0) && (i % length == 0)) {
                //execute the operation
                scrambled = cipher.doFinal(buffer);
                // add the result to our total result.
                toReturn = append(toReturn, scrambled);
                // here we calculate the length of the next buffer required
                int newlength = length;

                // if newlength would be longer than remaining bytes in the bytes array we shorten it.
                if (i + length > bytes.length) {
                    newlength = bytes.length - i;
                }
                // clean the buffer array
                buffer = new byte[newlength];
            }
            // copy byte into our buffer.
            buffer[i % length] = bytes[i];
        }

        // this step is needed if we had a trailing buffer. should only happen when encrypting.
        // example: we encrypt 110 bytes. 100 bytes per run means we "forgot" the last 10 bytes. they are in the buffer array
        scrambled = cipher.doFinal(buffer);

        // final step before we can return the modified data.
        toReturn = append(toReturn, scrambled);

        return toReturn;
    }

    private byte[] append(byte[] prefix, byte[] suffix) {
        byte[] toReturn = new byte[prefix.length + suffix.length];
        for (int i = 0; i < prefix.length; i++) {
            toReturn[i] = prefix[i];
        }
        for (int i = 0; i < suffix.length; i++) {
            toReturn[i + prefix.length] = suffix[i];
        }
        return toReturn;
    }

    private static String byte2Hex(byte b[]) {
        StringBuilder hs = new StringBuilder();
        String stmp = "";
        for (byte aB : b) {
            stmp = Integer.toHexString(aB & 0xff);
            if (stmp.length() == 1) {
                hs.append("0").append(stmp);
            } else {
                hs.append(stmp);
            }
        }
        return hs.toString().toLowerCase();
    }

    private static byte hex2Byte(char a1, char a2) {
        int k;
        if (a1 >= '0' && a1 <= '9') {
            k = a1 - 48;
        } else if (a1 >= 'a' && a1 <= 'f') {
            k = (a1 - 97) + 10;
        } else if (a1 >= 'A' && a1 <= 'F') {
            k = (a1 - 65) + 10;
        } else {
            k = 0;
        }
        k <<= 4;
        if (a2 >= '0' && a2 <= '9') {
            k += a2 - 48;
        } else if (a2 >= 'a' && a2 <= 'f') {
            k += (a2 - 97) + 10;
        } else if (a2 >= 'A' && a2 <= 'F') {
            k += (a2 - 65) + 10;
        } else {
            k += 0;
        }
        return (byte) (k & 0xff);
    }

    private static byte[] hex2Byte(String str) {
        int len = str.length();
//        System.out.println("len:" + len);
        if (len % 2 != 0) {
            return null;
        }
        byte r[] = new byte[len / 2];
        int k = 0;
        for (int i = 0; i < str.length() - 1; i += 2) {
            r[k] = hex2Byte(str.charAt(i), str.charAt(i + 1));
            k++;
        }
        return r;
    }

    /**
     * Удаляет управляющие символы из строки.
     */
    private static String removeTheTrash(String s) {
        char[] buf = new char[1024];
        int length = s.length();
        char[] oldChars = (length < 1024) ? buf : new char[length];
        s.getChars(0, length, oldChars, 0);
        int newLen = 0;
        for (int j = 0; j < length; j++) {
            char ch = oldChars[j];
            if (ch >= ' ') {
                oldChars[newLen] = ch;
                newLen++;
            }
        }
        if (newLen != length) {
            s = new String(oldChars, 0, newLen);
        }
        return s;
    }
    //</editor-fold>

    //<editor-fold desc="I/O">

    /**
     * Выводит в консоль ключевую пару
     *
     * @param keyPair ключевая пара
     */
    void dumpKeyPair(KeyPair keyPair) {
        PublicKey pub = keyPair.getPublic();
        System.out.println("Public Key: " + getHexString(pub.getEncoded()));

        PrivateKey priv = keyPair.getPrivate();
        System.out.println("Private Key: " + getHexString(priv.getEncoded()));
    }

    private String getHexString(byte[] b) {
        StringBuilder result = new StringBuilder();
        for (byte aB : b) {
            result.append(Integer.toString((aB & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }

    private void saveKeyPair(String path, KeyPair keyPair) throws IOException {
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        // Store Public Key.
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
        FileOutputStream fos = new FileOutputStream(path + "key.pub");
        fos.write(x509EncodedKeySpec.getEncoded());
        fos.close();

        // Store Private Key.
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
        fos = new FileOutputStream(path + "key.pr");
        fos.write(pkcs8EncodedKeySpec.getEncoded());
        fos.close();
    }

    private static KeyPair loadKeyPair(String path, String algorithm)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        // Read Public Key.
        File filePublicKey = new File(path + "." + EXT_PUBLIC);
        FileInputStream fis = new FileInputStream(path + "." + EXT_PUBLIC);
        byte[] encodedPublicKey = new byte[(int) filePublicKey.length()];
        fis.read(encodedPublicKey);
        fis.close();

        // Read Private Key.
        File filePrivateKey = new File(path + "." + EXT_PRIVATE);
        fis = new FileInputStream(path + "." + EXT_PRIVATE);
        byte[] encodedPrivateKey = new byte[(int) filePrivateKey.length()];
        fis.read(encodedPrivateKey);
        fis.close();

        // Generate KeyPair.
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

        return new KeyPair(publicKey, privateKey);
    }

    private PrivateKey loadPrivate(String path, String algorithm)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        return (PrivateKey) loadKey(path, algorithm, false);
    }

    private static PublicKey loadPublic(String path, String algorithm)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        return (PublicKey) loadKey(path, algorithm, true);
    }

    private static Key loadKey(String path, String algorithm, boolean isPublic)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        File filePublicKey = new File(path);
        FileInputStream fis = new FileInputStream(path);
        byte[] encodedKey = new byte[(int) filePublicKey.length()];
        fis.read(encodedKey);
        fis.close();

        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedKey);

        if (isPublic) return keyFactory.generatePublic(keySpec);
        else return keyFactory.generatePrivate(keySpec);
    }
    //</editor-fold>
}