package com.newstrust.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record NewsContentAnalysisRequest(
        @NotBlank(message = "title nao pode ser vazio") String title,
        @NotBlank(message = "content nao pode ser vazio") String content,
        @URL(message = "sourceUrl deve ser uma URL valida") String sourceUrl
) {
}
