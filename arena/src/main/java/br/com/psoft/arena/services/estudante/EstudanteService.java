package br.com.psoft.arena.services.estudante;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.psoft.arena.dtos.estudante.EstudanteRequestDTO;
import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.exception.ResourceNotFoundException;
import br.com.psoft.arena.models.estudante.EstudanteModel;
import br.com.psoft.arena.repositories.estudante.EstudanteRepository;

@Service
public class EstudanteService {

    private final EstudanteRepository repository;

    public EstudanteService(EstudanteRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public EstudanteModel criar(EstudanteRequestDTO dto) {
        if (repository.existsByMatricula(dto.matricula())) {
            throw new BusinessRuleException(
                "Já existe um estudante com a matrícula " + dto.matricula());
        }
        return repository.save(
            new EstudanteModel(dto.nome(), dto.matricula(), dto.email()));
    }

    @Transactional(readOnly = true)
    public EstudanteModel buscarPorId(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Estudante não encontrado com id " + id));
    }

    @Transactional
    public EstudanteModel editar(Long id, EstudanteRequestDTO dto) {
        EstudanteModel estudante = buscarPorId(id);

        // só verifica duplicidade se a matrícula mudou
        if (!estudante.getMatricula().equals(dto.matricula())
                && repository.existsByMatricula(dto.matricula())) {
            throw new BusinessRuleException(
                "Já existe um estudante com a matrícula " + dto.matricula());
        }

        estudante.setNome(dto.nome());
        estudante.setMatricula(dto.matricula());
        estudante.setEmail(dto.email());
        return repository.save(estudante);
    }

    @Transactional(readOnly = true)
    public List<EstudanteModel> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public EstudanteModel buscarPorMatricula(String matricula) {
        return repository.findByMatricula(matricula)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Estudante não encontrado com a matrícula " + matricula));
}
}