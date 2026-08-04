package com.newstrust.infrastructure.adapter.out.ai.provider;

import com.newstrust.infrastructure.adapter.out.persistence.NewsAnalysisEntity;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Unico ponto de configuracao do provedor de LLM/embeddings: Google Gemini via
 * LangChain4j (API direta do Google AI Studio), usado tanto em dev quanto em
 * prod - o que muda entre os dois e apenas a configuracao (application-dev vs
 * application-prod.properties: chave de API e nome do modelo), nunca o
 * provedor em si.
 * <p>
 * Os adapters ({@link com.newstrust.infrastructure.adapter.out.ai.LangChainEmbeddingAdapter},
 * {@link com.newstrust.infrastructure.adapter.out.ai.LangChainLlmExplanationAdapter}) dependem
 * apenas das interfaces genericas {@link ChatModel}/{@link EmbeddingModel} do LangChain4j, nunca
 * das classes especificas do Gemini abaixo - adicionar um provedor de fallback no futuro e uma
 * questao de alterar (ou compor) os beans desta classe, sem tocar nos adapters nem no dominio.
 */
@Configuration
public class GeminiLlmProvider {

    @Bean
    public ChatModel chatModel(
            @Value("${newstrust.ai.gemini.api-key}") String apiKey,
            @Value("${newstrust.ai.gemini.chat-model}") String modelName) {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel(
            @Value("${newstrust.ai.gemini.api-key}") String apiKey,
            @Value("${newstrust.ai.gemini.embedding-model}") String modelName) {
        return GoogleAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .taskType(GoogleAiEmbeddingModel.TaskType.RETRIEVAL_DOCUMENT)
                // gemini-embedding-2/001 usam Matryoshka Representation Learning e retornam
                // 3072 dimensoes por padrao - truncamos para bater com o VECTOR(768) do
                // schema (768 e um tamanho oficialmente suportado, nao um numero arbitrario).
                .outputDimensionality(NewsAnalysisEntity.EMBEDDING_DIMENSIONS)
                .build();
    }
}
