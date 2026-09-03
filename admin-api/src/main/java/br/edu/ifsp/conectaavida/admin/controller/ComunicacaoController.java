package br.edu.ifsp.conectaavida.admin.controller;

import br.edu.ifsp.conectaavida.core.repository.ComunicacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * CONTROLLER GENÉRICO DE COMUNICAÇÕES
 *
 * Explicação para a Equipe:
 * Usado para ações genéricas que servem tanto para Notícias quanto para Alertas e Campanhas
 * (Por exemplo: o botão de Excluir genérico).
 */
@RestController
@RequestMapping("/api/comunicacoes")
public class ComunicacaoController {

    @Autowired
    private ComunicacaoRepository comunicacaoRepository;

    // Rota DELETE: O React chama essa URL quando o admin clica no ícone de Lixeira
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarComunicacao(@PathVariable Long id) {
        if (!comunicacaoRepository.existsById(id)) {
            return ResponseEntity.notFound().build(); // Retorna Erro 404 se não achar
        }
        comunicacaoRepository.deleteById(id);
        return ResponseEntity.noContent().build(); // Retorna 204 No Content (Sucesso, sem corpo)
    }
}