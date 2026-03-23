package discussionforum.repository;

import discussionforum.model.Post;
import discussionforum.model.User;
import discussionforum.model.Vote;
import discussionforum.model.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByPostAndUser(Post post, User user);
    long countByPostIdAndType(Long postId, VoteType type);
}
