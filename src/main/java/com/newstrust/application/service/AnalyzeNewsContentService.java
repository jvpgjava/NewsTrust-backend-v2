package com.newstrust.application.service;

import com.newstrust.domain.model.CredibilityFactors;
import com.newstrust.domain.model.CredibilityScore;
import com.newstrust.domain.model.Embedding;
import com.newstrust.domain.model.NewsAnalysis;
import com.newstrust.domain.model.NewsContentSubmission;
import com.newstrust.domain.model.ScoreWeights;
import com.newstrust.domain.model.SimilarNewsMatch;
import com.newstrust.domain.model.SourceReputation;
import com.newstrust.domain.port.in.AnalyzeNewsContentUseCase;
import com.newstrust.domain.port.out.DisseminationPatternPort;
import com.newstrust.domain.port.out.EmbeddingGeneratorPort;
import com.newstrust.domain.port.out.LlmExplanationPort;
import com.newstrust.domain.port.out.NewsAnalysisRepositoryPort;
import com.newstrust.domain.port.out.SimilaritySearchPort;
import com.newstrust.domain.port.out.SourceReputationPort;
import com.newstrust.domain.port.out.TextConsistencyAnalyzerPort;
import com.newstrust.domain.service.CredibilityScoreCalculator;
import com.newstrust.domain.service.CrossVerificationScorer;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.OptionalDouble;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Orquestra o caso de uso de analise de conteudo: gera o embedding, busca contexto
 * RAG por similaridade, resolve os quatro fatores (R/T/V/D), delega o calculo do
 * score ao dominio puro, enriquece com explicacoes de IA e persiste o resultado.
 * <p>
 * As chamadas independentes a portas de saida (embedding, reputacao, analise
 * textual, disseminacao) rodam em virtual threads, ja que sao IO-bound e
 * concorrentes entre si.
 */
@Service
public class AnalyzeNewsContentService implements AnalyzeNewsContentUseCase {

    private static final int TOP_K_SIMILAR_NEWS = 5;

    private final EmbeddingGeneratorPort embeddingGeneratorPort;
    private final SimilaritySearchPort similaritySearchPort;
    private final NewsAnalysisRepositoryPort newsAnalysisRepositoryPort;
    private final LlmExplanationPort llmExplanationPort;
    private final SourceReputationPort sourceReputationPort;
    private final TextConsistencyAnalyzerPort textConsistencyAnalyzerPort;
    private final DisseminationPatternPort disseminationPatternPort;
    private final CredibilityScoreCalculator credibilityScoreCalculator;
    private final CrossVerificationScorer crossVerificationScorer;
    private final ScoreWeights activeScoreWeights;

    public AnalyzeNewsContentService(EmbeddingGeneratorPort embeddingGeneratorPort,
                                      SimilaritySearchPort similaritySearchPort,
                                      NewsAnalysisRepositoryPort newsAnalysisRepositoryPort,
                                      LlmExplanationPort llmExplanationPort,
                                      SourceReputationPort sourceReputationPort,
                                      TextConsistencyAnalyzerPort textConsistencyAnalyzerPort,
                                      DisseminationPatternPort disseminationPatternPort,
                                      CredibilityScoreCalculator credibilityScoreCalculator,
                                      CrossVerificationScorer crossVerificationScorer,
                                      ScoreWeights activeScoreWeights) {
        this.embeddingGeneratorPort = embeddingGeneratorPort;
        this.similaritySearchPort = similaritySearchPort;
        this.newsAnalysisRepositoryPort = newsAnalysisRepositoryPort;
        this.llmExplanationPort = llmExplanationPort;
        this.sourceReputationPort = sourceReputationPort;
        this.textConsistencyAnalyzerPort = textConsistencyAnalyzerPort;
        this.disseminationPatternPort = disseminationPatternPort;
        this.credibilityScoreCalculator = credibilityScoreCalculator;
        this.crossVerificationScorer = crossVerificationScorer;
        this.activeScoreWeights = activeScoreWeights;
    }

    @Override
    public NewsAnalysis analyze(NewsContentSubmission submission) {
        String fullText = submission.title() + "\n\n" + submission.content();

        try (ExecutorService virtualThreads = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<Embedding> embeddingFuture = virtualThreads.submit(() -> embeddingGeneratorPort.generate(fullText));
            Future<Double> textConsistencyFuture = virtualThreads.submit(
                    () -> textConsistencyAnalyzerPort.analyze(submission.title(), submission.content()));
            Future<SourceReputation> reputationFuture = virtualThreads.submit(() -> resolveSourceReputation(submission));
            Future<OptionalDouble> disseminationFuture = virtualThreads.submit(
                    () -> disseminationPatternPort.analyze(submission));

            Embedding embedding = await(embeddingFuture);
            List<SimilarNewsMatch> similarNews = similaritySearchPort.findMostSimilar(embedding, TOP_K_SIMILAR_NEWS);

            double textConsistency = await(textConsistencyFuture);
            SourceReputation reputation = await(reputationFuture);
            OptionalDouble dissemination = await(disseminationFuture);
            double crossVerification = crossVerificationScorer.score(similarNews);

            CredibilityFactors factors = new CredibilityFactors(
                    reputation.reputationScore(), textConsistency, crossVerification, dissemination);
            CredibilityScore score = credibilityScoreCalculator.calculate(factors, activeScoreWeights);

            List<String> aiExplanations = llmExplanationPort.explain(submission, score, similarNews);

            NewsAnalysis analysis = new NewsAnalysis(UUID.randomUUID(), submission.title(), submission.content(),
                    submission.sourceUrl(), embedding, score, aiExplanations, Instant.now());

            return newsAnalysisRepositoryPort.save(analysis);
        }
    }

    private SourceReputation resolveSourceReputation(NewsContentSubmission submission) {
        if (!submission.hasSourceUrl()) {
            return SourceReputation.neutral("desconhecido");
        }
        String domain = UrlDomainExtractor.extract(submission.sourceUrl());
        return sourceReputationPort.lookup(domain);
    }

    private static <T> T await(Future<T> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AnalysisOrchestrationException("Interrompido aguardando resultado assincrono", e);
        } catch (ExecutionException e) {
            throw new AnalysisOrchestrationException("Falha ao executar chamada assincrona", e.getCause());
        }
    }
}
