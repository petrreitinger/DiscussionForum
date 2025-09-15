package discussionforum.Service;

import discussionforum.Model.*;
import discussionforum.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Comprehensive search service for posts, comments, users, and communities
 * Komplexní vyhledávací služba pro příspěvky, komentáře, uživatele a komunity
 * 
 * @author Petr Reitinger
 * @version 1.0
 * @since 2025
 */
@Service
public class SearchService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;

    @Autowired
    public SearchService(PostRepository postRepository,
                        CommentRepository commentRepository,
                        UserRepository userRepository,
                        CommunityRepository communityRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.communityRepository = communityRepository;
    }

    /**
     * Search posts by query string across title, content, and author information
     * Vyhledávání příspěvků podle dotazu v názvu, obsahu a informacích o autorovi
     * 
     * @param query Search query // Vyhledávací dotaz
     * @param page Page number (0-based) // Číslo stránky (počínaje 0)
     * @param size Page size // Velikost stránky
     * @param sortBy Sort field (createdAt, score, title) // Pole pro řazení
     * @param sortDir Sort direction (asc, desc) // Směr řazení
     * @return Page of matching posts // Stránka odpovídajících příspěvků
     */
    public Page<Post> searchPosts(String query, int page, int size, String sortBy, String sortDir) {
        if (!StringUtils.hasText(query)) {
            return Page.empty();
        }

        Sort sort = createSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return postRepository.searchPosts(query.trim(), pageable);
    }

    /**
     * Search posts within a specific community // Vyhledávání příspěvků v konkrétní komunitě
     * 
     * @param community Target community // Cílová komunita
     * @param query Search query // Vyhledávací dotaz
     * @param page Page number // Číslo stránky
     * @param size Page size // Velikost stránky
     * @return Page of matching posts in community // Stránka odpovídajících příspěvků v komunitě
     */
    public Page<Post> searchPostsInCommunity(Community community, String query, int page, int size) {
        if (!StringUtils.hasText(query) || community == null) {
            return Page.empty();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return postRepository.searchPostsInCommunity(community, query.trim(), pageable);
    }

    /**
     * Search comments by content and author information // Vyhledávání komentářů podle obsahu a informací o autorovi
     * 
     * @param query Search query // Vyhledávací dotaz
     * @param page Page number // Číslo stránky
     * @param size Page size // Velikost stránky
     * @return Page of matching comments // Stránka odpovídajících komentářů
     */
    public Page<Comment> searchComments(String query, int page, int size) {
        if (!StringUtils.hasText(query)) {
            return Page.empty();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return commentRepository.searchComments(query.trim(), pageable);
    }

    /**
     * Search users by username, display name, or email // Vyhledávání uživatelů podle jména, zobrazovaného jména nebo emailu
     * 
     * @param query Search query // Vyhledávací dotaz
     * @param page Page number // Číslo stránky
     * @param size Page size // Velikost stránky
     * @return Page of matching users // Stránka odpovídajících uživatelů
     */
    public Page<User> searchUsers(String query, int page, int size) {
        if (!StringUtils.hasText(query)) {
            return Page.empty();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("username").ascending());
        return userRepository.searchUsers(query.trim(), pageable);
    }

    /**
     * Unified search across all content types // Jednotné vyhledávání napříč všemi typy obsahu
     * 
     * @param query Search query // Vyhledávací dotaz
     * @param type Content type (posts, comments, users, all) // Typ obsahu
     * @param page Page number // Číslo stránky
     * @param size Page size // Velikost stránky
     * @return SearchResults containing all matching content // Výsledky vyhledávání obsahující všechen odpovídající obsah
     */
    public SearchResults globalSearch(String query, String type, int page, int size) {
        if (!StringUtils.hasText(query)) {
            return new SearchResults();
        }

        SearchResults results = new SearchResults();
        results.setQuery(query);
        
        switch (type.toLowerCase()) {
            case "posts":
                Page<Post> postsPage = searchPosts(query, page, size, "createdAt", "desc");
                results.setPosts(postsPage.getContent().stream()
                    .map(SearchResultDTO.PostResult::new)
                    .collect(java.util.stream.Collectors.toList()));
                break;
            case "comments":
                Page<Comment> commentsPage = searchComments(query, page, size);
                results.setComments(commentsPage.getContent().stream()
                    .map(SearchResultDTO.CommentResult::new)
                    .collect(java.util.stream.Collectors.toList()));
                break;
            case "users":
                Page<User> usersPage = searchUsers(query, page, size);
                results.setUsers(usersPage.getContent().stream()
                    .map(SearchResultDTO.UserResult::new)
                    .collect(java.util.stream.Collectors.toList()));
                break;
            case "all":
            default:
                Page<Post> allPostsPage = searchPosts(query, 0, 5, "createdAt", "desc");
                results.setPosts(allPostsPage.getContent().stream()
                    .map(SearchResultDTO.PostResult::new)
                    .collect(java.util.stream.Collectors.toList()));
                    
                Page<Comment> allCommentsPage = searchComments(query, 0, 5);
                results.setComments(allCommentsPage.getContent().stream()
                    .map(SearchResultDTO.CommentResult::new)
                    .collect(java.util.stream.Collectors.toList()));
                    
                Page<User> allUsersPage = searchUsers(query, 0, 5);
                results.setUsers(allUsersPage.getContent().stream()
                    .map(SearchResultDTO.UserResult::new)
                    .collect(java.util.stream.Collectors.toList()));
                break;
        }
        
        return results;
    }

    /**
     * Get search suggestions for autocomplete // Získání návrhů vyhledávání pro automatické dokončování
     * 
     * @param query Partial search query // Částečný vyhledávací dotaz
     * @param limit Maximum number of suggestions // Maximální počet návrhů
     * @return Page of post titles matching the query // Stránka názvů příspěvků odpovídajících dotazu
     */
    public Page<Post> getSearchSuggestions(String query, int limit) {
        if (!StringUtils.hasText(query) || query.length() < 2) {
            return Page.empty();
        }

        Pageable pageable = PageRequest.of(0, limit, Sort.by("score").descending());
        return postRepository.findByTitleContainingIgnoreCase(query.trim(), pageable);
    }

    /**
     * Create sort object based on field and direction // Vytvoření objektu řazení podle pole a směru
     * 
     * @param sortBy Sort field // Pole pro řazení
     * @param sortDir Sort direction // Směr řazení
     * @return Sort object // Objekt řazení
     */
    private Sort createSort(String sortBy, String sortDir) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        switch (sortBy.toLowerCase()) {
            case "score":
                return Sort.by(direction, "score");
            case "title":
                return Sort.by(direction, "title");
            case "author":
                return Sort.by(direction, "author.username");
            case "createdat":
            default:
                return Sort.by(direction, "createdAt");
        }
    }

    /**
     * Search results container class // Třída kontejneru výsledků vyhledávání
     */
    public static class SearchResults {
        private String query;
        private java.util.List<SearchResultDTO.PostResult> posts;
        private java.util.List<SearchResultDTO.CommentResult> comments;
        private java.util.List<SearchResultDTO.UserResult> users;

        public SearchResults() {}

        // Getters and Setters // Gettery a Settery
        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }

        public java.util.List<SearchResultDTO.PostResult> getPosts() { return posts; }
        public void setPosts(java.util.List<SearchResultDTO.PostResult> posts) { this.posts = posts; }

        public java.util.List<SearchResultDTO.CommentResult> getComments() { return comments; }
        public void setComments(java.util.List<SearchResultDTO.CommentResult> comments) { this.comments = comments; }

        public java.util.List<SearchResultDTO.UserResult> getUsers() { return users; }
        public void setUsers(java.util.List<SearchResultDTO.UserResult> users) { this.users = users; }

        public boolean hasResults() {
            return (posts != null && !posts.isEmpty()) ||
                   (comments != null && !comments.isEmpty()) ||
                   (users != null && !users.isEmpty());
        }
    }
}