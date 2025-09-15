package discussionforum.Model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Comment creation request data transfer object with comprehensive validation // Požadavek na vytvoření komentáře s komplexní validací
 * Ensures secure and valid input for comment creation // Zajišťuje bezpečné a platné vstupy pro vytvoření komentáře
 * 
 * @author Petr Reitinger
 * @version 1.1
 * @since 2025
 */
public class CommentRequestDTO {
    private Long postId;  // Set programmatically, no validation needed // Nastaveno programově, validace není potřeba
    
    private Long parentId;  // For replies // Pro odpovědi

    @NotBlank(message = "Comment content is required")
    @Size(min = 1, max = 2000, message = "Comment must be between 1 and 2,000 characters")
    private String content;

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}