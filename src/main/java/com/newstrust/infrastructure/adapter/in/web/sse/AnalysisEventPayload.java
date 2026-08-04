package com.newstrust.infrastructure.adapter.in.web.sse;

import com.newstrust.domain.model.RiskLevel;

import java.time.Instant;

/**
 * Payload minimo enviado via SSE quando uma nova analise e concluida - o
 * suficiente para o frontend atualizar um no do grafo (ou exibir uma notificacao)
 * sem precisar buscar o grafo inteiro a cada evento.
 */
public record AnalysisEventPayload(
        String network,
        String nodeId,
        String label,
        double credibilityScore,
        RiskLevel riskLevel,
        Instant occurredAt
) {
}
