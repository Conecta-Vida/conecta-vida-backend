package br.edu.ifsp.conectaavida.admin.controller;

import br.edu.ifsp.conectaavida.admin.dto.DashboardStatsDTO;
import br.edu.ifsp.conectaavida.core.repository.ComunicacaoRepository;
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
    private ComunicacaoRepository comunicacaoRepository;

    /**
     * @GetMapping avisa que esta é uma rota de LEITURA (acessada pelo navegador/React).
     * Monta o nosso DTO com as contagens rápidas do banco de dados.
     */
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        long alertas = comunicacaoRepository.countByTipo("Alerta");
        long alertasAtivos = comunicacaoRepository.countByTipoAndStatus("Alerta", "Ativo");
        long campanhas = comunicacaoRepository.countByTipo("Campanha");
        long noticias = comunicacaoRepository.countByTipo("Noticia");

        DashboardStatsDTO stats = new DashboardStatsDTO(alertas, alertasAtivos, campanhas, noticias);

        return ResponseEntity.ok(stats); // Retorna um HTTP 200 OK com o JSON dentro
    }
}