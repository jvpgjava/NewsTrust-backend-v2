package com.newstrust.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Resultado persistido da analise de uma noticia: o conteudo original, seu embedding
 * (para futuras buscas RAG), o score deterministico e auditavel, e explicacoes
 * textuais adicionais geradas por IA a partir do contexto recuperado (RAG) -
 * essas explicacoes complementam, mas nunca substituem, as {@link ScoreReason} do score.
 */
public record NewsAnalysis(
        UUID id,
        String title,
        String content,
        String sourceUrl,
        Embedding embedding,
        CredibilityScore score,
        List<String> aiGeneratedExplanations,
        Instant createdAt
) {

    public NewsAnalysis {
        Objects.requireNonNull(title, "title nao pode ser nulo");
        Objects.requireNonNull(content, "content nao pode ser nulo");
        Objects.requireNonNull(embedding, "embedding nao pode ser nulo");
        Objects.requireNonNull(score, "score nao pode ser nulo");
        Objects.requireNonNull(createdAt, "createdAt nao pode ser nulo");
        aiGeneratedExplanations = List.copyOf(aiGeneratedExplanations);
    }
}
