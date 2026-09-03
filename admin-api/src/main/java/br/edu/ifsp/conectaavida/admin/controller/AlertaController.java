package br.edu.ifsp.conectaavida.admin.controller;

import br.edu.ifsp.conectaavida.core.domain.Comunicacao;
import br.edu.ifsp.conectaavida.core.repository.ComunicacaoRepository;
import br.edu.ifsp.conectaavida.core.security.FcmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * CONTROLLER DE ALERTAS EPIDEMIOLÓGICOS
 */
@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    @Autowired
    private ComunicacaoRepository comunicacaoRepository;

    @Autowired
    private FcmService fcmService; // Importa o nosso disparador de Push Notifications

    /**
     * @PostMapping é usado para CRIAR novos dados.
     * Quando o gestor salva um Alerta, nós gravamos no banco e já vibramos o celular dos cidadãos.
     */
    @PostMapping
    public ResponseEntity<Comunicacao> criarAlerta(@RequestBody Comunicacao alerta) {
        alerta.setTipo("Alerta");
        alerta.setStatus("Ativo");
        alerta.setDataPostada(LocalDateTime.now());

        Comunicacao salvo = comunicacaoRepository.save(alerta);

        // INTEGRAÇÃO: Dispara a notificação Push para todos os celulares (FCM)
        fcmService.enviarParaTodos(
                "🚨 Alerta de Saúde: " + salvo.getTitulo(),
                salvo.getDescricao(),
                null
        );

        // Retorna HTTP 201 CREATED
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }
}