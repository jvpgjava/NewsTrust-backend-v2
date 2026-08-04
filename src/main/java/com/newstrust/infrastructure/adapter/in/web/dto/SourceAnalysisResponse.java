package com.newstrust.infrastructure.adapter.in.web.dto;

import com.newstrust.domain.model.SourceAnalysis;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SourceAnalysisResponse(
        UUID id,
        String domain,
        String url,
        double reputationScore,
        String reputationCategory,
        List<String> reputationSignals,
        CredibilityScoreResponse score,
        Instant analyzedAt
) {

    public static SourceAnalysisResponse from(SourceAnalysis analysis) {
        return new SourceAnalysisResponse(
                analysis.id(),
                analysis.domain(),
                analysis.url(),
                analysis.reputation().reputationScore(),
                analysis.reputation().category(),
                analysis.reputation().signals(),
                CredibilityScoreResponse.from(analysis.score()),
                analysis.createdAt());
    }
}
