package com.newstrust.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS so existe para permitir o frontend rodando isolado (ex: `ng serve` em
 * localhost:4200 contra o backend em localhost:8080 - origens diferentes).
 * Em producao e no ambiente dev/homolog da VPS, frontend e backend ficam atras
 * do mesmo dominio via proxy do Nginx (mesma origem), entao nenhuma origem e
 * configurada la e este bean simplesmente nao registra nada.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    public WebConfig(@Value("${newstrust.cors.allowed-origins:}") String allowedOrigins) {
        this.allowedOrigins = allowedOrigins.isBlank() ? new String[0] : allowedOrigins.split(",");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (allowedOrigins.length == 0) {
            return;
        }
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
