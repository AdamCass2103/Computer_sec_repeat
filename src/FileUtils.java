import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class FileUtils {

    private static final String USER_FILE = "user.txt";

    public static String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.getDecoder().decode(salt));
            byte[] hashed = md.digest(password.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveSaltAndHash(String salt, String hash) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE))) {
            writer.write(salt);
            writer.newLine();
            writer.write(hash);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String[] loadSaltAndHash() {
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String salt = reader.readLine();
            String hash = reader.readLine();
            if (salt != null && hash != null) {
                return new String[]{salt, hash};
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
