package br.edu.ifsp.conectaavida.admin.controller;

import br.edu.ifsp.conectaavida.admin.service.RelatorioService;
import br.edu.ifsp.conectaavida.core.domain.LogAtividade;
import br.edu.ifsp.conectaavida.core.repository.LogAtividadeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * CONTROLLER DE RELATÓRIOS
 *
 * Explicação para a Equipe:
 * Diferente dos outros controllers que retornam JSON, este retorna um ARQUIVO PDF binário.
 */
@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    @Autowired
    private LogAtividadeRepository logRepository;

    @Autowired
    private RelatorioService relatorioService;

    @GetMapping("/auditoria/pdf")
    public ResponseEntity<byte[]> baixarRelatorioAuditoria() {

        // Pega as últimas ações do banco
        List<LogAtividade> logs = logRepository.findTop5ByOrderByDataHoraDesc();

        // Manda o serviço desenhar o PDF
        byte[] pdfBytes = relatorioService.gerarRelatorioAuditoriaPdf(logs);

        // Prepara o cabeçalho HTTP avisando o navegador do usuário que isso é um Download de Arquivo
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "auditoria.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}