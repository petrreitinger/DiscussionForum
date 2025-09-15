package discussionforum.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded files using absolute path
        java.io.File uploadDirectory = new java.io.File(uploadDir).getAbsoluteFile();
        String uploadPath = uploadDirectory.toURI().toString();
        
        registry.addResourceHandler("/attachments/**")
                .addResourceLocations(uploadPath + "attachments/")
                .setCachePeriod(3600);
                
        registry.addResourceHandler("/avatars/**")
                .addResourceLocations(uploadPath + "avatars/")
                .setCachePeriod(3600);
                
        // Static resource mapping configured successfully // Mapování statických zdrojů úspěšně nakonfigurováno
    }
}