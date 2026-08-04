package com.newstrust.infrastructure.adapter.in.web.dto;

import com.newstrust.domain.model.NewsGraphNodeDetail;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record NewsGraphNodeDetailResponse(
        UUID id,
        String title,
        String content,
        CredibilityScoreResponse score,
        Instant analyzedAt,
        List<GraphEdgeResponse> connections
) {

    public static NewsGraphNodeDetailResponse from(NewsGraphNodeDetail detail) {
        return new NewsGraphNodeDetailResponse(
                detail.newsId(),
                detail.title(),
                detail.content(),
                CredibilityScoreResponse.from(detail.score()),
                detail.analyzedAt(),
                detail.connections().stream().map(GraphEdgeResponse::from).toList());
    }
}
