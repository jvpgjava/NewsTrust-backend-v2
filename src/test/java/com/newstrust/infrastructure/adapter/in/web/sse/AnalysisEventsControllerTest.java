package com.newstrust.infrastructure.adapter.in.web.sse;

import com.newstrust.domain.model.NewsAnalysis;
import com.newstrust.infrastructure.event.NewsAnalysisCreatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.newstrust.infrastructure.adapter.in.web.sse.AnalysisEventBroadcasterTest.someNewsAnalysis;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

/**
 * Testa a conexao SSE atraves do dispatch assincrono real do Spring MVC (via
 * MockMvc standalone) - a unica forma confiavel de exercitar o ciclo de vida
 * de um SseEmitter, ja que ResponseBodyEmitter.Handler (usado internamente
 * para inicializa-lo) nao e uma API publica.
 * <p>
 * Nao usa asyncDispatch(): como este emitter e um stream de longa duracao que
 * nunca se completa sozinho, forcar um dispatch assincrono sem uma conclusao
 * real registrada trava o teste. As asserções aqui se apoiam apenas no que ja
 * fica disponivel assim que a conexao inicial abre - se o processamento
 * assincrono comecou e qualquer dado que ja tenha sido escrito no emitter.
 */
class AnalysisEventsControllerTest {

    private final AnalysisEventBroadcaster broadcaster = new AnalysisEventBroadcaster();
    private final MockMvc mockMvc =
            MockMvcBuilders.standaloneSetup(new AnalysisEventsController(broadcaster)).build();

    @Test
    void opensAnSseConnectionAndRegistersASubscriber() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/events/analyses"))
                .andExpect(request().asyncStarted())
                .andReturn();

        assertThat(broadcaster.subscriberCount()).isEqualTo(1);
        assertThat(result.getRequest().isAsyncStarted()).isTrue();
    }

    @Test
    void broadcastEventReachesTheConnectedSubscriberAsSseContent() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/events/analyses"))
                .andExpect(request().asyncStarted())
                .andReturn();

        NewsAnalysis analysis = someNewsAnalysis();
        broadcaster.onNewsAnalysisCreated(new NewsAnalysisCreatedEvent(analysis));

        String body = result.getResponse().getContentAsString();
        assertThat(body).contains("event:analysis-created");
        assertThat(body).contains(analysis.id().toString());
        assertThat(body).contains("\"network\":\"news\"");
    }
}
