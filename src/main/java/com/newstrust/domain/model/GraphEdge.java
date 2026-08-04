package com.newstrust.domain.model;

import java.util.Objects;

/**
 * Uma aresta do grafo de credibilidade, ligando dois nos por similaridade
 * (de credibilidade entre fontes, ou de conteudo entre noticias).
 */
public record GraphEdge(String sourceId, String targetId, double weight) {

    public GraphEdge {
        Objects.requireNonNull(sourceId, "sourceId nao pode ser nulo");
        Objects.requireNonNull(targetId, "targetId nao pode ser nulo");
        if (weight < 0.0 || weight > 1.0) {
            throw new IllegalArgumentException("weight deve estar entre 0.0 e 1.0: " + weight);
        }
    }
}
