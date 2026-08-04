package com.newstrust.infrastructure.adapter.in.web;

import com.newstrust.domain.model.GraphData;
import com.newstrust.domain.port.in.GetTrustGraphUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Confirma que o mapeamento de rota + a documentacao OpenAPI, declarados apenas
 * na interface {@link com.newstrust.infrastructure.adapter.in.web.openapi.TrustGraphApi},
 * sao efetivamente herdados pelo controller (Spring MVC suporta isso desde o 4.3,
 * mas vale um teste de regressao explicito ja que o controller em si nao tem
 * nenhuma anotacao de mapeamento).
 */
@WebMvcTest(TrustGraphController.class)
class TrustGraphControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetTrustGraphUseCase getTrustGraphUseCase;

    @Test
    void getSourcesNetworkResolvesThroughTheInheritedMapping() throws Exception {
        when(getTrustGraphUseCase.sourceNetwork()).thenReturn(new GraphData(List.of(), List.of()));

        mockMvc.perform(get("/api/graph/sources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodes").isArray())
                .andExpect(jsonPath("$.edges").isArray());
    }

    @Test
    void getNewsNodeDetailReturns404WithProblemDetailWhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(getTrustGraphUseCase.newsNodeDetail(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/graph/news/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
