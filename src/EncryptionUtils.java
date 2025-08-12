

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.*;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

public class EncryptionUtils {

    private static final String AES = "AES/CBC/PKCS5Padding";

    public static String encrypt(String plainText, String password, String salt) throws Exception {
        Cipher cipher = Cipher.getInstance(AES);

        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        SecretKeySpec secretKey = getKeyFromPassword(password, salt);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

        byte[] encrypted = cipher.doFinal(plainText.getBytes());

        // Combine IV + ciphertext
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    public static String decrypt(String encryptedText, String password, String salt) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encryptedText);

        byte[] iv = new byte[16];
        byte[] ciphertext = new byte[combined.length - 16];

        System.arraycopy(combined, 0, iv, 0, 16);
        System.arraycopy(combined, 16, ciphertext, 0, ciphertext.length);

        Cipher cipher = Cipher.getInstance(AES);
        SecretKeySpec secretKey = getKeyFromPassword(password, salt);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

        byte[] original = cipher.doFinal(ciphertext);
        return new String(original);
    }

    private static SecretKeySpec getKeyFromPassword(String password, String salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), Base64.getDecoder().decode(salt), 65536, 256);
        byte[] secret = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(secret, "AES");
    }
}
