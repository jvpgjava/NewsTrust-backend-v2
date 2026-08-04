package com.newstrust.infrastructure.adapter.out.persistence;

import com.newstrust.domain.model.ScoreFactor;
import com.newstrust.domain.model.ScoreReason;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * Espelha {@link ScoreReason} para persistencia (a entidade JPA e infraestrutura;
 * o domain.model.ScoreReason em si permanece livre de anotacoes de framework).
 */
@Embeddable
public class ScoreReasonEmbeddable {

    @Enumerated(EnumType.STRING)
    @Column(name = "factor", nullable = false, length = 40)
    private ScoreFactor factor;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    protected ScoreReasonEmbeddable() {
    }

    public ScoreReasonEmbeddable(ScoreFactor factor, String description) {
        this.factor = factor;
        this.description = description;
    }

    public static ScoreReasonEmbeddable fromDomain(ScoreReason reason) {
        return new ScoreReasonEmbeddable(reason.factor(), reason.description());
    }

    public ScoreReason toDomain() {
        return new ScoreReason(factor, description);
    }
}
