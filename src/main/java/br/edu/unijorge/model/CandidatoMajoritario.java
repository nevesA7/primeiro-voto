package br.edu.unijorge.model;

public class CandidatoMajoritario extends Candidato {
    private String nomeVice;
    private String caminhoFotoVice;

    public CandidatoMajoritario(String numero, String nome, String partidoFicticio, String siglaPartido,
                                String caminhoFoto, String nomeVice, String caminhoFotoVice) {
        super(numero, nome, partidoFicticio, siglaPartido, caminhoFoto);
        this.nomeVice = nomeVice;
        this.caminhoFotoVice = caminhoFotoVice;
    }

    @Override
    public CargoEnum getCargo() {
        return CargoEnum.PREFEITO;
    }

    @Override
    public String getDescricaoExibicao() {
        return String.format("Prefeito: %s (Vice: %s) - %s", getNome(), nomeVice, getSiglaPartido());
    }

    public String getNomeVice() {
        return nomeVice;
    }

    public void setNomeVice(String nomeVice) {
        this.nomeVice = nomeVice;
    }

    public String getCaminhoFotoVice() {
        return caminhoFotoVice;
    }

    public void setCaminhoFotoVice(String caminhoFotoVice) {
        this.caminhoFotoVice = caminhoFotoVice;
    }
}
