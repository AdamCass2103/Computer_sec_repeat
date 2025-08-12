import java.io.*;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Scanner;

public class AuthManager {
    private static final String USER_FILE = "user.txt";
    private static String storedSalt;
    private static String storedHash;

    public static boolean login(Scanner scanner) {
        try {
            if (!new File(USER_FILE).exists()) {
                System.out.println("No credentials found. Let's set one up.");
                setupPassword(scanner);
            }

            loadStoredCredentials();

            System.out.print("Enter your password: ");
            String inputPassword = scanner.nextLine();

            if (verifyPassword(inputPassword, storedSalt, storedHash)) {
                System.out.println("Access granted!");
                return true;
            } else {
                System.out.println("Incorrect password. Exiting...");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error during login: " + e.getMessage());
            return false;
        }
    }

    private static void setupPassword(Scanner scanner) throws Exception {
        System.out.print("Set a password: ");
        String password = scanner.nextLine();

        String salt = generateSalt();
        String hash = hashPassword(password, salt);

        saveCredentials(salt, hash);
        storedSalt = salt;
        storedHash = hash;

        System.out.println("Password setup complete!");
    }

    public static void changePassword(Scanner scanner) {
        try {
            System.out.print("Enter your current password: ");
            String currentPassword = scanner.nextLine();

            if (!verifyPassword(currentPassword, storedSalt, storedHash)) {
                System.out.println("Incorrect current password.");
                return;
            }

            System.out.print("Enter your new password: ");
            String newPassword = scanner.nextLine();

            String salt = generateSalt();
            String hash = hashPassword(newPassword, salt);

            saveCredentials(salt, hash);
            storedSalt = salt;
            storedHash = hash;

            System.out.println("Password changed successfully!");
        } catch (Exception e) {
            System.out.println("Error changing password: " + e.getMessage());
        }
    }

    private static void loadStoredCredentials() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(USER_FILE));
        String line = reader.readLine();
        reader.close();

        if (line == null || !line.contains(":")) {
            throw new IOException("Invalid credentials file format.");
        }

        String[] parts = line.split(":");
        storedSalt = parts[0];
        storedHash = parts[1];
    }

    private static void saveCredentials(String salt, String hash) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE));
        writer.write(salt + ":" + hash);
        writer.newLine();
        writer.close();
    }

    public static boolean verifyPassword(String password, String salt, String hash) throws Exception {
        String hashedInput = hashPassword(password, salt);
        return hashedInput.equals(hash);
    }

    private static String generateSalt() {
        byte[] saltBytes = new byte[16];
        new SecureRandom().nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }

    private static String hashPassword(String password, String salt) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(Base64.getDecoder().decode(salt));
        byte[] hashedBytes = md.digest(password.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(hashedBytes);
    }
}
