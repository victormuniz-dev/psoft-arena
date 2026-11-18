# Code Review — US9 (Acompanhamento da Submissão)

Documento de handoff para uma **segunda IA (ou revisor humano)** executar o code review da US9 do PSoft Arena.

> Este arquivo substitui a versão anterior de `CodeReview_US9.md`, que descrevia um código diferente do que estava de fato no repositório.
>
> **Escopo:** apenas US1–US10. Nenhuma US fora dessa faixa está implementada aqui — em particular, **não há reprocessamento (US12)** nem lógica de exibição de resultado (US11).

---

## 0. Como reproduzir

O projeto é **Maven** (não Gradle). Não existe `gradlew` neste repositório; o comando equivalente ao `./gradlew test` solicitado é:

```bash
MAVEN_USER_HOME="$PWD/.mvn-home" ./mvnw -o -Dmaven.repo.local="$PWD/.m2repo" test
```

- `MAVEN_USER_HOME` aponta o wrapper para a distribuição já baixada em `.mvn-home` (o `$HOME/.m2` é somente leitura neste ambiente).
- `-Dmaven.repo.local` usa o repositório offline em `.m2repo`.
- **Sem** esses dois parâmetros o build falha com `mkdir: não foi possível criar o diretório "/home/erick/.m2"`.

**Resultado atual:** `Tests run: 39, Failures: 0, Errors: 0, Skipped: 0` — `BUILD SUCCESS`, verificado em build do zero.

| Suíte | Testes |
|---|---|
| `ProblemaControllerTest` (US1, não tocada) | 8 |
| `SubmissaoControllerTest` (US9 integração) | 13 |
| `ValidadorSubmissaoTest` (ponte US8) | 1 |
| `SubmissaoModelTest` (US9 domínio/State) | 16 |
| `ArenaApplicationTests` | 1 |
| **Total** | **39** |

---

## 1. Escopo do que foi alterado

### Arquivos **alterados/criados** (US9)

```
config/AsyncConfig.java
controllers/submissao/SubmissaoController.java
models/submissao/SubmissaoModel.java
models/submissao/estado/EstadoSubmissao.java
models/submissao/estado/EstadoSubmetida.java
models/submissao/estado/EstadoEmFila.java
models/submissao/estado/EstadoProcessando.java
models/submissao/estado/EstadoConcluida.java
models/submissao/estado/EstadoFalha.java
repositories/submissao/SubmissaoRepository.java
repositories/submissao/HistoricoStatusSubmissaoRepository.java
services/submissao/FilaSubmissao.java
services/submissao/SubmissaoProcessador.java
services/submissao/SubmissaoService.java
services/submissao/SubmissaoTransicaoService.java
services/submissao/ValidadorSubmissao.java            (novo, ponte US8)
services/submissao/ValidadorSubmissaoPadrao.java      (novo, ponte US8)
pom.xml                                   (apenas dependência de teste Awaitility 4.3.0)
src/test/.../SubmissaoControllerTest.java
src/test/.../SubmissaoModelTest.java
src/test/java/br/com/psoft/arena/support/AvaliadorControlavel.java   (novo, test-scope)
src/test/java/br/com/psoft/arena/services/submissao/ValidadorSubmissaoTest.java (novo)
```

### Arquivos **deliberadamente NÃO tocados**

- US1–US7: `ProblemaModel`, `ProblemaService`, `ProblemaController`, `EstudanteModel`, `EstudanteRepository`, `ProfessorModel`, `TurmaModel`, `TurmaService`, `GlobalExceptionHandler`, `OpenApiConfig`, demais DTOs.
- `AvaliadorSubmissao` / `AvaliadorSubmissaoPadrao` — são o **ponto de extensão da US10**.
- `ValidadorSubmissao` / `ValidadorSubmissaoPadrao` — são o **ponto de extensão da US8** (ver seção 5). A US9 apenas os consome; a avaliação real (LLM, nota, evidências) e a persistência da nota pertencem à US10.
- `SubmissaoRequestDto`, `SubmissaoResponseDto`, `SubmissaoDetalheResponseDto`, `HistoricoStatusResponseDto` — DTOs de contrato já existentes e estáveis; não foram remodelados para não quebrar consumidores da US8.

