package com.finnza.repository;

import com.finnza.domain.entity.Permissao;
import com.finnza.domain.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para entidade Permissao
 */
@Repository
public interface PermissaoRepository extends JpaRepository<Permissao, Long> {

    /**
     * Busca todas as permissões de um usuário
     */
    List<Permissao> findByUsuario(Usuario usuario);

    /**
     * Busca permissão por usuário e módulo
     */
    Optional<Permissao> findByUsuarioAndModulo(Usuario usuario, Permissao.Modulo modulo);

    /**
     * Remove todas as permissões de um usuário
     */
    void deleteByUsuario(Usuario usuario);
}

