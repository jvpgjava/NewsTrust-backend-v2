package com.newstrust.infrastructure.config;

import com.newstrust.domain.model.ScoreWeights;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ScoringProperties.class)
public class ScoringConfig {

    /**
     * Resolve o perfil de pesos ativo (newstrust.scoring.active-profile) a partir
     * dos perfis configurados. Cai para {@link ScoreWeights#DEFAULT} se o perfil
     * ativo nao estiver definido em application.properties.
     */
    @Bean
    public ScoreWeights activeScoreWeights(ScoringProperties properties) {
        ScoringProperties.WeightsProfile profile = properties.getProfiles().get(properties.getActiveProfile());
        if (profile == null) {
            return ScoreWeights.DEFAULT;
        }
        return new ScoreWeights(profile.getReputationWeight(), profile.getTextualConsistencyWeight(),
                profile.getCrossVerificationWeight(), profile.getDisseminationWeight());
    }
}
