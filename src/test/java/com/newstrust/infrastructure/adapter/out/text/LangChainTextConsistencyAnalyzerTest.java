package com.newstrust.infrastructure.adapter.out.text;

import dev.langchain4j.model.chat.ChatModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LangChainTextConsistencyAnalyzerTest {

    @Mock
    private ChatModel chatModel;

    private LangChainTextConsistencyAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new LangChainTextConsistencyAnalyzer(new HeuristicTextConsistencyAnalyzer(), chatModel);
    }

    @Test
    void averagesHeuristicAndSemanticScoresWhenTheLlmRespondsCleanly() {
        // heuristica para este texto limpo = 100.0
        when(chatModel.chat(anyString())).thenReturn("80");

        double result = analyzer.analyze("Título normal", "Conteúdo perfeitamente comum e sóbrio.");

        assertThat(result).isEqualTo((100.0 + 80.0) / 2.0);
    }

    @Test
    void extractsTheNumberEvenWhenTheLlmAddsExtraText() {
        when(chatModel.chat(anyString())).thenReturn("O valor e 65, considerando o texto.");

        double result = analyzer.analyze("Título normal", "Conteúdo perfeitamente comum e sóbrio.");

        assertThat(result).isEqualTo((100.0 + 65.0) / 2.0);
    }

    @Test
    void clampsAnOutOfRangeSemanticScoreToTheValidInterval() {
        when(chatModel.chat(anyString())).thenReturn("150");

        double result = analyzer.analyze("Título normal", "Conteúdo perfeitamente comum e sóbrio.");

        assertThat(result).isEqualTo((100.0 + 100.0) / 2.0);
    }

    @Test
    void fallsBackToTheHeuristicScoreWhenTheLlmThrows() {
        when(chatModel.chat(anyString())).thenThrow(new RuntimeException("Gemini indisponível"));

        double result = analyzer.analyze("Título normal", "Conteúdo perfeitamente comum e sóbrio.");

        assertThat(result).isEqualTo(100.0);
    }

    @Test
    void fallsBackToTheHeuristicScoreWhenTheLlmResponseHasNoParseableNumber() {
        when(chatModel.chat(anyString())).thenReturn("não sei dizer");

        double result = analyzer.analyze("Título normal", "Conteúdo perfeitamente comum e sóbrio.");

        assertThat(result).isEqualTo(100.0);
    }
}
