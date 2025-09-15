package discussionforum.config;

import discussionforum.Service.CommunityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

/**
 * Global controller advice for adding common model attributes across all controllers. // Globální kontrolerová rada pro přidání společných atributů modelu napříč všemi kontrolery.
 * Ensures that commonly needed data is available to all views without explicit addition in each controller. // Zajišťuje, že běžně potřebná data jsou dostupná všem pohledům bez explicitního přidání v každém kontroleru.
 * 
 * @author Petr Reitinger
 * @version 1.0
 * @since 2025
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    /**
     * Service for handling community-related operations. // Služba pro zpracování operací souvisejících s komunitami.
     */
    @Autowired
    private CommunityService communityService;

    /**
     * Adds global model attributes to all controller methods. // Přidává globální atributy modelu do všech metod kontroleru.
     * Specifically adds joined communities data for authenticated users and empty list for non-authenticated users. // Konkrétně přidává data přihlášených komunit pro ověřené uživatele a prázdný seznam pro neověřené uživatele.
     * 
     * @param model Spring model for passing data to views // Spring model pro předávání dat do pohledů
     * @param principal Currently authenticated user principal or null if not authenticated // Aktuálně ověřený uživatel principal nebo null pokud není ověřený
     */
    @ModelAttribute
    public void addGlobalAttributes(Model model, Principal principal) {
        if (principal != null) {
            try {
                model.addAttribute("joinedCommunities", communityService.getJoinedCommunities(principal.getName()));
            } catch (Exception e) {
                model.addAttribute("joinedCommunities", java.util.Collections.emptyList());
            }
        } else {
            model.addAttribute("joinedCommunities", java.util.Collections.emptyList());
        }
    }
}