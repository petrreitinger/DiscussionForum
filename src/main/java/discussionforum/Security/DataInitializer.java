package discussionforum.Security;

import discussionforum.Model.Community;
import discussionforum.Repository.CommunityRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2) // Run after AdminUserInitializer (which is @Order(1) implicitly)
public class DataInitializer implements CommandLineRunner {
    
    private final CommunityRepository communityRepository;
    
    public DataInitializer(CommunityRepository communityRepository) {
        this.communityRepository = communityRepository;
    }
    
    @Override
    public void run(String... args) throws Exception {

        createCommunityIfNotExists("general", "General discussion about anything and everything");
    }
    
    private void createCommunityIfNotExists(String name, String description) {
        if (!communityRepository.existsByName(name)) {
            Community community = new Community();
            community.setName(name);
            community.setDescription(description);
            communityRepository.save(community);
        }
    }
}