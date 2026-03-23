package discussionforum.controllers;

import discussionforum.model.Post;
import discussionforum.model.VoteType;
import discussionforum.model.CommentRequestDTO;
import discussionforum.model.PostRequestDTO;
import discussionforum.service.CommentService;
import discussionforum.service.CommunityService;
import discussionforum.service.PostService;
import discussionforum.service.FileUploadService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Controller responsible for managing post-related operations. // Kontroler zodpovědný za správu operací souvisejících s příspěvky.
 * Handles post creation, viewing, commenting, voting, and saving functionality. // Zpracovává vytváření příspěvků, prohlížení, komentování, hlasování a ukládání.
 *
 * @author Petr Reitinger
 * @version 1.0
 * @since 2025
 */
@Controller
@RequestMapping("/posts")
public class PostController {

    private static final Logger log = LoggerFactory.getLogger(PostController.class);

    private final PostService postService;
    private final CommentService commentService;
    private final CommunityService communityService;
    private final FileUploadService fileUploadService;

    public PostController(PostService postService, CommentService commentService, CommunityService communityService, FileUploadService fileUploadService) {
        this.postService = postService;
        this.commentService = commentService;
        this.communityService = communityService;
        this.fileUploadService = fileUploadService;
    }

    @GetMapping("/new")
    public String newPost(@AuthenticationPrincipal UserDetails principal,
                         Model model) {
        model.addAttribute("post", new PostRequestDTO());
        model.addAttribute("communities", communityService.all());
        return "post/new";
    }

