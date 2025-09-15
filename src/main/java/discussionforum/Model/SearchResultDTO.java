package discussionforum.Model;

import java.time.LocalDateTime;

public class SearchResultDTO {
    
    public static class PostResult {
        private Long id;
        private String title;
        private String content;
        private LocalDateTime createdAt;
        private int score;
        private String authorUsername;
        private String authorDisplayName;
        private String communityName;
        
        public PostResult(Post post) {
            this.id = post.getId();
            this.title = post.getTitle();
            this.content = post.getContent();
            this.createdAt = post.getCreatedAt();
            this.score = post.getScore();
            this.authorUsername = post.getAuthor() != null ? post.getAuthor().getUsername() : null;
            this.authorDisplayName = post.getAuthor() != null ? post.getAuthor().getDisplayName() : null;
            this.communityName = post.getCommunity() != null ? post.getCommunity().getName() : null;
        }
        
        // Getters
        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public int getScore() { return score; }
        public String getAuthorUsername() { return authorUsername; }
        public String getAuthorDisplayName() { return authorDisplayName; }
        public String getCommunityName() { return communityName; }
    }
    
    public static class CommentResult {
        private Long id;
        private String content;
        private LocalDateTime createdAt;
        private int score;
        private String authorUsername;
        private String authorDisplayName;
        private Long postId;
        private String postTitle;
        
        public CommentResult(Comment comment) {
            this.id = comment.getId();
            this.content = comment.getContent();
            this.createdAt = comment.getCreatedAt();
            this.score = comment.getScore();
            this.authorUsername = comment.getAuthor() != null ? comment.getAuthor().getUsername() : null;
            this.authorDisplayName = comment.getAuthor() != null ? comment.getAuthor().getDisplayName() : null;
            this.postId = comment.getPost() != null ? comment.getPost().getId() : null;
            this.postTitle = comment.getPost() != null ? comment.getPost().getTitle() : null;
        }
        
        // Getters
        public Long getId() { return id; }
        public String getContent() { return content; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public int getScore() { return score; }
        public String getAuthorUsername() { return authorUsername; }
        public String getAuthorDisplayName() { return authorDisplayName; }
        public Long getPostId() { return postId; }
        public String getPostTitle() { return postTitle; }
    }
    
    public static class UserResult {
        private Long id;
        private String username;
        private String displayName;
        private String avatarUrl;
        
        public UserResult(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.displayName = user.getDisplayName();
            this.avatarUrl = user.getAvatarUrl();
        }
        
        // Getters
        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getDisplayName() { return displayName; }
        public String getAvatarUrl() { return avatarUrl; }
    }
}