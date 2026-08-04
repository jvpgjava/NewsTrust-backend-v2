package com.newstrust.domain.model;

import java.util.Objects;

/**
 * Uma razao legivel por humanos que justifica parte do score final.
 * O score nunca deve ser uma caixa-preta: cada fator contribui com uma razao auditavel.
 */
public record ScoreReason(ScoreFactor factor, String description) {

    public ScoreReason {
        Objects.requireNonNull(factor, "factor nao pode ser nulo");
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("description nao pode ser vazia");
        }
    }
}
