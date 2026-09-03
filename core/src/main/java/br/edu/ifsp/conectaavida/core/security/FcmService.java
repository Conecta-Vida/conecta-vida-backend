package br.edu.ifsp.conectaavida.core.security;

import br.edu.ifsp.conectaavida.core.service.PushTokenRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.Set;

/**
 * SERVIÇO DE DISPARO DE NOTIFICAÇÕES (Firebase Cloud Messaging)
 *
 * Objetivo: Conectar nossa API com os servidores da Google para fazer
 * o celular do cidadão vibrar e exibir um Alerta Emergencial.
 */
@Service
public class FcmService {

    // Lê a Server Key (Chave do Google) do application.properties
    @Value("${fcm.server.key:COLOQUE_A_SERVER_KEY_AQUI}")
    private String serverKey;

    @Autowired
    private PushTokenRegistry tokenRegistry;

    private static final String FCM_URL = "https://fcm.googleapis.com/fcm/send";

    /**
     * Monta o pacote de dados (Payload) e dispara para todos os celulares da lista.
     */
    public void enviarParaTodos(String titulo, String corpo, Map<String, String> dados) {

        // Pega todos os celulares registrados na memória
        Set<String> tokens = tokenRegistry.obterTodosOsTokens();

        if (tokens.isEmpty()) {
            System.out.println("⚠️ FCM: Nenhum dispositivo registrado para receber push.");
            return;
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "key=" + serverKey);

        // Dispara a notificação um por um (Em escala nacional, isso seria movido para o RabbitMQ/Kafka)
        for (String token : tokens) {
            try {
                // Estrutura exigida pela API Legada do Google Firebase
                Map<String, Object> payload = Map.of(
                        "to", token,
                        "priority", "high",
                        "notification", Map.of(
                                "title", titulo,
                                "body", corpo,
                                "sound", "default"
                        ),
                        "data", dados != null ? dados : Map.of()
                );

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
                ResponseEntity<String> response = restTemplate.postForEntity(FCM_URL, request, String.class);

                System.out.println("✅ FCM enviado. Status: " + response.getStatusCode());
            } catch (Exception e) {
                System.err.println("❌ FCM: Erro ao enviar push: " + e.getMessage());
            }
        }
    }
}