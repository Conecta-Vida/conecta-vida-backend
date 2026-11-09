package br.edu.ifsp.conectaavida.mobile.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import br.edu.ifsp.conectaavida.core.domain.Comunicacao;
import br.edu.ifsp.conectaavida.core.domain.Usuario;
import br.edu.ifsp.conectaavida.core.domain.UsuarioCampanha;
import br.edu.ifsp.conectaavida.core.repository.ComunicacaoRepository;
import br.edu.ifsp.conectaavida.core.repository.UsuarioCampanhaRepository;
import br.edu.ifsp.conectaavida.core.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/campanhas")
public class CampanhaMobileController {

    private final ComunicacaoRepository comunicacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioCampanhaRepository inscricaoRepository;

    public CampanhaMobileController(
            ComunicacaoRepository comunicacaoRepository,
            UsuarioRepository usuarioRepository,
            UsuarioCampanhaRepository inscricaoRepository) {
        this.comunicacaoRepository = comunicacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.inscricaoRepository = inscricaoRepository;
    }

    @PostMapping("/{comunicacaoId}/inscrever")
    @Transactional
    public ResponseEntity<?> inscrever(
            @PathVariable Long comunicacaoId,
            @AuthenticationPrincipal String authenticatedUserId) {
        Long usuarioId = Long.valueOf(authenticatedUserId);
        Optional<Comunicacao> comunicacaoOpt = comunicacaoRepository.findById(comunicacaoId);
        if (comunicacaoOpt.isEmpty()) return ResponseEntity.notFound().build();

        Comunicacao comunicacao = comunicacaoOpt.get();
        if (!"Campanha".equalsIgnoreCase(comunicacao.getTipo())) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", "A comunicação não é uma campanha."));
        }

        if (inscricaoRepository.findByUsuarioIdAndComunicacaoId(usuarioId, comunicacaoId).isEmpty()) {
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
            if (usuarioOpt.isEmpty()) return ResponseEntity.status(401).build();

            UsuarioCampanha inscricao = new UsuarioCampanha();
            inscricao.setUsuario(usuarioOpt.get());
            inscricao.setComunicacao(comunicacao);
            inscricao.setNotificacaoLida(false);
            inscricaoRepository.save(inscricao);
        }
        return ResponseEntity.ok(Map.of("mensagem", "Inscrição realizada."));
    }

    @DeleteMapping("/{comunicacaoId}/desinscrever")
    @Transactional
    public ResponseEntity<Void> desinscrever(
            @PathVariable Long comunicacaoId,
            @AuthenticationPrincipal String authenticatedUserId) {
        Long usuarioId = Long.valueOf(authenticatedUserId);
        inscricaoRepository.findByUsuarioIdAndComunicacaoId(usuarioId, comunicacaoId)
                .ifPresent(inscricaoRepository::delete);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{comunicacaoId}/inscritos")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> buscarMinhaInscricao(
            @PathVariable Long comunicacaoId,
            @AuthenticationPrincipal String authenticatedUserId) {
        Long usuarioId = Long.valueOf(authenticatedUserId);
        List<Map<String, Object>> inscricoes = inscricaoRepository
                .findByUsuarioIdAndComunicacaoId(usuarioId, comunicacaoId)
                .map(inscricao -> List.of(Map.<String, Object>of(
                        "usuario_id", usuarioId,
                        "comunicacao_id", comunicacaoId)))
                .orElseGet(List::of);
        return ResponseEntity.ok(inscricoes);
    }
}
