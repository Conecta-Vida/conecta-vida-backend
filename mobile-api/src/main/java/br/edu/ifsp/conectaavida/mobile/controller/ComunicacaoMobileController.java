package br.edu.ifsp.conectaavida.mobile.controller;

import java.util.List;

import br.edu.ifsp.conectaavida.core.domain.Comunicacao;
import br.edu.ifsp.conectaavida.core.repository.ComunicacaoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comunicacoes")
public class ComunicacaoMobileController {

    private final ComunicacaoRepository comunicacaoRepository;

    public ComunicacaoMobileController(ComunicacaoRepository comunicacaoRepository) {
        this.comunicacaoRepository = comunicacaoRepository;
    }

    @GetMapping
    public ResponseEntity<List<Comunicacao>> listar() {
        return ResponseEntity.ok(comunicacaoRepository.findAll());
    }
}
