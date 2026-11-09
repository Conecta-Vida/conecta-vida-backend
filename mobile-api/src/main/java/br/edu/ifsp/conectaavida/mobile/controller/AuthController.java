package br.edu.ifsp.conectaavida.mobile.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import br.edu.ifsp.conectaavida.core.domain.Usuario;
import br.edu.ifsp.conectaavida.core.repository.UsuarioRepository;
import br.edu.ifsp.conectaavida.core.security.PasswordEncryptionService;
import br.edu.ifsp.conectaavida.mobile.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncryptionService passwordEncryptionService;
    private final JwtService jwtService;

    public AuthController(
            UsuarioRepository usuarioRepository,
            PasswordEncryptionService passwordEncryptionService,
            JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncryptionService = passwordEncryptionService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registrar(@RequestBody Map<String, Object> payload) {
        String nome = texto(payload, "nome");
        String email = texto(payload, "email").toLowerCase();
        String senha = texto(payload, "senha");

        if (nome.isBlank() || email.isBlank() || senha.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of(
                    "mensagem", "Informe nome, e-mail e uma senha com pelo menos 6 caracteres."));
        }
        if (usuarioRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "mensagem", "Este e-mail já está cadastrado."));
        }

        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha(passwordEncryptionService.criptografarSenha(senha));
        usuario.setDataNascimento(numero(payload.get("data_nascimento")));
        usuario.setSexo(texto(payload, "sexo"));
        usuario.setLocalizacao(texto(payload, "localizacao"));
        usuario.setPermissao("Usuário Comum");

        Usuario salvo = usuarioRepository.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(dadosPublicos(salvo, null));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciais) {
        String email = credenciais.getOrDefault("email", "").trim().toLowerCase();
        String senha = credenciais.getOrDefault("senha", "");

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Credenciais inválidas."));
        }

        Usuario usuario = usuarioOpt.get();
        String hashAtual = passwordEncryptionService.criptografarSenha(senha);
        if (!hashAtual.equals(usuario.getSenha())) {
            // Permite contas antigas criadas antes da adoção do salt configurável.
            String hashLegado = passwordEncryptionService.criptografarSenhaLegada(senha);
            if (!hashLegado.equals(usuario.getSenha())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Credenciais inválidas."));
            }
            usuario.setSenha(hashAtual);
            usuarioRepository.save(usuario);
        }

        return ResponseEntity.ok(dadosPublicos(usuario, jwtService.gerarToken(usuario)));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> payload) {
        // O projeto ainda não possui fluxo de recuperação/validação por e-mail
        // no módulo mobile. Não retornar sucesso falso para uma operação não realizada.
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(Map.of(
                "mensagem", "A recuperação de senha por e-mail ainda não está configurada para o aplicativo."));
    }

    private Map<String, Object> dadosPublicos(Usuario usuario, String token) {
        Map<String, Object> resposta = new HashMap<>();
        resposta.put("id", usuario.getId());
        resposta.put("nome", usuario.getNome());
        resposta.put("email", usuario.getEmail());
        resposta.put("data_nascimento", usuario.getDataNascimento());
        resposta.put("sexo", usuario.getSexo());
        resposta.put("localizacao", usuario.getLocalizacao());
        if (token != null) resposta.put("token", token);
        return resposta;
    }

    private static String texto(Map<String, Object> payload, String chave) {
        Object valor = payload.get(chave);
        return valor == null ? "" : valor.toString().trim();
    }

    private static Integer numero(Object valor) {
        if (valor instanceof Number number) return number.intValue();
        if (valor == null) return null;
        try {
            return Integer.valueOf(valor.toString());
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
