package com.newstrust.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Uma noticia previamente processada e recuperada por similaridade de cosseno
 * (busca RAG via pgvector/HNSW) em relacao a uma nova submissao. Usada para
 * enriquecer o fator V (verificacao cruzada) via {@link com.newstrust.domain.service.CrossVerificationScorer}.
 */
public record SimilarNewsMatch(UUID newsId, String title, double cosineSimilarity, RiskLevel riskLevel) {

    public SimilarNewsMatch {
        Objects.requireNonNull(newsId, "newsId nao pode ser nulo");
        Objects.requireNonNull(title, "title nao pode ser nulo");
        Objects.requireNonNull(riskLevel, "riskLevel nao pode ser nulo");
        if (cosineSimilarity < 0.0 || cosineSimilarity > 1.0) {
            throw new IllegalArgumentException("cosineSimilarity deve estar entre 0.0 e 1.0: " + cosineSimilarity);
        }
    }
}
