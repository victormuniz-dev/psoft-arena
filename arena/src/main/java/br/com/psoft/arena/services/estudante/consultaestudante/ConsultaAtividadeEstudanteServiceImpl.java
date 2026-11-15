package br.com.psoft.arena.services.estudante.consultaestudante;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import br.com.psoft.arena.dtos.atividade.atividadeEstudante.AtividadeEstudanteResponseDto;
import br.com.psoft.arena.dtos.turma.TurmaEstudante.TurmaEstudanteResponseDto;
import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.exception.ResourceNotFoundException;
import br.com.psoft.arena.models.atividade.AtividadeModel;
import br.com.psoft.arena.models.turma.TurmaModel;
import br.com.psoft.arena.repositories.atividade.AtividadeRepository;
import br.com.psoft.arena.repositories.estudante.EstudanteRepository;
import br.com.psoft.arena.repositories.turma.TurmaRepository;
import lombok.RequiredArgsConstructor;


/**
 * Implementação do serviço de consulta (somente leitura) de turmas e atividades
 * sob a perspectiva do estudante, garantindo o controle de acesso às turmas matriculadas (US7).
 */

@Service 
@RequiredArgsConstructor 
@Transactional (readOnly =  true)
public class ConsultaAtividadeEstudanteServiceImpl implements ConsultaAtividadeEstudanteService {

    private final AtividadeRepository atividadeRepository;
    private final EstudanteRepository estudanteRepository; 
    private final TurmaRepository turmaRepository; 


    @Override
     public List<TurmaEstudanteResponseDto> listarTurmas(Long estudanteId){
       validarEstudanteExiste(estudanteId); 

       return  turmaRepository.findAllByEstudanteId(estudanteId)
              .stream()
              .map(TurmaEstudanteResponseDto::fromEntity)
              .toList(); 
    }

    @Override
    public List<AtividadeEstudanteResponseDto> listarAtividadesPorTurma(Long estudanteId, Long turmaId) {
        
        validarEstudanteExiste(estudanteId); 
        TurmaModel turma = turmaRepository.findById(turmaId)
                    .orElseThrow(() ->  new BusinessRuleException("Estudante não particoipa da turma")); 
        
        if (!turma.contemEstudante(estudanteId)) {
            throw new BusinessRuleException("O estudante não participa desta turma"); 
        }

        return  atividadeRepository.findAllByTurmaId(turmaId)
                .stream()
                .map(AtividadeEstudanteResponseDto::fromEntity)
                .toList(); 

    }
    


    @Override
    public List<AtividadeEstudanteResponseDto> listarAtividades(Long estudanteId) {
        validarEstudanteExiste(estudanteId);

        return  atividadeRepository.findAllByEstudanteId(estudanteId)
                .stream()
                .map(AtividadeEstudanteResponseDto::fromEntity)
                .toList();
    }

    @Override
    public AtividadeEstudanteResponseDto buscarAtividade(Long estudanteId, Long atividadeId) {
       validarEstudanteExiste(estudanteId);

    AtividadeModel atividade = atividadeRepository.findById(atividadeId)
                .orElseThrow(() -> new ResourceNotFoundException("Atividade não encontrada com o ID: " + atividadeId));

    if (!atividade.getTurma().contemEstudante(estudanteId)) {
        throw new BusinessRuleException(("O estudante não participa da turma desta atividade."));
        
    }

    return  AtividadeEstudanteResponseDto.fromEntity(atividade); 
    }

    /**
     * Valida a existência de um estudante na base de dados.
     *
     * @param estudanteId Identificador único do estudante.
     * @throws ResourceNotFoundException caso o estudante não exista.
     */
     private void validarEstudanteExiste(Long estudanteId) {
        if (!estudanteRepository.existsById(estudanteId)) {
            throw new ResourceNotFoundException("Estudante não encontrado com O ID: " + estudanteId); 
        }
     }
}
