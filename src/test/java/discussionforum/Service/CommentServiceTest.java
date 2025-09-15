package discussionforum.Service;

import discussionforum.Model.*;
import discussionforum.Repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for CommentService class // Komplexní unit testy pro třídu CommentService
 * Tests comment creation, reply functionality, and voting logic
 * Testuje vytváření komentářů, funkcionalitu odpovědí a logiku hlasování
 * 
 * @author Petr Reitinger
 * @version 1.0
 * @since 2025
 */
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    
    @Mock
    private PostRepository postRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private CommentVoteRepository commentVoteRepository;

    @InjectMocks
    private CommentService commentService;

    private User testUser;
    private Post testPost;
    private Comment testComment;
    private Comment testParentComment;
    private CommentRequestDTO testCommentRequest;
    private CommentVote testCommentVote;

    @BeforeEach
    void setUp() {
        // Initialize test data // Inicializace testovacích dat
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setDisplayName("Test User");

        Community testCommunity = new Community();
        testCommunity.setId(1L);
        testCommunity.setName("testcommunity");

        testPost = new Post();
        testPost.setId(1L);
        testPost.setTitle("Test Post");
        testPost.setContent("Test Content");
        testPost.setAuthor(testUser);
        testPost.setCommunity(testCommunity);

        testComment = new Comment();
        testComment.setId(1L);
        testComment.setContent("Test Comment");
        testComment.setAuthor(testUser);
        testComment.setPost(testPost);
        testComment.setScore(0);

        testParentComment = new Comment();
        testParentComment.setId(2L);
        testParentComment.setContent("Parent Comment");
        testParentComment.setAuthor(testUser);
        testParentComment.setPost(testPost);

        testCommentRequest = new CommentRequestDTO();
        testCommentRequest.setContent("Test Comment Content");
        testCommentRequest.setPostId(1L);

        testCommentVote = new CommentVote();
        testCommentVote.setId(1L);
        testCommentVote.setComment(testComment);
        testCommentVote.setUser(testUser);
        testCommentVote.setType(VoteType.UPVOTE);
    }

    @Test
    void add_ShouldCreateComment_WhenValidRequest() {
        // Arrange // Příprava
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        // Act // Akce
        Comment result = commentService.add(testCommentRequest, "testuser");

        // Assert // Ověření
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("Test Comment");
        assertThat(result.getAuthor()).isEqualTo(testUser);
        assertThat(result.getPost()).isEqualTo(testPost);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void add_ShouldThrowException_WhenPostNotFound() {
        // Arrange // Příprava
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert // Akce a ověření
        assertThatThrownBy(() -> commentService.add(testCommentRequest, "testuser"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Post not found");
    }

    @Test
    void add_ShouldThrowException_WhenUserNotFound() {
        // Arrange // Příprava
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert // Akce a ověření
        assertThatThrownBy(() -> commentService.add(testCommentRequest, "nonexistent"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void addReply_ShouldCreateReply_WhenValidRequest() {
        // Arrange // Příprava
        CommentRequestDTO replyRequest = new CommentRequestDTO();
        replyRequest.setContent("Reply Content");
        replyRequest.setPostId(1L);

        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(commentRepository.findById(2L)).thenReturn(Optional.of(testParentComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        // Act // Akce
        Comment result = commentService.addReply(replyRequest, "testuser", 2L);

        // Assert // Ověření
        assertThat(result).isNotNull();
        verify(commentRepository).save(argThat(comment -> 
            comment.getParent() == testParentComment &&
            comment.getPost() == testPost &&
            comment.getAuthor() == testUser
        ));
    }

    @Test
    void addReply_ShouldThrowException_WhenParentCommentNotFound() {
        // Arrange // Příprava
        CommentRequestDTO replyRequest = new CommentRequestDTO();
        replyRequest.setContent("Reply Content");
        replyRequest.setPostId(1L);

        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert // Akce a ověření
        assertThatThrownBy(() -> commentService.addReply(replyRequest, "testuser", 999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Parent comment not found");
    }

    @Test
    void vote_ShouldCreateUpvote_WhenNoExistingVote() {
        // Arrange // Příprava
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(commentVoteRepository.findByCommentAndUser(testComment, testUser))
                .thenReturn(Optional.empty());
        when(commentVoteRepository.save(any(CommentVote.class))).thenReturn(testCommentVote);
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        // Act // Akce
        int result = commentService.voteComment(1L, "testuser", VoteType.UPVOTE);

        // Assert // Ověření
        assertThat(result).isEqualTo(1); // Score should increase by 1 // Skóre by se mělo zvýšit o 1
        verify(commentVoteRepository).save(any(CommentVote.class));
        verify(commentRepository).save(testComment);
    }

    @Test
    void vote_ShouldToggleVote_WhenSameVoteExists() {
        // Arrange // Příprava
        testComment.setScore(1);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(commentVoteRepository.findByCommentAndUser(testComment, testUser))
                .thenReturn(Optional.of(testCommentVote));

        // Act // Akce
        int result = commentService.voteComment(1L, "testuser", VoteType.UPVOTE);

        // Assert // Ověření
        assertThat(result).isEqualTo(0); // Score should decrease by 1 // Skóre by se mělo snížit o 1
        verify(commentVoteRepository).delete(testCommentVote);
    }

    @Test
    void vote_ShouldSwitchVote_WhenDifferentVoteExists() {
        // Arrange // Příprava
        testCommentVote.setType(VoteType.DOWNVOTE);
        testComment.setScore(-1);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(commentVoteRepository.findByCommentAndUser(testComment, testUser))
                .thenReturn(Optional.of(testCommentVote));

        // Act // Akce
        int result = commentService.voteComment(1L, "testuser", VoteType.UPVOTE);

        // Assert // Ověření
        assertThat(result).isEqualTo(1); // Score should increase by 2 // Skóre by se mělo zvýšit o 2
        verify(commentVoteRepository).save(testCommentVote);
        assertThat(testCommentVote.getType()).isEqualTo(VoteType.UPVOTE);
    }

    @Test
    void vote_ShouldThrowException_WhenCommentNotFound() {
        // Arrange // Příprava
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert // Akce a ověření
        assertThatThrownBy(() -> commentService.voteComment(999L, "testuser", VoteType.UPVOTE))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Comment not found");
    }

    @Test
    void vote_ShouldThrowException_WhenUserNotFound() {
        // Arrange // Příprava
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert // Akce a ověření
        assertThatThrownBy(() -> commentService.voteComment(1L, "nonexistent", VoteType.UPVOTE))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void vote_ShouldCreateDownvote_WhenNoExistingVote() {
        // Arrange // Příprava
        testCommentVote.setType(VoteType.DOWNVOTE);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(commentVoteRepository.findByCommentAndUser(testComment, testUser))
                .thenReturn(Optional.empty());
        when(commentVoteRepository.save(any(CommentVote.class))).thenReturn(testCommentVote);
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        // Act // Akce
        int result = commentService.voteComment(1L, "testuser", VoteType.DOWNVOTE);

        // Assert // Ověření
        assertThat(result).isEqualTo(-1); // Score should decrease by 1 // Skóre by se mělo snížit o 1
        verify(commentVoteRepository).save(any(CommentVote.class));
        verify(commentRepository).save(testComment);
    }

    @Test
    void add_ShouldSetCorrectCommentProperties_WhenCreating() {
        // Arrange // Příprava
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(1L);
            return comment;
        });

        // Act // Akce
        Comment result = commentService.add(testCommentRequest, "testuser");

        // Assert // Ověření
        verify(commentRepository).save(argThat(comment -> 
            comment.getContent().equals("Test Comment Content") &&
            comment.getAuthor().equals(testUser) &&
            comment.getPost().equals(testPost) &&
            comment.getScore() == 0 &&
            comment.getParent() == null
        ));
    }

    @Test
    void addReply_ShouldSetCorrectReplyProperties_WhenCreating() {
        // Arrange // Příprava
        CommentRequestDTO replyRequest = new CommentRequestDTO();
        replyRequest.setContent("Reply Content");
        replyRequest.setPostId(1L);

        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(commentRepository.findById(2L)).thenReturn(Optional.of(testParentComment));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(3L);
            return comment;
        });

        // Act // Akce
        Comment result = commentService.addReply(replyRequest, "testuser", 2L);

        // Assert // Ověření
        verify(commentRepository).save(argThat(comment -> 
            comment.getContent().equals("Reply Content") &&
            comment.getAuthor().equals(testUser) &&
            comment.getPost().equals(testPost) &&
            comment.getParent().equals(testParentComment) &&
            comment.getScore() == 0
        ));
    }
}