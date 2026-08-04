package com.newstrust.domain.model;

/**
 * Entrada do caso de uso de analise de conteudo: titulo + corpo de uma noticia,
 * com URL de origem opcional (usada para consultar a reputacao da fonte).
 */
public record NewsContentSubmission(String title, String content, String sourceUrl) {

    public NewsContentSubmission {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title nao pode ser vazio");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content nao pode ser vazio");
        }
    }

    public boolean hasSourceUrl() {
        return sourceUrl != null && !sourceUrl.isBlank();
    }
}
