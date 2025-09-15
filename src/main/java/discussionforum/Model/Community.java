package discussionforum.Model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "communities")
public class Community {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 40)
    @NotBlank
    private String name;

    @Column(length = 500)
    private String description;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Post> posts;
    
    @PrePersist
    void onCreate() {
        setCreatedAt(LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Post> getPosts() {
        return posts;
    }

    public void setPosts(Set<Post> posts) {
        this.posts = posts;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Note: This method uses the lazy-loaded posts, use the service method instead for accurate counts
    public int getPostCount() {
        return posts != null ? posts.size() : 0;
    }
    
    public int getMemberCount(int actualPostCount) {
        // Calculate members based on actual post count
        // In a real application, you'd have a membership table
        return Math.max(actualPostCount * 25 + 100, 100);
    }
    
    public int getOnlineCount(int memberCount) {
        // Calculate online users as a fraction of members
        return Math.max(memberCount / 20, 1);
    }
}
