package br.edu.unijorge.model;

public enum CargoEnum {
    VEREADOR("Vereador", 5),
    PREFEITO("Prefeito", 2);

    private final String descricao;
    private final int qtdDigitos;

    CargoEnum(String descricao, int qtdDigitos) {
        this.descricao = descricao;
        this.qtdDigitos = qtdDigitos;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getQtdDigitos() {
        return qtdDigitos;
    }
}