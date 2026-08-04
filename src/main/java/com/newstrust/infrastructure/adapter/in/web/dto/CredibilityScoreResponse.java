package com.newstrust.infrastructure.adapter.in.web.dto;

import com.newstrust.domain.model.CredibilityScore;

import java.util.List;

public record CredibilityScoreResponse(
        double value,
        String riskLevel,
        ScoreFactorsResponse factors,
        List<ScoreReasonResponse> reasons,
        List<String> aiExplanations
) {

    public static CredibilityScoreResponse from(CredibilityScore score, List<String> aiExplanations) {
        return new CredibilityScoreResponse(
                score.value(),
                score.riskLevel().name(),
                ScoreFactorsResponse.from(score.factors()),
                score.reasons().stream().map(ScoreReasonResponse::from).toList(),
                aiExplanations);
    }

    public static CredibilityScoreResponse from(CredibilityScore score) {
        return from(score, List.of());
    }
}
