package com.newstrust.infrastructure.adapter.in.web.sse;

import com.newstrust.domain.model.CredibilityFactors;
import com.newstrust.domain.model.CredibilityScore;
import com.newstrust.domain.model.Embedding;
import com.newstrust.domain.model.NewsAnalysis;
import com.newstrust.domain.model.RiskLevel;
import com.newstrust.domain.model.ScoreWeights;
import com.newstrust.domain.model.SourceAnalysis;
import com.newstrust.domain.model.SourceReputation;
import com.newstrust.infrastructure.event.NewsAnalysisCreatedEvent;
import com.newstrust.infrastructure.event.SourceAnalysisCreatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.List;
import java.util.OptionalDouble;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Cobertura basica isolada do broadcaster. O comportamento de limpeza de
 * assinantes na desconexao (que depende do ciclo de vida assincrono real do
 * Spring MVC) e coberto por {@link AnalysisEventsControllerTest}, que exercita
 * o dispatch assincrono de verdade via MockMvc.
 */
class AnalysisEventBroadcasterTest {

    private final AnalysisEventBroadcaster broadcaster = new AnalysisEventBroadcaster();

    @Test
    void subscribeRegistersAnEmitter() {
        SseEmitter emitter = broadcaster.subscribe();

        assertThat(emitter).isNotNull();
        assertThat(broadcaster.subscriberCount()).isEqualTo(1);
    }

    @Test
    void multipleSubscribersAreTrackedIndependently() {
        broadcaster.subscribe();
        broadcaster.subscribe();

        assertThat(broadcaster.subscriberCount()).isEqualTo(2);
    }

    @Test
    void broadcastingWithNoSubscribersDoesNothing() {
        assertThatCode(() -> broadcaster.onNewsAnalysisCreated(new NewsAnalysisCreatedEvent(someNewsAnalysis())))
                .doesNotThrowAnyException();
        assertThatCode(() -> broadcaster.onSourceAnalysisCreated(new SourceAnalysisCreatedEvent(someSourceAnalysis())))
                .doesNotThrowAnyException();
        assertThat(broadcaster.subscriberCount()).isZero();
    }

    static NewsAnalysis someNewsAnalysis() {
        CredibilityFactors factors = CredibilityFactors.of(80, 70, 60, 50);
        CredibilityScore score = new CredibilityScore(75.0, RiskLevel.LOW_RISK, factors, ScoreWeights.DEFAULT, List.of());
        return new NewsAnalysis(UUID.randomUUID(), "Título", "Conteúdo", null,
                new Embedding(new float[]{0.1f}), score, List.of(), Instant.now());
    }

    static SourceAnalysis someSourceAnalysis() {
        SourceReputation reputation = new SourceReputation("example.com", 80, "confiável", List.of());
        CredibilityFactors factors = new CredibilityFactors(80, 0, 0, OptionalDouble.empty());
        CredibilityScore score = new CredibilityScore(80.0, RiskLevel.LOW_RISK, factors, ScoreWeights.SOURCE_ONLY, List.of());
        return new SourceAnalysis(UUID.randomUUID(), "example.com", "https://example.com", reputation, score, Instant.now());
    }
}
