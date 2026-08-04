package com.newstrust.infrastructure.event;

import com.newstrust.domain.model.NewsAnalysis;

/**
 * Evento interno (Spring ApplicationEvent, nao um evento de dominio) publicado
 * apos uma analise de conteudo ser persistida. Usado para alimentar o SSE do
 * grafo em tempo real - infraestrutura pura, o dominio nao sabe que isso existe.
 */
public record NewsAnalysisCreatedEvent(NewsAnalysis analysis) {
}
