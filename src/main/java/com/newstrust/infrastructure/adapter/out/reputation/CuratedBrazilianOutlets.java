package com.newstrust.infrastructure.adapter.out.reputation;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.OptionalDouble;
import java.util.Set;

/**
 * Lista curada de veiculos jornalisticos brasileiros com registro editorial
 * publico consolidado (artigo, Secao 3.2) - complementa o ScamAdviser no
 * fator R. Carregada uma vez de {@code reputation/curated-brazilian-outlets.txt}
 * no classpath; nao depende de rede, entao nunca falha por indisponibilidade
 * externa.
 */
@Component
public class CuratedBrazilianOutlets {

    /** Reputacao atribuida a qualquer dominio presente na lista - "confiavel" por definicao. */
    public static final double CURATED_REPUTATION_SCORE = 90.0;

    private static final String RESOURCE_PATH = "reputation/curated-brazilian-outlets.txt";

    private final Set<String> domains;

    public CuratedBrazilianOutlets() {
        this.domains = loadDomains();
    }

    public OptionalDouble reputationFor(String domain) {
        if (domain == null) {
            return OptionalDouble.empty();
        }
        return domains.contains(domain.toLowerCase()) ? OptionalDouble.of(CURATED_REPUTATION_SCORE) : OptionalDouble.empty();
    }

    private static Set<String> loadDomains() {
        Set<String> result = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource(RESOURCE_PATH).getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                result.add(trimmed.toLowerCase());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Nao foi possivel carregar " + RESOURCE_PATH, e);
        }
        return result;
    }
}
