package com.newstrust.infrastructure.adapter.in.web;

import com.newstrust.domain.port.in.GetTrustGraphUseCase;
import com.newstrust.infrastructure.adapter.in.web.dto.GraphResponse;
import com.newstrust.infrastructure.adapter.in.web.dto.NewsGraphNodeDetailResponse;
import com.newstrust.infrastructure.adapter.in.web.openapi.TrustGraphApi;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Traduz requisicoes HTTP em chamadas ao caso de uso do dominio. O mapeamento de
 * rota e a documentacao OpenAPI/Swagger ficam em {@link TrustGraphApi}, para
 * manter este controller enxuto.
 */
@RestController
public class TrustGraphController implements TrustGraphApi {

    private final GetTrustGraphUseCase getTrustGraphUseCase;

    public TrustGraphController(GetTrustGraphUseCase getTrustGraphUseCase) {
        this.getTrustGraphUseCase = getTrustGraphUseCase;
    }

    @Override
    public GraphResponse sourceNetwork() {
        return GraphResponse.from(getTrustGraphUseCase.sourceNetwork());
    }

    @Override
    public GraphResponse newsNetwork() {
        return GraphResponse.from(getTrustGraphUseCase.newsNetwork());
    }

    @Override
    public NewsGraphNodeDetailResponse newsNodeDetail(UUID id) {
        return getTrustGraphUseCase.newsNodeDetail(id)
                .map(NewsGraphNodeDetailResponse::from)
                .orElseThrow(() -> new NoSuchElementException("Noticia nao encontrada: " + id));
    }
}
