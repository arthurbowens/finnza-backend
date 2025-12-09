package com.finnza.config;

import com.finnza.service.AsaasService;
import com.finnza.service.ContratoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Componente que importa automaticamente contratos do Asaas na inicialização da aplicação
 */
@Slf4j
@Component
public class AsaasImportacaoAutomatica {

    @Autowired
    private ContratoService contratoService;

    @Autowired
    private AsaasService asaasService;

    @Value("${asaas.importacao.automatica.enabled:true}")
    private boolean importacaoAutomaticaEnabled;

    /**
     * Importa contratos do Asaas automaticamente quando a aplicação estiver pronta
     * Roda apenas uma vez na inicialização
     */
    @EventListener(ApplicationReadyEvent.class)
    @Order(1) // Executa após outros listeners
    public void importarContratosDoAsaas() {
        // Verificar se a importação automática está habilitada
        if (!importacaoAutomaticaEnabled) {
            log.info("Importação automática de contratos desabilitada via configuração.");
            return;
        }

        // Verificar se está em modo mock
        if (asaasService.isMockEnabled()) {
            log.info("Modo mock ativado. Importação automática de contratos desabilitada.");
            return;
        }

        log.info("=== INICIANDO IMPORTAÇÃO AUTOMÁTICA DE CONTRATOS DO ASAAS ===");
        
        try {
            int contratosImportados = contratoService.importarContratosDoAsaas();
            
            if (contratosImportados > 0) {
                log.info("✓ Importação automática concluída: {} contratos importados do Asaas", contratosImportados);
            } else {
                log.info("✓ Importação automática concluída: Nenhum contrato novo para importar (todos já estão sincronizados)");
            }
        } catch (Exception e) {
            log.error("✗ Erro na importação automática de contratos do Asaas: {}", e.getMessage(), e);
            // Não lança exceção para não impedir a inicialização da aplicação
        }
        
        log.info("=== IMPORTAÇÃO AUTOMÁTICA FINALIZADA ===");
    }
}

