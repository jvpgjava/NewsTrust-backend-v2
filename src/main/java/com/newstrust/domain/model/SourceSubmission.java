package com.newstrust.domain.model;

/**
 * Entrada do caso de uso de analise de fonte: uma URL a ser avaliada.
 */
public record SourceSubmission(String url) {

    public SourceSubmission {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("url nao pode ser vazia");
        }
    }
}
