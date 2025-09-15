package discussionforum.Model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Post creation request data transfer object with comprehensive validation // Požadavek na vytvoření příspěvku s komplexní validací
 * Ensures secure and valid input for post creation // Zajišťuje bezpečné a platné vstupy pro vytvoření příspěvku
 * 
 * @author Petr Reitinger
 * @version 1.1
 * @since 2025
 */
public class PostRequestDTO {
    
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(min = 3, max = 10000, message = "Content must be between 3 and 10,000 characters")
    private String content;

    @NotNull(message = "Community selection is required")
    private Long communityId;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getCommunityId() {
        return communityId;
    }

    public void setCommunityId(Long communityId) {
        this.communityId = communityId;
    }

    // getters and setters
    // ...
}
