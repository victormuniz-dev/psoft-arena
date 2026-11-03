package br.com.psoft.arena.services.estrategia;

import java.util.List;

import br.com.psoft.arena.dtos.estrategia.EstrategiaAvaliacaoResponseDto;
import br.com.psoft.arena.dtos.estrategia.EstrategiaAvaliacaoDto;
import br.com.psoft.arena.dtos.estrategia.PromptUpdateDto;
import br.com.psoft.arena.models.estrategia.EstrategiaAvaliacaoModel;
import br.com.psoft.arena.models.estrategia.TipoEstrategia;

/**
 * Interface de serviço responsável por definir o contrato das operações 
 * de negócio referentes às estratégias de avaliação.
 */
public interface EstrategiaAvaliacaoService{

    /**
     * Cadastra uma nova estratégia de avaliação no sistema.
     *
     * @param dto Dados para criação da estratégia de avaliação.
     * @return DTO contendo as informações da estratégia criada.
     */
    EstrategiaAvaliacaoResponseDto cadastrar(EstrategiaAvaliacaoDto dto); 

    /**
     * Busca os detalhes de uma estratégia de avaliação pelo seu identificador único.
     *
     * @param id Identificador único da estratégia desejada.
     * @return DTO com os dados da estratégia encontrada.
     */
    EstrategiaAvaliacaoResponseDto buscarPorId(Long id); 

    /**
     * Retorna a lista completa de todas as estratégias de avaliação cadastradas.
     *
     * @return Lista de DTOs contendo todas as estratégias disponíveis.
     */
    List<EstrategiaAvaliacaoResponseDto> liestarTodos(); 

    /**
     * Filtra e retorna as estratégias de avaliação com base no seu tipo.
     *
     * @param tipo Tipo de estratégia a ser filtrado (ex: CODE_SMELL, PADRAO_DE_PROJETO).
     * @return Lista de DTOs correspondentes ao tipo informado.
     */
    List<EstrategiaAvaliacaoResponseDto> listarPorTipo(TipoEstrategia tipo); 

    /**
     * Atualiza os dados cadastrais de uma estratégia de avaliação existente.
     *
     * @param id Identificador único da estratégia a ser atualizada.
     * @param dto Objeto com os novos dados da estratégia.
     * @return DTO com os dados atualizados.
     */
    EstrategiaAvaliacaoResponseDto atualizar(long id, EstrategiaAvaliacaoDto dto);

/**
     * Atualiza especificamente o prompt de avaliação associado a uma estratégia.
     *
     * @param id Identificador da estratégia.
     * @param dto DTO contendo o novo texto de prompt de avaliação.
     * @return DTO da estratégia com o prompt atualizado.
     */
    EstrategiaAvaliacaoResponseDto atualizarPromt(Long id, PromptUpdateDto dto); 

    /**
     * Busca e retorna diretamente a entidade de domínio da estratégia pelo seu ID.
     * Útil para integrações internas entre serviços.
     *
     * @param id Identificador da estratégia.
     * @return A entidade {@link EstrategiaAvaliacaoModel} correspondente.
     */
    EstrategiaAvaliacaoModel buscarEntidadePorId(Long id);

    /**
     * Busca e retorna uma lista de entidades de domínio para uma lista de IDs informados.
     *
     * @param ids Lista de identificadores das estratégias.
     * @return Lista de entidades {@link EstrategiaAvaliacaoModel} encontradas.
     */
    List<EstrategiaAvaliacaoModel> buscarEntidadesPorIds(List<Long> ids);

    /**
     * Busca e retorna objetos de estratégia a partir de uma coleção de IDs.
     *
     * @param copyOf Lista de identificadores para consulta.
     * @return Coleção contendo as entidades localizadas no repositório.
     */
    Object buscarPorIds(List<Long> copyOf);
}