package com.newstrust.domain.service;

import com.newstrust.domain.model.RiskLevel;
import com.newstrust.domain.model.SimilarNewsMatch;

import java.util.List;

/**
 * Calcula o fator V (verificacao cruzada) a partir de noticias similares ja
 * processadas pelo sistema (contexto recuperado via RAG/pgvector). Regra pura,
 * sem dependencia de IO ou de IA: recebe os matches ja recuperados e produz um
 * numero auditavel - a media, ponderada pela similaridade de cosseno, do quanto
 * cada match corrobora (baixo risco), e neutro (atencao) ou contradiz (alto risco)
 * a nova submissao. Quando nao ha nenhum match, assume baseline neutro, pelo
 * mesmo motivo que o fator D assume baseline quando nao ha dado de disseminacao:
 * a ausencia de evidencia nao deve penalizar nem favorecer o score.
 */
public class CrossVerificationScorer {

    public static final double NEUTRAL_BASELINE = 50.0;

    public double score(List<SimilarNewsMatch> similarMatches) {
        if (similarMatches == null || similarMatches.isEmpty()) {
            return NEUTRAL_BASELINE;
        }

        double weightedSum = 0.0;
        double weightTotal = 0.0;
        for (SimilarNewsMatch match : similarMatches) {
            double corroboration = corroborationValue(match.riskLevel());
            weightedSum += match.cosineSimilarity() * corroboration;
            weightTotal += match.cosineSimilarity();
        }

        if (weightTotal == 0.0) {
            return NEUTRAL_BASELINE;
        }

        double normalized = (weightedSum / weightTotal) * 100.0;
        return Math.max(0.0, Math.min(100.0, normalized));
    }

    private static double corroborationValue(RiskLevel riskLevel) {
        return switch (riskLevel) {
            case LOW_RISK -> 1.0;
            case ATTENTION -> 0.5;
            case HIGH_RISK -> 0.0;
        };
    }
}