> **Atenção:** `TurmaModel.java`, `TurmaService.java` e `ProblemaService.java` **já continham comentários** (`// TODO US6`, blocos comentados) **antes** deste trabalho. Eles pertencem a outras USs e foram mantidos intactos conforme a restrição. **A US9 não introduziu nenhum comentário** — verificação no item 7.

---

## 2. Mudanças obrigatórias solicitadas × implementação

### 2.1 Domínio e padrão State

| Requisito | Status | Onde |
|---|---|---|
| `StatusSubmissao` com os 5 estados | já existia, mantido | `models/submissao/StatusSubmissao.java` |
| Hierarquia `EstadoSubmissao` + 5 estados concretos | completo | `models/submissao/estado/*` |
| `@OrderBy("dataHora ASC")` em `historico` | ✅ (removido o `id ASC` extra para cumprir a anotação literal; desempate por `id` foi para o *repository*) | `SubmissaoModel:78` |
| Método central único de transição | ✅ `transicionarPara` é o **único** ponto que altera `status`, grava histórico e imprime `System.out.println` | `SubmissaoModel:151` |
| `@UniqueConstraint` `(estudante_id, atividade_id, versao)` | ✅ nomeada `uk_submissao_estudante_atividade_versao` | `SubmissaoModel:35-40` |

**Ponto único de escrita do status:** `SubmissaoModel.aplicarTransicao` é o único método que atribui `this.status`, grava o `HistoricoStatusSubmissaoModel` e imprime a notificação. O construtor também passa por ele, então o estado inicial `Submetida` gera histórico e `println` como qualquer outra transição. Transições ilegais são recusadas pelas implementações de `EstadoSubmissao` (os `default` da interface lançam `BusinessRuleException`); `Concluída` e `Falha` são terminais.

### 2.2 Resiliência do processamento assíncrono

| Requisito | Implementação |
|---|---|
| Remover `catch (RuntimeException)` silenciosos | ✅ Não há mais `catch` que apenas `return`. Todo erro passa por `registrarErroExplicito` (`log.error` + `System.err.println`). |
| Não deixar submissão presa em `Processando` | ✅ Se `iniciarProcessamento` falhar, a submissão é forçada para `Falha`. Se o próprio `falhar` falhar, é impresso aviso explícito (`ATENÇÃO: ... pode permanecer em Processando`). |
| `iniciarProcessamento`/`falhar` logados | ✅ Ambos em `SubmissaoProcessador.registrarErroExplicito` / `notificarSubmissaoPresa`. |
| `TaskRejectedException` → `Falha` com motivo | ✅ `SubmissaoService.agendarProcessamento` captura, libera o flag de drenagem, registra o erro e chama `submissaoTransicaoService.falhar(id, MOTIVO_FILA_CHEIA)`. |

A cadeia de causa do erro é percorrida até a raiz (`motivo(...)`) para que o histórico registre o motivo real, e há truncamento em 900 caracteres (limite da coluna é 1000).

### 2.3 Transações

- `buscarPorId(Long)` → `@Transactional(readOnly = true)` ✅
- `encaminharParaProcessamento` → **sem** `@Transactional` ✅. Assim `submissaoTransicaoService.encaminharParaFila` (que é `@Transactional`) **commita antes** de o ID ser inserido na `FilaSubmissao`.

### 2.4 Acesso e endpoints

- `POST /submissoes/{submissaoId}/encaminhar` agora **exige** `?estudanteId=` e valida a posse via `SubmissaoTransicaoService.buscarDoEstudante`, devolvendo **404** quando o estudante não é o dono. Sem o parâmetro → **400**.
- **Mudança de contrato (breaking):** antes era possível encaminhar qualquer submissão sem informar o dono. Ver item 5 para o impacto na US8.
- Os três endpoints de consulta do estudante estão anotados com `@Operation`, `@Parameter` e `@ApiResponse`, e `@Tag(name = "Submissões")`. Verificado empiricamente em `/v3/api-docs`.

