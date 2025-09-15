package discussionforum.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import discussionforum.Model.Community;

public interface CommunityRepository extends JpaRepository<Community, Long> {
    Optional<Community> findByName(String name);
    boolean existsByName(String name);
    
    @Query("SELECT COUNT(p) FROM Post p WHERE p.community.name = :communityName")
    int countPostsByCommunityName(@Param("communityName") String communityName);
}
