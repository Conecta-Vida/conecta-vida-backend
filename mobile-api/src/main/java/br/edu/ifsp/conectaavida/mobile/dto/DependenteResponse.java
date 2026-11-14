package br.edu.ifsp.conectaavida.mobile.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DependenteResponse(
        Long id,
        String nomeCompleto,
        LocalDate dataNascimento,
        String parentesco,
        String sexo,
        LocalDateTime criadoEm
) {
}
