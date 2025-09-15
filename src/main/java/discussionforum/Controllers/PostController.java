package discussionforum.Controllers;

import discussionforum.Model.Post;
import discussionforum.Model.VoteType;
import discussionforum.Model.CommentRequestDTO;
import discussionforum.Model.PostRequestDTO;
import discussionforum.Service.CommentService;
import discussionforum.Service.CommunityService;
import discussionforum.Service.PostService;
import discussionforum.Service.FileUploadService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller responsible for managing post-related operations. // Kontroler zodpovědný za správu operací souvisejících s příspěvky.
 * Handles post creation, viewing, commenting, voting, and saving functionality. // Zpracovává vytváření příspěvků, prohlížení, komentování, hlasování a ukládání.
 * 
 * @author Petr Reitinger
 * @version 1.0
 * @since 2025
 */
@Controller
@RequestMapping("/posts")
public class PostController {
    
    /**
     * Service for handling post-related operations. // Služba pro zpracování operací souvisejících s příspěvky.
     */
    private final PostService postService;
    
    /**
     * Service for handling comment-related operations. // Služba pro zpracování operací souvisejících s komentáři.
     */
    private final CommentService commentService;
    
    /**
     * Service for handling community-related operations. // Služba pro zpracování operací souvisejících s komunitami.
     */
    private final CommunityService communityService;
    
    /**
     * Service for handling file upload operations. // Služba pro zpracování operací s nahráváním souborů.
     */
    private final FileUploadService fileUploadService;

    /**
     * Constructor for dependency injection of required services. // Konstruktor pro vkládání závislostí požadovaných služeb.
     * 
     * @param postService Service handling post operations // Služba zpracovávající operace s příspěvky
     * @param commentService Service handling comment operations // Služba zpracovávající operace s komentáři
     * @param communityService Service handling community operations // Služba zpracovávající operace s komunitami
     * @param fileUploadService Service handling file upload operations // Služba zpracovávající operace s nahráváním souborů
     */
    public PostController(PostService postService, CommentService commentService, CommunityService communityService, FileUploadService fileUploadService) {
        this.postService = postService;
        this.commentService = commentService;
        this.communityService = communityService;
        this.fileUploadService = fileUploadService;
    }

    /**
     * Displays the form for creating a new post. // Zobrazuje formulář pro vytvoření nového příspěvku.
     * 
     * @param principal Currently authenticated user details // Podrobnosti aktuálně přihlášeného uživatele
     * @param model Spring model for passing data to the view // Spring model pro předávání dat do pohledu
     * @return View name "post/new" for the new post form // Název pohledu "post/new" pro formulář nového příspěvku
     */
    @GetMapping("/new")
    public String newPost(@AuthenticationPrincipal UserDetails principal,
                         Model model) {
        model.addAttribute("post", new PostRequestDTO());
        model.addAttribute("communities", communityService.all());
        
        return "post/new";
    }

