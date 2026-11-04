package br.edu.unijorge.service;

import br.edu.unijorge.model.Voto;

import java.util.ArrayList;
import java.util.List;

/** Armazena votos em memória. Usado como modo de simulação local e nos testes. */
public class MemoriaVotoRepository implements VotoRepository {

    private final List<Voto> votos = new ArrayList<>();

    @Override
    public synchronized void salvar(Voto voto) {
        votos.add(voto);
    }

    @Override
    public synchronized List<Voto> listarPorSessao(String sessaoId) {
        List<Voto> resultado = new ArrayList<>();
        for (Voto v : votos) {
            if (sessaoId == null || sessaoId.equals(v.getSessaoId())) {
                resultado.add(v);
            }
        }
        return resultado;
    }

    @Override
    public String getStatus() {
        return "Modo local (memória)";
    }
}
