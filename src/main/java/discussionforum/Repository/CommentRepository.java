package discussionforum.Repository;

import discussionforum.Model.Comment;
import discussionforum.Model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostOrderByCreatedAtAsc(Post post);
    
    @Query("SELECT c FROM Comment c WHERE c.post = :post AND c.parent IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findTopLevelCommentsByPost(@Param("post") Post post);
    
    @Query("SELECT DISTINCT c FROM Comment c LEFT JOIN FETCH c.replies WHERE c.post = :post AND c.parent IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findTopLevelCommentsByPostWithReplies(@Param("post") Post post);
    
    List<Comment> findByParentOrderByCreatedAtAsc(Comment parent);

    // Search methods for comments // Vyhledávací metody pro komentáře
    @Query("SELECT c FROM Comment c WHERE " +
           "c.content LIKE CONCAT('%', :query, '%') OR " +
           "LOWER(c.author.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.author.displayName) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Comment> searchComments(String query, Pageable pageable);
}
