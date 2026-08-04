package com.newstrust.infrastructure.adapter.in.web.openapi;

import com.newstrust.infrastructure.adapter.in.web.dto.GraphResponse;
import com.newstrust.infrastructure.adapter.in.web.dto.NewsGraphNodeDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

/**
 * Contrato + documentacao OpenAPI do grafo de credibilidade. Ver {@link AnalysisApi}
 * para a explicacao do padrao (mapeamento e Swagger na interface, implementacao
 * enxuta em {@link com.newstrust.infrastructure.adapter.in.web.TrustGraphController}).
 */
@Tag(name = "Grafo de credibilidade", description = "Rede de fontes e de noticias, pronta para D3.js")
@RequestMapping("/api/graph")
public interface TrustGraphApi {

    @Operation(summary = "Rede de fontes", description = """
            Nos = dominios ja analisados; arestas = similaridade de credibilidade entre eles.""")
    @ApiResponse(responseCode = "200", description = "Grafo retornado com sucesso",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = GraphResponse.class)))
    @GetMapping("/sources")
    GraphResponse sourceNetwork();

    @Operation(summary = "Rede de noticias", description = """
            Nos = noticias ja analisadas, com cor por faixa de risco; arestas = similaridade
            de conteudo via pgvector/HNSW.""")
    @ApiResponse(responseCode = "200", description = "Grafo retornado com sucesso",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = GraphResponse.class)))
    @GetMapping("/news")
    GraphResponse newsNetwork();

    @Operation(summary = "Detalhe de um no da rede de noticias", description = """
            Retorna a analise completa de uma noticia especifica (score, faixa de risco,
            razoes) e as arestas que a conectam a outras noticias similares.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "No encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = NewsGraphNodeDetailResponse.class))),
            @ApiResponse(responseCode = "404", description = "Noticia nao encontrada",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/news/{id}")
    NewsGraphNodeDetailResponse newsNodeDetail(
            @Parameter(description = "ID da noticia (UUID)") @PathVariable UUID id);
}
