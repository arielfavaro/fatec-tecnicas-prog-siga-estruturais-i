# SIGA — Atividade de padrões estruturais: Adapter e Facade

**Técnicas de Programação II (TP2) · Aula 10** — CST em Desenvolvimento de Software Multiplataforma · Fatec de Porto Ferreira

Repositório da atividade prática da Aula 10. O código inicial tinha três deslizes de estrutura: a conversão do formato legado escrita duas vezes e já divergente, um importador preso à classe concreta do serviço externo e uma tela que conhecia todo o subsistema de matrícula. A refatoração aplica o padrão **Adapter** entre o SIGA e o serviço legado da secretaria e o padrão **Facade** na frente do subsistema de matrícula.

## Estrutura final do projeto

```
siga-estruturais/
├── src/
│   └── siga/
│       ├── Aluno.java                      (entidade de domínio)
│       ├── Matricula.java                  (entidade de domínio)
│       ├── AlunoDAO.java                   (interface de persistência dos alunos)
│       ├── AlunoDAOMemoria.java            (implementação em memória do AlunoDAO)
│       ├── MatriculaDAO.java               (interface de persistência das matrículas)
│       ├── MatriculaDAOMemoria.java        (implementação em memória do MatriculaDAO)
│       ├── SecretariaLegadoWS.java         (componente externo; adaptado do Adapter, sem alteração)
│       ├── FonteDeAlunos.java              (alvo do Adapter: o que os clientes esperam)
│       ├── AdaptadorSecretariaLegado.java  (adaptador: único lugar que conhece o formato legado)
│       ├── ImportadorAlunos.java           (cliente do Adapter: importa os alunos para o DAO)
│       ├── RelatorioSituacao.java          (cliente do Adapter: imprime a situação dos alunos)
│       ├── ValidadorCpf.java               (subsistema de matrícula)
│       ├── ControleVagas.java              (subsistema de matrícula)
│       ├── CalculadoraDesconto.java        (subsistema de matrícula)
│       ├── NotificadorEmail.java           (subsistema de matrícula)
│       ├── FachadaMatricula.java           (fachada do Facade: orquestra o subsistema de matrícula)
│       ├── TelaMatricula.java              (cliente do Facade: só chama a fachada e trata o erro)
│       └── Main.java                       (monta as peças e demonstra os deslizes resolvidos)
├── Dockerfile
├── docker-compose.yml
└── README.md
```

## Como compilar e executar

Pré-requisito: JDK 17 ou superior (`java -version` para verificar).

```bash
# 1. Compilar (a saída vai para a pasta "bin")
javac -encoding UTF-8 -d bin src/siga/*.java

# 2. Executar
java -cp bin siga.Main
```

A saída mostra a aluna `2024003` ativa tanto no importador quanto no relatório e as quatro tentativas de matrícula pela tela, com uma matrícula realizada e três erros.

## Os três deslizes do código inicial

O código inicial compilava e executava, mas não aguentava crescer. Estes eram os três deslizes, todos resolvidos nas etapas abaixo.

| # | Deslize | Onde | Por que é um problema |
|---|---|---|---|
| 1 | **Conversão duplicada e divergente** | `ImportadorAlunos` e `RelatorioSituacao` | O conhecimento do formato legado (coluna 0 é matrícula, 2 é a situação em letra) foi escrito duas vezes. Alguém corrigiu o tratamento da letra minúscula em um dos dois e não no outro: a aluna `2024003` aparece **inativa** no importador e **ativa** no relatório. |
| 2 | **Dependência de classe concreta** | `ImportadorAlunos` | O importador recebe `SecretariaLegadoWS` no construtor. Trocar a origem dos alunos exige alterar o importador, e testá-lo exige o serviço externo. Violação do DIP (Aula 3). |
| 3 | **Subsistema exposto à apresentação** | `TelaMatricula` | A tela recebe **seis colaboradores** e conhece a ordem correta de chamá-los — reservar a vaga só depois de gravar a matrícula. Essa ordem é regra de negócio e não deveria morar na tela. |

## O que foi feito em cada etapa

