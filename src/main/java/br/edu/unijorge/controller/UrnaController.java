package br.edu.unijorge.controller;

import br.edu.unijorge.model.Candidato;
import br.edu.unijorge.model.CargoEnum;
import br.edu.unijorge.model.TipoVotoEnum;
import br.edu.unijorge.model.Voto;
import br.edu.unijorge.service.CandidatoRepository;
import br.edu.unijorge.service.VotoRepository;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Regras da urna: digitação, BRANCO, CORRIGE, CONFIRMA e passagem de VEREADOR para PREFEITO.
 * A gravação do voto roda em outra thread para não travar a interface.
 */
public class UrnaController {

    private final CandidatoRepository candidatoRepository;
    private final VotoRepository votoRepository;
    private final ExecutorService gravador;

    private volatile String sessaoId;
    private CargoEnum cargoAtual;
    private final StringBuilder numeroDigitado;
    private boolean branco;
    private boolean votacaoFinalizada;
    private String aviso;

    public UrnaController(CandidatoRepository candidatoRepository, VotoRepository votoRepository, String sessaoId) {
        this(candidatoRepository, votoRepository, sessaoId, criarGravadorPadrao());
    }

    public UrnaController(CandidatoRepository candidatoRepository, VotoRepository votoRepository,
                          String sessaoId, ExecutorService gravador) {
        this.candidatoRepository = candidatoRepository;
        this.votoRepository = votoRepository;
        this.sessaoId = sessaoId;
        this.gravador = gravador;
        this.numeroDigitado = new StringBuilder();
        this.cargoAtual = CargoEnum.VEREADOR; // primeira etapa
        this.votacaoFinalizada = false;
        this.aviso = "";
    }

    private static ExecutorService criarGravadorPadrao() {
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "gravador-votos");
            t.setDaemon(true);
            return t;
        });
    }

    // ------------------------------------------------------------ ações da urna

    /** @return true se o dígito foi aceito */
    public boolean digitarNumero(char digito) {
        if (votacaoFinalizada || !Character.isDigit(digito)) {
            return false;
        }
        if (branco) {
            aviso = "Para digitar um número, aperte CORRIGE antes.";
            return false;
        }
        if (numeroDigitado.length() >= cargoAtual.getQtdDigitos()) {
            return false;
        }
        numeroDigitado.append(digito);
        aviso = "";
        return true;
    }

    public void corrigir() {
        if (votacaoFinalizada) {
            return;
        }
        numeroDigitado.setLength(0);
        branco = false;
        aviso = "";
    }

    /** Como na urna real: BRANCO só vale com o campo vazio; o voto só é gravado no CONFIRMA. */
    public boolean votarBranco() {
        if (votacaoFinalizada) {
            return false;
        }
        if (numeroDigitado.length() > 0) {
            aviso = "Para votar em BRANCO, aperte CORRIGE e depois BRANCO.";
            return false;
        }
        branco = true;
        aviso = "";
        return true;
    }

    /** @return true se o voto foi registrado */
    public boolean confirmarVoto() {
        if (votacaoFinalizada) {
            return false;
        }

        Voto voto;
        if (branco) {
            voto = new Voto(cargoAtual, null, TipoVotoEnum.BRANCO, sessaoId);
        } else if (numeroDigitado.length() < cargoAtual.getQtdDigitos()) {
            aviso = "Digite o número completo ou aperte BRANCO.";
            return false;
        } else {
            TipoVotoEnum tipo = buscarCandidatoAtual().isPresent() ? TipoVotoEnum.NOMINAL : TipoVotoEnum.NULO;
            voto = new Voto(cargoAtual, numeroDigitado.toString(), tipo, sessaoId);
        }

        gravador.submit(() -> votoRepository.salvar(voto));
        avancarEtapa();
        return true;
    }

    private void avancarEtapa() {
        numeroDigitado.setLength(0);
        branco = false;
        aviso = "";
        if (cargoAtual == CargoEnum.VEREADOR) {
            cargoAtual = CargoEnum.PREFEITO;
        } else {
            votacaoFinalizada = true;
        }
    }

    public void reiniciarVotacao() {
        cargoAtual = CargoEnum.VEREADOR;
        numeroDigitado.setLength(0);
        branco = false;
        votacaoFinalizada = false;
        aviso = "";
    }

    /** Espera as gravações pendentes terminarem (chamado ao fechar a urna). */
    public void encerrar() {
        gravador.shutdown();
        try {
            gravador.awaitTermination(15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ------------------------------------------------------------------ consultas

    public Optional<Candidato> buscarCandidatoAtual() {
        if (!branco && numeroDigitado.length() == cargoAtual.getQtdDigitos()) {
            return candidatoRepository.buscarPorNumeroECargo(numeroDigitado.toString(), cargoAtual);
        }
        return Optional.empty();
    }

    public boolean isNumeroCompleto() {
        return numeroDigitado.length() == cargoAtual.getQtdDigitos();
    }

    public CargoEnum getCargoAtual() {
        return cargoAtual;
    }

    public String getNumeroDigitado() {
        return numeroDigitado.toString();
    }

    public boolean isBranco() {
        return branco;
    }

    public boolean isVotacaoFinalizada() {
        return votacaoFinalizada;
    }

    public String getAviso() {
        return aviso;
    }

    public String getSessaoId() {
        return sessaoId;
    }

    public void setSessaoId(String sessaoId) {
        this.sessaoId = sessaoId;
    }

    public String getStatusArmazenamento() {
        return votoRepository.getStatus();
    }

    public VotoRepository getVotoRepository() {
        return votoRepository;
    }

    public CandidatoRepository getCandidatoRepository() {
        return candidatoRepository;
    }
}
