package br.edu.ifsp.conectaavida.core.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * ENTIDADE: Trilha de Auditoria (Logs)
 *
 * Explicação para a Equipe:
 * Esta classe é o "dedo-duro" do sistema. Cada ação feita por um administrador no celular
 * ou no painel web (como salvar uma notícia ou apagar um alerta) é registrada aqui[cite: 2].
 */
@Entity
@Table(name = "logs_atividade", schema = "public")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class LogAtividade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Vincula a ação diretamente a quem fez (Relacionamento com a tabela de Usuários)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private String acao;

    // insertable = false e updatable = false transferem a responsabilidade de colocar a hora para o PostgreSQL
    @Column(name = "data_hora", nullable = false, insertable = false, updatable = false)
    private LocalDateTime dataHora;
}