### 2.5 Duplicidades

**Não existia** `SubmissaoRepository_1.java` nem `HistoricoStatusSubmissaoRepository_1.java` — verificado com `find` no disco, `git ls-files` e histórico. Nenhum arquivo foi apagado por esse motivo.

### 2.6 Testes

Usa **Awaitility** (`await().atMost(20s)`), **sem `Thread.sleep` fixo**. Cenários cobertos: ciclo completo, falha com motivo no histórico, rejeição pelo State, isolamento entre estudantes, posse no encaminhamento, versões concorrentes, rejeição do executor.

---

## 3. Riscos que o revisor deve avaliar

### R1 — `AvaliadorControlavel` é um bean `@Primary` de teste
Em `src/test`, substitui `AvaliadorSubmissaoPadrao` em **todo** o contexto de teste. Isso é intencional (permite injetar falha determinística), mas significa que `SubmissaoControllerTest` **não** exercita o avaliador real da US10. Quando a US10 chegar, os testes dela devem usar seu próprio bean para não colidir com este `@Primary`.

### R2 — Cálculo de versão sob concorrência
`registrar()` lê `max(versao)` e insere. A `@UniqueConstraint` garante a integridade; a requisição perdedora recebe `DataIntegrityViolationException`, que `SubmissaoService.registrar` converte em `BusinessRuleException` (HTTP 422, "conflito de versão. Tente novamente."). Não há retry automático. O teste `deveImpedirVersaoDuplicadaPelaConstraintDeUnicidade` prova que a constraint existe no banco.

### R3 — `DataIntegrityViolationException` sem handler
`GlobalExceptionHandler` é arquivo compartilhado (não alterado). Nenhuma exceção genérica é tratada, então erros de integridade retornam 500. Avaliar se vale um handler — fora do escopo da US9 sem autorização.

### R4 — `FilaSubmissao` expõe API de teste
`liberarDrenagem()`, `limpar()`, `estaEmDrenagem()` são usados pelo teste de rejeição e pelo `finally` do processador. São métodos públicos legítimos, mas `limpar()` é essencialmente utilitário de teste em código de produção.

### R5 — Observação de histórico limitada a 1000 caracteres
`transicionarPara` lança `BusinessRuleException` se a observação exceder 1000. É **proposital** (evita falha de persistência), mas significa que um motivo de falha muito longo derrubaria a própria transição. `SubmissaoProcessador` já trunca em 900, porém chamadas diretas a `falhar(...)` não truncam.

### R6 — `@OrderBy("dataHora ASC")` sem desempate
Duas transições no mesmo microssegundo podem empatar. Tratado por `findBySubmissaoIdOrderByDataHoraAscIdAsc` no repositório e pelo DTO que percorre a coleção; porém, a coleção em memória (`getHistorico()`) não tem desempate por `id`.

### R7 — Persistência da nota fica para a US10
A US9 **não** possui campo `nota`. O avaliador recebe a entidade já desanexada da transação e é chamado antes de `concluir`, portanto um setter de nota ali nunca seria persistido. A US10 deve criar a própria entidade de avaliação (nota, evidências, justificativa), vinculada à submissão, e gravá-la no seu `AvaliadorSubmissao`.

---

## 4. Fronteira com a US10 (Avaliação)

A US9 **não avalia** nada. O contrato consumido é:

```java
public interface AvaliadorSubmissao {
    void avaliar(SubmissaoModel submissao);
}
```

- `AvaliadorSubmissaoPadrao` valida apenas que há código-fonte.
- Se `avaliar` lançar exceção → US9 transiciona para `Falha` com o motivo da exceção.
- Se retornar normalmente → US9 transiciona para `Concluída`.
- A US10 deve persistir o resultado em entidade/serviço próprios, dentro de `avaliar()`.

---

## 5. Ponte com a US8 (Submissão de Soluções)

