package discussionforum.Service;

import discussionforum.Repository.*;
import discussionforum.Model.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class responsible for managing post operations in the discussion forum. // Služební třída zodpovědná za správu operací s příspěvky v diskuzním fóru.
 * Handles post creation, retrieval, voting, and saving functionality with full transactional support. // Zpracovává vytváření, načítání, hlasování a ukládání příspěvků s plnou transakcí podporou.
 * 
 * @author Petr Reitinger
 * @version 1.0
 * @since 2025
 */
@Service
public class PostService {
    
    /**
     * Repository for managing post data persistence. // Repozitář pro správu perzistence dat příspěvků.
     */
    private final PostRepository postRepository;
    
    /**
     * Repository for managing community data persistence. // Repozitář pro správu perzistence dat komunit.
     */
    private final CommunityRepository communityRepository;
    
    /**
     * Repository for managing user data persistence. // Repozitář pro správu perzistence uživatelských dat.
     */
    private final UserRepository userRepository;
    
    /**
     * Repository for managing vote data persistence. // Repozitář pro správu perzistence dat hlasování.
     */
    private final VoteRepository voteRepository;
    
    /**
     * Repository for managing post save data persistence. // Repozitář pro správu perzistence dat uložených příspěvků.
     */
    private final PostSaveRepository postSaveRepository;

    /**
     * Constructor for dependency injection of required repositories. // Konstruktor pro vkládání závislostí požadovaných repozitářů.
     * 
     * @param postRepository Repository for post operations // Repozitář pro operace s příspěvky
     * @param communityRepository Repository for community operations // Repozitář pro operace s komunitami
     * @param userRepository Repository for user operations // Repozitář pro uživatelské operace
     * @param voteRepository Repository for vote operations // Repozitář pro operace hlasování
     * @param postSaveRepository Repository for post save operations // Repozitář pro operace uložení příspěvků
     */
    public PostService(PostRepository postRepository, CommunityRepository communityRepository,
                       UserRepository userRepository, VoteRepository voteRepository,
                       PostSaveRepository postSaveRepository) {
        this.postRepository = postRepository;
        this.communityRepository = communityRepository;
        this.userRepository = userRepository;
        this.voteRepository = voteRepository;
        this.postSaveRepository = postSaveRepository;
    }

