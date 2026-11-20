#!/usr/bin/env bash
#
# test-us6.sh — Teste E2E da US6 (Criação de Atividades) via API REST.
#
# Uso:
#   ./scripts/test-us6.sh
#   BASE_URL=http://localhost:8080 ./scripts/test-us6.sh
#   TURMA_ID=123 ./scripts/test-us6.sh    # reutiliza uma turma já existente
#
# Requisitos:
#   - curl e jq instalados;
#   - API em execução (por padrão em http://localhost:8080);
#   - servidor iniciado com ARENA_CRIPTOGRAFIA_CHAVE_MESTRA definida
#     (chave AES-256 de 32 bytes em Base64). Sem ela, a criação de
#     atividade falha com HTTP 500 e sem persistência parcial.
#
# O script NÃO imprime chaves, NÃO altera arquivos do projeto e usa apenas
# dados fictícios.

set -uo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
TURMA_ID="${TURMA_ID:-}"

TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

PASS=0
FAIL=0
BLOCKED=0
HTTP_CODE=""
HTTP_BODY="$TMP_DIR/body.json"
HTTP_HEADERS="$TMP_DIR/headers.txt"

# ---------------------------------------------------------------------------
# Utilidades
# ---------------------------------------------------------------------------

log()  { printf '%s\n' "$*"; }
pass() { PASS=$((PASS + 1)); printf 'PASS    %s\n' "$1"; }
fail() { FAIL=$((FAIL + 1)); printf 'FAIL    %s\n' "$1"; }
blocked() { BLOCKED=$((BLOCKED + 1)); printf 'BLOCKED %s\n' "$1"; }
die()  { printf '\nFATAL   %s\n' "$1" >&2; exit 1; }

summary() {
    printf '\n----------------------------------------\n'
    printf 'Resumo: %d PASS, %d FAIL, %d BLOCKED\n' "$PASS" "$FAIL" "$BLOCKED"
}

require_tools() {
    command -v curl >/dev/null 2>&1 || die "curl não encontrado no PATH."
    command -v jq   >/dev/null 2>&1 || die "jq não encontrado no PATH."
}

# http METHOD PATH [JSON_BODY]
# Preenche HTTP_CODE, HTTP_BODY e HTTP_HEADERS.
http() {
    local method="$1" path="$2" body="${3-}"
    local args=(
        -sS --max-time 20
        -X "$method" "$BASE_URL$path"
        -H 'Accept: application/json'
        -D "$HTTP_HEADERS"
        -o "$HTTP_BODY"
        -w '%{http_code}'
    )
    if [ -n "$body" ]; then
        args+=(-H 'Content-Type: application/json' --data "$body")
    fi

    HTTP_CODE="$(curl "${args[@]}")" || die "Falha de conexão em $method $path ($BASE_URL)."
    printf '        %-6s %-55s -> HTTP %s\n' "$method" "$path" "$HTTP_CODE"
}

header_value() {
    local name="$1"
    awk -v h="$name" '
        BEGIN { h = tolower(h) }
        tolower(substr($0, 1, length(h) + 1)) == h ":" {
            sub(/^[^:]+:[[:space:]]*/, "")
            sub(/\r$/, "")
            print
            exit
        }
    ' "$HTTP_HEADERS"
}

json_get() {
    jq -r "$1" "$HTTP_BODY" 2>/dev/null
}

is_json() {
    jq -e . >/dev/null 2>&1 < "$HTTP_BODY"
}

assert_eq() {
    local desc="$1" expected="$2" actual="$3"
    if [ "$expected" = "$actual" ]; then
        pass "$desc"
    else
        fail "$desc (esperado='$expected', recebido='$actual')"
    fi
}

assert_true() {
    local desc="$1" condition="$2"
    if [ "$condition" = "true" ]; then
        pass "$desc"
    else
        fail "$desc"
    fi
}

# Para passos de setup: exige o código e interrompe se divergir.
require_code_or_exit() {
    local desc="$1" expected="$2"
    if [ "$HTTP_CODE" = "$expected" ]; then
        pass "$desc"
        return 0
    fi
    fail "$desc (esperado HTTP $expected, recebido HTTP $HTTP_CODE)"
    if is_json; then
        printf '        Corpo (trecho): '
        head -c 400 "$HTTP_BODY"
        printf '\n'
    fi
    summary
    exit 1
}

RUN_ID="$(date +%s)-$$"
NOME_MODELO="gpt-ficticio-e2e"
URL_API="https://api.exemplo.invalid/v1"
CHAVE_API="chave-ficticia-e2e-nao-usar"
TITULO="Atividade E2E $RUN_ID"
DESCRICAO="Descrição fictícia criada pelo teste E2E da US6."

