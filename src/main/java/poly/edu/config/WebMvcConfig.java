package poly.edu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	@Value("${file.upload-dir:uploads}")
	private String uploadDir;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/images/**")
				.addResourceLocations("classpath:/static/images/");

		// Add mapping for uploaded files (avatars, etc.)
		String absolutePath = Paths.get(uploadDir).toAbsolutePath().toString();
		if (!absolutePath.endsWith("/") && !absolutePath.endsWith("\\")) {
			absolutePath += "/";
		}
		registry.addResourceHandler("/uploads/**")
				.addResourceLocations("file:" + absolutePath)
				.addResourceLocations("file:uploads/")
				.addResourceLocations("file:bin/uploads/");
	}
}