package com.newstrust.infrastructure.adapter.out.text;

import com.newstrust.domain.port.out.TextConsistencyAnalyzerPort;
import dev.langchain4j.model.chat.ChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fator T = media entre um componente deterministico (heuristicas linguisticas,
 * ver {@link HeuristicTextConsistencyAnalyzer}) e um componente semantico via
 * Gemini (coerencia interna, inconsistencias logicas) - alinhado com a Secao 3.2
 * do artigo, que descreve T como calculado por "heuristicas linguisticas e
 * analise semantica via LLM".
 * <p>
 * Se a chamada ao Gemini falhar por qualquer motivo (indisponibilidade, chave
 * ausente, resposta nao interpretavel), T degrada graciosamente para o
 * componente heuristico puro - a IA aqui enriquece o fator, nunca e um ponto
 * unico de falha para o calculo do score.
 */
@Component
public class LangChainTextConsistencyAnalyzer implements TextConsistencyAnalyzerPort {

    private static final Logger log = LoggerFactory.getLogger(LangChainTextConsistencyAnalyzer.class);

    private static final Pattern FIRST_INTEGER = Pattern.compile("\\d+");
    private static final int MAX_CONTENT_CHARS_IN_PROMPT = 2000;

    private static final String PROMPT_TEMPLATE = """
            Voce e um analista de consistencia textual para um sistema de credibilidade de
            noticias. Avalie APENAS a qualidade e coerencia da escrita abaixo - coerencia
            semantica interna, presenca de inconsistencias ou contradicoes logicas dentro do
            proprio texto, e uso enganoso de linguagem. NAO avalie se o fato relatado e
            verdadeiro ou falso; isso e feito por outro fator do sistema.

            Titulo: %s
            Conteudo: %s

            Responda APENAS com um numero inteiro de 0 a 100, sem nenhum outro texto:
            100 = texto coerente, sem contradicoes internas nem sinais de manipulacao;
            0 = texto incoerente, contraditorio ou com fortes sinais de manipulacao textual.
            """;

    private final HeuristicTextConsistencyAnalyzer heuristicAnalyzer;
    private final ChatModel chatModel;

    public LangChainTextConsistencyAnalyzer(HeuristicTextConsistencyAnalyzer heuristicAnalyzer, ChatModel chatModel) {
        this.heuristicAnalyzer = heuristicAnalyzer;
        this.chatModel = chatModel;
    }

    @Override
    public double analyze(String title, String content) {
        double heuristicScore = heuristicAnalyzer.analyze(title, content);

        try {
            double semanticScore = analyzeSemanticConsistency(title, content);
            return (heuristicScore + semanticScore) / 2.0;
        } catch (Exception e) {
            log.warn("Falha ao gerar analise semantica via LLM para o fator T; usando apenas a heuristica", e);
            return heuristicScore;
        }
    }

    private double analyzeSemanticConsistency(String title, String content) {
        String prompt = PROMPT_TEMPLATE.formatted(title, truncate(content, MAX_CONTENT_CHARS_IN_PROMPT));
        String response = chatModel.chat(prompt);

        Matcher matcher = FIRST_INTEGER.matcher(response == null ? "" : response);
        if (!matcher.find()) {
            throw new IllegalStateException("Resposta do LLM nao contem um numero interpretavel: " + response);
        }

        double value = Double.parseDouble(matcher.group());
        return Math.max(0.0, Math.min(100.0, value));
    }

    private static String truncate(String text, int maxLength) {
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }
}