    /**
     * Handles the creation of a new post. // Zpracovává vytvoření nového příspěvku.
     * Validates the input and creates the post if valid. // Ověřuje vstup a vytváří příspěvek, pokud je platný.
     * 
     * @param req Post request DTO containing post data // DTO požadavku příspěvku obsahující data příspěvku
     * @param binding Validation binding result // Výsledek validačního bindingu
     * @param principal Currently authenticated user details // Podrobnosti aktuálně přihlášeného uživatele
     * @param model Spring model for passing data to the view // Spring model pro předávání dat do pohledu
     * @return Redirect to post detail page on success, or form view on error // Přesměrování na stránku detailu příspěvku při úspěchu, nebo pohled formuláře při chybě
     */
    @PostMapping
    public String create(@ModelAttribute("post") @Valid PostRequestDTO req,
                         BindingResult binding,
                         @RequestParam(value = "attachmentFiles", required = false) MultipartFile[] attachmentFiles,
                         @AuthenticationPrincipal UserDetails principal,
                         Model model) {
        // Validate post creation request // Ověření požadavku na vytvoření příspěvku
        
        if (binding.hasErrors()) {
            // Return form with validation errors // Vrátit formulář s chybami validace
            model.addAttribute("communities", communityService.all());
            return "post/new";
        }
        try {
            Post p = postService.create(req, principal.getUsername());
            // Post created successfully, handle file uploads if any // Příspěvek úspěšně vytvořen, zpracovat nahrávání souborů
            
            // Handle file uploads if any files are provided
            if (attachmentFiles != null && attachmentFiles.length > 0) {
                java.util.Set<String> uploadedUrls = new java.util.HashSet<>();
                for (MultipartFile file : attachmentFiles) {
                    if (!file.isEmpty()) {
                        try {
                            String fileUrl = fileUploadService.uploadPostAttachment(file, p.getId(), principal.getUsername());
                            uploadedUrls.add(fileUrl);
                        } catch (Exception fileUploadException) {
                            // Log the file upload error but don't fail the post creation
                            System.err.println("Failed to upload file: " + fileUploadException.getMessage());
                        }
                    }
                }
                
                // Save the uploaded file URLs to the post
                if (!uploadedUrls.isEmpty()) {
                    p.setAttachmentUrls(uploadedUrls);
                    postService.save(p); // Update the post with attachments
                }
            }
            
            return "redirect:/posts/" + p.getId();
        } catch (Exception e) {
            model.addAttribute("communities", communityService.all());
            model.addAttribute("error", "Failed to create post: " + e.getMessage());
            return "post/new";
        }
    }

    /**
     * Displays the detail view of a specific post. // Zobrazuje detailní pohled konkrétního příspěvku.
     * Shows the post content, comments, and allows adding new comments. // Zobrazuje obsah příspěvku, komentáře a umožňuje přidávání nových komentářů.
     * 
     * @param id Unique identifier of the post // Jedinečný identifikátor příspěvku
     * @param principal Currently authenticated user details // Podrobnosti aktuálně přihlášeného uživatele
     * @param model Spring model for passing data to the view // Spring model pro předávání dat do pohledu
     * @return View name "post/detail" for the post detail page // Název pohledu "post/detail" pro stránku detailu příspěvku
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, 
                        @AuthenticationPrincipal UserDetails principal,
                        Model model) {
        Post post = postService.get(id);
        model.addAttribute("post", post);
        model.addAttribute("comments", commentService.forPost(id));
        model.addAttribute("totalCommentCount", commentService.getTotalCommentCount(id));
        model.addAttribute("comment", new CommentRequestDTO());
        model.addAttribute("communities", communityService.all());
        
        return "post/detail";
    }

    /**
     * Adds a new comment to a specific post. // Přidává nový komentář ke konkrétnímu příspěvku.
     * Validates the comment content and creates the comment if valid. // Ověřuje obsah komentáře a vytváří komentář, pokud je platný.
     * 
     * @param id Unique identifier of the post to comment on // Jedinečný identifikátor příspěvku, ke kterému se komentuje
     * @param req Comment request DTO containing comment data // DTO požadavku komentáře obsahující data komentáře
     * @param binding Validation binding result // Výsledek validačního bindingu
     * @param principal Currently authenticated user details // Podrobnosti aktuálně přihlášeného uživatele
     * @return Redirect to post detail page // Přesměrování na stránku detailu příspěvku
     */
    @PostMapping("/{id}/comment")
    public String comment(@PathVariable Long id,
                          @ModelAttribute("comment") @Valid CommentRequestDTO req,
                          BindingResult binding,
                          @AuthenticationPrincipal UserDetails principal) {
        
        if (binding.hasErrors()) {
            return "redirect:/posts/" + id + "?cerror";
        }
        try {
            req.setPostId(id);
            commentService.add(req, principal.getUsername());
            return "redirect:/posts/" + id;
        } catch (Exception e) {
            return "redirect:/posts/" + id + "?cerror";
        }
    }

