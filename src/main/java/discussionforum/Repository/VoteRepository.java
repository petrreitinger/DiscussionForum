package discussionforum.Repository;

import discussionforum.Model.Post;
import discussionforum.Model.User;
import discussionforum.Model.Vote;
import discussionforum.Model.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByPostAndUser(Post post, User user);
    long countByPostIdAndType(Long postId, VoteType type);
}
