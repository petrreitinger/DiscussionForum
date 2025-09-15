package discussionforum.Repository;

import discussionforum.Model.Post;
import discussionforum.Model.Community;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findAllByCommunityOrderByCreatedAtDesc(Community community, Pageable pageable);
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Post> findAllByCommunity(Community community, Pageable pageable);

    // Advanced search methods for posts // Pokročilé vyhledávací metody pro příspěvky
    @Query("SELECT p FROM Post p WHERE " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "p.content LIKE CONCAT('%', :query, '%') OR " +
           "LOWER(p.author.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.author.displayName) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Post> searchPosts(String query, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.community = :community AND (" +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "p.content LIKE CONCAT('%', :query, '%') OR " +
           "LOWER(p.author.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.author.displayName) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Post> searchPostsInCommunity(Community community, String query, Pageable pageable);

    // Search by title only for autocomplete // Vyhledávání pouze podle názvu pro automatické dokončování
    @Query("SELECT p FROM Post p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Post> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
