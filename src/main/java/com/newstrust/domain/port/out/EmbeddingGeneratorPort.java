package com.newstrust.domain.port.out;

import com.newstrust.domain.model.Embedding;

/**
 * Gera o vetor de embedding de um texto. Implementado pelo adapter Spring AI/Gemini,
 * mas o dominio nao sabe disso - so conhece esta interface.
 */
public interface EmbeddingGeneratorPort {

    Embedding generate(String text);
}
