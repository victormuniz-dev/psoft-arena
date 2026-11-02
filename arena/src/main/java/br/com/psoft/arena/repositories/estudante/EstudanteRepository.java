package br.com.psoft.arena.repositories.estudante;

import br.com.psoft.arena.models.estudante.EstudanteModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstudanteRepository extends JpaRepository<EstudanteModel, Long> {
    Optional<EstudanteModel> findByMatricula(String matricula);
}
