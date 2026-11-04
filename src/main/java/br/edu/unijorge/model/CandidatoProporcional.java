package br.edu.unijorge.model;

public class CandidatoProporcional extends Candidato {

    public CandidatoProporcional(String numero, String nome, String partidoFicticio, String siglaPartido, String caminhoFoto) {
        super(numero, nome, partidoFicticio, siglaPartido, caminhoFoto);
    }

    @Override
    public CargoEnum getCargo() {
        return CargoEnum.VEREADOR;
    }

    @Override
    public String getDescricaoExibicao() {
        return String.format("Vereador: %s (%s) - Nº %s", getNome(), getSiglaPartido(), getNumero());
    }
}