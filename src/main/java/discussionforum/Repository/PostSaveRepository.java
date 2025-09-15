package discussionforum.Repository;

import discussionforum.Model.Post;
import discussionforum.Model.PostSave;
import discussionforum.Model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PostSaveRepository extends JpaRepository<PostSave, Long> {
    Optional<PostSave> findByUserAndPost(User user, Post post);
    boolean existsByUserAndPost(User user, Post post);
    
    @Query("SELECT ps.post FROM PostSave ps WHERE ps.user.username = :username ORDER BY ps.savedAt DESC")
    Page<Post> findSavedPostsByUsername(@Param("username") String username, Pageable pageable);
    
    @Modifying
    @Transactional
    void deleteByUserAndPost(User user, Post post);
}