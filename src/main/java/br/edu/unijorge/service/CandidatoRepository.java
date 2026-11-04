package br.edu.unijorge.service;

import br.edu.unijorge.model.Candidato;
import br.edu.unijorge.model.CandidatoMajoritario;
import br.edu.unijorge.model.CandidatoProporcional;
import br.edu.unijorge.model.CargoEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Cadastro de candidatos FICTÍCIOS da simulação (sem vínculo com partidos ou pessoas reais).
 */
public class CandidatoRepository {

    private final List<Candidato> candidatos;

    public CandidatoRepository() {
        this.candidatos = new ArrayList<>();
        carregarCandidatosFicticios();
    }

    private void carregarCandidatosFicticios() {
        // Vereador (5 dígitos)
        candidatos.add(new CandidatoProporcional("11111", "Ana Silva Fictícia",
                "Partido da Cidadania Criativa", "PCC", "/images/cand_11111.png"));
        candidatos.add(new CandidatoProporcional("22222", "Carlos Souza Fictício",
                "Partido do Futuro Digital", "PFD", "/images/cand_22222.png"));
        candidatos.add(new CandidatoProporcional("33333", "Juliana Rocha Fictícia",
                "Partido Voz da Juventude", "PVJ", "/images/cand_33333.png"));
        candidatos.add(new CandidatoProporcional("44444", "Pedro Nogueira Fictício",
                "Partido Voz da Juventude", "PVJ", "/images/cand_44444.png"));

        // Prefeito (2 dígitos)
        candidatos.add(new CandidatoMajoritario("11", "Mariana Lima Fictícia",
                "Partido da Cidadania Criativa", "PCC", "/images/cand_11.png",
                "Roberto Alves Fictício", "/images/vice_11.png"));
        candidatos.add(new CandidatoMajoritario("22", "Fernando Costa Fictício",
                "Partido do Futuro Digital", "PFD", "/images/cand_22.png",
                "Beatriz Ramos Fictícia", "/images/vice_22.png"));
        candidatos.add(new CandidatoMajoritario("33", "Helena Prado Fictícia",
                "Partido Voz da Juventude", "PVJ", "/images/cand_33.png",
                "Davi Moura Fictício", "/images/vice_33.png"));
    }

    public Optional<Candidato> buscarPorNumeroECargo(String numero, CargoEnum cargo) {
        return candidatos.stream()
                .filter(c -> c.getCargo() == cargo && c.getNumero().equals(numero))
                .findFirst();
    }

    public List<Candidato> getCandidatosPorCargo(CargoEnum cargo) {
        List<Candidato> resultado = new ArrayList<>();
        for (Candidato c : candidatos) {
            if (c.getCargo() == cargo) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    public List<Candidato> getTodosCandidatos() {
        return new ArrayList<>(candidatos);
    }
}
