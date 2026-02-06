package com.lunarlaurus.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * LunarLaurus Local Compute Plugin - MCP Server
 *
 * Main Spring Boot application that exposes local compute resources via MCP protocol.
 * Provides intelligent routing between GPU, CPU, and cloud resources.
 *
 * @author LunarLaurus
 * @organization Laurus Industries
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class MCPServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MCPServerApplication.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins("*")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*");
            }
        };
    }
}
