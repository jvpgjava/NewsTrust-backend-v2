package com.newstrust.infrastructure.adapter.out.ai;

import com.newstrust.domain.model.Embedding;
import com.newstrust.domain.port.out.EmbeddingGeneratorPort;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

/**
 * Gera embeddings via LangChain4j. Depende apenas da abstracao generica
 * {@link EmbeddingModel} do LangChain4j, nao de nenhuma classe especifica do
 * Gemini - trocar de provedor no futuro e uma questao de reconfigurar
 * {@link com.newstrust.infrastructure.adapter.out.ai.provider.GeminiLlmProvider},
 * sem tocar neste adapter nem no dominio.
 */
@Component
public class LangChainEmbeddingAdapter implements EmbeddingGeneratorPort {

    private final EmbeddingModel embeddingModel;

    public LangChainEmbeddingAdapter(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @Override
    public Embedding generate(String text) {
        float[] vector = embeddingModel.embed(text).content().vector();
        return new Embedding(vector);
    }
}
