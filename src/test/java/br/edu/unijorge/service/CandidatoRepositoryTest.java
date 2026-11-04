package br.edu.unijorge.service;

import br.edu.unijorge.model.Candidato;
import br.edu.unijorge.model.CandidatoMajoritario;
import br.edu.unijorge.model.CargoEnum;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CandidatoRepositoryTest {

    private final CandidatoRepository repo = new CandidatoRepository();

    @Test
    void buscaPorNumeroECargo() {
        Optional<Candidato> c = repo.buscarPorNumeroECargo("11", CargoEnum.PREFEITO);
        assertTrue(c.isPresent());
        assertInstanceOf(CandidatoMajoritario.class, c.get());
        assertEquals(CargoEnum.PREFEITO, c.get().getCargo());
    }

    @Test
    void numeroDeVereadorNaoServeParaPrefeito() {
        assertTrue(repo.buscarPorNumeroECargo("11111", CargoEnum.PREFEITO).isEmpty());
        assertTrue(repo.buscarPorNumeroECargo("11", CargoEnum.VEREADOR).isEmpty());
    }

    @Test
    void numeroInexistenteNaoRetornaCandidato() {
        assertTrue(repo.buscarPorNumeroECargo("99", CargoEnum.PREFEITO).isEmpty());
        assertTrue(repo.buscarPorNumeroECargo("99999", CargoEnum.VEREADOR).isEmpty());
    }

    @Test
    void numerosTemTamanhoCertoESaoUnicosPorCargo() {
        for (CargoEnum cargo : CargoEnum.values()) {
            List<Candidato> lista = repo.getCandidatosPorCargo(cargo);
            assertFalse(lista.isEmpty());
            Set<String> numeros = new HashSet<>();
            for (Candidato c : lista) {
                assertEquals(cargo.getQtdDigitos(), c.getNumero().length());
                assertTrue(numeros.add(c.getNumero()), "número repetido: " + c.getNumero());
            }
        }
    }

    @Test
    void todosOsCandidatosSaoFicticios() {
        for (Candidato c : repo.getTodosCandidatos()) {
            assertTrue(c.getNome().contains("Fictíci"), c.getNome());
        }
    }

    @Test
    void viceNaoTemMarcadorDuplicado() {
        CandidatoMajoritario m = (CandidatoMajoritario) repo.buscarPorNumeroECargo("22", CargoEnum.PREFEITO).get();
        assertFalse(m.getNomeVice().contains("(Vice)"));
    }

    @Test
    void fotosDosCandidatosExistemNosRecursos() {
        for (Candidato c : repo.getTodosCandidatos()) {
            assertNotNull(getClass().getResource(c.getCaminhoFoto()), "falta imagem: " + c.getCaminhoFoto());
            if (c instanceof CandidatoMajoritario) {
                String vice = ((CandidatoMajoritario) c).getCaminhoFotoVice();
                assertNotNull(getClass().getResource(vice), "falta imagem: " + vice);
            }
        }
    }
}
