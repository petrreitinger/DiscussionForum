package discussionforum;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration test for the Discussion Forum application // Integrační test pro aplikaci Discussion Forum
 * Tests that the Spring context loads correctly with all components
 * Testuje, že se Spring kontext načítá správně se všemi komponentami
 * 
 * @author Petr Reitinger
 * @version 1.0
 * @since 2025
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ForumApplicationIntegrationTest {

    /**
     * Test that Spring Boot application context loads successfully
     * Testuje, že se Spring Boot aplikační kontext načítá úspěšně
     * This is a smoke test to ensure all configuration is correct
     * Toto je kouřový test pro zajištění správnosti všech konfigurací
     */
    @Test
    void contextLoads() {
        // Test passes if the application context loads without exceptions
        // Test projde, pokud se aplikační kontext načte bez výjimek
        // This verifies: // Toto ověřuje:
        // - All beans are created correctly // Všechny beany jsou vytvořeny správně
        // - Dependencies are injected properly // Závislosti jsou vloženy správně
        // - Configuration is valid // Konfigurace je platná
        // - Database connection works // Databázové připojení funguje
    }
}