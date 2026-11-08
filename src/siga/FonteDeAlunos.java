package siga;

import java.util.List;

/**
 * Alvo do padrão Adapter: a interface que o SIGA espera para obter alunos.
 *
 * Os clientes dependem só desta abstração e recebem objetos {@link Aluno}
 * prontos, sem conhecer de onde os dados vêm nem em que formato chegam.
 */
public interface FonteDeAlunos {

    List<Aluno> listar();
}
