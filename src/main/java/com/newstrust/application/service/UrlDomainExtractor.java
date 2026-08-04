package com.newstrust.application.service;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Extrai o dominio (host, sem "www.") de uma URL. Utilitario tecnico de
 * orquestracao, nao regra de negocio - por isso vive na camada de aplicacao.
 */
final class UrlDomainExtractor {

    private UrlDomainExtractor() {
    }

    static String extract(String url) {
        try {
            URI uri = new URI(url.trim());
            String host = uri.getHost();
            if (host == null) {
                // URI sem esquema (ex: "example.com/path"); tenta novamente com https:// prefixado
                uri = new URI("https://" + url.trim());
                host = uri.getHost();
            }
            if (host == null) {
                throw new IllegalArgumentException("Nao foi possivel extrair o dominio da URL: " + url);
            }
            return host.startsWith("www.") ? host.substring(4) : host;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("URL invalida: " + url, e);
        }
    }
}
