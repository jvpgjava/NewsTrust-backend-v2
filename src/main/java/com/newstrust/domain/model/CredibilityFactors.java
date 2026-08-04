package com.newstrust.domain.model;

import java.util.OptionalDouble;

/**
 * Os quatro fatores de entrada do modelo de pontuacao, cada um em escala 0-100:
 * R (reputacao da fonte), T (consistencia textual), V (verificacao cruzada)
 * e D (padrao de disseminacao).
 * <p>
 * D e opcional: quando o padrao de disseminacao nao esta disponivel (ex: noticia
 * recem-processada, sem historico de propagacao), o calculo assume um baseline
 * neutro em vez de distorcer o score - ver {@link com.newstrust.domain.service.CredibilityScoreCalculator}.
 */
public record CredibilityFactors(
        double sourceReputation,
        double textualConsistency,
        double crossVerification,
        OptionalDouble disseminationPattern
) {

    public CredibilityFactors {
        validateRange("sourceReputation", sourceReputation);
        validateRange("textualConsistency", textualConsistency);
        validateRange("crossVerification", crossVerification);
        if (disseminationPattern == null) {
            throw new IllegalArgumentException("disseminationPattern nao pode ser nulo; use OptionalDouble.empty()");
        }
        disseminationPattern.ifPresent(d -> validateRange("disseminationPattern", d));
    }

    public static CredibilityFactors of(double sourceReputation, double textualConsistency,
                                         double crossVerification, double disseminationPattern) {
        return new CredibilityFactors(sourceReputation, textualConsistency, crossVerification,
                OptionalDouble.of(disseminationPattern));
    }

    public static CredibilityFactors withoutDisseminationData(double sourceReputation, double textualConsistency,
                                                               double crossVerification) {
        return new CredibilityFactors(sourceReputation, textualConsistency, crossVerification, OptionalDouble.empty());
    }

    private static void validateRange(String fieldName, double value) {
        if (value < 0.0 || value > 100.0) {
            throw new IllegalArgumentException(fieldName + " deve estar entre 0 e 100, valor informado: " + value);
        }
    }
}