    /**
     * Handles upvoting a specific post. // Zpracovává hlasování pro konkrétní příspěvek.
     * Registers an upvote from the current user for the specified post. // Registruje pozitivní hlas od aktuálního uživatele pro zadaný příspěvek.
     * 
     * @param id Unique identifier of the post to upvote // Jedinečný identifikátor příspěvku pro pozitivní hlasování
     * @param principal Currently authenticated user details // Podrobnosti aktuálně přihlášeného uživatele
     * @param referer The referring URL to redirect back to // Odkazující URL pro přesměrování zpět
     * @return Redirect to the referring page or home page // Přesměrování na odkazující stránku nebo domovskou stránku
     */
    @PostMapping("/{id}/upvote")
    public String upvote(@PathVariable Long id, 
                        @AuthenticationPrincipal UserDetails principal,
                        @RequestHeader(value = "referer", required = false) String referer) {
        try {
            int newScore = postService.vote(id, principal.getUsername(), VoteType.UPVOTE);
        } catch (Exception e) {
            // Vote operation failed, but continue with redirect
        }
        return "redirect:" + (referer != null ? referer : "/");
    }

    /**
     * Handles downvoting a specific post. // Zpracovává negativní hlasování pro konkrétní příspěvek.
     * Registers a downvote from the current user for the specified post. // Registruje negativní hlas od aktuálního uživatele pro zadaný příspěvek.
     * 
     * @param id Unique identifier of the post to downvote // Jedinečný identifikátor příspěvku pro negativní hlasování
     * @param principal Currently authenticated user details // Podrobnosti aktuálně přihlášeného uživatele
     * @param referer The referring URL to redirect back to // Odkazující URL pro přesměrování zpět
     * @return Redirect to the referring page or home page // Přesměrování na odkazující stránku nebo domovskou stránku
     */
    @PostMapping("/{id}/downvote")
    public String downvote(@PathVariable Long id, 
                          @AuthenticationPrincipal UserDetails principal,
                          @RequestHeader(value = "referer", required = false) String referer) {
        try {
            int newScore = postService.vote(id, principal.getUsername(), VoteType.DOWNVOTE);
        } catch (Exception e) {
            // Vote operation failed, but continue with redirect
        }
        return "redirect:" + (referer != null ? referer : "/");
    }
    
    /**
     * Saves a post to the current user's saved posts collection. // Ukládá příspěvek do kolekce uložených příspěvků aktuálního uživatele.
     * Returns JSON response indicating success or failure. // Vrací JSON odpověď označující úspěch nebo neúspěch.
     * 
     * @param id Unique identifier of the post to save // Jedinečný identifikátor příspěvku k uložení
     * @param principal Currently authenticated user details // Podrobnosti aktuálně přihlášeného uživatele
     * @return ResponseEntity with JSON indicating operation result // ResponseEntity s JSON označující výsledek operace
     */
    @PostMapping("/{id}/save")
    @ResponseBody
    public ResponseEntity<?> savePost(@PathVariable Long id,
                                     @AuthenticationPrincipal UserDetails principal) {
        try {
            boolean success = postService.savePost(id, principal.getUsername());
            if (success) {
                return ResponseEntity.ok().body("{\"success\": true, \"message\": \"Post saved successfully!\"}");
            } else {
                return ResponseEntity.ok().body("{\"success\": false, \"message\": \"Post is already saved\"}");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Failed to save post: " + e.getMessage() + "\"}");
        }
    }
    
    /**
     * Removes a post from the current user's saved posts collection. // Odebírá příspěvek z kolekce uložených příspěvků aktuálního uživatele.
     * Returns JSON response indicating success or failure. // Vrací JSON odpověď označující úspěch nebo neúspěch.
     * 
     * @param id Unique identifier of the post to unsave // Jedinečný identifikátor příspěvku k odebrání z uložených
     * @param principal Currently authenticated user details // Podrobnosti aktuálně přihlášeného uživatele
     * @return ResponseEntity with JSON indicating operation result // ResponseEntity s JSON označující výsledek operace
     */
    @PostMapping("/{id}/unsave")
    @ResponseBody
    public ResponseEntity<?> unsavePost(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails principal) {
        try {
            boolean success = postService.unsavePost(id, principal.getUsername());
            if (success) {
                return ResponseEntity.ok().body("{\"success\": true, \"message\": \"Post unsaved successfully!\"}");
            } else {
                return ResponseEntity.ok().body("{\"success\": false, \"message\": \"Post was not saved\"}");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Failed to unsave post: " + e.getMessage() + "\"}");
        }
    }
    
