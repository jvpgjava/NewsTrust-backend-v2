package com.newstrust.domain.model;

/**
 * Pesos aplicados aos quatro fatores do score de credibilidade:
 * S = (reputationWeight * R) + (textualConsistencyWeight * T)
 *   + (crossVerificationWeight * V) + (disseminationWeight * D)
 * <p>
 * Configuravel por dominio de aplicacao (ex: contexto eleitoral eleva o peso
 * de V; saude publica eleva o peso de R) - nao deve ser hardcoded no calculo.
 */
public record ScoreWeights(
        double reputationWeight,
        double textualConsistencyWeight,
        double crossVerificationWeight,
        double disseminationWeight
) {

    private static final double SUM_TOLERANCE = 1e-6;

    /**
     * Pesos padrao definidos no modelo de pontuacao: S = 0.35R + 0.25T + 0.30V + 0.10D.
     */
    public static final ScoreWeights DEFAULT = new ScoreWeights(0.35, 0.25, 0.30, 0.10);

    /**
     * Perfil usado quando nao ha um conteudo de noticia especifico para avaliar
     * (analise de fonte/URL isolada): T (consistencia textual) e V (verificacao
     * cruzada) exigem um artigo concreto para serem calculados, e D (disseminacao)
     * so faz sentido para uma peca de conteudo especifica, nao para um dominio
     * isolado - a reputacao da fonte fica com todo o peso.
     */
    public static final ScoreWeights SOURCE_ONLY = new ScoreWeights(1.0, 0.0, 0.0, 0.0);

    public ScoreWeights {
        requireNonNegative("reputationWeight", reputationWeight);
        requireNonNegative("textualConsistencyWeight", textualConsistencyWeight);
        requireNonNegative("crossVerificationWeight", crossVerificationWeight);
        requireNonNegative("disseminationWeight", disseminationWeight);

        double sum = reputationWeight + textualConsistencyWeight + crossVerificationWeight + disseminationWeight;
        if (Math.abs(sum - 1.0) > SUM_TOLERANCE) {
            throw new IllegalArgumentException("A soma dos pesos deve ser 1.0, soma informada: " + sum);
        }
    }

    private static void requireNonNegative(String fieldName, double value) {
        if (value < 0.0) {
            throw new IllegalArgumentException(fieldName + " nao pode ser negativo: " + value);
        }
    }
}