# Prazo futuro calculado dinamicamente, em formato ISO LocalDateTime
# (sem timezone), compatível com o DTO LocalDateTime do projeto.
if ! PRAZO="$(date -d '+30 days' '+%Y-%m-%dT23:59:30' 2>/dev/null)"; then
    PRAZO="$(date -v+30d '+%Y-%m-%dT23:59:30')"
fi
if [ -z "$PRAZO" ]; then
    die "Não foi possível calcular o prazo futuro com 'date'."
fi

log "============================================================"
log " Teste E2E US6 — Criação de Atividades"
log " Base URL: $BASE_URL"
log "============================================================"

require_tools

# ---------------------------------------------------------------------------
# 1. Disponibilidade da API
# ---------------------------------------------------------------------------
log "\n[1] Disponibilidade da API"
http GET /professores
if [ "$HTTP_CODE" != "200" ]; then
    fail "API indisponível em $BASE_URL/professores"
    summary
    exit 1
fi
pass "API responde em $BASE_URL (HTTP 200)"

# ---------------------------------------------------------------------------
# 2. OpenAPI
# ---------------------------------------------------------------------------
log "\n[2] Documentação OpenAPI"
http GET /v3/api-docs
if [ "$HTTP_CODE" != "200" ]; then
    fail "/v3/api-docs não respondeu com 200"
    summary
    exit 1
fi
if is_json && jq -e '.openapi' >/dev/null 2>&1 < "$HTTP_BODY"; then
    pass "/v3/api-docs disponível e é um documento OpenAPI válido"
else
    fail "/v3/api-docs respondeu 200, mas o corpo não é um OpenAPI JSON válido"
fi

# ---------------------------------------------------------------------------
# 3. Dados de apoio: professor, problema e estratégia
# ---------------------------------------------------------------------------
log "\n[3] Criação dos dados de apoio"

PROFESSOR_BODY="$(jq -nc --arg nome "Professor E2E $RUN_ID" '{nome: $nome}')"
http POST /professores "$PROFESSOR_BODY"
require_code_or_exit "Professor criado (POST /professores)" "201"
PROFESSOR_ID="$(json_get '.id')"
if [ -n "$PROFESSOR_ID" ] && [ "$PROFESSOR_ID" != "null" ]; then
    pass "ID do professor extraído: $PROFESSOR_ID"
else
    fail "Não foi possível extrair o ID do professor"
    summary
    exit 1
fi

PROBLEMA_BODY="$(jq -nc \
    --arg titulo "Problema E2E $RUN_ID" \
    '{titulo: $titulo,
      descricao: "Problema fictício do teste E2E.",
      requisitosFuncionais: ["RF1 — requisito fictício"],
      requisitosProjeto: ["RP1 — requisito fictício"]}')"
http POST /problemas "$PROBLEMA_BODY"
require_code_or_exit "Problema criado (POST /problemas)" "201"
PROBLEMA_ID="$(json_get '.id')"
if [ -n "$PROBLEMA_ID" ] && [ "$PROBLEMA_ID" != "null" ]; then
    pass "ID do problema extraído: $PROBLEMA_ID"
else
    fail "Não foi possível extrair o ID do problema"
    summary
    exit 1
fi

ESTRATEGIA_BODY="$(jq -nc \
    --arg nome "Estratégia E2E $RUN_ID" \
    '{nome: $nome,
      tipo: "CODE_SMELL",
      descricao: "Estratégia fictícia do teste E2E.",
      promptAvaliacao: "Avalie o código fictício do teste E2E."}')"
http POST /api/estrategia "$ESTRATEGIA_BODY"
require_code_or_exit "Estratégia criada (POST /api/estrategia)" "201"
ESTRATEGIA_ID="$(json_get '.id')"
if [ -n "$ESTRATEGIA_ID" ] && [ "$ESTRATEGIA_ID" != "null" ]; then
    pass "ID da estratégia extraído: $ESTRATEGIA_ID"
else
    fail "Não foi possível extrair o ID da estratégia"
    summary
    exit 1
fi

# ---------------------------------------------------------------------------
# Turma (pré-requisito para atividade)
# ---------------------------------------------------------------------------
log "\n[4] Turma ativa"

if [ -n "$TURMA_ID" ]; then
    log "        Usando TURMA_ID informado: $TURMA_ID"
    http GET "/turmas/$TURMA_ID"
    require_code_or_exit "Turma informada existe (GET /turmas/$TURMA_ID)" "200"
    TURMA_STATUS="$(json_get '.status')"
    assert_eq "Turma informada está ATIVA" "ATIVA" "$TURMA_STATUS"
    if [ "$TURMA_STATUS" != "ATIVA" ]; then
        summary
        exit 1
    fi
