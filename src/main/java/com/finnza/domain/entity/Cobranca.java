package com.finnza.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidade Cobranca
 * Representa uma cobrança/pagamento de um contrato
 */
@Entity
@Table(name = "cobrancas",
       indexes = {
           @Index(name = "idx_cobranca_contrato", columnList = "contrato_id"),
           @Index(name = "idx_cobranca_status", columnList = "status"),
           @Index(name = "idx_cobranca_data_vencimento", columnList = "dataVencimento"),
           @Index(name = "idx_cobranca_asaas_id", columnList = "asaasPaymentId"),
           @Index(name = "idx_cobranca_contrato_status", columnList = "contrato_id,status")
       })
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"contrato"})
public class Cobranca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id", nullable = false)
    private Contrato contrato;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDate dataVencimento;

    @Column
    private LocalDate dataPagamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private StatusCobranca status = StatusCobranca.PENDING;

    // ID da cobrança no Asaas
    @Column(name = "asaas_payment_id", length = 50, unique = true)
    private String asaasPaymentId;

    // Link de pagamento gerado pelo Asaas
    @Column(name = "link_pagamento", length = 500)
    private String linkPagamento;

    // Código de barras (se for boleto)
    @Column(name = "codigo_barras", length = 100)
    private String codigoBarras;

    // Número da parcela (se for recorrente)
    @Column(name = "numero_parcela")
    private Integer numeroParcela;

    @CreatedDate
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @LastModifiedDate
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    /**
     * Enum para status da cobrança (baseado no Asaas)
     */
    public enum StatusCobranca {
        PENDING,              // Aguardando pagamento
        RECEIVED,             // Recebido (pago)
        OVERDUE,              // Vencido
        REFUNDED,             // Estornado
        RECEIVED_IN_CASH_UNDONE, // Recebido em dinheiro (aguardando confirmação)
        CHARGEBACK_REQUESTED, // Chargeback solicitado
        CHARGEBACK_DISPUTE,   // Em disputa
        AWAITING_CHARGEBACK_REVERSAL, // Aguardando reversão
        DUNNING_REQUESTED,    // Em processo de cobrança
        DUNNING_RECEIVED,     // Recebido após cobrança
        AWAITING_RISK_ANALYSIS // Aguardando análise de risco
    }

    /**
     * Verifica se a cobrança está paga
     */
    public boolean isPaga() {
        return status == StatusCobranca.RECEIVED || 
               status == StatusCobranca.RECEIVED_IN_CASH_UNDONE ||
               status == StatusCobranca.DUNNING_RECEIVED;
    }

    /**
     * Verifica se a cobrança está vencida
     */
    public boolean isVencida() {
        return status == StatusCobranca.OVERDUE || 
               (status == StatusCobranca.PENDING && dataVencimento.isBefore(LocalDate.now()));
    }
}

