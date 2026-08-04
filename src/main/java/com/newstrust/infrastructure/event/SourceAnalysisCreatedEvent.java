package com.newstrust.infrastructure.event;

import com.newstrust.domain.model.SourceAnalysis;

/**
 * Evento interno (Spring ApplicationEvent) publicado apos uma analise de fonte
 * ser persistida. Ver {@link NewsAnalysisCreatedEvent}.
 */
public record SourceAnalysisCreatedEvent(SourceAnalysis analysis) {
}