else
    TURMA_BODY="$(jq -nc \
        --arg nome "Turma E2E $RUN_ID" \
        --arg codigo "E2E-$RUN_ID" \
        --argjson professorId "$PROFESSOR_ID" \
        '{nome: $nome, codigo: $codigo, periodoLetivo: "2026.2", professorId: $professorId}')"
    http POST /turmas "$TURMA_BODY"

    if [ "$HTTP_CODE" != "201" ]; then
        blocked "Turma ativa não pôde ser criada (POST /turmas retornou HTTP $HTTP_CODE) — pré-requisito dos cenários de atividade"
        log "\n        BLOQUEIO DE PRÉ-REQUISITO:"
        log "        Não foi possível criar a turma necessária para os cenários de"
        log "        atividade (POST /turmas retornou HTTP $HTTP_CODE)."
        log "        Sem turma, os cenários de atividade NÃO podem ser executados."
        log "        Para rodar o fluxo completo, informe TURMA_ID de uma turma ATIVA."
        if is_json; then
            printf '        Corpo do erro (trecho): '
            head -c 400 "$HTTP_BODY"
            printf '\n'
        fi
        TURMA_ID=""
    else
        pass "Turma ativa criada (POST /turmas)"
        TURMA_ID="$(json_get '.id')"
        if [ -n "$TURMA_ID" ] && [ "$TURMA_ID" != "null" ]; then
            pass "ID da turma extraído: $TURMA_ID"
        else
            fail "Não foi possível extrair o ID da turma"
            TURMA_ID=""
        fi
    fi
fi

# Teste de validação do DTO.
# Só é conclusivo se houver turma: assim o 400 pode ser atribuído à validação
# do corpo (@Valid) e não a uma turma inexistente. Com turma existente,
# executamos também um controle com turma inexistente: se ainda assim vier
# 400, fica comprovado que a validação do DTO precede a busca da turma.
log "\n[5] Validação de campos obrigatórios"
INVALID_BODY="$(jq -nc \
    --arg prazo "$PRAZO" \
    --argjson problemaId "${PROBLEMA_ID:-1}" \
    --argjson estrategiaId "${ESTRATEGIA_ID:-1}" \
    '{descricao: "Sem título",
      prazoSubmissao: $prazo,
      problemaId: $problemaId,
      estrategiaId: $estrategiaId,
      configuracaoLlm: {nomeModelo: "gpt-ficticio", urlApi: "https://exemplo.invalid", chaveApi: "ficticia"}}')"

if [ -n "$TURMA_ID" ]; then
    http POST "/turmas/$TURMA_ID/atividades" "$INVALID_BODY"
    if [ "$HTTP_CODE" = "400" ]; then
        pass "Atividade com título ausente rejeitada com HTTP 400 (turma existente)"
    else
        fail "Esperado HTTP 400 para título ausente com turma existente, recebido HTTP $HTTP_CODE"
    fi

    # Controle de independência da turma: corpo inválido + turma inexistente.
    # Se retornar 400, a validação do DTO ocorreu antes de qualquer busca.
    http POST "/turmas/999999999/atividades" "$INVALID_BODY"
    assert_eq "Validação do DTO precede a busca da turma (turma inexistente também retorna 400)" \
        "400" "$HTTP_CODE"
else
    blocked "Teste de título ausente INCONCLUSIVO: sem turma não é possível distinguir 400 de validação do DTO de erro/404 de turma"
fi

if [ -z "$TURMA_ID" ]; then
    log "\n[6..13] Cenários dependentes de turma — não executados"
    blocked "Criação da atividade (POST /turmas/{turmaId}/atividades)"
    blocked "GET /atividades/{id}"
    blocked "GET /turmas/{turmaId}/atividades"
    blocked "PATCH /atividades/{id}/encerrar"
    blocked "Segundo encerramento (HTTP 422)"
    blocked "Criação em turma encerrada (HTTP 422)"
    blocked "Remoção de problema associado (HTTP 422)"
    summary
    printf 'Resultado: BLOQUEADO por pré-requisito (turma) — cenários de atividade não executados.\n'
    exit 2
fi

