package com.finnza.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidade Permissao
 * Representa as permissões específicas de um usuário
 */
@Entity
@Table(name = "permissoes",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"usuario_id", "modulo"})
       },
       indexes = {
           @Index(name = "idx_permissao_usuario", columnList = "usuario_id")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"usuario"})
public class Permissao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Modulo modulo;

    @Column(nullable = false)
    @Builder.Default
    private Boolean habilitado = true;

    /**
     * Enum para módulos do sistema
     */
    public enum Modulo {
        DASHBOARD,
        RELATORIO,
        MOVIMENTACOES,
        FLUXO_CAIXA,
        CONTRATOS,
        CHAT,
        ASSINATURA,
        GERENCIAR_ACESSOS
    }

    /**
     * Verifica se a permissão está habilitada
     */
    public boolean isHabilitada() {
        return Boolean.TRUE.equals(this.habilitado);
    }
}