    /**
     * Handles upvoting a specific comment. // Zpracovává pozitivní hlasování pro konkrétní komentář.
     * Returns JSON response with the updated comment score. // Vrací JSON odpověď s aktualizovaným skóre komentáře.
     * 
     * @param postId Unique identifier of the post containing the comment // Jedinečný identifikátor příspěvku obsahující komentář
     * @param commentId Unique identifier of the comment to upvote // Jedinečný identifikátor komentáře pro pozitivní hlasování
     * @param principal Currently authenticated user details // Podrobnosti aktuálně přihlášeného uživatele
     * @return ResponseEntity with JSON containing operation result and new score // ResponseEntity s JSON obsahující výsledek operace a nové skóre
     */
    @PostMapping("/{postId}/comments/{commentId}/upvote")
    @ResponseBody
    public ResponseEntity<?> upvoteComment(@PathVariable Long postId,
                                          @PathVariable Long commentId,
                                          @AuthenticationPrincipal UserDetails principal) {
        try {
            int newScore = commentService.voteComment(commentId, principal.getUsername(), VoteType.UPVOTE);
            return ResponseEntity.ok().body("{\"success\": true, \"score\": " + newScore + "}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Failed to upvote comment: " + e.getMessage() + "\"}");
        }
    }
    
    /**
     * Handles downvoting a specific comment. // Zpracovává negativní hlasování pro konkrétní komentář.
     * Returns JSON response with the updated comment score. // Vrací JSON odpověď s aktualizovaným skóre komentáře.
     * 
     * @param postId Unique identifier of the post containing the comment // Jedinečný identifikátor příspěvku obsahující komentář
     * @param commentId Unique identifier of the comment to downvote // Jedinečný identifikátor komentáře pro negativní hlasování
     * @param principal Currently authenticated user details // Podrobnosti aktuálně přihlášeného uživatele
     * @return ResponseEntity with JSON containing operation result and new score // ResponseEntity s JSON obsahující výsledek operace a nové skóre
     */
    @PostMapping("/{postId}/comments/{commentId}/downvote")
    @ResponseBody
    public ResponseEntity<?> downvoteComment(@PathVariable Long postId,
                                            @PathVariable Long commentId,
                                            @AuthenticationPrincipal UserDetails principal) {
        try {
            int newScore = commentService.voteComment(commentId, principal.getUsername(), VoteType.DOWNVOTE);
            return ResponseEntity.ok().body("{\"success\": true, \"score\": " + newScore + "}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Failed to downvote comment: " + e.getMessage() + "\"}");
        }
    }
    
    /**
     * Adds a reply to a specific comment. // Přidává odpověď na konkrétní komentář.
     * Creates a nested comment structure by linking the new comment to a parent comment. // Vytváří vnořenou strukturu komentářů propojením nového komentáře s rodičovským komentářem.
     * 
     * @param postId Unique identifier of the post containing the parent comment // Jedinečný identifikátor příspěvku obsahující rodičovský komentář
     * @param parentId Unique identifier of the parent comment to reply to // Jedinečný identifikátor rodičovského komentáře, na který se odpovídá
     * @param req Comment request DTO containing reply data // DTO požadavku komentáře obsahující data odpovědi
     * @param binding Validation binding result // Výsledek validačního bindingu
     * @param principal Currently authenticated user details // Podrobnosti aktuálně přihlášeného uživatele
     * @return Redirect to post detail page // Přesměrování na stránku detailu příspěvku
     */
    @PostMapping("/{postId}/comments/{parentId}/reply")
    public String replyToComment(@PathVariable Long postId,
                                @PathVariable Long parentId,
                                @ModelAttribute("comment") @Valid CommentRequestDTO req,
                                BindingResult binding,
                                @AuthenticationPrincipal UserDetails principal) {
        
        if (binding.hasErrors()) {
            return "redirect:/posts/" + postId + "?rerror";
        }
        
        try {
            req.setPostId(postId);
            commentService.addReply(req, principal.getUsername(), parentId);
            return "redirect:/posts/" + postId;
        } catch (Exception e) {
            return "redirect:/posts/" + postId + "?rerror";
        }
    }
}