1. **Etapa 1 - Localizar a duplicação:**
   - Registrada na seção [Etapa 1 — A conversão duplicada do formato legado](#etapa-1--a-conversão-duplicada-do-formato-legado): os dois trechos estão em `ImportadorAlunos.importar()` e `RelatorioSituacao.imprimir()`, e divergem na conversão da coluna 2, com `equals` em um e `equalsIgnoreCase` no outro.

2. **Etapa 2 - Adapter:**
   - Criada a interface `FonteDeAlunos`, com `List<Aluno> listar()`.
   - Criada a classe `AdaptadorSecretariaLegado`, que implementa `FonteDeAlunos` e recebe `SecretariaLegadoWS` por composição, como atributo.
   - A posição das colunas fica em constantes, e a regra da letra minúscula fica só no método `estaAtivo`, com `equalsIgnoreCase`.

3. **Etapa 3 - Clientes do Adapter:**
   - `ImportadorAlunos` e `RelatorioSituacao` passaram a receber `FonteDeAlunos` e só percorrem os alunos já convertidos. A palavra `SecretariaLegadoWS` não aparece mais em nenhum dos dois, nem nos comentários.
   - O `Main` cria um único `AdaptadorSecretariaLegado` e o entrega aos dois clientes.
   - A saída foi comparada com a de antes: a única diferença é a aluna `2024003`, que passou a aparecer ativa no importador.

4. **Etapa 4 - Facade:**
   - Criada a classe `FachadaMatricula` com `matricular(String matricula, String cpf, String codigoTurma)`. Ela recebe as seis peças do subsistema e concentra a ordem das chamadas: validar, gravar a matrícula, reservar a vaga e notificar.
   - `TelaMatricula` passou a ter um único atributo, a fachada, e contém só a chamada e o tratamento de erro.
   - A saída foi comparada com a de antes: as matrículas e os erros são os mesmos.

5. **Etapa 5 - Justificativa:**
   - Registrada na seção [Etapa 5 — Por que cada caso pediu o padrão escolhido](#etapa-5--por-que-cada-caso-pediu-o-padrão-escolhido): o problema de cada deslize vem antes do nome do padrão, com uma comparação entre Adapter e Facade.

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

## Etapa 5 — Por que cada caso pediu o padrão escolhido

### 1. Deslizes 1 e 2: o formato do legado espalhado pelos clientes

**O problema:** o SIGA trabalha com objetos `Aluno`, mas o serviço da secretaria devolve uma matriz de texto: uma linha por aluno, com a matrícula na coluna 0, o nome na coluna 1 e a situação em letra na coluna 2, às vezes minúscula. São duas formas incompatíveis de representar o mesmo dado.

- O `SecretariaLegadoWS` é de outra equipe e não pode ser alterado. Não dá para fazer ele devolver `Aluno`.
- Os clientes também não deveriam conhecer a matriz. Mesmo assim, `ImportadorAlunos` e `RelatorioSituacao` faziam a tradução cada um por conta própria. Cada cópia evoluiu de um jeito e a aluna `2024003` passou a ter duas situações.
- O importador recebia a classe concreta do legado no construtor. Trocar a origem dos alunos ou testar o importador sem o serviço externo exigia alterar o importador, o que viola o DIP.

O que faltava era uma peça no meio: algo que recebesse o serviço do jeito que ele é e entregasse aos clientes o que eles esperam, sem mudar nenhum dos dois lados.

**O padrão:** essa é a situação do **Adapter**. Ele converte a interface de uma classe existente na interface que o cliente espera.

| Papel no Adapter | Classe no projeto |
|---|---|
| Alvo: o que o cliente espera | `FonteDeAlunos`, com `List<Aluno> listar()` |
| Adaptado: o que já existe e não muda | `SecretariaLegadoWS` |
| Adaptador: faz a tradução | `AdaptadorSecretariaLegado` |
| Clientes | `ImportadorAlunos` e `RelatorioSituacao` |

**Alterações:**

- A tradução da matriz existe só em `AdaptadorSecretariaLegado`, e a regra da letra minúscula fica no método `estaAtivo`. Uma correção futura no formato é feita uma vez e vale para os dois clientes.
- Os dois clientes recebem `FonteDeAlunos` e não citam mais `SecretariaLegadoWS`. A aluna `2024003` aparece ativa nos dois pontos.
- O adaptador recebe o serviço por composição, como atributo, em vez de estender `SecretariaLegadoWS`. Assim o método `consultarTabelaAlunos` não vaza para quem usa `FonteDeAlunos`, e os clientes ficam presos só à interface.
- Uma nova origem de alunos, como um arquivo CSV ou um banco, vira outra implementação de `FonteDeAlunos`. Num teste, basta uma fonte falsa com alunos fixos. Em nenhum dos casos o importador precisa mudar.

### 2. Deslize 3: o subsistema de matrícula exposto à tela

**O problema:** matricular um aluno passa por seis classes e sete passos: validar o CPF, buscar o aluno, conferir a vaga, calcular o desconto, gravar a matrícula, reservar a vaga e enviar o e-mail. A tela recebia as seis no construtor e fazia as chamadas ela mesma.

- A ordem das chamadas é regra de negócio: a vaga só pode ser reservada depois que a matrícula foi gravada. Essa regra morava na camada de apresentação.
- Qualquer outro ponto que precisasse matricular, como a matrícula em lote que a secretaria vai pedir, teria que copiar essas linhas. Copiadas, elas iam divergir como aconteceu no deslize 1.
- Uma mudança no subsistema, como uma peça nova ou uma ordem diferente, obrigava a alterar a tela, que não deveria saber disso.

As peças do subsistema estão certas e não precisam mudar. O que faltava era um ponto de entrada único: uma operação "matricular" que esconda quantas peças existem e em que ordem elas são chamadas.

**O padrão:** essa é a situação do **Facade**. Ele oferece uma interface simples para um conjunto de classes, sem alterar essas classes.

| Papel no Facade | Classe no projeto |
|---|---|
| Fachada | `FachadaMatricula`, com `matricular(matricula, cpf, codigoTurma)` |
| Subsistema | `ValidadorCpf`, `AlunoDAO`, `ControleVagas`, `CalculadoraDesconto`, `MatriculaDAO`, `NotificadorEmail` |
| Cliente | `TelaMatricula` |

**Alterações:**

- A sequência validar, gravar, reservar e notificar está só em `FachadaMatricula.matricular`.
- `TelaMatricula` tem um único atributo, a fachada, e faz só a chamada e o tratamento de erro.
- A matrícula em lote pode chamar a mesma fachada, sem copiar a ordem das chamadas.
- As matrículas e os erros impressos continuam os mesmos de antes: a fachada só mudou o lugar onde a orquestração mora.

### 3. Por que não o contrário

Os dois padrões são estruturais e os dois colocam uma classe entre o cliente e outras classes, mas resolvem problemas diferentes:

| | Adapter | Facade |
|---|---|---|
| Problema | uma interface existe, mas não é a que o cliente espera | muitas classes, e o cliente precisa saber como coordená-las |
| Quantas classes ficam atrás | uma: `SecretariaLegadoWS` | várias: as seis peças da matrícula |
| O que entrega ao cliente | a mesma informação, em outro formato | uma operação mais simples |

- O caso do legado não pedia Facade: o problema não era ter muitas classes, era uma única classe com um formato que o SIGA não usa.
- O caso da tela não pedia Adapter: nenhuma peça do subsistema tinha interface errada. O problema era a quantidade de peças e a ordem entre elas estar na tela.

## Critérios de sucesso atendidos

- [x] **(a)** A situação da aluna `2024003` é a mesma nos dois pontos do sistema: ativa no importador e no relatório. Os dois consomem a mesma `FonteDeAlunos`, e a regra da letra minúscula existe só em `AdaptadorSecretariaLegado`.
- [x] **(b)** `ImportadorAlunos` não menciona `SecretariaLegadoWS`, nem no código nem nos comentários. `RelatorioSituacao` também não.
- [x] **(c)** `TelaMatricula` tem um atributo colaborador, `FachadaMatricula fachada`, e não seis.
- [x] **(d)** `SecretariaLegadoWS` continua exatamente como veio: o arquivo não tem nenhuma alteração desde o commit inicial.
- [x] **(e)** O programa continua produzindo as mesmas matrículas e os mesmos erros de antes: a matrícula de Maria Silva com 50% de desconto, CPF inválido, aluno `2024009` não encontrado e turma `DSM-2B` sem vagas.

> O último item é importante: uma refatoração estrutural bem feita **não muda o comportamento externo** do sistema. Se a saída mudou — fora a correção da divergência do deslize 1 —, algo se perdeu no caminho.

## Padrão de entrega

Conforme a ficha de atividade prática: identificadores em português, um arquivo `.java` por classe pública, código formatado, entrega no repositório Git com README e commits descritivos. O uso de IA para gerar o código é proibido nesta atividade (ver seção 5.3 da ficha).

> **Projeto Integrador:** a Etapa 2, com entrega na Aula 13, exige a aplicação justificada de ao menos um padrão estrutural. O que você produzir aqui pode ser aproveitado nela — desde que a justificativa descreva o problema do SIGA, e não apenas o nome do padrão.