A US8 será feita por outra pessoa. Para **evitar conflito de merge**, a US9 não implementa nenhuma regra da US8 — ela expõe um ponto de extensão. A US8 entra sem alterar **nenhum arquivo** da US9.

### 5.1 O que a US8 precisa implementar

Criar **um arquivo novo**, somente:

```java
package br.com.psoft.arena.services.submissao;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import br.com.psoft.arena.exception.BusinessRuleException;

@Primary
@Component
public class ValidadorSubmissaoDaUserStory8 implements ValidadorSubmissao {

    @Override
    public void validar(Long estudanteId, Long atividadeId) {
        AtividadeModel atividade = atividadeRepository.findById(atividadeId)
            .orElseThrow(() -> new ResourceNotFoundException("Atividade", atividadeId));

        if (atividade.getStatus() != StatusAtividade.ABERTA) {
            throw new BusinessRuleException("A atividade não aceita novas submissões.");
        }

        if (!atividade.getTurma().possuiEstudante(estudanteId)) {
            throw new BusinessRuleException("O estudante não pertence à turma da atividade.");
        }
    }
}
```

É chamado automaticamente por `SubmissaoService.registrar(...)`, logo após as validações de entrada e **antes** do cálculo de versão. O código acima é ilustrativo: os nomes de `AtividadeModel`, `StatusAtividade` e do método de turma dependem de como a US6 for entregue.

### 5.2 Os três erros que quebram a ponte

1. **Esquecer `@Primary`** → o Spring sobe com `NoUniqueBeanDefinitionException` e o build inteiro falha, não só a US8.
2. **Criar a classe sem `implements ValidadorSubmissao`** → a US9 continua usando `ValidadorSubmissaoPadrao` (que não valida nada) e a regra da US8 **silenciosamente não roda**. É o pior cenário: os testes passam e a regra não vale.
3. **Implementar a validação dentro de `SubmissaoService.registrar()`** → volta a gerar conflito. A validação deve ficar **somente** no `ValidadorSubmissao`.

### 5.3 Outros contratos da US9

1. **`POST /submissoes` já encaminha automaticamente.** Use esse fluxo e **não** chame `/encaminhar` em seguida (retorna `422`, pois o estado já é `EM_FILA`).
2. **`POST /submissoes/{id}/encaminhar` exige `?estudanteId=`.** Se a US8 expuser esse endpoint ao estudante, repasse o ID do solicitante. Sem o parâmetro → `400`; não-dono → `404`.
3. **`atividadeId` é `Long`** em `SubmissaoModel`, porque `AtividadeModel` (US6) ainda não existe. Quando a US6 trouxer o modelo, avaliar trocar para `@ManyToOne` — isso é decisão da US6/US8, não da US9.
4. **Avaliação (US10)** é ponto de extensão: implementar `AvaliadorSubmissao` e marcar `@Primary`, substituindo `AvaliadorSubmissaoPadrao`.

---

## 6. Checklist sugerido para o revisor

- [ ] O único ponto de escrita de `status` é `SubmissaoModel.transicionarPara`? (`grep -n "this.status ="`)
- [ ] Existe algum `catch` que descarta exceção sem log em `services/submissao`?
- [ ] `encaminharParaProcessamento` continua sem `@Transactional`?
- [ ] `Concluída` e `Falha` não possuem nenhuma transição de saída?
- [ ] `POST /{id}/encaminhar` retorna 404 para não-dono e 400 sem `estudanteId`?
- [ ] Os 3 endpoints de consulta estão em `/v3/api-docs`?
- [ ] Algum comentário foi introduzido nos arquivos da US9?
- [ ] Algum arquivo de outra US foi modificado? (`git status`)

---

## 7. Verificações objetivas executadas

```text
grep -rE "^\s*//|/\*|\*/|^\s*\*" src/main/.../submissao src/test/.../submissao src/test/.../support config/AsyncConfig.java
→ nenhum resultado (exit 1)

git status  → apenas arquivos dentro do escopo US9 + pom.xml (dependência de teste)
```

Suíte completa: **39 testes, 0 falhas**.
