package com.finnza.repository;

import com.finnza.domain.entity.Contrato;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository para Contrato
 */
@Repository
public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    /**
     * Lista contratos não deletados com paginação
     */
    @Query("SELECT c FROM Contrato c WHERE c.deleted = false")
    Page<Contrato> findAllNaoDeletados(Pageable pageable);

    /**
     * Busca contratos por cliente (não deletados)
     */
    @Query("SELECT c FROM Contrato c WHERE c.cliente.id = :clienteId AND c.deleted = false")
    Page<Contrato> findByClienteId(@Param("clienteId") Long clienteId, Pageable pageable);

    /**
     * Busca contratos por status (não deletados)
     */
    @Query("SELECT c FROM Contrato c WHERE c.status = :status AND c.deleted = false")
    Page<Contrato> findByStatus(@Param("status") Contrato.StatusContrato status, Pageable pageable);

    /**
     * Busca contratos vencidos
     */
    @Query("SELECT c FROM Contrato c WHERE c.dataVencimento < :data AND c.status NOT IN ('PAGO', 'CANCELADO') AND c.deleted = false")
    List<Contrato> findContratosVencidos(@Param("data") LocalDate data);

    /**
     * Busca contrato por ID do Asaas (subscription)
     */
    @Query("SELECT c FROM Contrato c WHERE c.asaasSubscriptionId = :asaasSubscriptionId AND c.deleted = false")
    Optional<Contrato> findByAsaasSubscriptionId(@Param("asaasSubscriptionId") String asaasSubscriptionId);

    /**
     * Busca contratos com filtros
     */
    @Query(value = "SELECT * FROM contratos c WHERE " +
           "(:clienteId IS NULL OR c.cliente_id = :clienteId) AND " +
           "(:status IS NULL OR c.status = CAST(:status AS VARCHAR)) AND " +
           "(:termo IS NULL OR :termo = '' OR c.titulo LIKE '%' || CAST(:termo AS VARCHAR) || '%') AND " +
           "c.deleted = false",
           countQuery = "SELECT COUNT(*) FROM contratos c WHERE " +
           "(:clienteId IS NULL OR c.cliente_id = :clienteId) AND " +
           "(:status IS NULL OR c.status = CAST(:status AS VARCHAR)) AND " +
           "(:termo IS NULL OR :termo = '' OR c.titulo LIKE '%' || CAST(:termo AS VARCHAR) || '%') AND " +
           "c.deleted = false",
           nativeQuery = true)
    Page<Contrato> buscarComFiltros(
            @Param("clienteId") Long clienteId,
            @Param("status") String status,
            @Param("termo") String termo,
            Pageable pageable);
}

