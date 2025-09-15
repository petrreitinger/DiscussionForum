package discussionforum.Exception;

/**
 * Exception thrown when attempting to register a user with username or email that already exists
 * Výjimka vyvolaná při pokusu o registraci uživatele s uživatelským jménem nebo emailem, který již existuje
 * 
 * @author Petr Reitinger
 * @version 1.0
 * @since 2025
 */
public class UserAlreadyExistsException extends RuntimeException {
    
    public UserAlreadyExistsException(String message) {
        super(message);
    }
    
    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}