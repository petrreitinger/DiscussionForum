package discussionforum.Repository;

import discussionforum.Model.Community;
import discussionforum.Model.CommunityMembership;
import discussionforum.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommunityMembershipRepository extends JpaRepository<CommunityMembership, Long> {
    Optional<CommunityMembership> findByUserAndCommunity(User user, Community community);
    boolean existsByUserAndCommunity(User user, Community community);
    
    @Query("SELECT cm.community FROM CommunityMembership cm WHERE cm.user.username = :username ORDER BY cm.joinedAt DESC")
    List<Community> findCommunitiesByUsername(@Param("username") String username);
    
    @Query("SELECT COUNT(cm) FROM CommunityMembership cm WHERE cm.community = :community")
    int countByCommunity(@Param("community") Community community);
}