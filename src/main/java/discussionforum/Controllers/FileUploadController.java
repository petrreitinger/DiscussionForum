package discussionforum.Controllers;

import discussionforum.Model.User;
import discussionforum.Repository.UserRepository;
import discussionforum.Service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/upload")
public class FileUploadController {

    private final FileUploadService fileUploadService;
    private final UserRepository userRepository;

    @Autowired
    public FileUploadController(FileUploadService fileUploadService, UserRepository userRepository) {
        this.fileUploadService = fileUploadService;
        this.userRepository = userRepository;
    }

    @PostMapping("/avatar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadAvatar(
            @RequestParam("avatar") MultipartFile file,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = authentication.getName();
            
            String avatarUrl = fileUploadService.uploadAvatar(file, username);
            
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // Delete old avatar if exists
                if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                    fileUploadService.deleteFile(user.getAvatarUrl());
                }
                
                user.setAvatarUrl(avatarUrl);
                userRepository.save(user);
                
                response.put("success", true);
                response.put("message", "Avatar uploaded successfully");
                response.put("avatarUrl", avatarUrl);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Failed to upload avatar: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/attachment")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadAttachment(
            @RequestParam("file") MultipartFile file,
            @RequestParam("postId") Long postId,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = authentication.getName();
            String attachmentUrl = fileUploadService.uploadPostAttachment(file, postId, username);
            
            response.put("success", true);
            response.put("message", "File uploaded successfully");
            response.put("fileUrl", attachmentUrl);
            response.put("fileName", file.getOriginalFilename());
            response.put("fileSize", file.getSize());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/attachments")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadMultipleAttachments(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("postId") Long postId,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = authentication.getName();
            List<String> uploadedFiles = fileUploadService.uploadMultipleFiles(files, postId, username);
            
            response.put("success", true);
            response.put("message", "Files uploaded successfully");
            response.put("fileUrls", uploadedFiles);
            response.put("fileCount", uploadedFiles.size());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Failed to upload files: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/file")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteFile(
            @RequestParam("filePath") String filePath,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Additional security check could be added here to verify user owns the file
            boolean deleted = fileUploadService.deleteFile(filePath);
            
            if (deleted) {
                response.put("success", true);
                response.put("message", "File deleted successfully");
            } else {
                response.put("success", false);
                response.put("message", "File not found or could not be deleted");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/info")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getFileInfo(@RequestParam("filePath") String filePath) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            FileUploadService.FileInfo fileInfo = fileUploadService.getFileInfo(filePath);
            
            if (fileInfo != null) {
                response.put("success", true);
                response.put("fileName", fileInfo.getFileName());
                response.put("fileSize", fileInfo.getFormattedSize());
                response.put("fileType", fileInfo.getExtension());
                response.put("mimeType", fileInfo.getMimeType());
                response.put("isImage", fileInfo.isImage());
                response.put("url", fileInfo.getUrl());
            } else {
                response.put("success", false);
                response.put("message", "File not found");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error retrieving file info: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}