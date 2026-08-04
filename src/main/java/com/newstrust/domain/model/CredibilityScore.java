package com.newstrust.domain.model;

import java.util.List;
import java.util.Objects;

/**
 * Resultado final e auditavel do calculo de credibilidade: o valor numerico (0-100),
 * a faixa de risco derivada, os fatores e pesos usados, e as razoes que o justificam.
 */
public record CredibilityScore(
        double value,
        RiskLevel riskLevel,
        CredibilityFactors factors,
        ScoreWeights weights,
        List<ScoreReason> reasons
) {

    public CredibilityScore {
        if (value < 0.0 || value > 100.0) {
            throw new IllegalArgumentException("value deve estar entre 0 e 100, valor informado: " + value);
        }
        Objects.requireNonNull(riskLevel, "riskLevel nao pode ser nulo");
        Objects.requireNonNull(factors, "factors nao pode ser nulo");
        Objects.requireNonNull(weights, "weights nao pode ser nulo");
        Objects.requireNonNull(reasons, "reasons nao pode ser nulo");
        reasons = List.copyOf(reasons);
    }
}
