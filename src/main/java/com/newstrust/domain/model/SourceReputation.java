package com.newstrust.domain.model;

import java.util.List;
import java.util.Objects;

/**
 * Resultado de uma consulta de reputacao de dominio (hoje via ScamAdviser).
 * Alimenta o fator R (reputacao da fonte) do modelo de pontuacao.
 */
public record SourceReputation(String domain, double reputationScore, String category, List<String> signals) {

    public SourceReputation {
        Objects.requireNonNull(domain, "domain nao pode ser nulo");
        if (reputationScore < 0.0 || reputationScore > 100.0) {
            throw new IllegalArgumentException("reputationScore deve estar entre 0 e 100: " + reputationScore);
        }
        Objects.requireNonNull(category, "category nao pode ser nulo");
        signals = List.copyOf(signals);
    }

    /**
     * Usado quando o provedor de reputacao esta indisponivel ou nao possui dados
     * para o dominio - assume baseline neutro em vez de penalizar a fonte sem evidencia.
     */
    public static SourceReputation neutral(String domain) {
        return new SourceReputation(domain, 50.0, "desconhecida",
                List.of("Nenhum dado de reputacao disponivel para o dominio; assumido baseline neutro."));
    }
}
