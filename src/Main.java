import java.io.File;
import java.util.Scanner;

public class Main {

    private static final String USER_FILE = "user.txt";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        File userFile = new File(USER_FILE);

        String salt;
        String storedHash;

        try {
            if (!userFile.exists()) {
                // No user file: register new user
                System.out.println("No user found. Please create a new password.");
                System.out.print("Enter new password: ");
                String newPassword = scanner.nextLine();

                salt = FileUtils.generateSalt();
                storedHash = FileUtils.hashPassword(newPassword, salt);
                FileUtils.saveSaltAndHash(salt, storedHash);

                System.out.println("User created! Please restart the app and login.");
                return;
            } else {
                // Load stored salt and hash
                String[] saltAndHash = FileUtils.loadSaltAndHash();
                if (saltAndHash == null) {
                    System.out.println("Error loading user data.");
                    return;
                }
                salt = saltAndHash[0];
                storedHash = saltAndHash[1];

                // Prompt for password and verify
                System.out.print("Enter your password: ");
                String inputPassword = scanner.nextLine();

                if (!AuthManager.verifyPassword(inputPassword, salt, storedHash)) {
                    System.out.println("Incorrect password. Exiting...");
                    return;
                }

                System.out.println("Access granted!");

                // Start note manager with password and salt for encryption/decryption if needed
                NoteManager noteManager = new NoteManager(inputPassword, salt);

                // Main menu loop
                while (true) {
                    System.out.println("\nSecure Notes Menu");
                    System.out.println("1. View All Notes");
                    System.out.println("2. Search Notes");
                    System.out.println("3. Create Note");
                    System.out.println("4. Edit Note");
                    System.out.println("5. Delete Note");
                    System.out.println("6. Change Password");
                    System.out.println("0. Exit");
                    System.out.print("Choose an option: ");

                    String choice = scanner.nextLine();

                    switch (choice) {
                        case "1":
                            noteManager.listAllNotes();
                            break;
                        case "2":
                            noteManager.searchNotes(scanner);
                            break;
                        case "3":
                            noteManager.createNote(scanner);
                            break;
                        case "4":
                            noteManager.editNote(scanner);
                            break;
                        case "5":
                            noteManager.deleteNote(scanner);
                            break;
                        case "6":
                            // Change password flow
                            System.out.print("Enter current password: ");
                            String currentPassword = scanner.nextLine();

                            if (!AuthManager.verifyPassword(currentPassword, salt, storedHash)) {
                                System.out.println("Incorrect current password.");
                                break;
                            }

                            System.out.print("Enter new password: ");
                            String newPassword = scanner.nextLine();

                            // Generate new salt and hash, save to file
                            salt = FileUtils.generateSalt();
                            storedHash = FileUtils.hashPassword(newPassword, salt);
                            FileUtils.saveSaltAndHash(salt, storedHash);

                            System.out.println("Password changed successfully!");

                            // Update NoteManager with new password and salt
                            noteManager = new NoteManager(newPassword, salt);

                            break;
                        case "0":
                            System.out.println("Goodbye!");
                            return;
                        default:
                            System.out.println("Invalid choice.");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}
