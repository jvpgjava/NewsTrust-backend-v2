package com.newstrust.application.service;

import com.newstrust.domain.model.CredibilityFactors;
import com.newstrust.domain.model.Embedding;
import com.newstrust.domain.model.NewsAnalysis;
import com.newstrust.domain.model.NewsContentSubmission;
import com.newstrust.domain.model.RiskLevel;
import com.newstrust.domain.model.ScoreWeights;
import com.newstrust.domain.model.SimilarNewsMatch;
import com.newstrust.domain.model.SourceReputation;
import com.newstrust.domain.port.out.DisseminationPatternPort;
import com.newstrust.domain.port.out.EmbeddingGeneratorPort;
import com.newstrust.domain.port.out.LlmExplanationPort;
import com.newstrust.domain.port.out.NewsAnalysisRepositoryPort;
import com.newstrust.domain.port.out.SimilaritySearchPort;
import com.newstrust.domain.port.out.SourceReputationPort;
import com.newstrust.domain.port.out.TextConsistencyAnalyzerPort;
import com.newstrust.domain.service.CredibilityScoreCalculator;
import com.newstrust.domain.service.CrossVerificationScorer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.OptionalDouble;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyzeNewsContentServiceTest {

    @Mock
    private EmbeddingGeneratorPort embeddingGeneratorPort;
    @Mock
    private SimilaritySearchPort similaritySearchPort;
    @Mock
    private NewsAnalysisRepositoryPort newsAnalysisRepositoryPort;
    @Mock
    private LlmExplanationPort llmExplanationPort;
    @Mock
    private SourceReputationPort sourceReputationPort;
    @Mock
    private TextConsistencyAnalyzerPort textConsistencyAnalyzerPort;
    @Mock
    private DisseminationPatternPort disseminationPatternPort;

    private AnalyzeNewsContentService service;

    @BeforeEach
    void setUp() {
        service = new AnalyzeNewsContentService(
                embeddingGeneratorPort,
                similaritySearchPort,
                newsAnalysisRepositoryPort,
                llmExplanationPort,
                sourceReputationPort,
                textConsistencyAnalyzerPort,
                disseminationPatternPort,
                new CredibilityScoreCalculator(),
                new CrossVerificationScorer(),
                ScoreWeights.DEFAULT);
    }

    @Test
    void combinesAllFourFactorsAndPersistsTheResult() {
        NewsContentSubmission submission = new NewsContentSubmission("Titulo", "Conteudo da noticia", "https://example.com/artigo");
        Embedding embedding = new Embedding(new float[]{0.1f, 0.2f, 0.3f});

        when(embeddingGeneratorPort.generate(anyString())).thenReturn(embedding);
        when(textConsistencyAnalyzerPort.analyze(eq("Titulo"), eq("Conteudo da noticia"))).thenReturn(80.0);
        when(sourceReputationPort.lookup("example.com"))
                .thenReturn(new SourceReputation("example.com", 90.0, "confiavel", List.of()));
        when(disseminationPatternPort.analyze(submission)).thenReturn(OptionalDouble.of(60.0));
        when(similaritySearchPort.findMostSimilar(eq(embedding), anyInt())).thenReturn(List.of(
                new SimilarNewsMatch(UUID.randomUUID(), "Noticia similar", 0.9, RiskLevel.LOW_RISK)));
        when(llmExplanationPort.explain(any(), any(), any())).thenReturn(List.of("Explicacao gerada pela IA"));
        when(newsAnalysisRepositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        NewsAnalysis result = service.analyze(submission);

        // S = 0.35*90 + 0.25*80 + 0.30*V + 0.10*60; V (cross-verification) = 100 (unico match, LOW_RISK)
        // S = 31.5 + 20 + 30 + 6 = 87.5
        assertThat(result.score().value()).isCloseTo(87.5, org.assertj.core.data.Offset.offset(1e-9));
        assertThat(result.score().riskLevel()).isEqualTo(RiskLevel.LOW_RISK);
        assertThat(result.aiGeneratedExplanations()).containsExactly("Explicacao gerada pela IA");

        ArgumentCaptor<NewsAnalysis> savedCaptor = ArgumentCaptor.forClass(NewsAnalysis.class);
        verify(newsAnalysisRepositoryPort).save(savedCaptor.capture());
        assertThat(savedCaptor.getValue().title()).isEqualTo("Titulo");
    }

    @Test
    void withoutSourceUrlUsesNeutralReputationInsteadOfCallingReputationPort() {
        NewsContentSubmission submission = new NewsContentSubmission("Titulo", "Conteudo", null);
        Embedding embedding = new Embedding(new float[]{0.5f});

        when(embeddingGeneratorPort.generate(anyString())).thenReturn(embedding);
        when(textConsistencyAnalyzerPort.analyze(anyString(), anyString())).thenReturn(50.0);
        when(disseminationPatternPort.analyze(submission)).thenReturn(OptionalDouble.empty());
        when(similaritySearchPort.findMostSimilar(eq(embedding), anyInt())).thenReturn(List.of());
        when(llmExplanationPort.explain(any(), any(), any())).thenReturn(List.of());
        when(newsAnalysisRepositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        NewsAnalysis result = service.analyze(submission);

        CredibilityFactors factors = result.score().factors();
        assertThat(factors.sourceReputation()).isEqualTo(50.0); // SourceReputation.neutral(...)
        assertThat(factors.disseminationPattern()).isEmpty();
        verify(sourceReputationPort, org.mockito.Mockito.never()).lookup(anyString());
    }
}
