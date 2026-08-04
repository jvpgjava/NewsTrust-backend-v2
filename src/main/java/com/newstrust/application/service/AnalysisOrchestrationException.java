package com.newstrust.application.service;

/**
 * Falha inesperada ao orquestrar as chamadas concorrentes de um caso de uso
 * (ex: interrupcao de uma virtual thread aguardando uma porta de saida).
 */
public class AnalysisOrchestrationException extends RuntimeException {

    public AnalysisOrchestrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
