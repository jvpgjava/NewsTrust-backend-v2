package com.newstrust.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Resultado persistido da analise de uma fonte (URL/dominio), independente de
 * um conteudo de noticia especifico.
 */
public record SourceAnalysis(
        UUID id,
        String domain,
        String url,
        SourceReputation reputation,
        CredibilityScore score,
        Instant createdAt
) {

    public SourceAnalysis {
        Objects.requireNonNull(domain, "domain nao pode ser nulo");
        Objects.requireNonNull(url, "url nao pode ser nulo");
        Objects.requireNonNull(reputation, "reputation nao pode ser nulo");
        Objects.requireNonNull(score, "score nao pode ser nulo");
        Objects.requireNonNull(createdAt, "createdAt nao pode ser nulo");
    }
}
