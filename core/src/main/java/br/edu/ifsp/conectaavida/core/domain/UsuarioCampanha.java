package br.edu.ifsp.conectaavida.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios_campanhas", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioCampanha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_usuarios_campanhas_usuario_id"))
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comunicacao_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_usuarios_campanhas_comunicacao_id"))
    private Comunicacao comunicacao;

    /**
     * Participante da inscrição. Nulo significa que o próprio titular participará.
     * A API valida que o dependente pertence ao titular autenticado.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dependente_id",
            foreignKey = @ForeignKey(name = "fk_usuarios_campanhas_dependente_id"))
    private Dependente dependente;

    @Column(name = "data_inscricao", nullable = false, insertable = false, updatable = false)
    private LocalDateTime dataInscricao;

    @Column(name = "data_notificacao_fim")
    private LocalDateTime dataNotificacaoFim;

    @Column(name = "notificacao_lida", nullable = false)
    private Boolean notificacaoLida = false;

    @Column(name = "criado_em", nullable = false, insertable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", insertable = false, updatable = false)
    private LocalDateTime atualizadoEm;
}
