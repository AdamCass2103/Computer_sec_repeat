import java.util.Scanner;

public class AuthManager {
    private static String storedSalt;
    private static String storedHash;

    public static boolean login(Scanner scanner) {
        try {
            String[] creds = FileUtils.loadSaltAndHash();
            if (creds == null) {
                System.out.println("No credentials found. Please set up a password first.");
                return false;
            }

            storedSalt = creds[0];
            storedHash = creds[1];

            System.out.print("Enter your password: ");
            String inputPassword = scanner.nextLine();

            if (verifyPassword(inputPassword, storedSalt, storedHash)) {
                System.out.println("Access granted!");
                return true;
            } else {
                System.out.println("Incorrect password.");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error during login: " + e.getMessage());
            return false;
        }
    }

    public static void changePassword(Scanner scanner) {
        try {
            String[] creds = FileUtils.loadSaltAndHash();
            if (creds == null) {
                System.out.println("No credentials found. Cannot change password.");
                return;
            }

            storedSalt = creds[0];
            storedHash = creds[1];

            System.out.print("Enter your current password: ");
            String currentPassword = scanner.nextLine();

            if (!verifyPassword(currentPassword, storedSalt, storedHash)) {
                System.out.println("Incorrect current password.");
                return;
            }

            System.out.print("Enter your new password: ");
            String newPassword = scanner.nextLine();

            String newSalt = FileUtils.generateSalt();
            String newHash = FileUtils.hashPassword(newPassword, newSalt);

            FileUtils.saveSaltAndHash(newSalt, newHash);
            storedSalt = newSalt;
            storedHash = newHash;

            System.out.println("Password changed successfully!");
        } catch (Exception e) {
            System.out.println("Error changing password: " + e.getMessage());
        }
    }

    public static boolean verifyPassword(String password, String salt, String hash) throws Exception {
        String hashedInput = FileUtils.hashPassword(password, salt);
        return hashedInput.equals(hash);
    }
}
