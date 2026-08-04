package com.newstrust.domain.model;

import java.util.Map;
import java.util.Objects;

/**
 * Um no do grafo de credibilidade (fonte por dominio, ou noticia por conteudo),
 * ja em formato pronto para consumo por bibliotecas de visualizacao (D3.js).
 */
public record GraphNode(String id, String label, double credibilityScore, RiskLevel riskLevel,
                         Map<String, Object> metadata) {

    public GraphNode {
        Objects.requireNonNull(id, "id nao pode ser nulo");
        Objects.requireNonNull(label, "label nao pode ser nulo");
        Objects.requireNonNull(riskLevel, "riskLevel nao pode ser nulo");
        metadata = Map.copyOf(metadata);
    }
}
