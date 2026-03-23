package discussionforum.security;

import discussionforum.model.Role;
import discussionforum.model.User;
import discussionforum.repository.RoleRepository;
import discussionforum.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

@Configuration
public class AdminUserInitializer {

    private static final Logger log = LoggerFactory.getLogger(AdminUserInitializer.class);

    @Value("${admin.username:admin}")
    private String adminUsername;

    @Value("${admin.password:#{null}}")
    private String adminPassword;

    @Bean
    @Order(1)
    CommandLineRunner initAdmin(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        return args -> {

            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName("ROLE_ADMIN");
                        return roleRepository.save(role);
                    });

            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName("ROLE_USER");
                        return roleRepository.save(role);
                    });

            Role moderatorRole = roleRepository.findByName("ROLE_MODERATOR")
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName("ROLE_MODERATOR");
                        return roleRepository.save(role);
                    });

            if (adminPassword == null) {
                log.warn("No admin password configured (set ADMIN_PASSWORD env var). Skipping admin user creation.");
                return;
            }

            if (userRepository.findByUsername(adminUsername).isEmpty()) {
                User admin = new User();
                admin.setUsername(adminUsername);
                admin.setEmail(adminUsername + "@localhost");
                admin.setDisplayName("Administrator");
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setEnabled(true);
                admin.setRoles(Collections.singleton(adminRole));
                userRepository.save(admin);
                log.info("Admin user '{}' created.", adminUsername);
            }
        };
    }
}
