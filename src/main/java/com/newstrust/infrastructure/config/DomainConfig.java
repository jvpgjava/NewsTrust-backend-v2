package com.newstrust.infrastructure.config;

import com.newstrust.domain.service.CredibilityScoreCalculator;
import com.newstrust.domain.service.CrossVerificationScorer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Expõe os serviços de domínio (Java puro, sem dependência de Spring) como beans,
 * para que a camada de aplicação possa recebê-los via injeção de dependência sem
 * que o domínio em si precise de nenhuma anotação do framework.
 */
@Configuration
public class DomainConfig {

    @Bean
    public CredibilityScoreCalculator credibilityScoreCalculator() {
        return new CredibilityScoreCalculator();
    }

    @Bean
    public CrossVerificationScorer crossVerificationScorer() {
        return new CrossVerificationScorer();
    }
}
