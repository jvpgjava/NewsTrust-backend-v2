package com.newstrust.infrastructure.adapter.in.web.dto;

import com.newstrust.domain.model.GraphNode;

import java.util.Map;

public record GraphNodeResponse(String id, String label, double credibilityScore, String riskLevel,
                                 Map<String, Object> metadata) {

    public static GraphNodeResponse from(GraphNode node) {
        return new GraphNodeResponse(node.id(), node.label(), node.credibilityScore(),
                node.riskLevel().name(), node.metadata());
    }
}
