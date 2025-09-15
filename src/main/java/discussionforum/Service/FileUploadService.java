package discussionforum.Service;

import discussionforum.Config.FileUploadConfig;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class FileUploadService {

    private final FileUploadConfig fileUploadConfig;
    
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
        "jpg", "jpeg", "png", "gif", "webp", "bmp"
    );
    
    private static final Set<String> ALLOWED_DOCUMENT_EXTENSIONS = Set.of(
        "pdf", "doc", "docx", "txt", "md", "rtf"
    );
    
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final long MAX_AVATAR_SIZE = 2 * 1024 * 1024; // 2MB

    @Autowired
    public FileUploadService(FileUploadConfig fileUploadConfig) {
        this.fileUploadConfig = fileUploadConfig;
    }

    public String uploadAvatar(MultipartFile file, String username) throws IOException {
        validateAvatarFile(file);
        
        String originalFilename = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalFilename).toLowerCase();
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("avatar_%s_%s.%s", username, timestamp, extension);
        
        Path uploadPath = Paths.get(fileUploadConfig.getUploadDir(), "avatars");
        Files.createDirectories(uploadPath);
        
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        return "/avatars/" + fileName;
    }

    public String uploadPostAttachment(MultipartFile file, Long postId, String username) throws IOException {
        // Validate and process file upload // Validace a zpracování nahrávání souboru
        
        validateAttachmentFile(file);
        
        String originalFilename = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalFilename).toLowerCase();
        String baseName = FilenameUtils.getBaseName(originalFilename);
        
        // Sanitize filename
        String sanitizedBaseName = baseName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("%s_%s_%s_%s.%s", 
            sanitizedBaseName, postId, username, timestamp, extension);
        
        // Create upload directory and save file // Vytvoření adresáře pro nahrávání a uložení souboru
        Path uploadPath = Paths.get(fileUploadConfig.getUploadDir(), "attachments");
        Files.createDirectories(uploadPath);
        
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return "/attachments/" + fileName;
    }

    public List<String> uploadMultipleFiles(MultipartFile[] files, Long postId, String username) throws IOException {
        if (files == null || files.length == 0) {
            return Collections.emptyList();
        }
        
        List<String> uploadedFiles = new ArrayList<>();
        
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String filePath = uploadPostAttachment(file, postId, username);
                uploadedFiles.add(filePath);
            }
        }
        
        return uploadedFiles;
    }

    public boolean deleteFile(String filePath) {
        try {
            Path fullPath = Paths.get(fileUploadConfig.getUploadDir(), filePath.substring(1));
            return Files.deleteIfExists(fullPath);
        } catch (IOException e) {
            return false;
        }
    }

    public String getFileUrl(String relativePath) {
        return relativePath; // Spring will handle the mapping via ResourceHandler
    }

    public FileInfo getFileInfo(String filePath) {
        try {
            Path fullPath = Paths.get(fileUploadConfig.getUploadDir(), filePath.substring(1));
            if (Files.exists(fullPath)) {
                String fileName = fullPath.getFileName().toString();
                long size = Files.size(fullPath);
                String extension = FilenameUtils.getExtension(fileName).toLowerCase();
                String mimeType = Files.probeContentType(fullPath);
                
                return new FileInfo(fileName, size, extension, mimeType, filePath);
            }
        } catch (IOException e) {
            // File doesn't exist or error reading
        }
        return null;
    }

    private void validateAvatarFile(MultipartFile file) throws IllegalArgumentException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Avatar file cannot be empty");
        }
        
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new IllegalArgumentException("Avatar file size cannot exceed 2MB");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("Invalid filename");
        }
        
        String extension = FilenameUtils.getExtension(originalFilename).toLowerCase();
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Only image files are allowed for avatars: " + 
                String.join(", ", ALLOWED_IMAGE_EXTENSIONS));
        }
    }

    private void validateAttachmentFile(MultipartFile file) throws IllegalArgumentException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Attachment file cannot be empty");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size cannot exceed 10MB");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("Invalid filename");
        }
        
        String extension = FilenameUtils.getExtension(originalFilename).toLowerCase();
        Set<String> allAllowedExtensions = new HashSet<>();
        allAllowedExtensions.addAll(ALLOWED_IMAGE_EXTENSIONS);
        allAllowedExtensions.addAll(ALLOWED_DOCUMENT_EXTENSIONS);
        
        if (!allAllowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("File type not allowed. Supported types: " + 
                String.join(", ", allAllowedExtensions));
        }
    }

    public static class FileInfo {
        private final String fileName;
        private final long size;
        private final String extension;
        private final String mimeType;
        private final String url;

        public FileInfo(String fileName, long size, String extension, String mimeType, String url) {
            this.fileName = fileName;
            this.size = size;
            this.extension = extension;
            this.mimeType = mimeType;
            this.url = url;
        }

        public String getFileName() { return fileName; }
        public long getSize() { return size; }
        public String getExtension() { return extension; }
        public String getMimeType() { return mimeType; }
        public String getUrl() { return url; }

        public String getFormattedSize() {
            if (size < 1024) return size + " B";
            if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        }

        public boolean isImage() {
            return ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase());
        }
    }
}