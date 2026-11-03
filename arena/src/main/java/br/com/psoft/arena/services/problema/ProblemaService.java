package br.com.psoft.arena.services.problema;

import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.psoft.arena.exception.ResourceNotFoundException;
import br.com.psoft.arena.models.problema.ProblemaModel;
import br.com.psoft.arena.repositories.problema.ProblemaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProblemaService {

    private final ProblemaRepository problemaRepository;

    public ProblemaModel criar(
        String titulo,
        String descricao,
        List<String> requisitosFuncionais,
        List<String> requisitosProjeto
    ) {
        ProblemaModel problema = new ProblemaModel(
            titulo.trim(),
            descricao.trim(),
            requisitosFuncionais,
            requisitosProjeto
        );

        return problemaRepository.save(problema);
    }

    public List<ProblemaModel> listar() {
        return problemaRepository.findAll();
    }

    public ProblemaModel buscarPorId(Long id) {
        return problemaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Problema", id
            ));
    }

    public ProblemaModel editar(
        Long id,
        String titulo,
        String descricao,
        List<String> requisitosFuncionais,
        List<String> requisitosProjeto
    ) {
        ProblemaModel problema = buscarPorId(id);

        problema.editar(
            titulo.trim(),
            descricao.trim(),
            requisitosFuncionais,
            requisitosProjeto
        );

        return problemaRepository.save(problema);
    }

    public ProblemaModel desativar(Long id) {
        ProblemaModel problema = buscarPorId(id);
        problema.desativar();

        return problemaRepository.save(problema);
    }

    public void remover(Long id) {
        // TODO US6: impedir exclusão se o problema estiver associado a alguma atividade.
        ProblemaModel problema = buscarPorId(id);
        problemaRepository.delete(problema);
    }

    public List<ProblemaModel> buscarPorIds(List<Long> ids) {
    List<ProblemaModel> problemas = problemaRepository.findAllById(ids);
    if (problemas.size() != new HashSet<>(ids).size()) {
        throw new ResourceNotFoundException("Algum dos problemas informados não foi encontrado");
    }
    return problemas;
}
}
