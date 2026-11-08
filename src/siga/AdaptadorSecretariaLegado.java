package siga;

import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador do padrão Adapter: implementa {@link FonteDeAlunos} usando o
 * componente externo {@link SecretariaLegadoWS}, recebido por composição.
 *
 * É o único lugar do sistema que conhece o formato da matriz do legado:
 * coluna 0 é a matrícula, 1 é o nome e 2 é a situação em letra.
 */
public class AdaptadorSecretariaLegado implements FonteDeAlunos {

    private static final int COLUNA_MATRICULA = 0;
    private static final int COLUNA_NOME = 1;
    private static final int COLUNA_SITUACAO = 2;
    private static final String SITUACAO_ATIVO = "A";

    private final SecretariaLegadoWS servicoLegado;

    public AdaptadorSecretariaLegado(SecretariaLegadoWS servicoLegado) {
        this.servicoLegado = servicoLegado;
    }

    @Override
    public List<Aluno> listar() {
        List<Aluno> alunos = new ArrayList<>();
        for (String[] linha : servicoLegado.consultarTabelaAlunos()) {
            alunos.add(converter(linha));
        }
        return alunos;
    }

    private Aluno converter(String[] linha) {
        Aluno aluno = new Aluno(linha[COLUNA_MATRICULA], linha[COLUNA_NOME]);
        aluno.setAtivo(estaAtivo(linha[COLUNA_SITUACAO]));
        return aluno;
    }

    /**
     * Regra da situação, aplicada uma vez só: o legado às vezes manda a letra
     * em minúscula, então "A" e "a" valem como ativo.
     */
    private boolean estaAtivo(String situacao) {
        return SITUACAO_ATIVO.equalsIgnoreCase(situacao);
    }
}
