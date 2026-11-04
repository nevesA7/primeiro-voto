# Primeiro Voto — Simulador de Urna Eletrônica

Projeto de extensão de Programação Orientada a Objetos (UNIJORGE).
Simulador educativo de urna eletrônica em **Java 17 + Swing**, com votos gravados no
**Cloud Firestore (Firebase)**. Os candidatos, partidos e fotos são **fictícios**: a ação é apartidária.

A urna vota em sequência para **Vereador (5 dígitos)** e **Prefeito (2 dígitos)**, com BRANCO, CORRIGE e CONFIRMA
como na urna real, e volta sozinha para o próximo eleitor após o FIM.

## Como rodar

Requisitos: JDK 17+ e Maven.

```bash
mvn test                      # roda os testes
mvn package                   # gera target/primeiro-voto.jar
java -jar target/primeiro-voto.jar                 # pergunta o nome da sessão ao abrir
java -jar target/primeiro-voto.jar escola-2a-serie # ou já passa a sessão como argumento
```

Também dá para rodar direto pela IDE: classe `br.edu.unijorge.Main`.

## Configurando o Firebase

1. No console do Firebase, crie um projeto e ative o **Cloud Firestore**.
2. Em *Configurações do projeto → Contas de serviço*, gere uma chave privada (JSON).
3. Informe a chave **sem colocá-la no Git**, de um destes jeitos (nesta ordem de prioridade):
   - variável de ambiente `FIREBASE_CONFIG_PATH=/caminho/para/chave.json`;
   - arquivo `firebase-key.json` na pasta de onde o programa é executado (ao lado do `.jar`);
   - `src/main/resources/firebase-key.json` (só para desenvolvimento; já está no `.gitignore` e é removido do `.jar`).

Sem chave, a urna funciona em **modo local** (memória), e o rodapé mostra o modo atual.
Os votos ficam na coleção `votos_simulacao`, com os campos `cargo`, `tipoVoto`, `numeroDigitado`, `sessaoId`, `dataHora`.

> Se a internet cair durante a oficina, os votos vão para a fila `votos-pendentes.tsv` e são reenviados
> automaticamente. Não apague esse arquivo antes de a fila esvaziar.

## Usando na oficina

- **Sessão**: cada oficina/turma deve ter um nome próprio (ex.: `escola-x-2a-serie-12nov`). Ele separa os resultados.
  Dá para trocar em *Oficina → Trocar sessão*.
- **Teclado físico**: números 0–9, `Enter` = CONFIRMA, `Backspace`/`Esc` = CORRIGE, `B` = BRANCO.
- **Apuração** (`Ctrl+R`): contagem por candidato, brancos e nulos da sessão, com botão **Exportar CSV**
  (útil para os resultados do Relatório Final, Desafio 3).
- **Atividade de desinformação** (`Ctrl+Q`): quiz "Verdadeiro ou Falso" sobre urna eletrônica, deepfakes e checagem de fatos.

## Estrutura (POO)

| Pacote | Conteúdo |
|---|---|
| `model` | `Candidato` (abstrata) → `CandidatoProporcional`, `CandidatoMajoritario`; `Voto`; enums `CargoEnum`, `TipoVotoEnum` |
| `service` | `VotoRepository` (interface) → `FirebaseService`, `MemoriaVotoRepository`; `CandidatoRepository`; `ApuracaoService` |
| `controller` | `UrnaController` (regras da urna; grava os votos fora da thread da interface) |
| `view` | `UrnaView`, `ResultadoView`, `QuizDesinformacaoDialog` |
| `config` / `util` | `FirebaseConfig`; `SomUrna` |

Conceitos aplicados: **abstração** (`Candidato`, `VotoRepository`), **herança** (candidatos), **polimorfismo**
(`getCargo()`, repositórios intercambiáveis), **encapsulamento** (atributos privados + getters/setters).

## Trocando as fotos

As imagens em `src/main/resources/images` são ilustrações genéricas. Para trocar, mantenha os nomes
(`cand_<número>.png`, `vice_<número>.png`), de preferência 260×320 px, e use apenas imagens sem pessoas reais
(ilustrações, avatares ou imagens geradas, sempre fictícias).
