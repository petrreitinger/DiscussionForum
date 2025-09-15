package discussionforum.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller responsible for handling error pages. // Kontroler zodpovědný za zpracování chybových stránek.
 * Manages custom error page displays for various HTTP error scenarios. // Spravuje zobrazení vlastních chybových stránek pro různé HTTP chybové scénáře.
 * 
 * @author Petr Reitinger
 * @version 1.0
 * @since 2025
 */
@Controller
public class ErrorPageController {

    /**
     * Displays the access denied error page. // Zobrazuje chybovou stránku odmítnutého přístupu.
     * Shown when user attempts to access a resource without sufficient permissions. // Zobrazuje se, když se uživatel pokusí o přístup k zdroji bez dostatečných oprávnění.
     * 
     * @return View name "access-denied" for the access denied page // Název pohledu "access-denied" pro stránku odmítnutého přístupu
     */
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}

