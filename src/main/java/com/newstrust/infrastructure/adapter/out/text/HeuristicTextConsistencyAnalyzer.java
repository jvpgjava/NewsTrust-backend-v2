package com.newstrust.infrastructure.adapter.out.text;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Calcula o componente deterministico (sem IA) do fator T, a partir de
 * indicadores linguisticos simples: pontuacao excessiva, uso de caixa alta e
 * termos sensacionalistas recorrentes em desinformacao. Comeca em 100 e
 * desconta por ocorrencia.
 * <p>
 * Nao implementa {@link com.newstrust.domain.port.out.TextConsistencyAnalyzerPort}
 * diretamente - e um colaborador usado por
 * {@link com.newstrust.infrastructure.adapter.out.text.LangChainTextConsistencyAnalyzer},
 * que combina este resultado com a analise semantica via LLM descrita no artigo
 * (Secao 3.2: "heuristicas linguisticas e analise semantica via LLM").
 */
@Component
public class HeuristicTextConsistencyAnalyzer {

    private static final Pattern EXCESSIVE_PUNCTUATION = Pattern.compile("[!?]{2,}");
    private static final Pattern ALL_CAPS_WORD = Pattern.compile("\\b[A-Z]{4,}\\b");

    private static final String[] SENSATIONALIST_TERMS = {
            "urgente", "chocante", "bomba", "voce nao vai acreditar",
            "compartilhe antes que apaguem", "midia esconde", "verdade que ninguem conta"
    };

    private static final double PUNCTUATION_PENALTY = 8.0;
    private static final double ALL_CAPS_PENALTY = 4.0;
    private static final double SENSATIONALISM_PENALTY = 15.0;

    public double analyze(String title, String content) {
        String combined = title + " " + content;
        String normalized = normalize(combined);

        double score = 100.0;
        score -= PUNCTUATION_PENALTY * countMatches(EXCESSIVE_PUNCTUATION, combined);
        score -= ALL_CAPS_PENALTY * countMatches(ALL_CAPS_WORD, combined);
        score -= SENSATIONALISM_PENALTY * countSensationalistTerms(normalized);

        return Math.max(0.0, Math.min(100.0, score));
    }

    private static int countMatches(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private static int countSensationalistTerms(String normalizedText) {
        int count = 0;
        for (String term : SENSATIONALIST_TERMS) {
            if (normalizedText.contains(term)) {
                count++;
            }
        }
        return count;
    }

    private static String normalize(String text) {
        String withoutAccents = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase();
    }
}
