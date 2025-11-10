package com.finnza.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO para atualização de permissões
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtualizarPermissoesRequest {

    @NotNull(message = "Permissões são obrigatórias")
    private Map<String, Boolean> permissions;
}

