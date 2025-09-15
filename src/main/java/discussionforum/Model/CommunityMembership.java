package discussionforum.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "community_memberships", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "community_id"}))
public class CommunityMembership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "community_id")
    private Community community;

    private LocalDateTime joinedAt;

    @PrePersist
    void onCreate() {
        setJoinedAt(LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Community getCommunity() {
        return community;
    }

    public void setCommunity(Community community) {
        this.community = community;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}