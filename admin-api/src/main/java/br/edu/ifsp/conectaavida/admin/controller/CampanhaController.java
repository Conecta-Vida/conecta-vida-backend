package br.edu.ifsp.conectaavida.admin.controller;

import br.edu.ifsp.conectaavida.admin.service.CampanhaService;
import br.edu.ifsp.conectaavida.core.domain.Comunicacao;
import br.edu.ifsp.conectaavida.core.repository.ComunicacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * CONTROLLER DE CAMPANHAS DE SAÚDE (Mutirões)
 */
@RestController
@RequestMapping("/api/campanhas")
public class CampanhaController {

    @Autowired
    private ComunicacaoRepository comunicacaoRepository;

    @Autowired
    private CampanhaService campanhaService;

    // Retorna a lista de todas as campanhas ativas ou passadas
    @GetMapping
    public ResponseEntity<List<Comunicacao>> listarCampanhas() {
        List<Comunicacao> campanhas = comunicacaoRepository.findByTipo("Campanha");
        return ResponseEntity.ok(campanhas);
    }

    // Cria uma nova campanha no painel
    @PostMapping
    public ResponseEntity<Comunicacao> criarCampanha(@RequestBody Comunicacao campanha) {
        campanha.setTipo("Campanha");
        campanha.setDataPostada(LocalDateTime.now());
        Comunicacao salva = comunicacaoRepository.save(campanha);
        return ResponseEntity.ok(salva);
    }

    /**
     * @PathVariable: Pega o número que vier na URL (ex: /api/campanhas/5/encerrar)
     * e joga para dentro da variável "id".
     */
    @PostMapping("/{id}/encerrar")
    public ResponseEntity<String> encerrarCampanha(@PathVariable Long id) {
        campanhaService.encerrarCampanha(id);
        return ResponseEntity.ok("Campanha encerrada com sucesso!");
    }
}