package siga;

/**
 * Monta as peças do sistema e demonstra os três deslizes resolvidos.
 *
 * Liga o {@link AdaptadorSecretariaLegado} aos dois clientes que consomem
 * {@link FonteDeAlunos} e entrega à {@link TelaMatricula} só a
 * {@link FachadaMatricula}, que esconde o subsistema de matrícula.
 */
public class Main {

    public static void main(String[] args) {
        SecretariaLegadoWS servicoLegado = new SecretariaLegadoWS();
        FonteDeAlunos fonteDeAlunos = new AdaptadorSecretariaLegado(servicoLegado);
        AlunoDAO alunoDAO = new AlunoDAOMemoria();

        System.out.println("=== SIGA — Aula 10: Adapter e Facade ===");

        System.out.println();
        System.out.println("1) Importação a partir do sistema legado");
        ImportadorAlunos importador = new ImportadorAlunos(fonteDeAlunos, alunoDAO);
        System.out.println("   Alunos importados: " + importador.importar());
        System.out.println("   Situação segundo o importador:");
        for (Aluno aluno : alunoDAO.listarTodos()) {
            System.out.println("     " + aluno);
        }

        System.out.println();
        System.out.println("2) O mesmo dado, lido por outro trecho do sistema");
        new RelatorioSituacao(fonteDeAlunos).imprimir();
        System.out.println("   >> DESLIZE 1 resolvido: a aluna 2024003 aparece ATIVA nos dois pontos.");
        System.out.println("      A conversao existe em um unico lugar: AdaptadorSecretariaLegado.");
        System.out.println("   >> DESLIZE 2 resolvido: o importador depende de FonteDeAlunos.");

        System.out.println();
        System.out.println("3) Matrícula pela tela");
        Aluno bolsista = alunoDAO.buscarPorMatricula("2024001");
        bolsista.setBolsista(true);

        FachadaMatricula fachadaMatricula = new FachadaMatricula(
                new ValidadorCpf(),
                alunoDAO,
                new ControleVagas(),
                new CalculadoraDesconto(),
                new MatriculaDAOMemoria(),
                new NotificadorEmail());
        TelaMatricula tela = new TelaMatricula(fachadaMatricula);

        tela.aoClicarEmMatricular("2024001", "123.456.789-09", "DSM-2A");
        tela.aoClicarEmMatricular("2024002", "111", "DSM-2A");
        tela.aoClicarEmMatricular("2024009", "123.456.789-09", "DSM-2A");
        tela.aoClicarEmMatricular("2024004", "123.456.789-09", "DSM-2B");
        System.out.println("   >> DESLIZE 3 resolvido: a tela recebe UM colaborador, FachadaMatricula,");
        System.out.println("      e a ordem das chamadas ficou na fachada.");
    }
}
