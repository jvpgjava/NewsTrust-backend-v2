package com.newstrust.domain.service;

import com.newstrust.domain.model.CredibilityFactors;
import com.newstrust.domain.model.CredibilityScore;
import com.newstrust.domain.model.RiskLevel;
import com.newstrust.domain.model.ScoreFactor;
import com.newstrust.domain.model.ScoreReason;
import com.newstrust.domain.model.ScoreWeights;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Regra de negocio central do NewsTrust: combina os quatro fatores de credibilidade
 * numa unica pontuacao auditavel via soma ponderada linear -
 * S = (reputationWeight * R) + (textualConsistencyWeight * T)
 *   + (crossVerificationWeight * V) + (disseminationWeight * D).
 * <p>
 * Nao possui nenhuma dependencia de Spring, JPA ou IA: e um objeto Java puro,
 * testavel isoladamente. A IA (RAG/Gemini) pode enriquecer os fatores de entrada
 * (em particular V, via verificacao cruzada) e gerar explicacoes textuais adicionais,
 * mas nunca decide o score - o resultado desta classe e sempre a fonte da verdade.
 */
public class CredibilityScoreCalculator {

    public static final double NEUTRAL_DISSEMINATION_BASELINE = 50.0;

    public CredibilityScore calculate(CredibilityFactors factors) {
        return calculate(factors, ScoreWeights.DEFAULT);
    }

    public CredibilityScore calculate(CredibilityFactors factors, ScoreWeights weights) {
        Objects.requireNonNull(factors, "factors nao pode ser nulo");
        Objects.requireNonNull(weights, "weights nao pode ser nulo");

        boolean disseminationIsBaseline = factors.disseminationPattern().isEmpty();
        double dissemination = factors.disseminationPattern().orElse(NEUTRAL_DISSEMINATION_BASELINE);

        double rawScore = weights.reputationWeight() * factors.sourceReputation()
                + weights.textualConsistencyWeight() * factors.textualConsistency()
                + weights.crossVerificationWeight() * factors.crossVerification()
                + weights.disseminationWeight() * dissemination;

        double score = clamp(rawScore, 0.0, 100.0);
        RiskLevel riskLevel = RiskLevel.fromScore(score);
        List<ScoreReason> reasons = buildReasons(factors, weights, dissemination, disseminationIsBaseline, score, riskLevel);

        return new CredibilityScore(score, riskLevel, factors, weights, reasons);
    }

    private List<ScoreReason> buildReasons(CredibilityFactors factors, ScoreWeights weights, double dissemination,
                                            boolean disseminationIsBaseline, double score, RiskLevel riskLevel) {
        List<ScoreReason> reasons = new ArrayList<>();

        reasons.add(new ScoreReason(ScoreFactor.SOURCE_REPUTATION, describeFactor(
                "Reputacao da fonte (R)", factors.sourceReputation(), weights.reputationWeight())));

        reasons.add(new ScoreReason(ScoreFactor.TEXTUAL_CONSISTENCY, describeFactor(
                "Consistencia textual (T)", factors.textualConsistency(), weights.textualConsistencyWeight())));

        reasons.add(new ScoreReason(ScoreFactor.CROSS_VERIFICATION, describeFactor(
                "Verificacao cruzada (V)", factors.crossVerification(), weights.crossVerificationWeight())));

        String disseminationLabel = "Padrao de disseminacao (D)";
        if (disseminationIsBaseline) {
            reasons.add(new ScoreReason(ScoreFactor.DISSEMINATION_PATTERN, String.format(Locale.ROOT,
                    "%s: sem dados disponiveis; assumido baseline neutro (%.2f/100, peso %.0f%%) para nao distorcer o score.",
                    disseminationLabel, NEUTRAL_DISSEMINATION_BASELINE, weights.disseminationWeight() * 100)));
        } else {
            reasons.add(new ScoreReason(ScoreFactor.DISSEMINATION_PATTERN,
                    describeFactor(disseminationLabel, dissemination, weights.disseminationWeight())));
        }

        reasons.add(new ScoreReason(ScoreFactor.OVERALL, String.format(Locale.ROOT,
                "Score final %.2f classifica o item na faixa de %s.", score, describeRiskBand(riskLevel))));

        return reasons;
    }

    private String describeFactor(String label, double value, double weight) {
        double contribution = value * weight;
        return String.format(Locale.ROOT, "%s contribuiu com %.2f pontos (peso %.0f%%, valor %.2f/100).",
                label, contribution, weight * 100, value);
    }

    private String describeRiskBand(RiskLevel riskLevel) {
        return switch (riskLevel) {
            case LOW_RISK -> "BAIXO RISCO (S >= " + (int) RiskLevel.LOW_RISK_THRESHOLD + ")";
            case ATTENTION -> "ATENCAO (" + (int) RiskLevel.ATTENTION_THRESHOLD + " <= S < "
                    + (int) RiskLevel.LOW_RISK_THRESHOLD + ")";
            case HIGH_RISK -> "ALTO RISCO (S < " + (int) RiskLevel.ATTENTION_THRESHOLD + ")";
        };
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
