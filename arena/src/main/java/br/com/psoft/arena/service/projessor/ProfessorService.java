package br.com.psoft.arena.service.projessor;

import java.util.List;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import br.com.psoft.arena.exception.ResourceNotFoundException;
import br.com.psoft.arena.model.professor.ProfessorModel;
import br.com.psoft.arena.repository.professor.ProfessorRepository;

@Service
@RequiredArgsConstructor
public class ProfessorService {
    private final ProfessorRepository professorRepository;

    public ProfessorModel criar(String nome){
        return professorRepository.save(new ProfessorModel(nome.trim()));
    } 

    public ProfessorModel buscarPorId(Long id){
        return professorRepository.findById(id).
            orElseThrow(() -> new ResourceNotFoundException(
                "Professor não encontrado: " + id));
    }

    public List<ProfessorModel> listar() {
        return professorRepository.findAll();
    }

    public ProfessorModel editar(Long id, String novoNome) {
        ProfessorModel professor = buscarPorId(id);
        professor.setNome(novoNome.trim());
        return professor;
    }
}
