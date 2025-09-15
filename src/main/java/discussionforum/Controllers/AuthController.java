package discussionforum.Controllers;

import discussionforum.Model.RegisterRequestDTO;
import discussionforum.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsible for user authentication operations. // Kontroler zodpovědný za operace ověření uživatele.
 * Handles user login and registration functionality. // Zpracovává funkčnost přihlášení a registrace uživatelů.
 * 
 * @author Petr Reitinger
 * @version 1.0
 * @since 2025
 */
@Controller
public class AuthController {
    
    /**
     * Service for handling user-related operations. // Služba pro zpracování operací souvisejících s uživateli.
     */
    private final UserService userService;

    /**
     * Constructor for dependency injection of required services. // Konstruktor pro vkládání závislostí požadovaných služeb.
     * 
     * @param userService Service handling user operations // Služba zpracovávající operace s uživateli
     */
    public AuthController(UserService userService) { this.userService = userService; }

    /**
     * Displays the login page for user authentication. // Zobrazuje přihlašovací stránku pro ověření uživatele.
     * 
     * @return View name "auth/login" for the login page // Název pohledu "auth/login" pro přihlašovací stránku
     */
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }


    /**
     * Displays the registration form for new user registration. // Zobrazuje registrační formulář pro registraci nových uživatelů.
     * 
     * @param model Spring model for passing data to the view // Spring model pro předávání dat do pohledu
     * @return View name "auth/register" for the registration form // Název pohledu "auth/register" pro registrační formulář
     */
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("register", new RegisterRequestDTO());
        return "auth/register";
    }

    /**
     * Processes user registration with validation and error handling. // Zpracovává registraci uživatele s validací a zpracováním chyb.
     * Creates a new user account and redirects to login page on success. // Vytváří nový uživatelský účet a přesměrovává na přihlašovací stránku při úspěchu.
     * 
     * @param req Registration request DTO containing user data // DTO požadavku registrace obsahující data uživatele
     * @param binding Validation binding result // Výsledek validačního bindingu
     * @param model Spring model for passing data to the view // Spring model pro předávání dat do pohledu
     * @return Redirect to login page on success, or registration form on error // Přesměrování na přihlašovací stránku při úspěchu, nebo registrační formulář při chybě
     */
    @PostMapping("/register")
    public String register(@ModelAttribute("register") @Valid RegisterRequestDTO req,
                           BindingResult binding, Model model) {
        if (binding.hasErrors()) return "auth/register";
        try {
            userService.registerUser(req);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
        return "redirect:/login?registered";
    }
}
