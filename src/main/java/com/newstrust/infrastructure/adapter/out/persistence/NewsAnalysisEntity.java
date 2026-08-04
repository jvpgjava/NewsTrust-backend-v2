package com.newstrust.infrastructure.adapter.out.persistence;

import com.newstrust.domain.model.CredibilityFactors;
import com.newstrust.domain.model.CredibilityScore;
import com.newstrust.domain.model.Embedding;
import com.newstrust.domain.model.NewsAnalysis;
import com.newstrust.domain.model.RiskLevel;
import com.newstrust.domain.model.ScoreWeights;
import com.newstrust.domain.service.CredibilityScoreCalculator;
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
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapeamento JPA/pgvector de {@link NewsAnalysis}. Vive inteiramente na
 * infraestrutura - o domain.model.NewsAnalysis nao tem nenhuma destas anotacoes.
 */
@Entity
@Table(name = "news_analysis")
public class NewsAnalysisEntity {

    /** Deve ser mantido em sincronia com VECTOR(768) em V1__init_schema.sql. */
    public static final int EMBEDDING_DIMENSIONS = 768;

    @Id
    private UUID id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "source_url")
    private String sourceUrl;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = EMBEDDING_DIMENSIONS)
    @Column(name = "embedding", nullable = false)
    private float[] embedding;

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

    @Column(name = "source_reputation_factor", nullable = false)
    private double sourceReputationFactor;
    @Column(name = "textual_consistency_factor", nullable = false)
    private double textualConsistencyFactor;
    @Column(name = "cross_verification_factor", nullable = false)
    private double crossVerificationFactor;
    @Column(name = "dissemination_factor", nullable = false)
    private double disseminationFactor;
    @Column(name = "dissemination_is_baseline", nullable = false)
    private boolean disseminationIsBaseline;

    @ElementCollection
    @CollectionTable(name = "news_analysis_reason", joinColumns = @JoinColumn(name = "news_analysis_id"))
    @OrderColumn(name = "position")
    private List<ScoreReasonEmbeddable> reasons = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "news_analysis_ai_explanation", joinColumns = @JoinColumn(name = "news_analysis_id"))
    @OrderColumn(name = "position")
    @Column(name = "explanation", columnDefinition = "TEXT")
    private List<String> aiGeneratedExplanations = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected NewsAnalysisEntity() {
        // JPA
    }

    public static NewsAnalysisEntity fromDomain(NewsAnalysis analysis) {
        NewsAnalysisEntity entity = new NewsAnalysisEntity();
        entity.id = analysis.id();
        entity.title = analysis.title();
        entity.content = analysis.content();
        entity.sourceUrl = analysis.sourceUrl();
        entity.embedding = analysis.embedding().vector();

        CredibilityScore score = analysis.score();
        entity.scoreValue = score.value();
        entity.riskLevel = score.riskLevel();

        ScoreWeights weights = score.weights();
        entity.reputationWeight = weights.reputationWeight();
        entity.textualConsistencyWeight = weights.textualConsistencyWeight();
        entity.crossVerificationWeight = weights.crossVerificationWeight();
        entity.disseminationWeight = weights.disseminationWeight();

        CredibilityFactors factors = score.factors();
        entity.sourceReputationFactor = factors.sourceReputation();
        entity.textualConsistencyFactor = factors.textualConsistency();
        entity.crossVerificationFactor = factors.crossVerification();
        entity.disseminationIsBaseline = factors.disseminationPattern().isEmpty();
        entity.disseminationFactor = factors.disseminationPattern()
                .orElse(CredibilityScoreCalculator.NEUTRAL_DISSEMINATION_BASELINE);

        entity.reasons = score.reasons().stream().map(ScoreReasonEmbeddable::fromDomain).collect(Collectors.toList());
        entity.aiGeneratedExplanations = new ArrayList<>(analysis.aiGeneratedExplanations());
        entity.createdAt = analysis.createdAt();
        return entity;
    }

    public NewsAnalysis toDomain() {
        ScoreWeights weights = new ScoreWeights(
                reputationWeight, textualConsistencyWeight, crossVerificationWeight, disseminationWeight);

        CredibilityFactors factors = new CredibilityFactors(
                sourceReputationFactor,
                textualConsistencyFactor,
                crossVerificationFactor,
                disseminationIsBaseline ? OptionalDouble.empty() : OptionalDouble.of(disseminationFactor));

        CredibilityScore score = new CredibilityScore(
                scoreValue,
                RiskLevel.fromScore(scoreValue),
                factors,
                weights,
                reasons.stream().map(ScoreReasonEmbeddable::toDomain).collect(Collectors.toList()));

        return new NewsAnalysis(id, title, content, sourceUrl, new Embedding(embedding), score,
                aiGeneratedExplanations, createdAt);
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public double getScoreValue() {
        return scoreValue;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }
}
