package com.finnza.controller;

import com.finnza.domain.entity.Cobranca;
import com.finnza.domain.entity.Contrato;
import com.finnza.repository.CobrancaRepository;
import com.finnza.repository.ContratoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

/**
 * Controller para receber webhooks do Asaas
 */
@Slf4j
@RestController
@RequestMapping("/api/webhooks/asaas")
@CrossOrigin(origins = "*")
public class AsaasWebhookController {

    @Autowired
    private CobrancaRepository cobrancaRepository;

    @Autowired
    private ContratoRepository contratoRepository;

    /**
     * Endpoint para receber notificações do Asaas
     * O Asaas envia POST quando há mudanças em pagamentos/assinaturas
     */
    @PostMapping
    public ResponseEntity<Void> receberWebhook(@RequestBody Map<String, Object> payload) {
        try {
            log.info("Webhook recebido do Asaas: {}", payload);

            String event = (String) payload.get("event");
            log.info("Evento do webhook: {}", event);

            // Processar eventos de pagamento
            @SuppressWarnings("unchecked")
            Map<String, Object> payment = (Map<String, Object>) payload.get("payment");
            if (payment != null) {
                processarEventoPagamento(payment, event);
            }

            // Processar eventos de assinatura
            @SuppressWarnings("unchecked")
            Map<String, Object> subscription = (Map<String, Object>) payload.get("subscription");
            if (subscription != null) {
                processarEventoAssinatura(subscription, event);
            }

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erro ao processar webhook do Asaas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Processa eventos relacionados a pagamentos
     */
    private void processarEventoPagamento(Map<String, Object> payment, String event) {
        String paymentId = (String) payment.get("id");
        String status = (String) payment.get("status");

        log.info("Processando evento de pagamento: {} - Status: {}", paymentId, status);

        // Buscar cobrança pelo ID do Asaas
        Optional<Cobranca> cobrancaOpt = cobrancaRepository.findByAsaasPaymentId(paymentId);

        if (cobrancaOpt.isPresent()) {
            Cobranca cobranca = cobrancaOpt.get();
            atualizarStatusCobranca(cobranca, status, payment);
        } else {
            log.warn("Cobrança não encontrada para paymentId: {}", paymentId);
        }
    }

    /**
     * Processa eventos relacionados a assinaturas
     */
    private void processarEventoAssinatura(Map<String, Object> subscription, String event) {
        String subscriptionId = (String) subscription.get("id");
        String status = (String) subscription.get("status");

        log.info("Processando evento de assinatura: {} - Status: {}", subscriptionId, status);

        // Buscar contrato pela assinatura do Asaas
        Optional<Contrato> contratoOpt = contratoRepository.findByAsaasSubscriptionId(subscriptionId);

        if (contratoOpt.isPresent()) {
            Contrato contrato = contratoOpt.get();
            atualizarStatusAssinatura(contrato, status, event);
        } else {
            log.warn("Contrato não encontrado para subscriptionId: {}", subscriptionId);
        }
    }

    /**
     * Atualiza status da assinatura baseado no webhook
     */
    private void atualizarStatusAssinatura(Contrato contrato, String statusAsaas, String event) {
        try {
            // Se a assinatura foi cancelada, atualizar contrato
            if ("SUBSCRIPTION_DELETED".equals(event) || "CANCELED".equals(statusAsaas)) {
                contrato.setStatus(Contrato.StatusContrato.CANCELADO);
                contratoRepository.save(contrato);
                log.info("Contrato {} cancelado devido ao cancelamento da assinatura", contrato.getId());
            } 
            // Se a assinatura está ativa e o contrato está pendente, atualizar para ASSINADO
            else if ("ACTIVE".equals(statusAsaas) && contrato.getStatus() == Contrato.StatusContrato.PENDENTE) {
                contrato.setStatus(Contrato.StatusContrato.ASSINADO);
                contratoRepository.save(contrato);
                log.info("Contrato {} atualizado para ASSINADO devido à ativação da assinatura", contrato.getId());
            }
            // Se a assinatura está com pagamento em atraso, atualizar para VENCIDO
            else if ("OVERDUE".equals(statusAsaas)) {
                contrato.setStatus(Contrato.StatusContrato.VENCIDO);
                contratoRepository.save(contrato);
                log.info("Contrato {} atualizado para VENCIDO devido ao atraso na assinatura", contrato.getId());
            }
            // Recalcular status baseado nas cobranças se houver
            else {
                contrato.calcularStatusBaseadoNasCobrancas();
                contratoRepository.save(contrato);
            }
        } catch (Exception e) {
            log.error("Erro ao atualizar status da assinatura do contrato {}", contrato.getId(), e);
        }
    }

    /**
     * Atualiza status da cobrança baseado no webhook
     */
    private void atualizarStatusCobranca(Cobranca cobranca, String statusAsaas, Map<String, Object> payment) {
        try {
            // Mapear status do Asaas para nosso enum
            Cobranca.StatusCobranca status = mapearStatus(statusAsaas);
            cobranca.setStatus(status);

            // Se foi pago, atualizar data de pagamento
            if (status == Cobranca.StatusCobranca.RECEIVED || 
                status == Cobranca.StatusCobranca.RECEIVED_IN_CASH_UNDONE ||
                status == Cobranca.StatusCobranca.DUNNING_RECEIVED) {
                
                Object paymentDate = payment.get("paymentDate");
                if (paymentDate != null) {
                    String dateStr = paymentDate.toString();
                    if (dateStr.length() >= 10) {
                        cobranca.setDataPagamento(LocalDate.parse(dateStr.substring(0, 10), DateTimeFormatter.ISO_DATE));
                    }
                } else {
                    cobranca.setDataPagamento(LocalDate.now());
                }
            }

            // Atualizar status do contrato baseado em todas as cobranças
            Contrato contrato = cobranca.getContrato();
            if (contrato != null) {
                // Recarregar contrato com todas as cobranças para ter a lista completa
                contrato = contratoRepository.findById(contrato.getId())
                        .orElse(null);
                if (contrato != null) {
                    // Se a cobrança ficou OVERDUE, atualizar imediatamente para VENCIDO
                    if (status == Cobranca.StatusCobranca.OVERDUE || 
                        status == Cobranca.StatusCobranca.DUNNING_REQUESTED ||
                        status == Cobranca.StatusCobranca.CHARGEBACK_REQUESTED) {
                        contrato.setStatus(Contrato.StatusContrato.VENCIDO);
                        log.info("Contrato {} atualizado para VENCIDO devido à cobrança {}", contrato.getId(), cobranca.getId());
                    } else {
                        // Caso contrário, recalcular baseado em todas as cobranças
                        contrato.calcularStatusBaseadoNasCobrancas();
                    }
                    contratoRepository.save(contrato);
                    log.info("Status do contrato {} atualizado para: {}", contrato.getId(), contrato.getStatus());
                }
            }

            cobrancaRepository.save(cobranca);
            log.info("Cobrança {} atualizada para status: {}", cobranca.getId(), status);

        } catch (Exception e) {
            log.error("Erro ao atualizar status da cobrança {}", cobranca.getId(), e);
        }
    }

    /**
     * Mapeia status do Asaas para nosso enum
     */
    private Cobranca.StatusCobranca mapearStatus(String statusAsaas) {
        if (statusAsaas == null) {
            return Cobranca.StatusCobranca.PENDING;
        }

        try {
            return Cobranca.StatusCobranca.valueOf(statusAsaas);
        } catch (IllegalArgumentException e) {
            log.warn("Status desconhecido do Asaas: {}", statusAsaas);
            return Cobranca.StatusCobranca.PENDING;
        }
    }
}

