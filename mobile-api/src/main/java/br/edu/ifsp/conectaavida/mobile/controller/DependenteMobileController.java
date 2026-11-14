package br.edu.ifsp.conectaavida.mobile.controller;

import br.edu.ifsp.conectaavida.core.domain.Dependente;
import br.edu.ifsp.conectaavida.core.domain.Usuario;
import br.edu.ifsp.conectaavida.core.repository.DependenteRepository;
import br.edu.ifsp.conectaavida.core.repository.UsuarioRepository;
import br.edu.ifsp.conectaavida.mobile.dto.DependenteRequest;
import br.edu.ifsp.conectaavida.mobile.dto.DependenteResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios/{usuarioId}/dependentes")
public class DependenteMobileController {

    private final DependenteRepository dependenteRepository;
    private final UsuarioRepository usuarioRepository;

    public DependenteMobileController(
            DependenteRepository dependenteRepository,
            UsuarioRepository usuarioRepository) {
        this.dependenteRepository = dependenteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> listar(
            @PathVariable Long usuarioId,
            @AuthenticationPrincipal String authenticatedUserId) {
        if (!titularAutenticado(usuarioId, authenticatedUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("mensagem", "Acesso negado."));
        }

        List<DependenteResponse> dependentes = dependenteRepository
                .findByUsuarioTitularIdAndAtivoTrueOrderByNomeCompletoAsc(usuarioId)
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(dependentes);
    }

    @GetMapping("/{dependenteId}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> buscar(
            @PathVariable Long usuarioId,
            @PathVariable Long dependenteId,
            @AuthenticationPrincipal String authenticatedUserId) {
        if (!titularAutenticado(usuarioId, authenticatedUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("mensagem", "Acesso negado."));
        }

        return dependenteRepository.findByIdAndUsuarioTitularIdAndAtivoTrue(dependenteId, usuarioId)
                .<ResponseEntity<?>>map(dependente -> ResponseEntity.ok(toResponse(dependente)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> criar(
            @PathVariable Long usuarioId,
            @Valid @RequestBody DependenteRequest request,
            @AuthenticationPrincipal String authenticatedUserId) {
        if (!titularAutenticado(usuarioId, authenticatedUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("mensagem", "Acesso negado."));
        }

        Usuario titular = usuarioRepository.findById(usuarioId).orElse(null);
        if (titular == null) return ResponseEntity.notFound().build();

        Dependente dependente = new Dependente();
        dependente.setUsuarioTitular(titular);
        aplicarRequest(dependente, request);
        dependente.setAtivo(true);

        Dependente salvo = dependenteRepository.save(dependente);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(salvo));
    }

    @PutMapping("/{dependenteId}")
    @Transactional
    public ResponseEntity<?> atualizar(
            @PathVariable Long usuarioId,
            @PathVariable Long dependenteId,
            @Valid @RequestBody DependenteRequest request,
            @AuthenticationPrincipal String authenticatedUserId) {
        if (!titularAutenticado(usuarioId, authenticatedUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("mensagem", "Acesso negado."));
        }

        Dependente dependente = dependenteRepository
                .findByIdAndUsuarioTitularIdAndAtivoTrue(dependenteId, usuarioId)
                .orElse(null);
        if (dependente == null) return ResponseEntity.notFound().build();

        aplicarRequest(dependente, request);
        return ResponseEntity.ok(toResponse(dependenteRepository.save(dependente)));
    }

    /**
     * Exclusão lógica para manter o histórico de inscrições/eventos do participante.
     */
    @DeleteMapping("/{dependenteId}")
    @Transactional
    public ResponseEntity<?> excluir(
            @PathVariable Long usuarioId,
            @PathVariable Long dependenteId,
            @AuthenticationPrincipal String authenticatedUserId) {
        if (!titularAutenticado(usuarioId, authenticatedUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("mensagem", "Acesso negado."));
        }

        Dependente dependente = dependenteRepository
                .findByIdAndUsuarioTitularIdAndAtivoTrue(dependenteId, usuarioId)
                .orElse(null);
        if (dependente == null) return ResponseEntity.notFound().build();

        dependente.setAtivo(false);
        dependenteRepository.save(dependente);
        return ResponseEntity.noContent().build();
    }

    private boolean titularAutenticado(Long usuarioId, String authenticatedUserId) {
        if (authenticatedUserId == null) return false;
        try {
            return usuarioId.equals(Long.valueOf(authenticatedUserId));
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private void aplicarRequest(Dependente dependente, DependenteRequest request) {
        dependente.setNomeCompleto(request.nomeCompleto().trim());
        dependente.setDataNascimento(request.dataNascimento());
        dependente.setParentesco(request.parentesco().trim());
        String sexo = request.sexo() == null ? null : request.sexo().trim();
        dependente.setSexo(sexo == null || sexo.isBlank() ? null : sexo);
    }

    private DependenteResponse toResponse(Dependente dependente) {
        return new DependenteResponse(
                dependente.getId(),
                dependente.getNomeCompleto(),
                dependente.getDataNascimento(),
                dependente.getParentesco(),
                dependente.getSexo(),
                dependente.getCriadoEm()
        );
    }
}
