package discussionforum.Controllers;

import discussionforum.Service.CommunityService;
import discussionforum.Service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import discussionforum.Model.Post;

/**
 * Controller responsible for managing saved posts functionality. // Kontroler zodpovědný za správu funkcionality uložených příspěvků.
 * Handles displaying and managing user's saved posts collection. // Zpracovává zobrazování a správu kolekce uložených příspěvků uživatele.
 * 
 * @author Petr Reitinger
 * @version 1.0
 * @since 2025
 */
@Controller
@RequestMapping("/saved")
public class SavedPostsController {
    
    /**
     * Service for handling post-related operations. // Služba pro zpracování operací souvisejících s příspěvky.
     */
    private final PostService postService;
    
    /**
     * Service for handling community-related operations. // Služba pro zpracování operací souvisejících s komunitami.
     */
    private final CommunityService communityService;

    /**
     * Constructor for dependency injection of required services. // Konstruktor pro vkládání závislostí požadovaných služeb.
     * 
     * @param postService Service handling post operations // Služba zpracovávající operace s příspěvky
     * @param communityService Service handling community operations // Služba zpracovávající operace s komunitami
     */
    public SavedPostsController(PostService postService, CommunityService communityService) {
        this.postService = postService;
        this.communityService = communityService;
    }

    /**
     * Displays the current user's saved posts with pagination. // Zobrazuje uložené příspěvky aktuálního uživatele s stránkováním.
     * Shows all posts that the user has saved for later viewing. // Zobrazuje všechny příspěvky, které si uživatel uložil pro pozdější prohlížení.
     * 
     * @param page Optional page number for pagination (0-based) // Volitelné číslo stránky pro stránkování (od 0)
     * @param size Optional page size for pagination // Volitelná velikost stránky pro stránkování
     * @param principal Currently authenticated user details // Podrobnosti aktuálně přihlášeného uživatele
     * @param model Spring model for passing data to the view // Spring model pro předávání dat do pohledu
     * @return View name "saved/index" for the saved posts page // Název pohledu "saved/index" pro stránku uložených příspěvků
     */
    @GetMapping
    public String savedPosts(@RequestParam(required = false) Integer page,
                            @RequestParam(required = false) Integer size,
                            @AuthenticationPrincipal UserDetails principal,
                            Model model) {
        Page<Post> savedPosts = postService.getSavedPosts(principal.getUsername(), page, size);
        
        model.addAttribute("posts", savedPosts);
        model.addAttribute("communities", communityService.all());
        
        return "saved/index";
    }
}