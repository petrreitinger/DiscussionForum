package discussionforum.Controllers;

import discussionforum.Model.*;
import discussionforum.Service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for PostController class // Komplexní unit testy pro třídu PostController
 * Tests all web layer functionality including post creation, voting, commenting
 * Testuje veškerou funkcionalitu webové vrstvy včetně vytváření příspěvků, hlasování a komentování
 * 
 * @author Petr Reitinger
 * @version 1.0
 * @since 2025
 */
@WebMvcTest(PostController.class)
@ActiveProfiles("test")
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @MockBean
    private CommentService commentService;

    @MockBean
    private CommunityService communityService;

    private Post testPost;
    private Comment testComment;
    private Community testCommunity;
    private User testUser;

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
        testPost.setScore(5);

        testComment = new Comment();
        testComment.setId(1L);
        testComment.setContent("Test Comment");
        testComment.setAuthor(testUser);
        testComment.setPost(testPost);
        testComment.setScore(3);
    }

    @Test
    @WithMockUser(username = "testuser")
    void newPostPage_ShouldReturnNewPostView_WhenAuthenticated() throws Exception {
        // Arrange // Příprava
        when(communityService.all()).thenReturn(Arrays.asList(testCommunity));

        // Act & Assert // Akce a ověření
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("post/new"))
                .andExpect(model().attributeExists("communities"));

        verify(communityService).all();
    }

    @Test
    void newPostPage_ShouldRedirectToLogin_WhenNotAuthenticated() throws Exception {
        // Act & Assert // Akce a ověření
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "testuser")
    void createPost_ShouldRedirectToPost_WhenValidInput() throws Exception {
        // Arrange // Příprava
        when(postService.create(any(PostRequestDTO.class), eq("testuser")))
                .thenReturn(testPost);

        // Act & Assert // Akce a ověření
        mockMvc.perform(post("/posts/new")
                        .with(csrf())
                        .param("title", "Test Post")
                        .param("content", "Test Content")
                        .param("communityId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/1"));

        verify(postService).create(any(PostRequestDTO.class), eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void createPost_ShouldReturnFormWithErrors_WhenInvalidInput() throws Exception {
        // Arrange // Příprava
        when(communityService.all()).thenReturn(Arrays.asList(testCommunity));

        // Act & Assert // Akce a ověření
        mockMvc.perform(post("/posts/new")
                        .with(csrf())
                        .param("title", "") // Empty title should cause validation error
                        .param("content", "Test Content")
                        .param("communityId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("post/new"))
                .andExpect(model().attributeHasFieldErrors("postRequest", "title"));

        verify(postService, never()).create(any(PostRequestDTO.class), anyString());
    }

    @Test
    @WithMockUser(username = "testuser")
    void viewPost_ShouldReturnPostDetailView_WhenPostExists() throws Exception {
        // Arrange // Příprava
        when(postService.get(1L)).thenReturn(testPost);
        when(communityService.all()).thenReturn(Arrays.asList(testCommunity));

        testPost.setComments(Set.of(testComment));

        // Act & Assert // Akce a ověření
        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("post/detail"))
                .andExpect(model().attributeExists("post"))
                .andExpect(model().attributeExists("communities"))
                .andExpect(model().attributeExists("commentRequest"));

        verify(postService).get(1L);
    }

    @Test
    @WithMockUser(username = "testuser")
    void votePost_ShouldReturnUpdatedScore_WhenValidVote() throws Exception {
        // Arrange // Příprava
        when(postService.vote(1L, "testuser", VoteType.UPVOTE)).thenReturn(6);

        // Act & Assert // Akce a ověření
        mockMvc.perform(post("/posts/1/upvote")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"success\": true, \"score\": 6}"));

        verify(postService).vote(1L, "testuser", VoteType.UPVOTE);
    }

    @Test
    @WithMockUser(username = "testuser")
    void downvotePost_ShouldReturnUpdatedScore_WhenValidVote() throws Exception {
        // Arrange // Příprava
        when(postService.vote(1L, "testuser", VoteType.DOWNVOTE)).thenReturn(4);

        // Act & Assert // Akce a ověření
        mockMvc.perform(post("/posts/1/downvote")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"success\": true, \"score\": 4}"));

        verify(postService).vote(1L, "testuser", VoteType.DOWNVOTE);
    }

    @Test
    @WithMockUser(username = "testuser")
    void savePost_ShouldReturnSuccess_WhenPostSaved() throws Exception {
        // Arrange // Příprava
        when(postService.savePost(1L, "testuser")).thenReturn(true);

        // Act & Assert // Akce a ověření
        mockMvc.perform(post("/posts/1/save")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"success\": true, \"message\": \"Post saved successfully!\"}"));

        verify(postService).savePost(1L, "testuser");
    }

    @Test
    @WithMockUser(username = "testuser")
    void savePost_ShouldReturnAlreadySaved_WhenPostAlreadySaved() throws Exception {
        // Arrange // Příprava
        when(postService.savePost(1L, "testuser")).thenReturn(false);

        // Act & Assert // Akce a ověření
        mockMvc.perform(post("/posts/1/save")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"success\": false, \"message\": \"Post is already saved\"}"));

        verify(postService).savePost(1L, "testuser");
    }

    @Test
    @WithMockUser(username = "testuser")
    void unsavePost_ShouldReturnSuccess_WhenPostUnsaved() throws Exception {
        // Arrange // Příprava
        when(postService.unsavePost(1L, "testuser")).thenReturn(true);

        // Act & Assert // Akce a ověření
        mockMvc.perform(post("/posts/1/unsave")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"success\": true, \"message\": \"Post unsaved successfully!\"}"));

        verify(postService).unsavePost(1L, "testuser");
    }

    @Test
    @WithMockUser(username = "testuser")
    void addComment_ShouldRedirectToPost_WhenValidComment() throws Exception {
        // Arrange // Příprava
        when(commentService.add(any(CommentRequestDTO.class), eq("testuser")))
                .thenReturn(testComment);

        // Act & Assert // Akce a ověření
        mockMvc.perform(post("/posts/1/comments")
                        .with(csrf())
                        .param("content", "Test Comment")
                        .param("postId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/1"));

        verify(commentService).add(any(CommentRequestDTO.class), eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void addComment_ShouldRedirectWithError_WhenEmptyComment() throws Exception {
        // Act & Assert // Akce a ověření
        mockMvc.perform(post("/posts/1/comments")
                        .with(csrf())
                        .param("content", "") // Empty content
                        .param("postId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/1?error=empty_comment"));

        verify(commentService, never()).add(any(CommentRequestDTO.class), anyString());
    }

    @Test
    @WithMockUser(username = "testuser")
    void addReply_ShouldRedirectToPost_WhenValidReply() throws Exception {
        // Arrange // Příprava
        when(commentService.addReply(any(CommentRequestDTO.class), eq("testuser"), any(Long.class)))
                .thenReturn(testComment);

        // Act & Assert // Akce a ověření
        mockMvc.perform(post("/posts/1/comments/reply")
                        .with(csrf())
                        .param("content", "Test Reply")
                        .param("postId", "1")
                        .param("parentId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/1"));

        verify(commentService).addReply(any(CommentRequestDTO.class), eq("testuser"), any(Long.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void voteComment_ShouldReturnUpdatedScore_WhenValidVote() throws Exception {
        // Arrange // Příprava
        when(commentService.voteComment(1L, "testuser", VoteType.UPVOTE)).thenReturn(4);

        // Act & Assert // Akce a ověření
        mockMvc.perform(post("/posts/1/comments/1/upvote")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"success\": true, \"score\": 4}"));

        verify(commentService).voteComment(1L, "testuser", VoteType.UPVOTE);
    }

    @Test
    void votePost_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // Act & Assert // Akce a ověření
        mockMvc.perform(post("/posts/1/upvote")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "testuser")
    void votePost_ShouldReturnErrorResponse_WhenServiceThrowsException() throws Exception {
        // Arrange // Příprava
        when(postService.vote(1L, "testuser", VoteType.UPVOTE))
                .thenThrow(new RuntimeException("Post not found"));

        // Act & Assert // Akce a ověření
        mockMvc.perform(post("/posts/1/upvote")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"success\": false, \"message\": \"Error voting on post: Post not found\"}"));

        verify(postService).vote(1L, "testuser", VoteType.UPVOTE);
    }
}