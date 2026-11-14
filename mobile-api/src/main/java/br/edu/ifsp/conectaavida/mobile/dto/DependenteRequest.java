package br.edu.ifsp.conectaavida.mobile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record DependenteRequest(
        @NotBlank @Size(max = 150) String nomeCompleto,
        @NotNull @PastOrPresent LocalDate dataNascimento,
        @NotBlank @Size(max = 50) String parentesco,
        @Size(max = 20) String sexo
) {
}
