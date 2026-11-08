package siga;

/**
 * Cliente do padrão Adapter: importa os alunos para o DAO.
 *
 * Depende só da abstração {@link FonteDeAlunos} e recebe os alunos já
 * convertidos. Não conhece o formato do sistema de origem, então trocar a
 * origem dos alunos ou testar o importador não exige alterar esta classe.
 */
public class ImportadorAlunos {

    private final FonteDeAlunos fonte;
    private final AlunoDAO dao;

    public ImportadorAlunos(FonteDeAlunos fonte, AlunoDAO dao) {
        this.fonte = fonte;
        this.dao = dao;
    }

    public int importar() {
        int importados = 0;
        for (Aluno aluno : fonte.listar()) {
            dao.inserir(aluno);
            importados++;
        }
        return importados;
    }
}
