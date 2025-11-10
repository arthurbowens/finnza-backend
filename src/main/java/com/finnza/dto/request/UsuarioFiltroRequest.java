package com.finnza.dto.request;

import com.finnza.domain.entity.Usuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para filtros de busca de usuários
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioFiltroRequest {

    private String nome;
    private String email;
    private Usuario.Role role;
    private Usuario.StatusUsuario status;
    private Boolean incluirDeletados;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 10;

    @Builder.Default
    private String sortBy = "nome";

    @Builder.Default
    private String sortDirection = "ASC";
}

