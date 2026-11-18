package br.com.psoft.arena.services.projessor;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.psoft.arena.exception.ResourceNotFoundException;
import br.com.psoft.arena.models.professor.ProfessorModel;
import br.com.psoft.arena.repositories.professor.ProfessorRepository;
import lombok.RequiredArgsConstructor;

/**
 * Service responsável pela lógica de negócio relacionada aos Professores.
 * Gerencia o cadastro, listagem, busca por ID e edição de professores no sistema.
 */
@Service
@RequiredArgsConstructor
public class ProfessorService {
    
    private final ProfessorRepository professorRepository;

    /**
     * Cria e persiste um novo professor no sistema com o nome informado.
     * 
     * @param nome O nome do professor a ser criado.
     * @return O {@link ProfessorModel} criado e salvo.
     */
    public ProfessorModel criar(String nome){
        return professorRepository.save(new ProfessorModel(nome.trim()));
    } 

    /**
     * Busca um professor pelo seu ID.
     * 
     * @param id O ID do professor procurado.
     * @return O {@link ProfessorModel} correspondente.
     * @throws ResourceNotFoundException se o professor não for encontrado.
     */
    public ProfessorModel buscarPorId(Long id){
        return professorRepository.findById(id).
            orElseThrow(() -> new ResourceNotFoundException(
                "Professor não encontrado: " + id));
    }

    /**
     * Lista todos os professores cadastrados no sistema.
     * 
     * @return Uma lista com todos os {@link ProfessorModel} encontrados.
     */
    public List<ProfessorModel> listar() {
        return professorRepository.findAll();
    }

    /**
     * Edita o nome de um professor existente.
     * 
     * @param id ID do professor a ser editado.
     * @param novoNome O novo nome para atualização.
     * @return O {@link ProfessorModel} atualizado.
     */
    public ProfessorModel editar(Long id, String novoNome) {
        ProfessorModel professor = buscarPorId(id);
        professor.setNome(novoNome.trim());
        return professor;
    }
}