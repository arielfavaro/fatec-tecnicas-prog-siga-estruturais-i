# SIGA — Atividade de padrões estruturais: Adapter e Facade (código inicial)

**Técnicas de Programação II (TP2) · Aula 10** — CST em Desenvolvimento de Software Multiplataforma · Fatec de Porto Ferreira

Este é o **código inicial** da atividade prática da Aula 10. Ele funciona, compila e executa — e é justamente esse o ponto. O problema não é que o código esteja quebrado; é que ele contém **três deslizes de estrutura** que só aparecem quando o sistema cresce. Sua tarefa é corrigi-los aplicando os dois padrões da aula.

## Estrutura do projeto

```
siga-estruturais/
└── src/
    └── siga/
        ├── Aluno.java                 (entidade de domínio; pronta)
        ├── Matricula.java             (entidade de domínio; pronta)
        ├── AlunoDAO.java              (interface da Aula 7; pronta)
        ├── AlunoDAOMemoria.java       (implementação da Aula 8; pronta)
        ├── MatriculaDAO.java          (interface; pronta)
        ├── MatriculaDAOMemoria.java   (implementação; pronta)
        ├── SecretariaLegadoWS.java    (COMPONENTE EXTERNO — não alterar)
        ├── ValidadorCpf.java          (subsistema de matrícula; pronta)
        ├── ControleVagas.java         (subsistema de matrícula; pronta)
        ├── CalculadoraDesconto.java   (subsistema de matrícula; pronta)
        ├── NotificadorEmail.java      (subsistema de matrícula; pronta)
        ├── ImportadorAlunos.java      (deslizes 1A e 2)
        ├── RelatorioSituacao.java     (deslize 1B)
        ├── TelaMatricula.java         (deslize 3)
        └── Main.java                  (demonstra os três deslizes)
```

## Como compilar e executar

Pré-requisito: JDK 17 ou superior (`java -version` para verificar).

```bash
# 1. Compilar (a saída vai para a pasta "bin")
javac -encoding UTF-8 -d bin src/siga/*.java

# 2. Executar
java -cp bin siga.Main
```

Execute antes de ler o restante deste arquivo: a saída **mostra os três deslizes acontecendo**.

## Os três deslizes propositais

| # | Deslize | Onde | Por que é um problema |
|---|---|---|---|
| 1 | **Conversão duplicada e divergente** | `ImportadorAlunos` e `RelatorioSituacao` | O conhecimento do formato legado (coluna 0 é matrícula, 2 é a situação em letra) foi escrito duas vezes. Alguém corrigiu o tratamento da letra minúscula em um dos dois e não no outro: a aluna `2024003` aparece **inativa** no importador e **ativa** no relatório. |
| 2 | **Dependência de classe concreta** | `ImportadorAlunos` | O importador recebe `SecretariaLegadoWS` no construtor. Trocar a origem dos alunos exige alterar o importador, e testá-lo exige o serviço externo. Violação do DIP (Aula 3). |
| 3 | **Subsistema exposto à apresentação** | `TelaMatricula` | A tela recebe **seis colaboradores** e conhece a ordem correta de chamá-los — reservar a vaga só depois de gravar a matrícula. Essa ordem é regra de negócio e não deveria morar na tela. |

## Sua tarefa

Siga as etapas da ficha de atividade prática:

1. **Localizar a duplicação.** Identifique os dois trechos que convertem o formato legado e descreva, neste README, em que ponto exato eles divergem.
2. **Criar o Adapter.** Escreva a interface `FonteDeAlunos`, com o método `List<Aluno> listar()`, e a classe `AdaptadorSecretariaLegado`, que a implementa recebendo `SecretariaLegadoWS` por composição. A conversão deve passar a existir em **um único lugar**, com a regra da letra minúscula aplicada uma vez só.
3. **Refatorar o cliente.** `ImportadorAlunos` deve passar a receber `FonteDeAlunos`, e não mais a classe legada. Ao terminar, a palavra `SecretariaLegadoWS` não deve aparecer em nenhum lugar do importador. Ajuste `RelatorioSituacao` para consumir a mesma fonte.
4. **Criar a Facade.** Escreva `FachadaMatricula` com a operação `matricular(String matricula, String cpf, String codigoTurma)`, movendo para ela a orquestração hoje presente na tela. `TelaMatricula` deve passar a receber **um** colaborador e a conter apenas a chamada e o tratamento de erro da interface.
5. **Justificar.** Explique neste README por que cada caso pediu o padrão escolhido, descrevendo **o problema** antes de nomear o padrão.

