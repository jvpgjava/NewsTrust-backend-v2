package com.newstrust.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record SourceAnalysisRequest(
        @NotBlank(message = "url nao pode ser vazia") @URL(message = "url deve ser uma URL valida") String url
) {
}
