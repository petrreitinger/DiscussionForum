package discussionforum.Service;

import discussionforum.Model.User;
import discussionforum.Model.Role;
import discussionforum.Repository.UserRepository;
import discussionforum.Repository.RoleRepository;
import discussionforum.Model.RegisterRequestDTO;
import discussionforum.Exception.UserAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user with comprehensive validation and duplicate checking
     * Registruje nového uživatele s komplexní validací a kontrolou duplicit
     * 
     * @param req Registration request with user details // Registrační požadavek s údaji uživatele
     * @throws UserAlreadyExistsException if username or email already exists // Pokud uživatelské jméno nebo email již existuje
     * @throws RuntimeException if ROLE_USER is not found // Pokud ROLE_USER není nalezena
     */
    @Transactional
    public void registerUser(RegisterRequestDTO req) {
        // Check for duplicate username // Kontrola duplicitního uživatelského jména
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new UserAlreadyExistsException("Username '" + req.getUsername() + "' is already taken");
        }

        // Check for duplicate email // Kontrola duplicitního emailu
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new UserAlreadyExistsException("Email '" + req.getEmail() + "' is already registered");
        }

        // Create new user with validated data // Vytvoření nového uživatele s validovanými daty
        User user = new User();
        user.setUsername(req.getUsername().toLowerCase().trim()); // Normalize username // Normalizace uživatelského jména
        user.setEmail(req.getEmail().toLowerCase().trim()); // Normalize email // Normalizace emailu
        user.setDisplayName(req.getDisplayName().trim());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setEnabled(true);

        // Fetch ROLE_USER from DB // Načtení ROLE_USER z databáze
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("ROLE_USER not found in database"));

        user.setRoles(Collections.singleton(userRole));

        try {
            userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create user account: " + e.getMessage(), e);
        }
    }
}

