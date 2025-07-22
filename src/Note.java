import java.time.LocalDateTime;
import java.util.List;

public class Note {
    private int id;
    private String title;
    private String text;
    private LocalDateTime createdAt;
    private List<String> tags;

    public Note(int id, String title, String text, LocalDateTime createdAt, List<String> tags) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.createdAt = createdAt;
        this.tags = tags;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getText() { return text; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<String> getTags() { return tags; }

    public void setTitle(String title) { this.title = title; }
    public void setText(String text) { this.text = text; }
    public void setTags(List<String> tags) { this.tags = tags; }

    @Override
    public String toString() {
        return "\nID: " + id + "\nTitle: " + title + "\nText: " + text + "\nCreated At: " + createdAt + "\nTags: " + tags;
    }
}
