package br.edu.ifsp.conectaavida.mobile.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import br.edu.ifsp.conectaavida.core.domain.Comunicacao;
import br.edu.ifsp.conectaavida.core.domain.Dependente;
import br.edu.ifsp.conectaavida.core.domain.Usuario;
import br.edu.ifsp.conectaavida.core.domain.UsuarioCampanha;
import br.edu.ifsp.conectaavida.core.repository.ComunicacaoRepository;
import br.edu.ifsp.conectaavida.core.repository.DependenteRepository;
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
    private final DependenteRepository dependenteRepository;
    private final UsuarioCampanhaRepository inscricaoRepository;

    public CampanhaMobileController(
            ComunicacaoRepository comunicacaoRepository,
            UsuarioRepository usuarioRepository,
            DependenteRepository dependenteRepository,
            UsuarioCampanhaRepository inscricaoRepository) {
        this.comunicacaoRepository = comunicacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.dependenteRepository = dependenteRepository;
        this.inscricaoRepository = inscricaoRepository;
    }

    /**
     * Lista as inscrições feitas pela conta autenticada.
     * dependente_id nulo significa inscrição do próprio titular.
     */
    @GetMapping("/minhas")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> minhasInscricoes(
            @AuthenticationPrincipal String authenticatedUserId) {
        Long usuarioId = Long.valueOf(authenticatedUserId);

        List<Map<String, Object>> inscricoes = inscricaoRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(this::toInscricaoResponse)
                .toList();

        return ResponseEntity.ok(inscricoes);
    }

    /**
     * Sem dependente_id, preserva o comportamento anterior (inscrição do titular).
     * Com dependente_id, valida que o dependente pertence ao usuário autenticado.
     */
    @PostMapping("/{comunicacaoId}/inscrever")
    @Transactional
    public ResponseEntity<?> inscrever(
            @PathVariable Long comunicacaoId,
            @RequestBody(required = false) Map<String, Object> payload,
            @AuthenticationPrincipal String authenticatedUserId) {
        Long usuarioId = Long.valueOf(authenticatedUserId);
        Long dependenteId;
        try {
            dependenteId = parseDependenteId(payload);
        } catch (NumberFormatException exception) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", "dependente_id inválido."));
        }

        Optional<Comunicacao> comunicacaoOpt = comunicacaoRepository.findById(comunicacaoId);
        if (comunicacaoOpt.isEmpty()) return ResponseEntity.notFound().build();

        Comunicacao comunicacao = comunicacaoOpt.get();
        if (!"Campanha".equalsIgnoreCase(comunicacao.getTipo())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensagem", "A comunicação não é uma campanha."));
        }

        Usuario titular = usuarioRepository.findById(usuarioId).orElse(null);
        if (titular == null) return ResponseEntity.status(401).build();

        Dependente dependente = null;
        if (dependenteId != null) {
            dependente = dependenteRepository
                    .findByIdAndUsuarioTitularIdAndAtivoTrue(dependenteId, usuarioId)
                    .orElse(null);
            if (dependente == null) {
                return ResponseEntity.status(404)
                        .body(Map.of("mensagem", "Dependente não encontrado ou inativo."));
            }
        }

        Optional<UsuarioCampanha> existente = dependente == null
                ? inscricaoRepository.findByUsuarioIdAndComunicacaoIdAndDependenteIsNull(usuarioId, comunicacaoId)
                : inscricaoRepository.findByUsuarioIdAndComunicacaoIdAndDependente_Id(
                        usuarioId, comunicacaoId, dependenteId);

        if (existente.isEmpty()) {
            UsuarioCampanha inscricao = new UsuarioCampanha();
            inscricao.setUsuario(titular);
            inscricao.setComunicacao(comunicacao);
            inscricao.setDependente(dependente);
            inscricao.setNotificacaoLida(false);
            inscricaoRepository.save(inscricao);
        }

        return ResponseEntity.ok(Map.of("mensagem", "Inscrição realizada."));
    }

    /**
     * dependente_id ausente/nulo cancela somente a inscrição do titular.
     */
    @DeleteMapping("/{comunicacaoId}/desinscrever")
    @Transactional
    public ResponseEntity<Void> desinscrever(
            @PathVariable Long comunicacaoId,
            @RequestParam(name = "dependente_id", required = false) Long dependenteId,
            @AuthenticationPrincipal String authenticatedUserId) {
        Long usuarioId = Long.valueOf(authenticatedUserId);

        if (dependenteId == null) {
            inscricaoRepository.findByUsuarioIdAndComunicacaoIdAndDependenteIsNull(usuarioId, comunicacaoId)
                    .ifPresent(inscricaoRepository::delete);
        } else {
            boolean pertenceAoTitular = dependenteRepository
                    .findByIdAndUsuarioTitularId(dependenteId, usuarioId)
                    .isPresent();
            if (!pertenceAoTitular) return ResponseEntity.notFound().build();

            inscricaoRepository.findByUsuarioIdAndComunicacaoIdAndDependente_Id(
                    usuarioId, comunicacaoId, dependenteId)
                    .ifPresent(inscricaoRepository::delete);
        }

        return ResponseEntity.noContent().build();
    }

    /**
     * Retorna apenas inscrições da própria conta, sem expor a lista de outros cidadãos.
     */
    @GetMapping("/{comunicacaoId}/inscritos")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> buscarMinhasInscricoesNaCampanha(
            @PathVariable Long comunicacaoId,
            @AuthenticationPrincipal String authenticatedUserId) {
        Long usuarioId = Long.valueOf(authenticatedUserId);

        List<Map<String, Object>> inscricoes = inscricaoRepository
                .findByUsuarioIdAndComunicacaoId(usuarioId, comunicacaoId)
                .stream()
                .map(this::toInscricaoResponse)
                .toList();

        return ResponseEntity.ok(inscricoes);
    }

    private Long parseDependenteId(Map<String, Object> payload) {
        if (payload == null || payload.get("dependente_id") == null) return null;
        Object raw = payload.get("dependente_id");
        if (raw instanceof Number number) return number.longValue();
        return Long.valueOf(raw.toString());
    }

    private Map<String, Object> toInscricaoResponse(UsuarioCampanha inscricao) {
        Map<String, Object> response = new LinkedHashMap<>();
        Dependente dependente = inscricao.getDependente();
        response.put("inscricao_id", inscricao.getId());
        response.put("comunicacao_id", inscricao.getComunicacao().getId());
        response.put("dependente_id", dependente == null ? null : dependente.getId());
        response.put("participante_nome", dependente == null
                ? inscricao.getUsuario().getNome()
                : dependente.getNomeCompleto());
        response.put("parentesco", dependente == null ? "Titular" : dependente.getParentesco());
        return response;
    }
}
