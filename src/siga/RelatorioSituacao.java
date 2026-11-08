package siga;

/**
 * Cliente do padrão Adapter: imprime a situação de cada aluno.
 *
 * Consome a mesma {@link FonteDeAlunos} que o importador, então os dois
 * pontos do sistema mostram sempre a mesma situação para o mesmo aluno.
 */
public class RelatorioSituacao {

    private final FonteDeAlunos fonte;

    public RelatorioSituacao(FonteDeAlunos fonte) {
        this.fonte = fonte;
    }

    public void imprimir() {
        System.out.println("   Situação segundo o relatório:");
        for (Aluno aluno : fonte.listar()) {
            System.out.println("     " + aluno);
        }
    }
}
