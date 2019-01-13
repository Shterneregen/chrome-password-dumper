package random.util;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Encryption {

    private static String RSA = "RSA";

    private static String EXT_PUBLIC = "pub";
    private static String EXT_PRIVATE = "key";

    /**
     * Формирует пару открытый/закрытый ключ по заданному открытому ключу
     *
     * @param publicKey открытый ключ
     */
    private KeyPair createPair(PublicKey publicKey) {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(RSA);
            kpg.initialize(1024);
            KeyPair tempKeypair = kpg.generateKeyPair();
            KeyPair keypair = publicKey == null
                    ? kpg.generateKeyPair()
                    : new KeyPair(publicKey, tempKeypair.getPrivate());
            return keypair;
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Encryption.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    //<editor-fold desc="encrypt">
    public static String encrypt(String pubKeyPath, String originalStr) {
        try {
            return encrypt(originalStr, loadPublic(pubKeyPath));
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String encrypt(String plaintext, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance(RSA);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] bytes = plaintext.getBytes("UTF-8");
            byte[] encrypted = blockCipher(bytes, Cipher.ENCRYPT_MODE, cipher);
            return byte2Hex(encrypted);
        } catch (Exception ex) {
            Logger.getLogger(Encryption.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "no encrypt result";
    }
    //</editor-fold>

    //<editor-fold desc="decrypt">
    public static String decrypt(String privateKeyPath, String encryptedStr) {
        try {
            PrivateKey privateKey = loadPrivate(privateKeyPath);
            Cipher cipher = Cipher.getInstance(RSA);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] bts = hex2Byte(encryptedStr.replaceAll("\\s+", ""));
            byte[] decrypted = blockCipher(bts, Cipher.DECRYPT_MODE, cipher);
            String resStr = new String(decrypted, "UTF-8");
            return removeTheTrash(resStr);
        } catch (Exception ex) {
            Logger.getLogger(Encryption.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "no decrypt result";
    }
    //</editor-fold>

    //<editor-fold desc="Encryption logic">
    public static byte[] blockCipher(byte[] bytes, int mode, Cipher cipher) throws IllegalBlockSizeException, BadPaddingException {
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

    private static byte[] append(byte[] prefix, byte[] suffix) {
        byte[] toReturn = new byte[prefix.length + suffix.length];
        for (int i = 0; i < prefix.length; i++) {
            toReturn[i] = prefix[i];
        }
        for (int i = 0; i < suffix.length; i++) {
            toReturn[i + prefix.length] = suffix[i];
        }
        return toReturn;
    }

    public static String byte2Hex(byte b[]) {
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

    public static byte[] hex2Byte(String str) {
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
    public static String removeTheTrash(String s) {
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

    @Deprecated
    private void saveKeyPair(String path, KeyPair keyPair) throws IOException {
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        // Store Public Key.
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
        FileOutputStream fos = new FileOutputStream(path + "key." + EXT_PUBLIC);
        fos.write(x509EncodedKeySpec.getEncoded());
        fos.close();

        // Store Private Key.
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
        fos = new FileOutputStream(path + "key." + EXT_PRIVATE);
        fos.write(pkcs8EncodedKeySpec.getEncoded());
        fos.close();
    }

    public static void saveKeyPairBase64(String path) {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(RSA);
            kpg.initialize(1024);
            KeyPair keyPair = kpg.generateKeyPair();

            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();

            // Store Public Key.
            FileOutputStream fos = new FileOutputStream(path + "key." + EXT_PUBLIC);
//        fos.write("-----BEGIN RSA PUBLIC KEY-----\n");
            fos.write(Base64.getEncoder().encodeToString(publicKey.getEncoded()).getBytes("UTF-8"));
//        fos.write("\n-----END RSA PUBLIC KEY-----\n");
            fos.close();

            // Store Private Key.
            fos = new FileOutputStream(path + "key." + EXT_PRIVATE);
            fos.write(Base64.getEncoder().encodeToString(privateKey.getEncoded()).getBytes("UTF-8"));
            fos.close();
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
    }

//    private static KeyPair loadKeyPair(String path, String algorithm)
//            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
//        // Read Public Key.
//        File filePublicKey = new File(path + "." + EXT_PUBLIC);
//        FileInputStream fis = new FileInputStream(path + "." + EXT_PUBLIC);
//        byte[] encodedPublicKey = new byte[(int) filePublicKey.length()];
//        fis.read(encodedPublicKey);
//        fis.close();
//
//        // Read Private Key.
//        File filePrivateKey = new File(path + "." + EXT_PRIVATE);
//        fis = new FileInputStream(path + "." + EXT_PRIVATE);
//        byte[] encodedPrivateKey = new byte[(int) filePrivateKey.length()];
//        fis.read(encodedPrivateKey);
//        fis.close();
//
//        // Generate KeyPair.
//        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
//        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
//        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
//
//        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
//        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
//
//        return new KeyPair(publicKey, privateKey);
//    }

    private static PrivateKey loadPrivate(String path)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = Files.readAllBytes(Paths.get(path));
        byte[] privateKeyBytes = Base64.getDecoder().decode(keyBytes);
        EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory kf = KeyFactory.getInstance(RSA);
        return kf.generatePrivate(spec);
    }

    private static PublicKey loadPublic(String path)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = Files.readAllBytes(Paths.get(path));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.getDecoder().decode(keyBytes));
        KeyFactory kf = KeyFactory.getInstance(RSA);
        return kf.generatePublic(spec);
    }
    //</editor-fold>
}
