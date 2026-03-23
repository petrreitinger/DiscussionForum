package discussionforum.controllers;

import discussionforum.model.Comment;
import discussionforum.model.CommentRequestDTO;
import discussionforum.model.Community;
import discussionforum.model.Post;
import discussionforum.model.PostRequestDTO;
import discussionforum.model.User;
import discussionforum.model.VoteType;
import discussionforum.service.CommentService;
import discussionforum.service.CommunityService;
import discussionforum.service.FileUploadService;
import discussionforum.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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

    @MockBean
    private FileUploadService fileUploadService;

    private Post testPost;
    private Comment testComment;
    private Community testCommunity;
    private User testUser;

    @BeforeEach
    void setUp() {
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
        when(communityService.all()).thenReturn(Arrays.asList(testCommunity));

        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("post/new"))
                .andExpect(model().attributeExists("communities"));

        verify(communityService).all();
    }

    @Test
    void newPostPage_ShouldRedirectToLogin_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "testuser")
    void createPost_ShouldRedirectToPost_WhenValidInput() throws Exception {
        when(postService.create(any(PostRequestDTO.class), eq("testuser")))
                .thenReturn(testPost);

        mockMvc.perform(post("/posts")
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
        when(communityService.all()).thenReturn(Arrays.asList(testCommunity));

        mockMvc.perform(post("/posts")
                        .with(csrf())
                        .param("title", "")
                        .param("content", "Test Content")
                        .param("communityId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("post/new"));

        verify(postService, never()).create(any(PostRequestDTO.class), anyString());
    }

    @Test
    @WithMockUser(username = "testuser")
    void viewPost_ShouldReturnPostDetailView_WhenPostExists() throws Exception {
        when(postService.get(1L)).thenReturn(testPost);
        when(commentService.forPost(1L)).thenReturn(Collections.emptyList());
        when(commentService.getTotalCommentCount(1L)).thenReturn(0);
        when(communityService.all()).thenReturn(Arrays.asList(testCommunity));

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("post/detail"))
                .andExpect(model().attributeExists("post"))
                .andExpect(model().attributeExists("communities"));

        verify(postService).get(1L);
    }

    @Test
    @WithMockUser(username = "testuser")
    void upvotePost_ShouldRedirect_WhenValidVote() throws Exception {
        when(postService.vote(1L, "testuser", VoteType.UPVOTE)).thenReturn(6);

        mockMvc.perform(post("/posts/1/upvote")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        verify(postService).vote(1L, "testuser", VoteType.UPVOTE);
    }

    @Test
    @WithMockUser(username = "testuser")
    void downvotePost_ShouldRedirect_WhenValidVote() throws Exception {
        when(postService.vote(1L, "testuser", VoteType.DOWNVOTE)).thenReturn(4);

        mockMvc.perform(post("/posts/1/downvote")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        verify(postService).vote(1L, "testuser", VoteType.DOWNVOTE);
    }

    @Test
    @WithMockUser(username = "testuser")
    void savePost_ShouldReturnSuccess_WhenPostSaved() throws Exception {
        when(postService.savePost(1L, "testuser")).thenReturn(true);

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
        when(postService.savePost(1L, "testuser")).thenReturn(false);

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
        when(postService.unsavePost(1L, "testuser")).thenReturn(true);

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
        when(commentService.add(any(CommentRequestDTO.class), eq("testuser")))
                .thenReturn(testComment);

        mockMvc.perform(post("/posts/1/comment")
                        .with(csrf())
                        .param("content", "Test Comment"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/1"));

        verify(commentService).add(any(CommentRequestDTO.class), eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void addReply_ShouldRedirectToPost_WhenValidReply() throws Exception {
        when(commentService.addReply(any(CommentRequestDTO.class), eq("testuser"), eq(1L)))
                .thenReturn(testComment);

        mockMvc.perform(post("/posts/1/comments/1/reply")
                        .with(csrf())
                        .param("content", "Test Reply"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/1"));

        verify(commentService).addReply(any(CommentRequestDTO.class), eq("testuser"), eq(1L));
    }

    @Test
    @WithMockUser(username = "testuser")
    void voteComment_ShouldReturnUpdatedScore_WhenValidVote() throws Exception {
        when(commentService.voteComment(1L, "testuser", VoteType.UPVOTE)).thenReturn(4);

        mockMvc.perform(post("/posts/1/comments/1/upvote")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"success\": true, \"score\": 4}"));

        verify(commentService).voteComment(1L, "testuser", VoteType.UPVOTE);
    }

    @Test
    void votePost_ShouldRedirectToLogin_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/posts/1/upvote")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }
}
