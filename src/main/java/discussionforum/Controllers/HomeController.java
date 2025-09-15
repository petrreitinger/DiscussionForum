package discussionforum.Controllers;

import discussionforum.Model.Post;
import discussionforum.Service.CommunityService;
import discussionforum.Service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsible for handling home page requests and displaying the main feed. // Kontroler zodpovědný za zpracování požadavků na domovskou stránku a zobrazování hlavního feedu.
 * Manages the display of posts with different sorting options and pagination. // Spravuje zobrazování příspěvků s různými možnostmi řazení a stránkování.
 * 
 * @author Petr Reitinger
 * @version 1.0
 * @since 2025
 */
@Controller
public class HomeController {
    
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
    public HomeController(PostService postService, CommunityService communityService) {
        this.postService = postService;
        this.communityService = communityService;
    }

    /**
     * Handles GET requests to the home page ("/"). // Zpracovává GET požadavky na domovskou stránku ("/").
     * Displays a paginated and sorted feed of posts from all communities. // Zobrazuje stránkovaný a seřazený feed příspěvků ze všech komunit.
     * 
     * @param page Optional page number for pagination (0-based) // Volitelné číslo stránky pro stránkování (od 0)
     * @param size Optional page size for pagination // Volitelná velikost stránky pro stránkování
     * @param sort Sort criteria: "hot" (default), "new", or "top" // Kritéria řazení: "hot" (výchozí), "new", nebo "top"
     * @param principal Currently authenticated user details // Podrobnosti aktuálně přihlášeného uživatele
     * @param model Spring model for passing data to the view // Spring model pro předávání dat do pohledu
     * @return View name "index" to render the home page // Název pohledu "index" pro vykreslení domovské stránky
     */
    @GetMapping("/")
    public String home(@RequestParam(required = false) Integer page,
                       @RequestParam(required = false) Integer size,
                       @RequestParam(required = false, defaultValue = "hot") String sort,
                       @AuthenticationPrincipal UserDetails principal,
                       Model model) {
        Page<Post> feed = postService.feed(page, size, sort);
        model.addAttribute("posts", feed);
        model.addAttribute("communities", communityService.all());
        model.addAttribute("currentSort", sort);
        
        return "index";
    }
}
