package br.com.psoft.arena.services.problema;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.exception.ResourceNotFoundException;
import br.com.psoft.arena.models.problema.ProblemaModel;
import br.com.psoft.arena.repositories.atividade.AtividadeRepository;
import br.com.psoft.arena.repositories.problema.ProblemaRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProblemaService")
class ProblemaServiceTest {

    @Mock
    private ProblemaRepository problemaRepository;

    @Mock
    private AtividadeRepository atividadeRepository;

    @InjectMocks
    private ProblemaService service;

    private ProblemaModel problema() {
        return new ProblemaModel("P1", "Descrição", List.of("RF1"), List.of("RP1"));
    }

    @Test
    @DisplayName("remove o problema quando ele não está associado a nenhuma atividade")
    void removeProblemaLivre() {
        ProblemaModel problema = problema();
        when(problemaRepository.findById(10L)).thenReturn(Optional.of(problema));
        when(atividadeRepository.existsByProblemaId(10L)).thenReturn(false);

        service.remover(10L);

        verify(problemaRepository).delete(problema);
    }

    @Test
    @DisplayName("não remove problema associado a uma atividade")
    void naoRemoveProblemaAssociado() {
        ProblemaModel problema = problema();
        when(problemaRepository.findById(10L)).thenReturn(Optional.of(problema));
        when(atividadeRepository.existsByProblemaId(10L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> service.remover(10L));

        assertEquals("O problema está associado a uma atividade e não pode ser removido", ex.getMessage());
        verify(problemaRepository, never()).delete(problema);
    }

    @Test
    @DisplayName("lança exceção quando o problema não existe")
    void problemaNaoEncontrado() {
        when(problemaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.remover(99L));

        verifyNoInteractions(atividadeRepository);
        verify(problemaRepository, never()).delete(org.mockito.ArgumentMatchers.any());
    }
}
