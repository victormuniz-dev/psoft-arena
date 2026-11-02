<<<<<<<< HEAD:arena/src/main/java/br/com/psoft/arena/services/estudante/EstudanteService.java
package br.com.psoft.arena.services.estudante;

import java.util.List;
========
package br.com.psoft.arena.service;

import br.com.psoft.arena.dto.EstudanteDTO;
import br.com.psoft.arena.model.Estudante;
import br.com.psoft.arena.repository.EstudanteRepository;
>>>>>>>> 1b553ec (feat(US3): implementa gestão de estratégias de avaliação e exceções customizadas):arena/src/main/java/br/com/psoft/arena/service/EstudanteService.java

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.psoft.arena.dtos.estudante.EstudanteDTO;
import br.com.psoft.arena.models.estudante.EstudanteModel;
import br.com.psoft.arena.repositories.estudante.EstudanteRepository;

@Service
public class EstudanteService {

    @Autowired
    private EstudanteRepository estudanteRepository;

    public Estudante criarEstudante(EstudanteDTO dto) {
        if (estudanteRepository.findByMatricula(dto.matricula()).isPresent()) {
            throw new IllegalArgumentException("Já existe um estudante cadastrado com esta matrícula.");
        }
        Estudante estudante = new Estudante(dto.nome(), dto.matricula(), dto.email());
        return estudanteRepository.save(estudante);
    }

    public List<Estudante> listarEstudantes() {
        return estudanteRepository.findAll();
    }

    public Estudante consultarPorMatricula(String matricula) {
        return estudanteRepository.findByMatricula(matricula)
                .orElseThrow(() -> new RuntimeException("Estudante não encontrado com a matrícula informada."));
    }

    public Estudante atualizarEstudante(String matricula, EstudanteDTO dadosAtualizados) {
        Estudante estudanteExistente = consultarPorMatricula(matricula);
        estudanteExistente.setNome(dadosAtualizados.nome());
        estudanteExistente.setEmail(dadosAtualizados.email());
        return estudanteRepository.save(estudanteExistente);
    }
}