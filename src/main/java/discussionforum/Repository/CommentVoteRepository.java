package discussionforum.Repository;

import discussionforum.Model.Comment;
import discussionforum.Model.CommentVote;
import discussionforum.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentVoteRepository extends JpaRepository<CommentVote, Long> {
    Optional<CommentVote> findByCommentAndUser(Comment comment, User user);
}