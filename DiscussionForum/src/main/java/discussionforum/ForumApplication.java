package discussionforum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Discussion Forum application. // Hlavní aplikační třída pro aplikaci Discussion Forum.
 * This is the entry point that starts the Spring Boot application. // Toto je vstupní bod, který spouští Spring Boot aplikaci.
 * 
 * @author Petr Reitinger
 * @version 1.0
 * @since 2025
 */
@SpringBootApplication
public class ForumApplication {
    
    /**
     * Main method that serves as the application entry point. // Hlavní metoda, která slouží jako vstupní bod aplikace.
     * Initializes and starts the Spring Boot application context. // Inicializuje a spouští kontext Spring Boot aplikace.
     * 
     * @param args Command line arguments passed to the application // Argumenty příkazové řádky předané aplikaci
     */
    public static void main(String[] args) {
        SpringApplication.run(ForumApplication.class, args);
    }
}