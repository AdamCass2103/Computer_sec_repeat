import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Scanner;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class AuthManager {

    private static final String USER_FILE = "user.txt";
    private static final int MAX_ATTEMPTS = 3;
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;

    private static String storedSalt;
    private static String storedHash;

    public static boolean login(Scanner scanner) {
        File userFile = new File(USER_FILE);

        try {
            if (!userFile.exists()) {
                System.out.println("Welcome! Let's set up your new password.");
                return setupPassword(scanner);
            }

            loadStoredCredentials();

            int attempts = 0;
            while (attempts < MAX_ATTEMPTS) {
                System.out.print("Enter your password: ");
                String password = scanner.nextLine();

                if (verifyPassword(password, storedSalt, storedHash)) {
                    System.out.println("✅ Access granted.");
                    return true;
                } else {
                    attempts++;
                    System.out.println("❌ Incorrect password. Attempts remaining: " + (MAX_ATTEMPTS - attempts));
                }
            }

        } catch (Exception e) {
            System.out.println("Error during login: " + e.getMessage());
        }

        return false;
    }

    private static boolean setupPassword(Scanner scanner) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        System.out.print("Create a new password: ");
        String password = scanner.nextLine();

        String salt = generateSalt();
        String hash = hashPassword(password, salt);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE))) {
            writer.write(salt + ":" + hash);
        }

        System.out.println("[DEBUG] Created salt: " + salt);
        System.out.println("[DEBUG] Created hash: " + hash);
        System.out.println("✅ Password created successfully.");
        return true;
    }

    public static boolean changePassword(Scanner scanner) {
        try {
            loadStoredCredentials();

            System.out.print("Enter your current password: ");
            String oldPassword = scanner.nextLine();

            if (!verifyPassword(oldPassword, storedSalt, storedHash)) {
                System.out.println("❌ Incorrect password.");
                return false;
            }

            System.out.print("Enter your new password: ");
            String newPassword = scanner.nextLine();

            String newSalt = generateSalt();
            String newHash = hashPassword(newPassword, newSalt);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE))) {
                writer.write(newSalt + ":" + newHash);
            }

            System.out.println("✅ Password changed successfully.");
            return true;

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    private static void loadStoredCredentials() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line = reader.readLine();
            if (line == null || !line.contains(":")) {
                throw new IOException("Invalid credentials file format.");
            }
            String[] parts = line.trim().split(":");
            storedSalt = parts[0].trim();
            storedHash = parts[1].trim();
        }

        System.out.println("[DEBUG] Loaded salt: " + storedSalt);
        System.out.println("[DEBUG] Loaded hash: " + storedHash);
    }

    private static String generateSalt() throws NoSuchAlgorithmException {
        byte[] salt = new byte[16];
        SecureRandom.getInstanceStrong().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private static String hashPassword(String password, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), Base64.getDecoder().decode(salt), ITERATIONS, KEY_LENGTH);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash);
    }

    private static boolean verifyPassword(String password, String salt, String expectedHash) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String hash = hashPassword(password, salt);
        return hash.equals(expectedHash);
    }
}
