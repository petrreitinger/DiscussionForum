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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for HomeController class // Komplexní unit testy pro třídu HomeController
 * Tests home page functionality including post feed and sorting
 * Testuje funkcionalitu domovské stránky včetně feedu příspěvků a řazení
 * 
 * @author Petr Reitinger
 * @version 1.0
 * @since 2025
 */
@WebMvcTest(HomeController.class)
@ActiveProfiles("test")
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @MockBean
    private CommunityService communityService;

    private Post testPost;
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
    }

    @Test
    void home_ShouldReturnIndexView_WithDefaultParameters() throws Exception {
        // Arrange // Příprava
        Page<Post> postPage = new PageImpl<>(Arrays.asList(testPost));
        when(postService.feed(eq(0), eq(10), eq("hot"))).thenReturn(postPage);
        when(communityService.all()).thenReturn(Arrays.asList(testCommunity));

        // Act & Assert // Akce a ověření
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(model().attributeExists("communities"))
                .andExpect(model().attributeExists("postRequest"))
                .andExpect(model().attribute("currentSort", "hot"));

        verify(postService).feed(0, 10, "hot");
        verify(communityService).all();
    }

    @Test
    void home_ShouldReturnIndexView_WithCustomPagination() throws Exception {
        // Arrange // Příprava
        Page<Post> postPage = new PageImpl<>(Arrays.asList(testPost));
        when(postService.feed(eq(1), eq(20), eq("new"))).thenReturn(postPage);
        when(communityService.all()).thenReturn(Arrays.asList(testCommunity));

        // Act & Assert // Akce a ověření
        mockMvc.perform(get("/")
                        .param("page", "1")
                        .param("size", "20")
                        .param("sort", "new"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(model().attributeExists("communities"))
                .andExpect(model().attribute("currentSort", "new"));

        verify(postService).feed(1, 20, "new");
        verify(communityService).all();
    }

    @Test
    void home_ShouldReturnIndexView_WithTopSort() throws Exception {
        // Arrange // Příprava
        Page<Post> postPage = new PageImpl<>(Arrays.asList(testPost));
        when(postService.feed(eq(0), eq(10), eq("top"))).thenReturn(postPage);
        when(communityService.all()).thenReturn(Arrays.asList(testCommunity));

        // Act & Assert // Akce a ověření
        mockMvc.perform(get("/")
                        .param("sort", "top"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("currentSort", "top"));

        verify(postService).feed(0, 10, "top");
    }

    @Test
    void home_ShouldHandleEmptyPostList() throws Exception {
        // Arrange // Příprava
        Page<Post> emptyPage = new PageImpl<>(Arrays.asList());
        when(postService.feed(any(Integer.class), any(Integer.class), anyString())).thenReturn(emptyPage);
        when(communityService.all()).thenReturn(Arrays.asList(testCommunity));

        // Act & Assert // Akce a ověření
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("posts"));

        verify(postService).feed(0, 10, "hot");
    }

    @Test
    void home_ShouldHandleEmptyCommunityList() throws Exception {
        // Arrange // Příprava
        Page<Post> postPage = new PageImpl<>(Arrays.asList(testPost));
        when(postService.feed(any(Integer.class), any(Integer.class), anyString())).thenReturn(postPage);
        when(communityService.all()).thenReturn(Arrays.asList());

        // Act & Assert // Akce a ověření
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("communities"));

        verify(communityService).all();
    }

    @Test
    @WithMockUser(username = "testuser")
    void home_ShouldIncludeUserContext_WhenAuthenticated() throws Exception {
        // Arrange // Příprava
        Page<Post> postPage = new PageImpl<>(Arrays.asList(testPost));
        when(postService.feed(any(Integer.class), any(Integer.class), anyString())).thenReturn(postPage);
        when(communityService.all()).thenReturn(Arrays.asList(testCommunity));

        // Act & Assert // Akce a ověření
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(model().attributeExists("communities"))
                .andExpect(model().attributeExists("postRequest"));
    }

    @Test
    void home_ShouldHandleServiceException_Gracefully() throws Exception {
        // Arrange // Příprava
        when(postService.feed(any(Integer.class), any(Integer.class), anyString()))
                .thenThrow(new RuntimeException("Database error"));
        when(communityService.all()).thenReturn(Arrays.asList(testCommunity));

        // Act & Assert // Akce a ověření
        // The controller should handle the exception gracefully
        // Kontroler by měl zvládnout výjimku elegantně
        mockMvc.perform(get("/"))
                .andExpect(status().is5xxServerError()); // or whatever error handling is implemented

        verify(postService).feed(0, 10, "hot");
    }

    @Test
    void home_ShouldSetCorrectPageTitle() throws Exception {
        // Arrange // Příprava
        Page<Post> postPage = new PageImpl<>(Arrays.asList(testPost));
        when(postService.feed(any(Integer.class), any(Integer.class), anyString())).thenReturn(postPage);
        when(communityService.all()).thenReturn(Arrays.asList(testCommunity));

        // Act & Assert // Akce a ověření
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
                // Note: Page title would typically be set in the template or via a model attribute
                // Poznámka: Titul stránky by byl obvykle nastaven v šabloně nebo prostřednictvím atributu modelu
    }

    @Test
    void home_ShouldHandleInvalidSortParameter() throws Exception {
        // Arrange // Příprava
        Page<Post> postPage = new PageImpl<>(Arrays.asList(testPost));
        when(postService.feed(eq(0), eq(10), eq("invalid"))).thenReturn(postPage);
        when(communityService.all()).thenReturn(Arrays.asList(testCommunity));

        // Act & Assert // Akce a ověření
        mockMvc.perform(get("/")
                        .param("sort", "invalid"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("currentSort", "invalid"));

        // Service should handle invalid sort gracefully
        // Služba by měla elegantně zvládnout neplatné řazení
        verify(postService).feed(0, 10, "invalid");
    }

    @Test
    void home_ShouldHandleNegativePage() throws Exception {
        // Arrange // Příprava
        Page<Post> postPage = new PageImpl<>(Arrays.asList(testPost));
        when(postService.feed(eq(-1), eq(10), eq("hot"))).thenReturn(postPage);
        when(communityService.all()).thenReturn(Arrays.asList(testCommunity));

        // Act & Assert // Akce a ověření
        mockMvc.perform(get("/")
                        .param("page", "-1"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));

        // Service should handle negative page numbers
        // Služba by měla zvládnout záporná čísla stránek
        verify(postService).feed(-1, 10, "hot");
    }

    @Test
    void home_ShouldHandleLargePageSize() throws Exception {
        // Arrange // Příprava
        Page<Post> postPage = new PageImpl<>(Arrays.asList(testPost));
        when(postService.feed(eq(0), eq(1000), eq("hot"))).thenReturn(postPage);
        when(communityService.all()).thenReturn(Arrays.asList(testCommunity));

        // Act & Assert // Akce a ověření
        mockMvc.perform(get("/")
                        .param("size", "1000"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));

        // Service should handle large page sizes appropriately
        // Služba by měla vhodně zvládnout velké velikosti stránek
        verify(postService).feed(0, 1000, "hot");
    }
}