package discussionforum.Service;

import discussionforum.Model.*;
import discussionforum.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import java.util.*;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentVoteRepository commentVoteRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository, 
                         UserRepository userRepository, CommentVoteRepository commentVoteRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentVoteRepository = commentVoteRepository;
    }

    @Transactional
    public Comment add(CommentRequestDTO req, String username) {
        
        Post post = postRepository.findById(req.getPostId())
                .orElseThrow(() -> new RuntimeException(
                        "Post not found with id: " + req.getPostId()
                ));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with username: " + username
                ));
        Comment c = new Comment();
        c.setPost(post);
        c.setAuthor(user);
        c.setContent(req.getContent());
        
        Comment savedComment = commentRepository.save(c);
        
        return savedComment;
    }
    
    @Transactional
    public Comment addReply(CommentRequestDTO req, String username, Long parentId) {
        
        Post post = postRepository.findById(req.getPostId())
                .orElseThrow(() -> new RuntimeException(
                        "Post not found with id: " + req.getPostId()
                ));
        
        Comment parent = commentRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException(
                        "Parent comment not found with id: " + parentId
                ));
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with username: " + username
                ));
        
        Comment reply = new Comment();
        reply.setPost(post);
        reply.setAuthor(user);
        reply.setContent(req.getContent());
        reply.setParent(parent);
        
        Comment savedReply = commentRepository.save(reply);
        
        return savedReply;
    }

    public List<Comment> forPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No post found with id: " + postId
                ));


        List<Comment> allComments = commentRepository.findByPostOrderByCreatedAtAsc(post);
        

        List<Comment> topLevelComments = new ArrayList<>();
        Map<Long, Comment> commentMap = new HashMap<>();
        

        for (Comment comment : allComments) {
            commentMap.put(comment.getId(), comment);
            if (comment.getParent() == null) {
                topLevelComments.add(comment);
            }
        }
        

        for (Comment comment : allComments) {
            if (comment.getParent() != null) {
                Comment parent = commentMap.get(comment.getParent().getId());
                if (parent != null) {

                    if (parent.getReplies() == null) {
                        parent.setReplies(new HashSet<>());
                    }
                    parent.getReplies().add(comment);
                }
            }
        }
        
        return topLevelComments;
    }
    
    public int getTotalCommentCount(Long postId) {
        List<Comment> topLevelComments = forPost(postId);
        return countCommentsRecursively(topLevelComments);
    }
    
    private int countCommentsRecursively(List<Comment> comments) {
        int count = 0;
        for (Comment comment : comments) {
            count += 1;
            if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
                count += countCommentsRecursively(comment.getReplies().stream().toList());
            }
        }
        return count;
    }
    
    @Transactional
    public int voteComment(Long commentId, String username, VoteType type) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id " + commentId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));

        CommentVote vote = commentVoteRepository.findByCommentAndUser(comment, user).orElse(null);
        if (vote == null) {
            vote = new CommentVote();
            vote.setComment(comment);
            vote.setUser(user);
            vote.setType(type);
            commentVoteRepository.save(vote);
            comment.setScore(comment.getScore() + (type == VoteType.UPVOTE ? 1 : -1));
        } else if (vote.getType() == type) {
            commentVoteRepository.delete(vote);
            comment.setScore(comment.getScore() + (type == VoteType.UPVOTE ? -1 : 1)); // undo
        } else {
            vote.setType(type);
            commentVoteRepository.save(vote);
            comment.setScore(comment.getScore() + (type == VoteType.UPVOTE ? 2 : -2)); // switch
        }
        commentRepository.save(comment);
        return comment.getScore();
    }
}
