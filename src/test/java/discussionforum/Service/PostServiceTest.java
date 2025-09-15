package discussionforum.Service;

import discussionforum.Model.*;
import discussionforum.Repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for PostService class // Komplexní unit testy pro třídu PostService
 * Tests all business logic including post creation, voting, saving functionality
 * Testuje veškerou obchodní logiku včetně vytváření příspěvků, hlasování a ukládání
 * 
 * @author Petr Reitinger
 * @version 1.0
 * @since 2025
 */
@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;
    
    @Mock
    private CommunityRepository communityRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private VoteRepository voteRepository;
    
    @Mock
    private PostSaveRepository postSaveRepository;

    @InjectMocks
    private PostService postService;

    private User testUser;
    private Community testCommunity;
    private Post testPost;
    private PostRequestDTO testPostRequest;
    private Vote testVote;
    private PostSave testPostSave;

    @BeforeEach
    void setUp() {
        // Initialize test data // Inicializace testovacích dat
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setDisplayName("Test User");

        testCommunity = new Community();
        testCommunity.setId(1L);
        testCommunity.setName("testcommunity");
        testCommunity.setDescription("Test Community");

        testPost = new Post();
        testPost.setId(1L);
        testPost.setTitle("Test Post");
        testPost.setContent("Test Content");
        testPost.setAuthor(testUser);
        testPost.setCommunity(testCommunity);
        testPost.setScore(0);

        testPostRequest = new PostRequestDTO();
        testPostRequest.setTitle("Test Post");
        testPostRequest.setContent("Test Content");
        testPostRequest.setCommunityId(1L);

        testVote = new Vote();
        testVote.setId(1L);
        testVote.setPost(testPost);
        testVote.setUser(testUser);
        testVote.setType(VoteType.UPVOTE);

        testPostSave = new PostSave();
        testPostSave.setId(1L);
        testPostSave.setPost(testPost);
        testPostSave.setUser(testUser);
    }

    @Test
    void create_ShouldCreatePost_WhenValidRequest() {
        // Arrange // Příprava
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(communityRepository.findById(1L)).thenReturn(Optional.of(testCommunity));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        // Act // Akce
        Post result = postService.create(testPostRequest, "testuser");

        // Assert // Ověření
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Post");
        assertThat(result.getContent()).isEqualTo("Test Content");
        assertThat(result.getAuthor()).isEqualTo(testUser);
        assertThat(result.getCommunity()).isEqualTo(testCommunity);
        assertThat(result.getScore()).isEqualTo(0);

        verify(postRepository).save(any(Post.class));
    }

    @Test
    void create_ShouldThrowException_WhenUserNotFound() {
        // Arrange // Příprava
        when(userRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());

        // Act & Assert // Akce a ověření
        assertThatThrownBy(() -> postService.create(testPostRequest, "nonexistentuser"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void create_ShouldThrowException_WhenCommunityNotFound() {
        // Arrange // Příprava
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(communityRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert // Akce a ověření
        assertThatThrownBy(() -> postService.create(testPostRequest, "testuser"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Community not found");
    }

    @Test
    void feed_ShouldReturnPostsWithHotSort_WhenSortIsNull() {
        // Arrange // Příprava
        Page<Post> expectedPage = new PageImpl<>(Arrays.asList(testPost));
        when(postRepository.findAll(any(Pageable.class))).thenReturn(expectedPage);

        // Act // Akce
        Page<Post> result = postService.feed(0, 10, null);

        // Assert // Ověření
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(testPost);
    }

    @Test
    void feed_ShouldReturnPostsWithNewSort_WhenSortIsNew() {
        // Arrange // Příprava
        Page<Post> expectedPage = new PageImpl<>(Arrays.asList(testPost));
        when(postRepository.findAll(any(Pageable.class))).thenReturn(expectedPage);

        // Act // Akce
        Page<Post> result = postService.feed(0, 10, "new");

        // Assert // Ověření
        assertThat(result).isNotNull();
        verify(postRepository).findAll(any(Pageable.class));
    }

    @Test
    void get_ShouldReturnPost_WhenPostExists() {
        // Arrange // Příprava
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

        // Act // Akce
        Post result = postService.get(1L);

        // Assert // Ověření
        assertThat(result).isEqualTo(testPost);
    }

    @Test
    void get_ShouldThrowException_WhenPostNotFound() {
        // Arrange // Příprava
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert // Akce a ověření
        assertThatThrownBy(() -> postService.get(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Post not found");
    }

    @Test
    void vote_ShouldCreateUpvote_WhenNoExistingVote() {
        // Arrange // Příprava
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(voteRepository.findByPostAndUser(testPost, testUser)).thenReturn(Optional.empty());
        when(voteRepository.save(any(Vote.class))).thenReturn(testVote);
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        // Act // Akce
        int result = postService.vote(1L, "testuser", VoteType.UPVOTE);

        // Assert // Ověření
        assertThat(result).isEqualTo(1); // Score should increase by 1 // Skóre by se mělo zvýšit o 1
        verify(voteRepository).save(any(Vote.class));
        verify(postRepository).save(testPost);
    }

    @Test
    void vote_ShouldToggleVote_WhenSameVoteExists() {
        // Arrange // Příprava
        testPost.setScore(1);
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(voteRepository.findByPostAndUser(testPost, testUser)).thenReturn(Optional.of(testVote));

        // Act // Akce
        int result = postService.vote(1L, "testuser", VoteType.UPVOTE);

        // Assert // Ověření
        assertThat(result).isEqualTo(0); // Score should decrease by 1 // Skóre by se mělo snížit o 1
        verify(voteRepository).delete(testVote);
    }

    @Test
    void vote_ShouldSwitchVote_WhenDifferentVoteExists() {
        // Arrange // Příprava
        testVote.setType(VoteType.DOWNVOTE);
        testPost.setScore(-1);
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(voteRepository.findByPostAndUser(testPost, testUser)).thenReturn(Optional.of(testVote));

        // Act // Akce
        int result = postService.vote(1L, "testuser", VoteType.UPVOTE);

        // Assert // Ověření
        assertThat(result).isEqualTo(1); // Score should increase by 2 // Skóre by se mělo zvýšit o 2
        verify(voteRepository).save(testVote);
        assertThat(testVote.getType()).isEqualTo(VoteType.UPVOTE);
    }

    @Test
    void savePost_ShouldSavePost_WhenNotAlreadySaved() {
        // Arrange // Příprava
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(postSaveRepository.existsByUserAndPost(testUser, testPost)).thenReturn(false);
        when(postSaveRepository.save(any(PostSave.class))).thenReturn(testPostSave);

        // Act // Akce
        boolean result = postService.savePost(1L, "testuser");

        // Assert // Ověření
        assertThat(result).isTrue();
        verify(postSaveRepository).save(any(PostSave.class));
    }

    @Test
    void savePost_ShouldReturnFalse_WhenAlreadySaved() {
        // Arrange // Příprava
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(postSaveRepository.existsByUserAndPost(testUser, testPost)).thenReturn(true);

        // Act // Akce
        boolean result = postService.savePost(1L, "testuser");

        // Assert // Ověření
        assertThat(result).isFalse();
        verify(postSaveRepository, never()).save(any(PostSave.class));
    }

    @Test
    void unsavePost_ShouldUnsavePost_WhenPostIsSaved() {
        // Arrange // Příprava
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(postSaveRepository.findByUserAndPost(testUser, testPost))
                .thenReturn(Optional.of(testPostSave));

        // Act // Akce
        boolean result = postService.unsavePost(1L, "testuser");

        // Assert // Ověření
        assertThat(result).isTrue();
        verify(postSaveRepository).delete(testPostSave);
    }

    @Test
    void unsavePost_ShouldReturnFalse_WhenPostNotSaved() {
        // Arrange // Příprava
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(postSaveRepository.findByUserAndPost(testUser, testPost))
                .thenReturn(Optional.empty());

        // Act // Akce
        boolean result = postService.unsavePost(1L, "testuser");

        // Assert // Ověření
        assertThat(result).isFalse();
        verify(postSaveRepository, never()).delete(any(PostSave.class));
    }

    @Test
    void isPostSaved_ShouldReturnTrue_WhenPostIsSaved() {
        // Arrange // Příprava
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(postSaveRepository.existsByUserAndPost(testUser, testPost)).thenReturn(true);

        // Act // Akce
        boolean result = postService.isPostSaved(1L, "testuser");

        // Assert // Ověření
        assertThat(result).isTrue();
    }

    @Test
    void isPostSaved_ShouldReturnFalse_WhenUserNotFound() {
        // Arrange // Příprava
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act // Akce
        boolean result = postService.isPostSaved(1L, "testuser");

        // Assert // Ověření
        assertThat(result).isFalse();
    }

    @Test
    void getSavedPosts_ShouldReturnSavedPosts_WhenUserHasSavedPosts() {
        // Arrange // Příprava
        Page<Post> expectedPage = new PageImpl<>(Arrays.asList(testPost));
        when(postSaveRepository.findSavedPostsByUsername(eq("testuser"), any(Pageable.class)))
                .thenReturn(expectedPage);

        // Act // Akce
        Page<Post> result = postService.getSavedPosts("testuser", 0, 10);

        // Assert // Ověření
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(testPost);
    }

    @Test
    void byCommunity_ShouldReturnCommunityPosts_WhenCommunityExists() {
        // Arrange // Příprava
        Page<Post> expectedPage = new PageImpl<>(Arrays.asList(testPost));
        when(communityRepository.findByName("testcommunity"))
                .thenReturn(Optional.of(testCommunity));
        when(postRepository.findAllByCommunity(eq(testCommunity), any(Pageable.class)))
                .thenReturn(expectedPage);

        // Act // Akce
        Page<Post> result = postService.byCommunity("testcommunity", 0, 10, "hot");

        // Assert // Ověření
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(testPost);
    }

    @Test
    void byCommunity_ShouldThrowException_WhenCommunityNotFound() {
        // Arrange // Příprava
        when(communityRepository.findByName("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert // Akce a ověření
        assertThatThrownBy(() -> postService.byCommunity("nonexistent", 0, 10, "hot"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Community not found");
    }
}