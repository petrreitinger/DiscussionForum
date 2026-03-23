package discussionforum.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        java.io.File uploadDirectory = new java.io.File(uploadDir).getAbsoluteFile();
        String uploadPath = uploadDirectory.toURI().toString();

        registry.addResourceHandler("/attachments/**")
                .addResourceLocations(uploadPath + "attachments/")
                .setCachePeriod(3600);

        registry.addResourceHandler("/avatars/**")
                .addResourceLocations(uploadPath + "avatars/")
                .setCachePeriod(3600);

        log.info("Configured file serving from: {}", uploadPath);
    }
}
