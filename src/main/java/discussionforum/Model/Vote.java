package discussionforum.Model;


import jakarta.persistence.*;

@Entity
@Table(name = "votes", uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user_id"}))
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Post post;

    @ManyToOne(optional = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private VoteType type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public VoteType getType() {
        return type;
    }

    public void setType(VoteType type) {
        this.type = type;
    }
}
