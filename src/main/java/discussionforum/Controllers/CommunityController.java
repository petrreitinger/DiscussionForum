package discussionforum.Controllers;

import discussionforum.Model.Community;
import discussionforum.Service.CommunityService;
import discussionforum.Service.PostService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsible for managing community-related operations. // Kontroler zodpovědný za správu operací souvisejících s komunitami.
 * Handles community viewing, creation, joining, and leaving functionality. // Zpracovává prohlížení, vytváření, připojování a opouštění komunit.
 * 
 * @author Petr Reitinger
 * @version 1.0
 * @since 2025
 */
@Controller
@RequestMapping("/c")
@Validated
public class CommunityController {
    
    /**
     * Service for handling community-related operations. // Služba pro zpracování operací souvisejících s komunitami.
     */
    private final CommunityService communityService;
    
    /**
     * Service for handling post-related operations. // Služba pro zpracování operací souvisejících s příspěvky.
     */
    private final PostService postService;

    /**
     * Constructor for dependency injection of required services. // Konstruktor pro vkládání závislostí požadovaných služeb.
     * 
     * @param communityService Service handling community operations // Služba zpracovávající operace s komunitami
     * @param postService Service handling post operations // Služba zpracovávající operace s příspěvky
     */
    public CommunityController(CommunityService communityService, PostService postService) {
        this.communityService = communityService;
        this.postService = postService;
    }

    /**
     * Displays the community view page with posts and member information. // Zobrazuje stránku pohledu komunity s příspěvky a informacemi o členech.
     * Shows community details, posts, and membership status for the current user. // Zobrazuje detaily komunity, příspěvky a stav členství pro aktuálního uživatele.
     * 
     * @param name Name of the community to display // Název komunity k zobrazení
     * @param page Optional page number for pagination (0-based) // Volitelné číslo stránky pro stránkování (od 0)
     * @param size Optional page size for pagination // Volitelná velikost stránky pro stránkování
     * @param sort Sort criteria: "hot" (default), "new", or "top" // Kritéria řazení: "hot" (výchozí), "new", nebo "top"
     * @param principal Currently authenticated user details // Podrobnosti aktuálně přihlášeného uživatele
     * @param model Spring model for passing data to the view // Spring model pro předávání dat do pohledu
     * @return View name "community/view" for the community page // Název pohledu "community/view" pro stránku komunity
     */
    @GetMapping("/{name}")
    public String view(@PathVariable String name,
                       @RequestParam(required = false) Integer page,
                       @RequestParam(required = false) Integer size,
                       @RequestParam(required = false, defaultValue = "hot") String sort,
                       @AuthenticationPrincipal UserDetails principal,
                       Model model) {
        Community community = communityService.getByName(name);
        int postCount = communityService.getPostCount(name);
        int actualMemberCount = communityService.getActualMemberCount(community);
        boolean isMember = principal != null && communityService.isMember(name, principal.getUsername());
        
        model.addAttribute("communityName", name);
        model.addAttribute("community", community);
        model.addAttribute("postCount", postCount);
        model.addAttribute("actualMemberCount", actualMemberCount);
        model.addAttribute("isMember", isMember);
        model.addAttribute("posts", postService.byCommunity(name, page, size, sort));
        model.addAttribute("communities", communityService.all());
        model.addAttribute("currentSort", sort);
        
        return "community/view";
    }

    /**
     * Creates a new community with the specified name and description. // Vytváří novou komunitu se zadaným názvem a popisem.
     * Validates input parameters and redirects to the new community page on success. // Ověřuje vstupní parametry a přesměrovuje na stránku nové komunity při úspěchu.
     * 
     * @param name Community name (2-40 characters, not blank) // Název komunity (2-40 znaků, ne prázdný)
     * @param description Community description (max 500 characters) // Popis komunity (max 500 znaků)
     * @return Redirect to community page on success, or home with error on failure // Přesměrování na stránku komunity při úspěchu, nebo domů s chybou při neúspěchu
     */
    @PostMapping("/new")
    public String create(@RequestParam @NotBlank @Size(min = 2, max = 40) String name, 
                        @RequestParam @Size(max = 500) String description) {
        try {
            communityService.create(name, description);
            return "redirect:/c/" + name;
        } catch (Exception e) {
            return "redirect:/?error=community_creation_failed";
        }
    }
    
    /**
     * Allows the current user to join a specific community. // Umožňuje aktuálnímu uživateli připojit se ke konkrétní komunitě.
     * Returns JSON response indicating success or failure of the join operation. // Vrací JSON odpověď označující úspěch nebo neúspěch operace připojení.
     * 
     * @param name Name of the community to join // Název komunity, ke které se chce připojit
     * @param principal Currently authenticated user details // Podrobnosti aktuálně přihlášeného uživatele
     * @return ResponseEntity with JSON indicating operation result // ResponseEntity s JSON označující výsledek operace
     */
    @PostMapping("/{name}/join")
    @ResponseBody
    public ResponseEntity<?> joinCommunity(@PathVariable String name,
                                         @AuthenticationPrincipal UserDetails principal) {
        try {
            boolean success = communityService.joinCommunity(name, principal.getUsername());
            if (success) {
                return ResponseEntity.ok().body("{\"success\": true, \"message\": \"Successfully joined " + name + "!\"}");
            } else {
                return ResponseEntity.ok().body("{\"success\": false, \"message\": \"You are already a member of " + name + "\"}");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Failed to join community: " + e.getMessage() + "\"}");
        }
    }
    
    /**
     * Allows the current user to leave a specific community. // Umožňuje aktuálnímu uživateli opustit konkrétní komunitu.
     * Returns JSON response indicating success or failure of the leave operation. // Vrací JSON odpověď označující úspěch nebo neúspěch operace opuštění.
     * 
     * @param name Name of the community to leave // Název komunity, kterou chce opustit
     * @param principal Currently authenticated user details // Podrobnosti aktuálně přihlášeného uživatele
     * @return ResponseEntity with JSON indicating operation result // ResponseEntity s JSON označující výsledek operace
     */
    @PostMapping("/{name}/leave")
    @ResponseBody
    public ResponseEntity<?> leaveCommunity(@PathVariable String name,
                                          @AuthenticationPrincipal UserDetails principal) {
        try {
            boolean success = communityService.leaveCommunity(name, principal.getUsername());
            if (success) {
                return ResponseEntity.ok().body("{\"success\": true, \"message\": \"Successfully left " + name + "\"}");
            } else {
                return ResponseEntity.ok().body("{\"success\": false, \"message\": \"You are not a member of " + name + "\"}");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Failed to leave community: " + e.getMessage() + "\"}");
        }
    }
}
