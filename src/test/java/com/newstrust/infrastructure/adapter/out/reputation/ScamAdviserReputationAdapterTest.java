package com.newstrust.infrastructure.adapter.out.reputation;

import com.newstrust.domain.model.SourceReputation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.OptionalDouble;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testa apenas {@link ScamAdviserReputationAdapter#blendWithCuratedList}, a
 * logica de decisao pura extraida do adapter - sem precisar mockar a cadeia
 * fluente do RestClient para exercitar as regras de negocio.
 */
class ScamAdviserReputationAdapterTest {

    @Test
    void keepsScamAdviserResultWhenDomainIsNotInTheCuratedList() {
        SourceReputation scamAdviserResult = new SourceReputation("example.com", 60, "moderada", List.of());

        SourceReputation result =
                ScamAdviserReputationAdapter.blendWithCuratedList(scamAdviserResult, OptionalDouble.empty());

        assertThat(result).isEqualTo(scamAdviserResult);
    }

    @Test
    void keepsScamAdviserResultWhenItAlreadyScoresHigherThanTheCuratedList() {
        SourceReputation scamAdviserResult = new SourceReputation("g1.globo.com", 95, "confiavel", List.of());

        SourceReputation result = ScamAdviserReputationAdapter.blendWithCuratedList(
                scamAdviserResult, OptionalDouble.of(CuratedBrazilianOutlets.CURATED_REPUTATION_SCORE));

        assertThat(result).isEqualTo(scamAdviserResult);
    }

    @Test
    void raisesReputationToTheCuratedScoreWhenItIsHigherThanScamAdviser() {
        SourceReputation scamAdviserResult = SourceReputation.neutral("g1.globo.com");

        SourceReputation result = ScamAdviserReputationAdapter.blendWithCuratedList(
                scamAdviserResult, OptionalDouble.of(CuratedBrazilianOutlets.CURATED_REPUTATION_SCORE));

        assertThat(result.reputationScore()).isEqualTo(CuratedBrazilianOutlets.CURATED_REPUTATION_SCORE);
        assertThat(result.category()).isEqualTo("confiavel");
        assertThat(result.signals()).anyMatch(signal -> signal.contains("lista curada"));
    }

    @Test
    void preservesScamAdviserSignalsWhenBlendingWithTheCuratedList() {
        SourceReputation scamAdviserResult =
                new SourceReputation("g1.globo.com", 50, "moderada", List.of("dominio recente"));

        SourceReputation result = ScamAdviserReputationAdapter.blendWithCuratedList(
                scamAdviserResult, OptionalDouble.of(CuratedBrazilianOutlets.CURATED_REPUTATION_SCORE));

        assertThat(result.signals()).contains("dominio recente");
        assertThat(result.signals()).hasSize(2);
    }
}
