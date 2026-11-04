package br.edu.unijorge.service;

import br.edu.unijorge.model.CargoEnum;
import br.edu.unijorge.model.TipoVotoEnum;
import br.edu.unijorge.model.Voto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApuracaoServiceTest {

    private final ApuracaoService apuracao = new ApuracaoService();
    private final CandidatoRepository candidatos = new CandidatoRepository();

    private List<Voto> amostra() {
        List<Voto> v = new ArrayList<>();
        v.add(new Voto(CargoEnum.VEREADOR, "11111", TipoVotoEnum.NOMINAL, "s"));
        v.add(new Voto(CargoEnum.VEREADOR, "11111", TipoVotoEnum.NOMINAL, "s"));
        v.add(new Voto(CargoEnum.VEREADOR, "22222", TipoVotoEnum.NOMINAL, "s"));
        v.add(new Voto(CargoEnum.VEREADOR, null, TipoVotoEnum.BRANCO, "s"));
        v.add(new Voto(CargoEnum.PREFEITO, "99", TipoVotoEnum.NULO, "s"));
        return v;
    }

    @Test
    void contaVotosPorCandidatoBrancosENulos() {
        String texto = apuracao.gerarRelatorio("s", amostra(), candidatos.getTodosCandidatos());

        assertTrue(texto.contains("SESSÃO: s"));
        assertTrue(texto.contains("total de votos: 4"));   // vereador
        assertTrue(texto.contains("total de votos: 1"));   // prefeito

        String ana = linhaCom(texto, "11111 - Ana Silva Fictícia");
        assertTrue(ana.matches(".*\\s2\\s+\\(\\s*50[.,]0%\\).*"), ana);
        assertTrue(linhaCom(texto, "Brancos").matches(".*\\s1\\s+\\(\\s*25[.,]0%\\).*"));

        String secaoPrefeito = texto.substring(texto.indexOf("CARGO: PREFEITO"));
        assertTrue(linhaCom(secaoPrefeito, "Nulos").matches(".*\\s1\\s+\\(\\s*100[.,]0%\\).*"));
    }

    @Test
    void semVotosNaoDividePorZero() {
        String texto = apuracao.gerarRelatorio("vazia", List.of(), candidatos.getTodosCandidatos());
        assertTrue(texto.contains("total de votos: 0"));
        assertFalse(texto.contains("NaN"));
    }

    @Test
    void exportaCsvComCabecalhoEUmaLinhaPorVoto(@TempDir Path dir) throws IOException {
        Path arquivo = dir.resolve("votos.csv");
        apuracao.exportarCsv(amostra(), arquivo);

        List<String> linhas = Files.readAllLines(arquivo);
        assertEquals("sessao,cargo,tipo_voto,numero_digitado,data_hora", linhas.get(0));
        assertEquals(6, linhas.size());
        assertTrue(linhas.get(1).startsWith("s,VEREADOR,NOMINAL,11111,"));
    }

    private String linhaCom(String texto, String trecho) {
        for (String l : texto.split("\n")) {
            if (l.contains(trecho)) {
                return l;
            }
        }
        fail("linha não encontrada: " + trecho);
        return "";
    }
}
