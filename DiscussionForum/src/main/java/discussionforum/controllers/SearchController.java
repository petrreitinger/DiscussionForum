package discussionforum.controllers;

import discussionforum.model.Comment;
import discussionforum.model.Post;
import discussionforum.model.User;
import discussionforum.service.SearchService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/search")
public class SearchController {

    private static final Logger log = LoggerFactory.getLogger(SearchController.class);

    private final SearchService searchService;

    @Autowired
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public String searchPage(Model model) {
        return "search";
    }

    @GetMapping("/posts")
    public ResponseEntity<Page<Post>> searchPosts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Page<Post> posts = searchService.searchPosts(query, page, size, sortBy, sortDir);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/comments")
    public ResponseEntity<Page<Comment>> searchComments(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        
        Page<Comment> comments = searchService.searchComments(query, page, size);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/users")
    public ResponseEntity<Page<User>> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        
        Page<User> users = searchService.searchUsers(query, page, size);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/global")
    public ResponseEntity<SearchService.SearchResults> globalSearch(
            @RequestParam String query,
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        
        log.debug("Global search: query='{}', type={}, page={}, size={}", query, type, page, size);

        SearchService.SearchResults results = searchService.globalSearch(query, type, page, size);

        log.debug("Search results: posts={}, comments={}, users={}, hasResults={}",
                results.getPosts() != null ? results.getPosts().size() : 0,
                results.getComments() != null ? results.getComments().size() : 0,
                results.getUsers() != null ? results.getUsers().size() : 0,
                results.hasResults());

        return ResponseEntity.ok(results);
    }

    @GetMapping("/suggestions")
    public ResponseEntity<Page<Post>> getSearchSuggestions(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") @Min(1) @Max(10) int limit) {
        
        Page<Post> suggestions = searchService.getSearchSuggestions(query, limit);
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/results")
    public String searchResults(
            @RequestParam String query,
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        SearchService.SearchResults results = searchService.globalSearch(query, type, page, size);
        
        model.addAttribute("results", results);
        model.addAttribute("query", query);
        model.addAttribute("type", type);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        
        return "search-results";
    }
}