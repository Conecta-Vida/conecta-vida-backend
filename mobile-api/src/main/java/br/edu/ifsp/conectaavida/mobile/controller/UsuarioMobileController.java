package br.edu.ifsp.conectaavida.mobile.controller;

import java.util.Map;
import java.util.Optional;

import br.edu.ifsp.conectaavida.core.domain.Usuario;
import br.edu.ifsp.conectaavida.core.repository.UsuarioRepository;
import br.edu.ifsp.conectaavida.core.security.PasswordEncryptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioMobileController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncryptionService passwordEncryptionService;

    public UsuarioMobileController(
            UsuarioRepository usuarioRepository,
            PasswordEncryptionService passwordEncryptionService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncryptionService = passwordEncryptionService;
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> buscarPorEmail(
            @PathVariable String email,
            @AuthenticationPrincipal String authenticatedUserId) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(Long.valueOf(authenticatedUserId));
        if (usuarioOpt.isEmpty()) return ResponseEntity.notFound().build();

        Usuario usuario = usuarioOpt.get();
        if (!usuario.getEmail().equalsIgnoreCase(email)) {
            return ResponseEntity.status(403).body(Map.of("mensagem", "Acesso negado."));
        }
        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> atualizar(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload,
            @AuthenticationPrincipal String authenticatedUserId) {
        if (!id.equals(Long.valueOf(authenticatedUserId))) {
            return ResponseEntity.status(403).body(Map.of("mensagem", "Acesso negado."));
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) return ResponseEntity.notFound().build();
        Usuario usuario = usuarioOpt.get();

        if (payload.get("nome") != null) usuario.setNome(payload.get("nome").toString().trim());
        if (payload.get("email") != null) {
            String novoEmail = payload.get("email").toString().trim().toLowerCase();
            if (!novoEmail.equalsIgnoreCase(usuario.getEmail())
                    && usuarioRepository.existsByEmail(novoEmail)) {
                return ResponseEntity.badRequest().body(Map.of("mensagem", "Este e-mail já está em uso."));
            }
            usuario.setEmail(novoEmail);
        }
        Object nascimento = payload.get("data_nascimento");
        if (nascimento instanceof Number number) usuario.setDataNascimento(number.intValue());
        if (payload.get("sexo") != null) usuario.setSexo(payload.get("sexo").toString());
        if (payload.get("localizacao") != null) usuario.setLocalizacao(payload.get("localizacao").toString());

        Object senha = payload.get("senha");
        if (senha != null && !senha.toString().isBlank()) {
            usuario.setSenha(passwordEncryptionService.criptografarSenha(senha.toString()));
        }

        usuarioRepository.save(usuario);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> excluir(
            @PathVariable Long id,
            @AuthenticationPrincipal String authenticatedUserId) {
        if (!id.equals(Long.valueOf(authenticatedUserId))) {
            return ResponseEntity.status(403).build();
        }
        if (!usuarioRepository.existsById(id)) return ResponseEntity.notFound().build();
        usuarioRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
