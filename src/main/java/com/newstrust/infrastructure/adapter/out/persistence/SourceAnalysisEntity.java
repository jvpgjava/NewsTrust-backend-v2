package com.newstrust.infrastructure.adapter.out.persistence;

import com.newstrust.domain.model.CredibilityFactors;
import com.newstrust.domain.model.CredibilityScore;
import com.newstrust.domain.model.RiskLevel;
import com.newstrust.domain.model.ScoreWeights;
import com.newstrust.domain.model.SourceAnalysis;
import com.newstrust.domain.model.SourceReputation;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapeamento JPA de {@link SourceAnalysis}.
 */
@Entity
@Table(name = "source_analysis")
public class SourceAnalysisEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String domain;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "reputation_score", nullable = false)
    private double reputationScore;

    @Column(name = "reputation_category", nullable = false, length = 60)
    private String reputationCategory;

    @ElementCollection
    @CollectionTable(name = "source_analysis_signal", joinColumns = @JoinColumn(name = "source_analysis_id"))
    @OrderColumn(name = "position")
    @Column(name = "signal", columnDefinition = "TEXT")
    private List<String> reputationSignals = new ArrayList<>();

    @Column(name = "score_value", nullable = false)
    private double scoreValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 20)
    private RiskLevel riskLevel;

    @Column(name = "reputation_weight", nullable = false)
    private double reputationWeight;
    @Column(name = "textual_consistency_weight", nullable = false)
    private double textualConsistencyWeight;
    @Column(name = "cross_verification_weight", nullable = false)
    private double crossVerificationWeight;
    @Column(name = "dissemination_weight", nullable = false)
    private double disseminationWeight;

    @ElementCollection
    @CollectionTable(name = "source_analysis_reason", joinColumns = @JoinColumn(name = "source_analysis_id"))
    @OrderColumn(name = "position")
    private List<ScoreReasonEmbeddable> reasons = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected SourceAnalysisEntity() {
        // JPA
    }

    public static SourceAnalysisEntity fromDomain(SourceAnalysis analysis) {
        SourceAnalysisEntity entity = new SourceAnalysisEntity();
        entity.id = analysis.id();
        entity.domain = analysis.domain();
        entity.url = analysis.url();

        SourceReputation reputation = analysis.reputation();
        entity.reputationScore = reputation.reputationScore();
        entity.reputationCategory = reputation.category();
        entity.reputationSignals = new ArrayList<>(reputation.signals());

        CredibilityScore score = analysis.score();
        entity.scoreValue = score.value();
        entity.riskLevel = score.riskLevel();

        ScoreWeights weights = score.weights();
        entity.reputationWeight = weights.reputationWeight();
        entity.textualConsistencyWeight = weights.textualConsistencyWeight();
        entity.crossVerificationWeight = weights.crossVerificationWeight();
        entity.disseminationWeight = weights.disseminationWeight();

        entity.reasons = score.reasons().stream().map(ScoreReasonEmbeddable::fromDomain).collect(Collectors.toList());
        entity.createdAt = analysis.createdAt();
        return entity;
    }

    public SourceAnalysis toDomain() {
        SourceReputation reputation = new SourceReputation(domain, reputationScore, reputationCategory, reputationSignals);

        ScoreWeights weights = new ScoreWeights(
                reputationWeight, textualConsistencyWeight, crossVerificationWeight, disseminationWeight);

        CredibilityFactors factors = new CredibilityFactors(reputationScore, 0.0, 0.0, OptionalDouble.empty());

        CredibilityScore score = new CredibilityScore(
                scoreValue,
                RiskLevel.fromScore(scoreValue),
                factors,
                weights,
                reasons.stream().map(ScoreReasonEmbeddable::toDomain).collect(Collectors.toList()));

        return new SourceAnalysis(id, domain, url, reputation, score, createdAt);
    }

    public String getDomain() {
        return domain;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
