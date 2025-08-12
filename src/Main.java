import java.io.File;
import java.util.Scanner;

public class Main {

    private static final String USER_FILE = "user.txt";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Check if user file exists
        File file = new File(USER_FILE);

        String salt;
        String storedHash;
        String inputPassword;

        if (!file.exists()) {
            System.out.println("No user found. Please create a new password.");
            System.out.print("Enter new password: ");
            inputPassword = scanner.nextLine();

            salt = FileUtils.generateSalt();
            storedHash = FileUtils.hashPassword(inputPassword, salt);

            // Save salt and hash to file
            FileUtils.saveSaltAndHash(salt, storedHash);
            System.out.println("User created! Please restart the app and login.");
            return; // exit after registration
        } else {
            // User file exists, load salt and hash
            String[] saltAndHash = FileUtils.loadSaltAndHash();
            if (saltAndHash == null) {
                System.out.println("Error loading user data.");
                return;
            }
            salt = saltAndHash[0];
            storedHash = saltAndHash[1];

            System.out.print("Enter your password: ");
            inputPassword = scanner.nextLine();

            if (!AuthManager.login(scanner)) {
                System.out.println("Incorrect password. Exiting...");
                return;
            }
            System.out.println("Access granted!");


            // Initialize NoteManager and proceed with menu
            NoteManager noteManager = new NoteManager(inputPassword, salt);
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
                        AuthManager.changePassword(scanner);
                        break;
                    case "0":
                        System.out.println("Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid choice.");
                }
            }
        }
    }
}