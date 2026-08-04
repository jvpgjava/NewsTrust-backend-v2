package com.newstrust.infrastructure.adapter.in.web.dto;

import com.newstrust.domain.model.NewsAnalysis;

import java.time.Instant;
import java.util.UUID;

public record NewsContentAnalysisResponse(
        UUID id,
        String title,
        String sourceUrl,
        CredibilityScoreResponse score,
        Instant analyzedAt
) {

    public static NewsContentAnalysisResponse from(NewsAnalysis analysis) {
        return new NewsContentAnalysisResponse(
                analysis.id(),
                analysis.title(),
                analysis.sourceUrl(),
                CredibilityScoreResponse.from(analysis.score(), analysis.aiGeneratedExplanations()),
                analysis.createdAt());
    }
}
