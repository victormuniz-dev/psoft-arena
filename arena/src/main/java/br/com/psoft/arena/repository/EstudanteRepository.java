<<<<<<<< HEAD:arena/src/main/java/br/com/psoft/arena/repositories/estudante/EstudanteRepository.java
package br.com.psoft.arena.repositories.estudante;
========
package br.com.psoft.arena.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
>>>>>>>> 1b553ec (feat(US3): implementa gestão de estratégias de avaliação e exceções customizadas):arena/src/main/java/br/com/psoft/arena/repository/EstudanteRepository.java

import br.com.psoft.arena.model.Estudante;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.psoft.arena.models.estudante.EstudanteModel;

@Repository
public interface EstudanteRepository extends JpaRepository<EstudanteModel, Long> {
    Optional<EstudanteModel> findByMatricula(String matricula);
}
