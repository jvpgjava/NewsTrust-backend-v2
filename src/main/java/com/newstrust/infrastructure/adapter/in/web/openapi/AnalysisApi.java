package com.newstrust.infrastructure.adapter.in.web.openapi;

import com.newstrust.infrastructure.adapter.in.web.dto.NewsContentAnalysisRequest;
import com.newstrust.infrastructure.adapter.in.web.dto.NewsContentAnalysisResponse;
import com.newstrust.infrastructure.adapter.in.web.dto.SourceAnalysisRequest;
import com.newstrust.infrastructure.adapter.in.web.dto.SourceAnalysisResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Contrato + documentacao OpenAPI dos endpoints de analise. O mapeamento de rota
 * (@PostMapping) e as anotacoes de validacao (@Valid) tambem vivem aqui - Spring MVC
 * herda anotacoes de metodo declaradas em interfaces, entao
 * {@link com.newstrust.infrastructure.adapter.in.web.AnalysisController} so
 * implementa esta interface e fica livre de qualquer anotacao de mapeamento ou Swagger.
 */
@Tag(name = "Analise", description = "Analise de credibilidade de noticias e fontes")
@RequestMapping("/api/analysis")
public interface AnalysisApi {

    @Operation(summary = "Analisa o conteudo de uma noticia", description = """
            Recebe titulo e conteudo de uma noticia, gera seu embedding, busca noticias
            similares ja processadas (RAG/pgvector) para enriquecer a verificacao cruzada,
            e retorna o score de credibilidade (0-100), a faixa de risco e as razoes
            auditaveis que o justificam.""")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Analise concluida com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = NewsContentAnalysisResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada invalidos",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/content")
    ResponseEntity<NewsContentAnalysisResponse> analyzeContent(@Valid @RequestBody NewsContentAnalysisRequest request);

    @Operation(summary = "Analisa a credibilidade de uma fonte (URL)", description = """
            Extrai o dominio da URL, consulta sua reputacao (ScamAdviser) e retorna um score
            de credibilidade baseado apenas na reputacao da fonte - sem um artigo especifico,
            os fatores de consistencia textual e verificacao cruzada nao se aplicam.""")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Analise concluida com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SourceAnalysisResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada invalidos",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/source")
    ResponseEntity<SourceAnalysisResponse> analyzeSource(@Valid @RequestBody SourceAnalysisRequest request);
}
