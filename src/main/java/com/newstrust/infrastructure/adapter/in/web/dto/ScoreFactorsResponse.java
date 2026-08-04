package com.newstrust.infrastructure.adapter.in.web.dto;

import com.newstrust.domain.model.CredibilityFactors;
import com.newstrust.domain.service.CredibilityScoreCalculator;

public record ScoreFactorsResponse(
        double sourceReputation,
        double textualConsistency,
        double crossVerification,
        double disseminationPattern,
        boolean disseminationIsBaseline
) {

    public static ScoreFactorsResponse from(CredibilityFactors factors) {
        boolean isBaseline = factors.disseminationPattern().isEmpty();
        double dissemination = factors.disseminationPattern()
                .orElse(CredibilityScoreCalculator.NEUTRAL_DISSEMINATION_BASELINE);
        return new ScoreFactorsResponse(
                factors.sourceReputation(), factors.textualConsistency(), factors.crossVerification(),
                dissemination, isBaseline);
    }
}
