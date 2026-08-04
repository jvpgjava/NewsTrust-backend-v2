package com.newstrust.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Liga os perfis de pesos do score (newstrust.scoring.*) definidos em
 * application.properties. Permite trocar o perfil ativo por ambiente/dominio de
 * aplicacao (ex: eleitoral, saude publica) sem recompilar o codigo.
 */
@ConfigurationProperties(prefix = "newstrust.scoring")
public class ScoringProperties {

    private String activeProfile = "default";
    private Map<String, WeightsProfile> profiles = new LinkedHashMap<>();

    public String getActiveProfile() {
        return activeProfile;
    }

    public void setActiveProfile(String activeProfile) {
        this.activeProfile = activeProfile;
    }

    public Map<String, WeightsProfile> getProfiles() {
        return profiles;
    }

    public void setProfiles(Map<String, WeightsProfile> profiles) {
        this.profiles = profiles;
    }

    public static class WeightsProfile {
        private double reputationWeight;
        private double textualConsistencyWeight;
        private double crossVerificationWeight;
        private double disseminationWeight;

        public double getReputationWeight() {
            return reputationWeight;
        }

        public void setReputationWeight(double reputationWeight) {
            this.reputationWeight = reputationWeight;
        }

        public double getTextualConsistencyWeight() {
            return textualConsistencyWeight;
        }

        public void setTextualConsistencyWeight(double textualConsistencyWeight) {
            this.textualConsistencyWeight = textualConsistencyWeight;
        }

        public double getCrossVerificationWeight() {
            return crossVerificationWeight;
        }

        public void setCrossVerificationWeight(double crossVerificationWeight) {
            this.crossVerificationWeight = crossVerificationWeight;
        }

        public double getDisseminationWeight() {
            return disseminationWeight;
        }

        public void setDisseminationWeight(double disseminationWeight) {
            this.disseminationWeight = disseminationWeight;
        }
    }
}
