package com.newstrust.domain.port.out;

import com.newstrust.domain.model.CredibilityScore;
import com.newstrust.domain.model.NewsContentSubmission;
import com.newstrust.domain.model.SimilarNewsMatch;

import java.util.List;

/**
 * Gera explicacoes textuais adicionais, em linguagem natural, a partir do contexto
 * recuperado via RAG (noticias similares ja processadas). Implementado pelo adapter
 * Spring AI/Gemini.
 * <p>
 * Importante: esta porta so PRODUZ TEXTO explicativo. Ela nunca recebe autoridade
 * para alterar o score - o resultado aqui e anexado como enriquecimento explicativo
 * as {@link com.newstrust.domain.model.ScoreReason} deterministicas, nunca as substitui.
 */
public interface LlmExplanationPort {

    List<String> explain(NewsContentSubmission submission, CredibilityScore deterministicScore,
                          List<SimilarNewsMatch> similarNews);
}
