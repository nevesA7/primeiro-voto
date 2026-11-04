package br.edu.unijorge.model;

import java.util.Date;

/**
 * Voto registrado na simulação.
 *
 * Os campos são String/Date porque o Firestore serializa POJOs por getters e setters.
 * A conversão para enum é feita pelos métodos {@link #tipo()} e {@link #cargoEnum()},
 * que não seguem o padrão getX e por isso não são gravados no banco.
 */
public class Voto {
    private String cargo;
    private String numeroDigitado; // nulo quando o voto é em branco
    private String tipoVoto;
    private String sessaoId;       // identifica a oficina/turma (ex.: "escola-x-2a-serie")
    private Date dataHora;

    // Construtor sem argumentos (obrigatório para a desserialização no Firebase)
    public Voto() {
    }

    public Voto(CargoEnum cargo, String numeroDigitado, TipoVotoEnum tipoVoto, String sessaoId) {
        this(cargo, numeroDigitado, tipoVoto, sessaoId, new Date());
    }

    public Voto(CargoEnum cargo, String numeroDigitado, TipoVotoEnum tipoVoto, String sessaoId, Date dataHora) {
        this.cargo = cargo.name();
        this.numeroDigitado = numeroDigitado;
        this.tipoVoto = tipoVoto.name();
        this.sessaoId = sessaoId;
        this.dataHora = dataHora;
    }

    public TipoVotoEnum tipo() {
        return TipoVotoEnum.valueOf(tipoVoto);
    }

    public CargoEnum cargoEnum() {
        return CargoEnum.valueOf(cargo);
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getNumeroDigitado() {
        return numeroDigitado;
    }

    public void setNumeroDigitado(String numeroDigitado) {
        this.numeroDigitado = numeroDigitado;
    }

    public String getTipoVoto() {
        return tipoVoto;
    }

    public void setTipoVoto(String tipoVoto) {
        this.tipoVoto = tipoVoto;
    }

    public String getSessaoId() {
        return sessaoId;
    }

    public void setSessaoId(String sessaoId) {
        this.sessaoId = sessaoId;
    }

    public Date getDataHora() {
        return dataHora;
    }

    public void setDataHora(Date dataHora) {
        this.dataHora = dataHora;
    }
}
