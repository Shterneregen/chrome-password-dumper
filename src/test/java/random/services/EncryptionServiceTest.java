package random.services;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static random.TestUtils.TEST_RESOURCES;
import static random.TestUtils.getTestResource;
import static random.services.EncryptionService.*;

class EncryptionServiceTest {

    @Test
    void name() {
        String name = UUID.randomUUID().toString();
        createKeyPairBase64(name, TEST_RESOURCES.toString());

        File pubKey = getTestResource("%s.pub".formatted(name)).toFile();
        pubKey.deleteOnExit();
        File pk = getTestResource("%s.key".formatted(name)).toFile();
        pk.deleteOnExit();

        assertTrue(pubKey.exists());
        assertTrue(pk.exists());
    }

    @Test
    void testEncryption() {
        String plainText = "secret";

        String encryptString = encrypt(getTestResource("key.pub").toString(), plainText);
        String decryptedString = decrypt(getTestResource("key.key").toString(), encryptString);

        assertEquals(plainText, decryptedString);
    }
}
