package com.newstrust.domain.model;

import java.util.List;
import java.util.Objects;

/**
 * Grafo completo (rede de fontes ou rede de noticias): lista de nos + lista de arestas.
 */
public record GraphData(List<GraphNode> nodes, List<GraphEdge> edges) {

    public GraphData {
        Objects.requireNonNull(nodes, "nodes nao pode ser nulo");
        Objects.requireNonNull(edges, "edges nao pode ser nulo");
        nodes = List.copyOf(nodes);
        edges = List.copyOf(edges);
    }
}
