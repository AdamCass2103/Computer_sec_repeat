package ie.dkit.securenotes;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class NoteManager {

    private List<Note> notes = new ArrayList<>();
    private int nextId = 1;

    // Store the user password and salt temporarily (not stored in plaintext!)
    private final String password;
    private final String salt;
    private final String NOTES_FILE = "notes.txt";

    public NoteManager(String password, String salt) {
        this.password = password;
        this.salt = salt;
        loadNotes();
    }

    public void createNote(Scanner scanner) {
        System.out.print("Enter title (max 55 chars): ");
        String title = scanner.nextLine().trim();
        if (title.length() > 55 || title.isEmpty()) {
            System.out.println("❌ Invalid title.");
            return;
        }

        System.out.print("Enter note text (max 255 chars): ");
        String text = scanner.nextLine().trim();
        if (text.length() > 255 || text.isEmpty()) {
            System.out.println("❌ Invalid note text.");
            return;
        }

        List<String> tags = new ArrayList<>();
        System.out.print("Enter up to 3 tags (separated by spaces): ");
        String[] tagArray = scanner.nextLine().split("\\s+");
        for (String tag : tagArray) {
            if (tag.matches("^[a-z0-9_]{1,16}$")) {
                tags.add(tag);
            } else {
                System.out.println("❌ Invalid tag: " + tag);
            }
            if (tags.size() == 3) break;
        }

        Note note = new Note(nextId++, title, text, LocalDateTime.now(), tags);
        notes.add(note);
        saveNotes();
        System.out.println("✅ Note created.");
    }

    public void listAllNotes() {
        if (notes.isEmpty()) {
            System.out.println("No notes found.");
            return;
        }

        notes.sort(Comparator.comparing(Note::getCreatedAt));
        for (Note note : notes) {
            System.out.println(note);
        }
    }

    public void searchNotes(Scanner scanner) {
        System.out.print("Search by (1) Title or (2) Tag? ");
        String option = scanner.nextLine();

        if (option.equals("1")) {
            System.out.print("Enter title keyword: ");
            String keyword = scanner.nextLine().toLowerCase();
            List<Note> results = notes.stream()
                    .filter(n -> n.getTitle().toLowerCase().contains(keyword))
                    .collect(Collectors.toList());
            printSearchResults(results);
        } else if (option.equals("2")) {
            System.out.print("Enter tag: ");
            String tag = scanner.nextLine();
            List<Note> results = notes.stream()
                    .filter(n -> n.getTags().contains(tag))
                    .collect(Collectors.toList());
            printSearchResults(results);
        } else {
            System.out.println("❌ Invalid option.");
        }
    }

    public void deleteNote(Scanner scanner) {
        System.out.print("Enter ID of note to delete: ");
        int id = Integer.parseInt(scanner.nextLine());
        Optional<Note> optionalNote = notes.stream().filter(n -> n.getId() == id).findFirst();

        if (optionalNote.isPresent()) {
            System.out.print("Are you sure? (yes/no): ");
            String confirm = scanner.nextLine().toLowerCase();
            if (confirm.equals("yes")) {
                notes.remove(optionalNote.get());
                saveNotes();
                System.out.println("✅ Note deleted.");
            } else {
                System.out.println("Deletion cancelled.");
            }
        } else {
            System.out.println("❌ Note not found.");
        }
    }

    public void editNote(Scanner scanner) {
        System.out.print("Enter ID of note to edit: ");
        int id = Integer.parseInt(scanner.nextLine());
        Optional<Note> optionalNote = notes.stream().filter(n -> n.getId() == id).findFirst();

        if (optionalNote.isPresent()) {
            Note note = optionalNote.get();

            System.out.print("New title (or press Enter to keep): ");
            String newTitle = scanner.nextLine().trim();
            if (!newTitle.isEmpty() && newTitle.length() <= 55) {
                note.setTitle(newTitle);
            }

            System.out.print("New text (or press Enter to keep): ");
            String newText = scanner.nextLine().trim();
            if (!newText.isEmpty() && newText.length() <= 255) {
                note.setText(newText);
            }

            System.out.print("New tags (or press Enter to keep): ");
            String tagInput = scanner.nextLine().trim();
            if (!tagInput.isEmpty()) {
                List<String> newTags = new ArrayList<>();
                for (String tag : tagInput.split("\\s+")) {
                    if (tag.matches("^[a-z0-9_]{1,16}$")) {
                        newTags.add(tag);
                        if (newTags.size() == 3) break;
                    }
                }
                note.setTags(newTags);
            }

            saveNotes();
            System.out.println("✅ Note updated.");
        } else {
            System.out.println("❌ Note not found.");
        }
    }

    private void printSearchResults(List<Note> results) {
        if (results.isEmpty()) {
            System.out.println("No matching notes found.");
        } else {
            results.forEach(System.out::println);
        }
    }

    private void saveNotes() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(notes);
            out.close();

            String encrypted = EncryptionUtils.encrypt(Base64.getEncoder().encodeToString(bos.toByteArray()), password, salt);
            try (FileWriter writer = new FileWriter(NOTES_FILE)) {
                writer.write(encrypted);
            }
        } catch (Exception e) {
            System.out.println("Error saving notes: " + e.getMessage());
        }
    }

    private void loadNotes() {
        File file = new File(NOTES_FILE);
        if (!file.exists()) return;

        try {
            String encrypted = new String(Files.readAllBytes(file.toPath()));
            String decrypted = EncryptionUtils.decrypt(encrypted, password, salt);

            byte[] decoded = Base64.getDecoder().decode(decrypted);
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(decoded));
            notes = (List<Note>) in.readObject();
            in.close();

            // Set nextId correctly
            nextId = notes.stream().mapToInt(Note::getId).max().orElse(0) + 1;

        } catch (Exception e) {
            System.out.println("Error loading notes: " + e.getMessage());
        }
    }
}
