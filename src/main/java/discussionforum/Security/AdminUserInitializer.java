package discussionforum.Security;
import discussionforum.Model.Role;
import discussionforum.Model.User;
import discussionforum.Repository.RoleRepository;
import discussionforum.Repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

@Configuration
public class AdminUserInitializer {

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

            // Then create admin user if it doesn't exist
            String adminUsername = "admin";
            String rawPassword = "admin123";

            if (userRepository.findByUsername(adminUsername).isEmpty()) {
                User admin = new User();
                admin.setUsername(adminUsername);
                admin.setDisplayName("Administrator");
                admin.setPassword(passwordEncoder.encode(rawPassword));
                admin.setEnabled(true);
                admin.setRoles(Collections.singleton(adminRole));

                userRepository.save(admin);
            } else {
            }
        };
    }
}
