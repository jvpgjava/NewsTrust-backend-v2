package com.newstrust.application.service;

import com.newstrust.domain.model.CredibilityFactors;
import com.newstrust.domain.model.CredibilityScore;
import com.newstrust.domain.model.ScoreWeights;
import com.newstrust.domain.model.SourceAnalysis;
import com.newstrust.domain.model.SourceReputation;
import com.newstrust.domain.model.SourceSubmission;
import com.newstrust.domain.port.in.AnalyzeSourceUseCase;
import com.newstrust.domain.port.out.SourceAnalysisRepositoryPort;
import com.newstrust.domain.port.out.SourceReputationPort;
import com.newstrust.domain.service.CredibilityScoreCalculator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.OptionalDouble;
import java.util.UUID;

/**
 * Orquestra o caso de uso de analise de fonte: nao ha um artigo especifico, entao
 * apenas a reputacao do dominio (R) e usada, com o perfil de pesos {@link ScoreWeights#SOURCE_ONLY}.
 */
@Service
public class AnalyzeSourceService implements AnalyzeSourceUseCase {

    private final SourceReputationPort sourceReputationPort;
    private final SourceAnalysisRepositoryPort sourceAnalysisRepositoryPort;
    private final CredibilityScoreCalculator credibilityScoreCalculator;

    public AnalyzeSourceService(SourceReputationPort sourceReputationPort,
                                 SourceAnalysisRepositoryPort sourceAnalysisRepositoryPort,
                                 CredibilityScoreCalculator credibilityScoreCalculator) {
        this.sourceReputationPort = sourceReputationPort;
        this.sourceAnalysisRepositoryPort = sourceAnalysisRepositoryPort;
        this.credibilityScoreCalculator = credibilityScoreCalculator;
    }

    @Override
    public SourceAnalysis analyze(SourceSubmission submission) {
        String domain = UrlDomainExtractor.extract(submission.url());
        SourceReputation reputation = sourceReputationPort.lookup(domain);

        CredibilityFactors factors = new CredibilityFactors(
                reputation.reputationScore(), 0.0, 0.0, OptionalDouble.empty());
        CredibilityScore score = credibilityScoreCalculator.calculate(factors, ScoreWeights.SOURCE_ONLY);

        SourceAnalysis analysis = new SourceAnalysis(
                UUID.randomUUID(), domain, submission.url(), reputation, score, Instant.now());

        return sourceAnalysisRepositoryPort.save(analysis);
    }
}
