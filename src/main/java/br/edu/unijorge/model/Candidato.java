package br.edu.unijorge.model;

public abstract class Candidato {
    private String numero;
    private String nome;
    private String partidoFicticio;
    private String siglaPartido;
    private String caminhoFoto;

    public Candidato(String numero, String nome, String partidoFicticio, String siglaPartido, String caminhoFoto) {
        this.numero = numero;
        this.nome = nome;
        this.partidoFicticio = partidoFicticio;
        this.siglaPartido = siglaPartido;
        this.caminhoFoto = caminhoFoto;
    }

    // Método abstrato que força o comportamento polimórfico nas subclasse
    public abstract CargoEnum getCargo();

    // Método abstrato para formatação textual do candidato na interface da urna
    public abstract String getDescricaoExibicao();

    // Getters e Setters (Encapsulamento)
    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPartidoFicticio() {
        return partidoFicticio;
    }

    public void setPartidoFicticio(String partidoFicticio) {
        this.partidoFicticio = partidoFicticio;
    }

    public String getSiglaPartido() {
        return siglaPartido;
    }

    public void setSiglaPartido(String siglaPartido) {
        this.siglaPartido = siglaPartido;
    }

    public String getCaminhoFoto() {
        return caminhoFoto;
    }

    public void setCaminhoFoto(String caminhoFoto) {
        this.caminhoFoto = caminhoFoto;
    }
}