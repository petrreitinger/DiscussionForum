package discussionforum.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.MultipartConfigElement;
import java.io.File;

@Configuration
public class FileUploadConfig { // Removed WebMvcConfigurer to fix redirect loop

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @Value("${file.upload.max-file-size:10MB}")
    private String maxFileSize;

    @Value("${file.upload.max-request-size:50MB}")
    private String maxRequestSize;

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.parse(maxFileSize));
        factory.setMaxRequestSize(DataSize.parse(maxRequestSize));
        return factory.createMultipartConfig();
    }

    // Resource handlers removed to fix redirect loop issue
    // Files will be served through controller endpoints instead

    public String getUploadDir() {
        return uploadDir;
    }
}