package br.edu.unijorge.service;

import br.edu.unijorge.model.Voto;

import java.util.List;

/**
 * Abstração de onde os votos são guardados (nuvem, memória...).
 * O controller e as telas dependem só desta interface.
 */
public interface VotoRepository {

    /** Salva o voto. Nunca deve lançar exceção para a interface gráfica. */
    void salvar(Voto voto);

    /** Lista os votos de uma sessão (oficina/turma). */
    List<Voto> listarPorSessao(String sessaoId);

    /** Descrição curta do modo de armazenamento, exibida no rodapé da urna. */
    String getStatus();
}