## Etapa 1 — A conversão duplicada do formato legado

### 1. Os dois trechos que convertem o formato legado

- **Trecho A:** `ImportadorAlunos.java`, método `importar()`, linhas 27 a 31.

```java
for (String[] linha : servicoLegado.consultarTabelaAlunos()) {
    Aluno aluno = new Aluno(linha[0], linha[1]);
    aluno.setAtivo("A".equals(linha[2]));   // não trata a letra minúscula
    alunos.add(aluno);
}
```

- **Trecho B:** `RelatorioSituacao.java`, método `imprimir()`, linhas 20 a 24.

```java
for (String[] linha : servicoLegado.consultarTabelaAlunos()) {
    Aluno aluno = new Aluno(linha[0], linha[1]);
    aluno.setAtivo("A".equalsIgnoreCase(linha[2]));   // aqui trata
    System.out.println("     " + aluno);
}
```

Os dois trechos repetem o mesmo conhecimento sobre o formato de `SecretariaLegadoWS`:

- a matriz devolvida por `consultarTabelaAlunos()` tem uma linha por aluno;
- a coluna 0 é a matrícula e a coluna 1 é o nome, usados no construtor de `Aluno`;
- a coluna 2 é a situação em letra, que precisa virar o `boolean` de `setAtivo`.

### 2. O ponto exato em que divergem

A divergência está na conversão da coluna 2, a situação, para `boolean`:

- **Trecho A**, `ImportadorAlunos.java` linha 29: `"A".equals(linha[2])`. Compara com diferença entre maiúscula e minúscula, então só `"A"` vira ativo.
- **Trecho B**, `RelatorioSituacao.java` linha 22: `"A".equalsIgnoreCase(linha[2])`. Ignora a caixa, então `"A"` e `"a"` viram ativo.

Todo o resto dos dois trechos é igual. Corrigiu o tratamento da letra minúscula no relatório e a correção não chegou ao importador.

| Situação no legado | Importador: `equals` | Relatório: `equalsIgnoreCase` |
|:---:|:---:|:---:|
| `"A"` | ativo | ativo |
| `"I"` | inativo | inativo |
| `"a"` | **inativo** | **ativo** |
| `"i"` | inativo | inativo |

### 3. Efeito na saída

O legado devolve a aluna `2024003 - Ana Pereira` com a situação `"a"`, minúscula. É a única linha que cai no caso divergente:

```
Situação segundo o importador:
  2024003 - Ana Pereira (inativo)

Situação segundo o relatório:
  2024003 - Ana Pereira (ativo)
```

As outras três alunas vêm com `"A"` ou `"I"` maiúsculos e aparecem iguais nos dois pontos. Por isso o erro passa despercebido enquanto o legado não manda uma letra minúscula.

### 4. Conclusão

**O problema:** a regra de conversão do formato legado não tem dono. Ela foi escrita duas vezes, e cada correção precisa ser lembrada nos dois lugares. Quando uma das cópias foi esquecida, o mesmo dado passou a ter dois significados no sistema.

A versão correta é a do relatório: o próprio `SecretariaLegadoWS` avisa no Javadoc que a situação vem "às vezes em minúscula". Na etapa 2, essa regra deve passar a existir em um único lugar, e os dois clientes vão consumir o resultado já convertido.

## Critério de sucesso

Ao final, verifique que: (a) a situação da aluna `2024003` é a **mesma** nos dois pontos do sistema; (b) `ImportadorAlunos` não menciona `SecretariaLegadoWS`; (c) `TelaMatricula` tem **um** atributo colaborador, não seis; (d) `SecretariaLegadoWS` continua **exatamente como veio**; e (e) o programa continua produzindo as mesmas matrículas e os mesmos erros de antes.

> O último item é importante: uma refatoração estrutural bem feita **não muda o comportamento externo** do sistema. Se a saída mudou — fora a correção da divergência do deslize 1 —, algo se perdeu no caminho.

## Padrão de entrega

Conforme a ficha de atividade prática: identificadores em português, um arquivo `.java` por classe pública, código formatado, entrega no repositório Git com README e commits descritivos. O uso de IA para gerar o código é proibido nesta atividade (ver seção 5.3 da ficha).

> **Projeto Integrador:** a Etapa 2, com entrega na Aula 13, exige a aplicação justificada de ao menos um padrão estrutural. O que você produzir aqui pode ser aproveitado nela — desde que a justificativa descreva o problema do SIGA, e não apenas o nome do padrão.
