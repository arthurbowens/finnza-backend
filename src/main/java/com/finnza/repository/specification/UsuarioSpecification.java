package com.finnza.repository.specification;

import com.finnza.domain.entity.Usuario;
import com.finnza.dto.request.UsuarioFiltroRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Specifications para filtros dinâmicos de usuários
 */
public class UsuarioSpecification {

    /**
     * Cria specification baseada nos filtros
     */
    public static Specification<Usuario> comFiltros(UsuarioFiltroRequest filtros) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtro por nome (LIKE case-insensitive)
            if (filtros.getNome() != null && !filtros.getNome().trim().isEmpty()) {
                predicates.add(cb.like(
                    cb.lower(root.get("nome")),
                    "%" + filtros.getNome().toLowerCase() + "%"
                ));
            }

            // Filtro por email (LIKE case-insensitive)
            if (filtros.getEmail() != null && !filtros.getEmail().trim().isEmpty()) {
                predicates.add(cb.like(
                    cb.lower(root.get("email")),
                    "%" + filtros.getEmail().toLowerCase() + "%"
                ));
            }

            // Filtro por role
            if (filtros.getRole() != null) {
                predicates.add(cb.equal(root.get("role"), filtros.getRole()));
            }

            // Filtro por status
            if (filtros.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filtros.getStatus()));
            }

            // Filtro por soft delete
            if (filtros.getIncluirDeletados() == null || !filtros.getIncluirDeletados()) {
                // Por padrão, não inclui deletados
                predicates.add(
                    cb.or(
                        cb.isNull(root.get("deleted")),
                        cb.equal(root.get("deleted"), false)
                    )
                );
            }
            // Se incluirDeletados for true, não adiciona filtro de deleted

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

