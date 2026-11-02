package br.com.psoft.arena.services.estudante;

import br.com.psoft.arena.dtos.estudante.EstudanteDTO;
import br.com.psoft.arena.models.estudante.EstudanteModel;
import br.com.psoft.arena.repositories.estudante.EstudanteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EstudanteService {

    @Autowired
    private EstudanteRepository estudanteRepository;

    public EstudanteModel criarEstudante(EstudanteDTO dto) {
        if (estudanteRepository.findByMatricula(dto.matricula()).isPresent()) {
            throw new IllegalArgumentException("Já existe um estudante cadastrado com esta matrícula.");
        }
        EstudanteModel estudante = new EstudanteModel(dto.nome(), dto.matricula(), dto.email());
        return estudanteRepository.save(estudante);
    }

    public List<EstudanteModel> listarEstudantes() {
        return estudanteRepository.findAll();
    }

    public EstudanteModel consultarPorMatricula(String matricula) {
        return estudanteRepository.findByMatricula(matricula)
                .orElseThrow(() -> new RuntimeException("Estudante não encontrado com a matrícula informada."));
    }

    public EstudanteModel atualizarEstudante(String matricula, EstudanteDTO dadosAtualizados) {
        EstudanteModel estudanteExistente = consultarPorMatricula(matricula);
        estudanteExistente.setNome(dadosAtualizados.nome());
        estudanteExistente.setEmail(dadosAtualizados.email());
        return estudanteRepository.save(estudanteExistente);
    }
}
