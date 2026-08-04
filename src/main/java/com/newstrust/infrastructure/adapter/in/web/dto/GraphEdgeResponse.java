package com.newstrust.infrastructure.adapter.in.web.dto;

import com.newstrust.domain.model.GraphEdge;

/**
 * Campos "source"/"target" seguem a convencao de links do D3.js (d3-force),
 * para que o frontend possa consumir a resposta diretamente.
 */
public record GraphEdgeResponse(String source, String target, double weight) {

    public static GraphEdgeResponse from(GraphEdge edge) {
        return new GraphEdgeResponse(edge.sourceId(), edge.targetId(), edge.weight());
    }
}
