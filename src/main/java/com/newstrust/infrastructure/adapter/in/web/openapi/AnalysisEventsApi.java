package com.newstrust.infrastructure.adapter.in.web.openapi;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Eventos em tempo real", description = "Stream SSE de novas analises, para atualizar o grafo ao vivo")
@RequestMapping("/api/events")
public interface AnalysisEventsApi {

    @Operation(summary = "Stream de eventos de novas analises (Server-Sent Events)", description = """
            Emite um evento "analysis-created" toda vez que uma analise de conteudo ou de fonte
            e concluida em qualquer lugar do sistema, com dados minimos (id, label, score, faixa
            de risco, rede afetada) para o cliente atualizar o grafo em tempo real sem depender de polling.""")
    @ApiResponse(responseCode = "200", description = "Conexão SSE aberta",
            content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE))
    @GetMapping(path = "/analyses", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    SseEmitter streamAnalysisEvents();
}
