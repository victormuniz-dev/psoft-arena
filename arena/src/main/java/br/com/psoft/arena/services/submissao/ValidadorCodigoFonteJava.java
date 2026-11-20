package br.com.psoft.arena.services.submissao;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import br.com.psoft.arena.exception.BusinessRuleException;

@Component
public class ValidadorCodigoFonteJava implements ValidadorCodigoFonte {

    private static final Pattern COMENTARIO_DE_BLOCO = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
    private static final Pattern COMENTARIO_DE_LINHA = Pattern.compile("//[^\\n\\r]*");
    private static final Pattern LITERAL_TEXTO = Pattern.compile("\"(?:\\\\.|[^\"\\\\\\n\\r])*\"");
    private static final Pattern LITERAL_CHAR = Pattern.compile("'(?:\\\\.|[^'\\\\\\n\\r])+'");
    private static final Pattern DECLARACAO_DE_TIPO =
        Pattern.compile("\\b(class|interface|enum|record)\\s+[A-Za-z_$][\\w$]*");

    @Override
    public void validar(String codigoFonte) {
        if (codigoFonte == null || codigoFonte.isBlank()) {
            throw new BusinessRuleException("O código-fonte da submissão não pode ser vazio.");
        }
        String codigo = limpar(codigoFonte);

        if (!DECLARACAO_DE_TIPO.matcher(codigo).find()) {
            throw new BusinessRuleException(
                "A solução deve ser submetida em Java: nenhuma declaração de class, "
                    + "interface, enum ou record foi encontrada.");
        }
        if (!chavesBalanceadas(codigo)) {
            throw new BusinessRuleException(
                "A solução deve ser submetida em Java: as chaves do código estão desbalanceadas.");
        }
    }

    // Remove comentários e literais, para que "class" dentro deles não faça texto qualquer passar por Java.
    private String limpar(String codigo) {
        String s = COMENTARIO_DE_BLOCO.matcher(codigo).replaceAll(" ");
        s = LITERAL_TEXTO.matcher(s).replaceAll("\"\"");
        s = LITERAL_CHAR.matcher(s).replaceAll("' '");
        return COMENTARIO_DE_LINHA.matcher(s).replaceAll(" ");
    }

    private boolean chavesBalanceadas(String codigo) {
        int abertas = 0;
        for (char c : codigo.toCharArray()) {
            if (c == '{') {
                abertas++;
            } else if (c == '}' && --abertas < 0) {
                return false;
            }
        }
        return abertas == 0;
    }
}