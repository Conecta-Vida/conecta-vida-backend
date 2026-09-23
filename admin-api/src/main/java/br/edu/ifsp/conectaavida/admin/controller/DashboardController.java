package br.edu.ifsp.conectaavida.admin.controller;

import br.edu.ifsp.conectaavida.core.dto.DashboardStatsDTO;
import br.edu.ifsp.conectaavida.core.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * CONTROLLER DO DASHBOARD
 *
 * Retorna os dados estatísticos que alimentam a tela inicial do Painel Administrativo.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * @GetMapping avisa que esta é uma rota de LEITURA (acessada pelo navegador/React).
     * Delega a busca para o DashboardService, onde o Redis gerencia o cache em alta performance.
     */
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        DashboardStatsDTO stats = dashboardService.getStats();
        return ResponseEntity.ok(stats); // Retorna um HTTP 200 OK com o JSON limpo vindo do serviço
    }
}