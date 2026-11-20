package br.com.psoft.arena.dtos.estudante;

import br.com.psoft.arena.models.estudante.EstudanteModel;

public record EstudanteResponseDTO(
    Long id,
    String nome,
    String matricula,
    String email
) {

    public static EstudanteResponseDTO from(EstudanteModel estudante) {
        return new EstudanteResponseDTO(
            estudante.getId(),
            estudante.getNome(),
            estudante.getMatricula(),
            estudante.getEmail()
        );
    }
}