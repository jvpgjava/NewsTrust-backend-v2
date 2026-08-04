package com.newstrust.infrastructure.adapter.in.web.dto;

import com.newstrust.domain.model.GraphData;

import java.util.List;

public record GraphResponse(List<GraphNodeResponse> nodes, List<GraphEdgeResponse> edges) {

    public static GraphResponse from(GraphData data) {
        return new GraphResponse(
                data.nodes().stream().map(GraphNodeResponse::from).toList(),
                data.edges().stream().map(GraphEdgeResponse::from).toList());
    }
}
