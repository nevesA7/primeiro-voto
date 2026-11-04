package br.edu.unijorge.service;

import br.edu.unijorge.model.Candidato;
import br.edu.unijorge.model.CargoEnum;
import br.edu.unijorge.model.TipoVotoEnum;
import br.edu.unijorge.model.Voto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/** Conta os votos e gera o texto da apuração e o arquivo CSV da sessão. */
public class ApuracaoService {

    public String gerarRelatorio(String sessaoId, List<Voto> votos, List<Candidato> candidatos) {
        StringBuilder sb = new StringBuilder();
        sb.append("APURAÇÃO - SESSÃO: ").append(sessaoId).append('\n');
        sb.append("(simulação educativa, candidatos fictícios)\n\n");

        for (CargoEnum cargo : CargoEnum.values()) {
            List<Voto> doCargo = new ArrayList<>();
            for (Voto v : votos) {
                if (cargo.name().equals(v.getCargo())) {
                    doCargo.add(v);
                }
            }
            int total = doCargo.size();

            sb.append("CARGO: ").append(cargo.getDescricao().toUpperCase())
                    .append("  (total de votos: ").append(total).append(")\n");

            for (Candidato c : candidatos) {
                if (c.getCargo() != cargo) {
                    continue;
                }
                int qtd = 0;
                for (Voto v : doCargo) {
                    if (v.tipo() == TipoVotoEnum.NOMINAL && c.getNumero().equals(v.getNumeroDigitado())) {
                        qtd++;
                    }
                }
                sb.append(linha(c.getNumero() + " - " + c.getNome() + " (" + c.getSiglaPartido() + ")", qtd, total));
            }

            sb.append(linha("Brancos", contar(doCargo, TipoVotoEnum.BRANCO), total));
            sb.append(linha("Nulos", contar(doCargo, TipoVotoEnum.NULO), total));
            sb.append('\n');
        }
        return sb.toString();
    }

    public void exportarCsv(List<Voto> votos, Path destino) throws IOException {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<String> linhas = new ArrayList<>();
        linhas.add("sessao,cargo,tipo_voto,numero_digitado,data_hora");
        for (Voto v : votos) {
            linhas.add(String.join(",",
                    nz(v.getSessaoId()), nz(v.getCargo()), nz(v.getTipoVoto()),
                    nz(v.getNumeroDigitado()),
                    v.getDataHora() == null ? "" : fmt.format(v.getDataHora())));
        }
        Files.write(destino, linhas, StandardCharsets.UTF_8);
    }

    private int contar(List<Voto> votos, TipoVotoEnum tipo) {
        int n = 0;
        for (Voto v : votos) {
            if (v.tipo() == tipo) {
                n++;
            }
        }
        return n;
    }

    private String linha(String rotulo, int qtd, int total) {
        double pct = total == 0 ? 0 : (qtd * 100.0) / total;
        return String.format(" - %-48s %4d  (%5.1f%%)%n", rotulo, qtd, pct);
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }
}