    /**
     * Creates a new post from the provided request data. // Vytváří nový příspěvek z poskytnutých dat požadavku.
     * Validates user and community existence before creating the post. // Ověřuje existenci uživatele a komunity před vytvořením příspěvku.
     * 
     * @param req Post request DTO containing post data // DTO požadavku příspěvku obsahující data příspěvku
     * @param username Username of the post author // Uživatelské jméno autora příspěvku
     * @return Created post entity // Vytvořená entita příspěvku
     * @throws EntityNotFoundException if user or community not found // Pokud uživatel nebo komunita nebyla nalezena
     */
    @Transactional
    public Post create(PostRequestDTO req, String username) {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));
        Community community = communityRepository.findById(req.getCommunityId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Community not found with id: " + req.getCommunityId()
                ));
        Post post = new Post();
        post.setTitle(req.getTitle());
        post.setContent(req.getContent());
        post.setAuthor(author);
        post.setCommunity(community);
        post.setScore(0);
        return postRepository.save(post);
    }

    /**
     * Retrieves paginated posts feed with specified sorting options. // Načítá stránkovaný feed příspěvků se specifikovanými možnostmi řazení.
     * Supports "hot", "new", and "top" sorting algorithms with default pagination settings. // Podporuje algoritmy řazení "hot", "new" a "top" s výchozím nastavením stránkování.
     * 
     * @param page Page number for pagination (0-based, defaults to 0) // Číslo stránky pro stránkování (od 0, výchozí 0)
     * @param size Page size for pagination (defaults to 10) // Velikost stránky pro stránkování (výchozí 10)
     * @param sort Sorting criteria: "hot", "new", or "top" (defaults to "hot") // Kritéria řazení: "hot", "new" nebo "top" (výchozí "hot")
     * @return Paginated list of posts // Stránkovaný seznam příspěvků
     */
    public Page<Post> feed(Integer page, Integer size, String sort) {
        Sort sortBy;
        switch (sort != null ? sort.toLowerCase() : "hot") {
            case "new":
                sortBy = Sort.by("createdAt").descending();
                break;
            case "top":
                sortBy = Sort.by("score").descending().and(Sort.by("createdAt").descending());
                break;
            case "hot":
            default:
                sortBy = Sort.by("score").descending().and(Sort.by("createdAt").descending());
                break;
        }
        
        Pageable pageable = PageRequest.of(page == null ? 0 : page, size == null ? 10 : size, sortBy);
        return postRepository.findAll(pageable);
    }

    /**
     * Retrieves paginated posts from a specific community with sorting options. // Načítá stránkované příspěvky z konkrétní komunity s možnostmi řazení.
     * Filters posts by community and applies the specified sorting algorithm. // Filtruje příspěvky podle komunity a aplikuje specifikovaný algoritmus řazení.
     * 
     * @param name Community name to filter posts by // Název komunity pro filtrování příspěvků
     * @param page Page number for pagination (0-based, defaults to 0) // Číslo stránky pro stránkování (od 0, výchozí 0)
     * @param size Page size for pagination (defaults to 10) // Velikost stránky pro stránkování (výchozí 10)
     * @param sort Sorting criteria: "hot", "new", or "top" (defaults to "hot") // Kritéria řazení: "hot", "new" nebo "top" (výchozí "hot")
     * @return Paginated list of posts from the specified community // Stránkovaný seznam příspěvků ze specifikované komunity
     * @throws EntityNotFoundException if community not found // Pokud komunita nebyla nalezena
     */
    public Page<Post> byCommunity(String name, Integer page, Integer size, String sort) {
        Community c = communityRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Community not found with name: " + name));
        
        Sort sortBy;
        switch (sort != null ? sort.toLowerCase() : "hot") {
            case "new":
                sortBy = Sort.by("createdAt").descending();
                break;
            case "top":
                sortBy = Sort.by("score").descending().and(Sort.by("createdAt").descending());
                break;
            case "hot":
            default:
                sortBy = Sort.by("score").descending().and(Sort.by("createdAt").descending());
                break;
        }
        
        Pageable pageable = PageRequest.of(page == null ? 0 : page, size == null ? 10 : size, sortBy);
        return postRepository.findAllByCommunity(c, pageable);
    }

    /**
     * Retrieves a specific post by its unique identifier. // Načítá konkrétní příspěvek podle jeho jedinečného identifikátoru.
     * 
     * @param id Unique identifier of the post // Jedinečný identifikátor příspěvku
     * @return Post entity with the specified ID // Entita příspěvku se specifikovaným ID
     * @throws EntityNotFoundException if post not found // Pokud příspěvek nebyl nalezen
     */
    public Post get(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + id));
    }

    /**
     * Handles voting on a post (upvote/downvote) with vote switching logic. // Zpracovává hlasování o příspěvku (kladné/záporné hlasování) s logikou přepínání hlasů.
     * Manages vote creation, deletion, and switching between vote types while updating post score. // Spravuje vytváření, mazání a přepínání mezi typy hlasů při aktualizaci skóre příspěvku.
     * 
     * @param postId ID of the post to vote on // ID příspěvku pro hlasování
     * @param username Username of the voting user // Uživatelské jméno hlasujícího uživatele
     * @param type Vote type (UPVOTE or DOWNVOTE) // Typ hlasování (UPVOTE nebo DOWNVOTE)
     * @return Updated post score after voting // Aktualizované skóre příspěvku po hlasování
     * @throws EntityNotFoundException if post or user not found // Pokud příspěvek nebo uživatel nebyl nalezen
     */
    @Transactional
    public int vote(Long postId, String username, VoteType type) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id " + postId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));

        Vote vote = voteRepository.findByPostAndUser(post, user).orElse(null);
        if (vote == null) {
            vote = new Vote();
            vote.setPost(post);
            vote.setUser(user);
            vote.setType(type);
            voteRepository.save(vote);
            post.setScore(post.getScore() + (type == VoteType.UPVOTE ? 1 : -1));
        } else if (vote.getType() == type) {
            voteRepository.delete(vote);
            post.setScore(post.getScore() + (type == VoteType.UPVOTE ? -1 : 1));
        } else {
            vote.setType(type);
            voteRepository.save(vote);
            post.setScore(post.getScore() + (type == VoteType.UPVOTE ? 2 : -2));
        }
        postRepository.save(post);
        return post.getScore();
    }
    
    /**
     * Saves a post to a user's saved posts collection. // Uloží příspěvek do kolekce uložených příspěvků uživatele.
     * Prevents duplicate saves by checking existing save relationship. // Zabraňuje duplicitnímu ukládání kontrolou existující relace uložení.
     * 
     * @param postId ID of the post to save // ID příspěvku k uložení
     * @param username Username of the user saving the post // Uživatelské jméno uživatele ukládajícího příspěvek
     * @return true if successfully saved, false if already saved // true pokud úspěšně uloženo, false pokud už bylo uloženo
     * @throws EntityNotFoundException if post or user not found // Pokud příspěvek nebo uživatel nebyl nalezen
     */
    @Transactional
    public boolean savePost(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id " + postId));
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));
        
        if (postSaveRepository.existsByUserAndPost(user, post)) {
            return false;
        }
        
        PostSave postSave = new PostSave();
        postSave.setUser(user);
        postSave.setPost(post);
        postSaveRepository.save(postSave);
        
        return true;
    }
    
    /**
     * Removes a post from a user's saved posts collection. // Odstraní příspěvek z kolekce uložených příspěvků uživatele.
     * 
     * @param postId ID of the post to unsave // ID příspěvku k odebrání z uložených
     * @param username Username of the user unsaving the post // Uživatelské jméno uživatele odebírajícího příspěvek
     * @return true if successfully unsaved, false if was not saved // true pokud úspěšně odebráno, false pokud nebylo uloženo
     * @throws EntityNotFoundException if post or user not found // Pokud příspěvek nebo uživatel nebyl nalezen
     */
    @Transactional
    public boolean unsavePost(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id " + postId));
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));
        
        return postSaveRepository.findByUserAndPost(user, post)
                .map(postSave -> {
                    postSaveRepository.delete(postSave);
                    return true;
                })
                .orElse(false);
    }
    
    /**
     * Checks if a specific post is saved by a user. // Ověřuje, zda je konkrétní příspěvek uložen uživatelem.
     * 
     * @param postId ID of the post to check // ID příspěvku k ověření
     * @param username Username of the user // Uživatelské jméno uživatele
     * @return true if post is saved by user, false otherwise // true pokud je příspěvek uložen uživatelem, false jinak
     */
    public boolean isPostSaved(Long postId, String username) {
        try {
            Post post = postRepository.findById(postId).orElse(null);
            User user = userRepository.findByUsername(username).orElse(null);
            if (post == null || user == null) return false;
            return postSaveRepository.existsByUserAndPost(user, post);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Retrieves paginated list of posts saved by a specific user. // Načítá stránkovaný seznam příspěvků uložených konkrétním uživatelem.
     * Orders saved posts by save date in descending order. // Řadí uložené příspěvky podle data uložení v sestupném pořadí.
     * 
     * @param username Username of the user whose saved posts to retrieve // Uživatelské jméno uživatele, jehož uložené příspěvky načíst
     * @param page Page number for pagination (0-based, defaults to 0) // Číslo stránky pro stránkování (od 0, výchozí 0)
     * @param size Page size for pagination (defaults to 10) // Velikost stránky pro stránkování (výchozí 10)
     * @return Paginated list of saved posts // Stránkovaný seznam uložených příspěvků
     */
    public Page<Post> getSavedPosts(String username, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page == null ? 0 : page, size == null ? 10 : size, Sort.by("savedAt").descending());
        return postSaveRepository.findSavedPostsByUsername(username, pageable);
    }
    
    /**
     * Updates an existing post. // Aktualizuje existující příspěvek.
     * 
     * @param post Post to update // Příspěvek k aktualizaci
     * @return Updated post // Aktualizovaný příspěvek
     */
    @Transactional
    public Post save(Post post) {
        return postRepository.save(post);
    }
}