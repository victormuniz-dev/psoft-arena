package br.com.psoft.arena.repositories.estudante;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.psoft.arena.models.estudante.EstudanteModel;

@Repository
public interface EstudanteRepository extends JpaRepository<EstudanteModel, Long> {
    Optional<EstudanteModel> findByMatricula(String matricula);
}