# ---------------------------------------------------------------------------
# 6. Criação da atividade
# ---------------------------------------------------------------------------
log "\n[6] Criação da atividade"
ATIVIDADE_BODY="$(jq -nc \
    --arg titulo "$TITULO" \
    --arg descricao "$DESCRICAO" \
    --arg prazo "$PRAZO" \
    --argjson problemaId "$PROBLEMA_ID" \
    --argjson estrategiaId "$ESTRATEGIA_ID" \
    --arg nomeModelo "$NOME_MODELO" \
    --arg urlApi "$URL_API" \
    --arg chaveApi "$CHAVE_API" \
    '{titulo: $titulo,
      descricao: $descricao,
      prazoSubmissao: $prazo,
      problemaId: $problemaId,
      estrategiaId: $estrategiaId,
      configuracaoLlm: {nomeModelo: $nomeModelo, urlApi: $urlApi, chaveApi: $chaveApi}}')"

http POST "/turmas/$TURMA_ID/atividades" "$ATIVIDADE_BODY"
if [ "$HTTP_CODE" != "201" ]; then
    fail "Criação da atividade (esperado HTTP 201, recebido HTTP $HTTP_CODE)"
    if [ "$HTTP_CODE" = "500" ]; then
        log "        HTTP 500: verifique se o servidor foi iniciado com"
        log "        ARENA_CRIPTOGRAFIA_CHAVE_MESTRA (AES-256 Base64)."
    fi
    if is_json; then
        printf '        Corpo do erro (trecho): '
        head -c 400 "$HTTP_BODY"
        printf '\n'
    fi
    summary
    exit 1
fi
pass "Atividade criada com HTTP 201"

ATIVIDADE_ID="$(json_get '.id')"
if [ -n "$ATIVIDADE_ID" ] && [ "$ATIVIDADE_ID" != "null" ]; then
    pass "ID da atividade extraído: $ATIVIDADE_ID"
else
    fail "Não foi possível extrair o ID da atividade"
    summary
    exit 1
fi

LOCATION="$(header_value 'Location')"
if [ -n "$LOCATION" ]; then
    pass "Cabeçalho Location presente: $LOCATION"
else
    fail "Cabeçalho Location ausente"
fi
assert_eq "Location aponta para a atividade criada" "/atividades/$ATIVIDADE_ID" "$LOCATION"

# ---------------------------------------------------------------------------
# 7. GET /atividades/{id}
# ---------------------------------------------------------------------------
log "\n[7] Consulta GET /atividades/$ATIVIDADE_ID"
http GET "/atividades/$ATIVIDADE_ID"
if [ "$HTTP_CODE" = "200" ]; then
    pass "GET da atividade retornou HTTP 200"
else
    fail "GET da atividade retornou HTTP $HTTP_CODE"
    summary
    exit 1
fi

assert_eq "Campo id"                     "$ATIVIDADE_ID"   "$(json_get '.id')"
assert_eq "Campo titulo"                 "$TITULO"         "$(json_get '.titulo')"
assert_eq "Campo descricao"              "$DESCRICAO"      "$(json_get '.descricao')"
assert_eq "Campo status"                 "ABERTA"          "$(json_get '.status')"
assert_eq "Campo turmaId"                "$TURMA_ID"       "$(json_get '.turmaId')"
assert_eq "Campo problemaId"             "$PROBLEMA_ID"    "$(json_get '.problemaId')"
assert_eq "Campo estrategiaId"           "$ESTRATEGIA_ID"  "$(json_get '.estrategiaId')"
assert_eq "LLM nomeModelo"               "$NOME_MODELO"    "$(json_get '.configuracaoLlm.nomeModelo')"
assert_eq "LLM urlApi"                   "$URL_API"        "$(json_get '.configuracaoLlm.urlApi')"
assert_eq "LLM padrao"                   "true"            "$(json_get '.configuracaoLlm.padrao')"
assert_eq "LLM chaveConfigurada"         "true"            "$(json_get '.configuracaoLlm.chaveConfigurada')"
assert_true "Campo prazoSubmissao preservado" \
    "$(jq -r --arg p "$PRAZO" '.prazoSubmissao | startswith($p)' "$HTTP_BODY" 2>/dev/null)"

# ---------------------------------------------------------------------------
# 8. Segurança: a chave não pode aparecer na resposta
# ---------------------------------------------------------------------------
log "\n[8] Verificação de exposição de credenciais"
if grep -qF "$CHAVE_API" "$HTTP_BODY"; then
    fail "A chave original da API apareceu na resposta HTTP"
else
    pass "A chave original da API não aparece na resposta"
fi

if grep -q '"chaveApi"' "$HTTP_BODY" || grep -q '"chaveApiCriptografada"' "$HTTP_BODY"; then
    fail "A resposta expõe campo de chave (chaveApi/chaveApiCriptografada)"
else
    pass "A resposta não expõe campos chaveApi nem chaveApiCriptografada"
