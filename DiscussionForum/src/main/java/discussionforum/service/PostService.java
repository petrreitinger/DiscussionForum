package discussionforum.service;

import discussionforum.model.Community;
import discussionforum.model.Post;
import discussionforum.model.PostRequestDTO;
import discussionforum.model.PostSave;
import discussionforum.model.User;
import discussionforum.model.Vote;
import discussionforum.model.VoteType;
import discussionforum.repository.CommunityRepository;
import discussionforum.repository.PostRepository;
import discussionforum.repository.PostSaveRepository;
import discussionforum.repository.UserRepository;
import discussionforum.repository.VoteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    private final PostRepository postRepository;
    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final VoteRepository voteRepository;
    private final PostSaveRepository postSaveRepository;

    public PostService(PostRepository postRepository, CommunityRepository communityRepository,
                       UserRepository userRepository, VoteRepository voteRepository,
                       PostSaveRepository postSaveRepository) {
        this.postRepository = postRepository;
        this.communityRepository = communityRepository;
        this.userRepository = userRepository;
        this.voteRepository = voteRepository;
        this.postSaveRepository = postSaveRepository;
    }

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

    public Page<Post> feed(Integer page, Integer size, String sort) {
        int p = page == null ? 0 : page;
        int s = size == null ? 10 : size;
        String sortKey = sort != null ? sort.toLowerCase() : "hot";
        Pageable pageable = PageRequest.of(p, s, resolveSort(sortKey));
        return postRepository.findAll(pageable);
    }

    public Page<Post> byCommunity(String name, Integer page, Integer size, String sort) {
        Community c = communityRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Community not found with name: " + name));

        int p = page == null ? 0 : page;
        int s = size == null ? 10 : size;
        String sortKey = sort != null ? sort.toLowerCase() : "hot";
        Pageable pageable = PageRequest.of(p, s, resolveSort(sortKey));
        return postRepository.findAllByCommunity(c, pageable);
    }

    private Sort resolveSort(String sortKey) {
        return switch (sortKey) {
            case "new" -> Sort.by("createdAt").descending();
            case "top" -> Sort.by("score").descending().and(Sort.by("createdAt").descending());
            // "hot" = recency-weighted: newest first, score as tiebreaker
            case "hot" -> Sort.by("createdAt").descending().and(Sort.by("score").descending());
            default -> Sort.by("createdAt").descending().and(Sort.by("score").descending());
        };
    }

    public Post get(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + id));
    }

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

    public Page<Post> getSavedPosts(String username, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page == null ? 0 : page, size == null ? 10 : size, Sort.by("savedAt").descending());
        return postSaveRepository.findSavedPostsByUsername(username, pageable);
    }

    @Transactional
    public Post save(Post post) {
        return postRepository.save(post);
    }
}
