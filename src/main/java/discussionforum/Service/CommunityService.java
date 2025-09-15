package discussionforum.Service;

import discussionforum.Model.Community;
import discussionforum.Model.CommunityMembership;
import discussionforum.Model.User;
import discussionforum.Repository.CommunityRepository;
import discussionforum.Repository.CommunityMembershipRepository;
import discussionforum.Repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommunityService {
    private final CommunityRepository communityRepository;
    private final CommunityMembershipRepository membershipRepository;
    private final UserRepository userRepository;

    public CommunityService(CommunityRepository communityRepository, 
                          CommunityMembershipRepository membershipRepository,
                          UserRepository userRepository) {
        this.communityRepository = communityRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
    }

    public List<Community> all() { return communityRepository.findAll(); }

    public Community getByName(String name) {
        return communityRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Community not found: " + name));
    }
    
    public int getPostCount(String communityName) {
        return communityRepository.countPostsByCommunityName(communityName);
    }

    @Transactional
    public Community create(String name, String description) {
        
        if (communityRepository.existsByName(name)) {
            throw new IllegalArgumentException("Community exists");
        }
        
        Community c = new Community();
        c.setName(name);
        c.setDescription(description);
        
        Community savedCommunity = communityRepository.save(c);
        
        return savedCommunity;
    }
    
    @Transactional
    public boolean joinCommunity(String communityName, String username) {
        
        Community community = getByName(communityName);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
        

        if (membershipRepository.existsByUserAndCommunity(user, community)) {
            return false;
        }
        
        CommunityMembership membership = new CommunityMembership();
        membership.setUser(user);
        membership.setCommunity(community);
        CommunityMembership saved = membershipRepository.save(membership);
        
        return true;
    }
    
    @Transactional
    public boolean leaveCommunity(String communityName, String username) {
        Community community = getByName(communityName);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
        
        return membershipRepository.findByUserAndCommunity(user, community)
                .map(membership -> {
                    membershipRepository.delete(membership);
                    return true;
                })
                .orElse(false);
    }
    
    public boolean isMember(String communityName, String username) {
        try {
            Community community = getByName(communityName);
            User user = userRepository.findByUsername(username)
                    .orElse(null);
            if (user == null) return false;
            return membershipRepository.existsByUserAndCommunity(user, community);
        } catch (EntityNotFoundException e) {
            return false;
        }
    }
    
    public List<Community> getJoinedCommunities(String username) {
        List<Community> joinedCommunities = membershipRepository.findCommunitiesByUsername(username);
        return joinedCommunities;
    }
    
    public int getActualMemberCount(Community community) {
        return membershipRepository.countByCommunity(community);
    }
}