fi

if jq -e '.configuracaoLlm | has("chaveConfigurada")' >/dev/null 2>&1 < "$HTTP_BODY"; then
    pass "A resposta expõe apenas o indicador chaveConfigurada"
else
    fail "A resposta não contém o indicador chaveConfigurada"
fi

# ---------------------------------------------------------------------------
# 9. GET /turmas/{turmaId}/atividades
# ---------------------------------------------------------------------------
log "\n[9] Consulta GET /turmas/$TURMA_ID/atividades"
http GET "/turmas/$TURMA_ID/atividades"
if [ "$HTTP_CODE" = "200" ]; then
    pass "Listagem de atividades da turma retornou HTTP 200"
else
    fail "Listagem de atividades da turma retornou HTTP $HTTP_CODE"
fi

if jq -e --argjson id "$ATIVIDADE_ID" 'any(.[]; .id == $id)' >/dev/null 2>&1 < "$HTTP_BODY"; then
    pass "Listagem contém a atividade criada"
else
    fail "Listagem não contém a atividade criada"
fi

if grep -qF "$CHAVE_API" "$HTTP_BODY" || grep -q '"chaveApi"' "$HTTP_BODY" || grep -q '"chaveApiCriptografada"' "$HTTP_BODY"; then
    fail "A listagem expõe credenciais"
else
    pass "A listagem não expõe credenciais"
fi

# ---------------------------------------------------------------------------
# 10. PATCH /atividades/{id}/encerrar
# ---------------------------------------------------------------------------
log "\n[10] Encerramento da atividade"
http PATCH "/atividades/$ATIVIDADE_ID/encerrar"
assert_eq "Encerramento retornou HTTP 200" "200" "$HTTP_CODE"
assert_eq "Status após encerramento" "CONCLUIDA" "$(json_get '.status')"

log "\n[11] Tentativa de encerrar novamente"
http PATCH "/atividades/$ATIVIDADE_ID/encerrar"
assert_eq "Segundo encerramento retorna HTTP 422" "422" "$HTTP_CODE"

# ---------------------------------------------------------------------------
# 12. Atividade em turma encerrada
# ---------------------------------------------------------------------------
log "\n[12] Atividade em turma encerrada"
http PATCH "/turmas/$TURMA_ID/encerrar"
assert_eq "Encerramento da turma retornou HTTP 200" "200" "$HTTP_CODE"

ATIVIDADE_EM_TURMA_ENCERRADA="$(jq -nc \
    --arg titulo "Atividade proibida $RUN_ID" \
    --arg descricao "Não deve ser criada em turma encerrada." \
    --arg prazo "$PRAZO" \
    --argjson problemaId "$PROBLEMA_ID" \
    --argjson estrategiaId "$ESTRATEGIA_ID" \
    '{titulo: $titulo,
      descricao: $descricao,
      prazoSubmissao: $prazo,
      problemaId: $problemaId,
      estrategiaId: $estrategiaId,
      configuracaoLlm: {nomeModelo: "gpt-ficticio", urlApi: "https://exemplo.invalid", chaveApi: "ficticia"}}')"

http POST "/turmas/$TURMA_ID/atividades" "$ATIVIDADE_EM_TURMA_ENCERRADA"
assert_eq "Criação em turma encerrada retorna HTTP 422" "422" "$HTTP_CODE"

# ---------------------------------------------------------------------------
# 13. Proteção contra remoção de problema associado
# ---------------------------------------------------------------------------
log "\n[13] Proteção contra remoção de problema associado"
http DELETE "/problemas/$PROBLEMA_ID"
assert_eq "Remoção de problema associado retorna HTTP 422" "422" "$HTTP_CODE"
if is_json || [ -s "$HTTP_BODY" ]; then
    MENSAGEM="$(head -c 200 "$HTTP_BODY")"
    if printf '%s' "$MENSAGEM" | grep -qi 'associad'; then
        pass "Resposta indica que o problema está associado a uma atividade"
    else
        fail "Resposta 422 não menciona a associação do problema"
    fi
fi

# ---------------------------------------------------------------------------
# Resultado final
# ---------------------------------------------------------------------------
summary
if [ "$FAIL" -gt 0 ]; then
    printf 'Resultado: FALHOU (%d assertiva(s) com FAIL)\n' "$FAIL"
    exit 1
fi
if [ "$BLOCKED" -gt 0 ]; then
    printf 'Resultado: BLOQUEADO (%d cenário(s) não executado(s))\n' "$BLOCKED"
    exit 2
fi
printf 'Resultado: TODOS OS CENÁRIOS PASSARAM\n'
exit 0