    @PostMapping
    public String create(@ModelAttribute("post") @Valid PostRequestDTO req,
                         BindingResult binding,
                         @RequestParam(value = "attachmentFiles", required = false) MultipartFile[] attachmentFiles,
                         @AuthenticationPrincipal UserDetails principal,
                         Model model) {
        log.debug("Post creation by user '{}', title: '{}'", principal.getUsername(), req.getTitle());

        if (binding.hasErrors()) {
            log.debug("Validation errors: {}", binding.getAllErrors());
            model.addAttribute("communities", communityService.all());
            return "post/new";
        }
        try {
            Post p = postService.create(req, principal.getUsername());
            log.debug("Post created with ID: {}", p.getId());

            if (attachmentFiles != null && attachmentFiles.length > 0) {
                Set<String> uploadedUrls = new HashSet<>();
                for (MultipartFile file : attachmentFiles) {
                    if (!file.isEmpty()) {
                        try {
                            String fileUrl = fileUploadService.uploadPostAttachment(file, p.getId(), principal.getUsername());
                            uploadedUrls.add(fileUrl);
                        } catch (Exception fileUploadException) {
                            log.error("Failed to upload file: {}", fileUploadException.getMessage());
                        }
                    }
                }

                if (!uploadedUrls.isEmpty()) {
                    p.setAttachmentUrls(uploadedUrls);
                    postService.save(p);
                }
            }

            return "redirect:/posts/" + p.getId();
        } catch (Exception e) {
            model.addAttribute("communities", communityService.all());
            model.addAttribute("error", "Failed to create post: " + e.getMessage());
            return "post/new";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                        @AuthenticationPrincipal UserDetails principal,
                        Model model) {
        Post post = postService.get(id);
        model.addAttribute("post", post);
        model.addAttribute("comments", commentService.forPost(id));
        model.addAttribute("totalCommentCount", commentService.getTotalCommentCount(id));
        model.addAttribute("comment", new CommentRequestDTO());
        model.addAttribute("communities", communityService.all());
        return "post/detail";
    }

    @PostMapping("/{id}/comment")
    public String comment(@PathVariable Long id,
                          @ModelAttribute("comment") @Valid CommentRequestDTO req,
                          BindingResult binding,
                          @AuthenticationPrincipal UserDetails principal) {
        if (binding.hasErrors()) {
            return "redirect:/posts/" + id + "?cerror";
        }
        try {
            req.setPostId(id);
            commentService.add(req, principal.getUsername());
            return "redirect:/posts/" + id;
        } catch (Exception e) {
            return "redirect:/posts/" + id + "?cerror";
        }
    }

    @PostMapping("/{id}/upvote")
    public String upvote(@PathVariable Long id,
                        @AuthenticationPrincipal UserDetails principal,
                        HttpServletRequest request) {
        try {
            postService.vote(id, principal.getUsername(), VoteType.UPVOTE);
        } catch (Exception e) {
            log.error("Upvote failed for post {}: {}", id, e.getMessage());
        }
        return "redirect:" + sanitizeRedirectUrl(request.getHeader("referer"));
    }

    @PostMapping("/{id}/downvote")
    public String downvote(@PathVariable Long id,
                          @AuthenticationPrincipal UserDetails principal,
                          HttpServletRequest request) {
        try {
            postService.vote(id, principal.getUsername(), VoteType.DOWNVOTE);
        } catch (Exception e) {
            log.error("Downvote failed for post {}: {}", id, e.getMessage());
        }
        return "redirect:" + sanitizeRedirectUrl(request.getHeader("referer"));
    }

    @PostMapping("/{id}/save")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> savePost(@PathVariable Long id,
                                                        @AuthenticationPrincipal UserDetails principal) {
        try {
            boolean success = postService.savePost(id, principal.getUsername());
            if (success) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Post saved successfully!"));
            } else {
                return ResponseEntity.ok(Map.of("success", false, "message", "Post is already saved"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Failed to save post: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/unsave")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> unsavePost(@PathVariable Long id,
                                                          @AuthenticationPrincipal UserDetails principal) {
        try {
            boolean success = postService.unsavePost(id, principal.getUsername());
            if (success) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Post unsaved successfully!"));
            } else {
                return ResponseEntity.ok(Map.of("success", false, "message", "Post was not saved"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Failed to unsave post: " + e.getMessage()));
        }
    }

    @PostMapping("/{postId}/comments/{commentId}/upvote")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> upvoteComment(@PathVariable Long postId,
                                                             @PathVariable Long commentId,
                                                             @AuthenticationPrincipal UserDetails principal) {
        try {
            int newScore = commentService.voteComment(commentId, principal.getUsername(), VoteType.UPVOTE);
            return ResponseEntity.ok(Map.of("success", true, "score", newScore));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Failed to upvote comment: " + e.getMessage()));
        }
    }

    @PostMapping("/{postId}/comments/{commentId}/downvote")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> downvoteComment(@PathVariable Long postId,
                                                               @PathVariable Long commentId,
                                                               @AuthenticationPrincipal UserDetails principal) {
        try {
            int newScore = commentService.voteComment(commentId, principal.getUsername(), VoteType.DOWNVOTE);
            return ResponseEntity.ok(Map.of("success", true, "score", newScore));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Failed to downvote comment: " + e.getMessage()));
        }
    }

    @PostMapping("/{postId}/comments/{parentId}/reply")
    public String replyToComment(@PathVariable Long postId,
                                @PathVariable Long parentId,
                                @ModelAttribute("comment") @Valid CommentRequestDTO req,
                                BindingResult binding,
                                @AuthenticationPrincipal UserDetails principal) {
        if (binding.hasErrors()) {
            return "redirect:/posts/" + postId + "?rerror";
        }
        try {
            req.setPostId(postId);
            commentService.addReply(req, principal.getUsername(), parentId);
            return "redirect:/posts/" + postId;
        } catch (Exception e) {
            return "redirect:/posts/" + postId + "?rerror";
        }
    }

    private String sanitizeRedirectUrl(String referer) {
        if (referer == null) {
            return "/";
        }
        try {
            URI uri = URI.create(referer);
            String path = uri.getPath();
            String query = uri.getQuery();
            if (path == null || path.isEmpty()) {
                return "/";
            }
            return query != null ? path + "?" + query : path;
        } catch (Exception e) {
            return "/";
        }
    }
}
