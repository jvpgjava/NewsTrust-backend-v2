package com.newstrust.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Detalhe de um no especifico da rede de noticias: a analise completa da noticia
 * mais as arestas que a conectam a outras noticias similares.
 */
public record NewsGraphNodeDetail(UUID newsId, String title, String content, CredibilityScore score,
                                   Instant analyzedAt, List<GraphEdge> connections) {

    public NewsGraphNodeDetail {
        Objects.requireNonNull(newsId, "newsId nao pode ser nulo");
        Objects.requireNonNull(title, "title nao pode ser nulo");
        Objects.requireNonNull(content, "content nao pode ser nulo");
        Objects.requireNonNull(score, "score nao pode ser nulo");
        Objects.requireNonNull(analyzedAt, "analyzedAt nao pode ser nulo");
        connections = List.copyOf(connections);
    }
}
