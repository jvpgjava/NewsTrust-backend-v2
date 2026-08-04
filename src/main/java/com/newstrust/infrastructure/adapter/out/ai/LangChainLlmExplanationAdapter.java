package com.newstrust.infrastructure.adapter.out.ai;

import com.newstrust.domain.model.CredibilityScore;
import com.newstrust.domain.model.NewsContentSubmission;
import com.newstrust.domain.model.SimilarNewsMatch;
import com.newstrust.domain.port.out.LlmExplanationPort;
import dev.langchain4j.model.chat.ChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Gera explicacoes em linguagem natural via LangChain4j/Gemini, usando o contexto
 * recuperado por RAG (noticias similares). Depende apenas de {@link ChatModel},
 * a abstracao generica do LangChain4j - mesma razao de design que o adapter de embeddings.
 * <p>
 * O score em si (deterministicScore) e passado como informacao de ENTRADA para o
 * prompt, nunca recalculado ou sobrescrito aqui: esta classe so produz texto explicativo.
 * Qualquer falha na chamada ao modelo e absorvida (log + lista vazia) para que a
 * indisponibilidade do provedor de IA nunca impeca o caso de uso de retornar o
 * score deterministico ja calculado.
 */
@Component
public class LangChainLlmExplanationAdapter implements LlmExplanationPort {

    private static final Logger log = LoggerFactory.getLogger(LangChainLlmExplanationAdapter.class);

    private static final String PROMPT_TEMPLATE = """
            Voce e um assistente de checagem de fatos que EXPLICA um score de credibilidade
            ja calculado por uma formula deterministica - voce nao decide nem altera esse
            score, apenas descreve, em portugues, de forma clara e objetiva (3 a 5 frases
            curtas), por que o contexto abaixo e consistente com o resultado apresentado.

            Titulo da noticia: %s
            Conteudo (resumo): %s

            Score de credibilidade ja calculado (0-100): %.2f
            Faixa de risco ja classificada: %s

            Noticias similares ja verificadas pelo sistema (contexto de verificacao cruzada via RAG):
            %s

            Responda apenas com a explicacao em texto corrido, sem recalcular ou repetir o score.
            """;

    private static final int MAX_CONTENT_CHARS_IN_PROMPT = 1000;

    private final ChatModel chatModel;

    public LangChainLlmExplanationAdapter(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public List<String> explain(NewsContentSubmission submission, CredibilityScore deterministicScore,
                                 List<SimilarNewsMatch> similarNews) {
        try {
            String prompt = PROMPT_TEMPLATE.formatted(
                    submission.title(),
                    truncate(submission.content(), MAX_CONTENT_CHARS_IN_PROMPT),
                    deterministicScore.value(),
                    deterministicScore.riskLevel(),
                    summarizeSimilarNews(similarNews));

            String response = chatModel.chat(prompt);
            return (response == null || response.isBlank()) ? List.of() : List.of(response.trim());
        } catch (Exception e) {
            log.warn("Falha ao gerar explicacao via LLM; prosseguindo apenas com as razoes deterministicas", e);
            return List.of();
        }
    }

    private static String summarizeSimilarNews(List<SimilarNewsMatch> similarNews) {
        if (similarNews == null || similarNews.isEmpty()) {
            return "Nenhuma noticia similar encontrada na base ate o momento.";
        }
        return similarNews.stream()
                .map(match -> "- \"%s\" (similaridade %.0f%%, faixa %s)".formatted(
                        match.title(), match.cosineSimilarity() * 100, match.riskLevel()))
                .collect(Collectors.joining("\n"));
    }

    private static String truncate(String text, int maxLength) {
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }
}
