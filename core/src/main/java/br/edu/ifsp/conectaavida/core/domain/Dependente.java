package br.edu.ifsp.conectaavida.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "dependentes", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Dependente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_titular_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_dependente_usuario"))
    private Usuario usuarioTitular;

    @Column(name = "nome_completo", nullable = false, length = 150)
    private String nomeCompleto;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(nullable = false, length = 50)
    private String parentesco;

    @Column(length = 20)
    private String sexo;

    @Column(name = "criado_em", nullable = false, insertable = false, updatable = false)
    private LocalDateTime criadoEm;

    /**
     * Exclusão lógica: inscrições anteriores continuam associadas ao participante.
     */
    @Column(nullable = false)
    private Boolean ativo = true;
}
