package discussionforum.repository;

import discussionforum.model.Comment;
import discussionforum.model.CommentVote;
import discussionforum.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentVoteRepository extends JpaRepository<CommentVote, Long> {
    Optional<CommentVote> findByCommentAndUser(Comment comment, User user);
}