package com.newstrust.infrastructure.adapter.out.reputation;

import com.newstrust.domain.model.SourceReputation;
import com.newstrust.domain.port.out.SourceReputationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

/**
 * Consulta a reputacao de um dominio combinando duas fontes, como descrito no
 * artigo (Secao 3.2): o ScamAdviser (base ja usada pelo sistema hoje em
 * producao) e uma lista curada de veiculos jornalisticos brasileiros
 * consolidados ({@link CuratedBrazilianOutlets}). Estar na lista curada so
 * pode ELEVAR a reputacao (nunca reduzir abaixo do que o ScamAdviser jaz
 * indicar) - e um sinal editorial adicional, nao um substituto.
 * <p>
 * Qualquer falha do ScamAdviser (sem chave configurada, timeout, erro HTTP,
 * resposta inesperada) cai para um baseline neutro em vez de penalizar a fonte
 * sem evidencia - o mesmo principio de "ausencia de dado nao distorce o score"
 * aplicado ao fator D no dominio.
 */
@Component
public class ScamAdviserReputationAdapter implements SourceReputationPort {

    private static final Logger log = LoggerFactory.getLogger(ScamAdviserReputationAdapter.class);
    private static final String CURATED_LIST_SIGNAL =
            "Domínio consta na lista curada de veículos jornalísticos brasileiros consolidados.";

    private final RestClient restClient;
    private final String apiKey;
    private final CuratedBrazilianOutlets curatedOutlets;

    public ScamAdviserReputationAdapter(RestClient.Builder restClientBuilder,
                                         @Value("${newstrust.reputation.scamadviser.base-url}") String baseUrl,
                                         @Value("${newstrust.reputation.scamadviser.api-key:}") String apiKey,
                                         CuratedBrazilianOutlets curatedOutlets) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
        this.curatedOutlets = curatedOutlets;
    }

    @Override
    public SourceReputation lookup(String domain) {
        SourceReputation scamAdviserResult = lookupScamAdviser(domain);
        OptionalDouble curatedScore = curatedOutlets.reputationFor(domain);
        return blendWithCuratedList(scamAdviserResult, curatedScore);
    }

    /**
     * Pura, sem IO - separada de {@link #lookup(String)} para ser testavel sem
     * precisar mockar a cadeia fluente do {@link RestClient}.
     */
    static SourceReputation blendWithCuratedList(SourceReputation scamAdviserResult, OptionalDouble curatedScore) {
        if (curatedScore.isEmpty() || curatedScore.getAsDouble() <= scamAdviserResult.reputationScore()) {
            return scamAdviserResult;
        }

        double blended = curatedScore.getAsDouble();
        List<String> signals = new ArrayList<>(scamAdviserResult.signals());
        signals.add(0, CURATED_LIST_SIGNAL);
        return new SourceReputation(scamAdviserResult.domain(), blended, categoryFor(blended), signals);
    }

    private SourceReputation lookupScamAdviser(String domain) {
        if (apiKey == null || apiKey.isBlank()) {
            log.debug("Nenhuma chave da API do ScamAdviser configurada; usando baseline neutro para {}", domain);
            return SourceReputation.neutral(domain);
        }

        try {
            ScamAdviserResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/v2/trust/single").queryParam("url", domain).build())
                    .header("x-api-key", apiKey)
                    .retrieve()
                    .body(ScamAdviserResponse.class);

            if (response == null || response.trustScore() == null) {
                return SourceReputation.neutral(domain);
            }

            double score = Math.max(0.0, Math.min(100.0, response.trustScore()));
            List<String> signals = response.riskFactors() == null ? List.of() : response.riskFactors();
            return new SourceReputation(domain, score, categoryFor(score), signals);
        } catch (Exception e) {
            log.warn("Falha ao consultar reputacao no ScamAdviser para {}; usando baseline neutro", domain, e);
            return SourceReputation.neutral(domain);
        }
    }

    private static String categoryFor(double score) {
        if (score >= 80.0) {
            return "confiavel";
        }
        if (score >= 50.0) {
            return "moderada";
        }
        return "suspeita";
    }
}
