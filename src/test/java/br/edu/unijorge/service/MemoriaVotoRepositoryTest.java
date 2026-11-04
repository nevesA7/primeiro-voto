package br.edu.unijorge.service;

import br.edu.unijorge.model.CargoEnum;
import br.edu.unijorge.model.TipoVotoEnum;
import br.edu.unijorge.model.Voto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MemoriaVotoRepositoryTest {

    @Test
    void separaVotosPorSessao() {
        MemoriaVotoRepository repo = new MemoriaVotoRepository();
        repo.salvar(new Voto(CargoEnum.VEREADOR, null, TipoVotoEnum.BRANCO, "turma-a"));
        repo.salvar(new Voto(CargoEnum.PREFEITO, "11", TipoVotoEnum.NOMINAL, "turma-b"));

        assertEquals(1, repo.listarPorSessao("turma-a").size());
        assertEquals(1, repo.listarPorSessao("turma-b").size());
        assertEquals(0, repo.listarPorSessao("outra").size());
    }
}
