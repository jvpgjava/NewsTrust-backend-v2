package com.newstrust.infrastructure.adapter.in.web.sse;

import com.newstrust.infrastructure.event.NewsAnalysisCreatedEvent;
import com.newstrust.infrastructure.event.SourceAnalysisCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Mantem as conexoes SSE ativas e transmite um evento "analysis-created" para
 * todas elas sempre que uma analise (conteudo ou fonte) e persistida em
 * qualquer lugar do sistema - e assim que o grafo se atualiza em tempo real
 * sem que o frontend precise fazer polling. Cada conexao aberta segura uma
 * virtual thread (spring.threads.virtual.enabled=true), entao mante-las
 * abertas por longos periodos e barato.
 */
@Component
public class AnalysisEventBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(AnalysisEventBroadcaster.class);

    /** O EventSource do navegador reconecta sozinho apos o timeout, entao um valor alto e seguro. */
    private static final long EMITTER_TIMEOUT_MILLIS = 30 * 60 * 1000L;

    private static final String EVENT_NAME = "analysis-created";

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MILLIS);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(ex -> emitters.remove(emitter));
        return emitter;
    }

    @EventListener
    public void onNewsAnalysisCreated(NewsAnalysisCreatedEvent event) {
        var analysis = event.analysis();
        broadcast(new AnalysisEventPayload("news", analysis.id().toString(), analysis.title(),
                analysis.score().value(), analysis.score().riskLevel(), analysis.createdAt()));
    }

    @EventListener
    public void onSourceAnalysisCreated(SourceAnalysisCreatedEvent event) {
        var analysis = event.analysis();
        broadcast(new AnalysisEventPayload("sources", analysis.domain(), analysis.domain(),
                analysis.score().value(), analysis.score().riskLevel(), analysis.createdAt()));
    }

    private void broadcast(AnalysisEventPayload payload) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(EVENT_NAME).data(payload));
            } catch (IOException | IllegalStateException ex) {
                log.debug("Removendo emitter SSE inativo", ex);
                emitters.remove(emitter);
            }
        }
    }

    int subscriberCount() {
        return emitters.size();
    }
}
