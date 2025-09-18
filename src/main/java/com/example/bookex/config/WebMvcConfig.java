package com.example.bookex.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AppProperties appProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadDir = appProperties.getUploadDir();
        Path uploadPath = StringUtils.hasText(uploadDir)
                ? Paths.get(uploadDir).toAbsolutePath().normalize()
                : Paths.get("uploads").toAbsolutePath().normalize();

        String location = uploadPath.toUri().toString();
        if (!location.endsWith("/")) location = location + "/";

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location)
                .setCachePeriod(3600);

        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springdoc-ui/")
                .resourceChain(false);
    }
}
