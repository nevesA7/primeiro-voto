package br.edu.unijorge.controller;

import br.edu.unijorge.model.CargoEnum;
import br.edu.unijorge.model.TipoVotoEnum;
import br.edu.unijorge.model.Voto;
import br.edu.unijorge.service.CandidatoRepository;
import br.edu.unijorge.service.MemoriaVotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UrnaControllerTest {

    private MemoriaVotoRepository votos;
    private UrnaController controller;

    @BeforeEach
    void setUp() {
        votos = new MemoriaVotoRepository();
        controller = new UrnaController(new CandidatoRepository(), votos, "sessao-teste");
    }

    private void digitar(String numero) {
        for (char c : numero.toCharArray()) {
            controller.digitarNumero(c);
        }
    }

    /** Espera a gravação em segundo plano terminar e devolve os votos da sessão. */
    private List<Voto> votosGravados() {
        controller.encerrar();
        return votos.listarPorSessao("sessao-teste");
    }

    @Test
    void votoNominalNosDoisCargosFinalizaAVotacao() {
        digitar("11111");
        assertTrue(controller.confirmarVoto());
        assertEquals(CargoEnum.PREFEITO, controller.getCargoAtual());

        digitar("22");
        assertTrue(controller.confirmarVoto());
        assertTrue(controller.isVotacaoFinalizada());

        List<Voto> gravados = votosGravados();
        assertEquals(2, gravados.size());
        assertEquals(TipoVotoEnum.NOMINAL, gravados.get(0).tipo());
        assertEquals("11111", gravados.get(0).getNumeroDigitado());
        assertEquals(CargoEnum.PREFEITO, gravados.get(1).cargoEnum());
        assertEquals("sessao-teste", gravados.get(1).getSessaoId());
    }

    @Test
    void numeroInexistenteViraVotoNulo() {
        digitar("99999");
        assertTrue(controller.buscarCandidatoAtual().isEmpty());
        assertTrue(controller.confirmarVoto());
        assertEquals(TipoVotoEnum.NULO, votosGravados().get(0).tipo());
    }

    @Test
    void numeroIncompletoNaoConfirma() {
        digitar("111");
        assertFalse(controller.confirmarVoto());
        assertFalse(controller.getAviso().isEmpty());
        assertEquals(CargoEnum.VEREADOR, controller.getCargoAtual());
        assertTrue(votosGravados().isEmpty());
    }

    @Test
    void naoAceitaMaisDigitosQueOCargo() {
        digitar("1234567");
        assertEquals("12345", controller.getNumeroDigitado());
    }

    @Test
    void ignoraCaracteresQueNaoSaoDigitos() {
        assertFalse(controller.digitarNumero('x'));
        assertEquals("", controller.getNumeroDigitado());
    }

    @Test
    void brancoSoValeComCampoVazioEGravaApenasNoConfirma() {
        assertTrue(controller.votarBranco());
        assertTrue(controller.isBranco());

        assertTrue(controller.confirmarVoto());
        Voto v = votosGravados().get(0);
        assertEquals(TipoVotoEnum.BRANCO, v.tipo());
        assertNull(v.getNumeroDigitado());
    }

    @Test
    void escolherBrancoNaoGravaAteConfirmar() {
        controller.votarBranco();
        controller.corrigir(); // desistiu
        assertFalse(controller.isBranco());
        assertTrue(votosGravados().isEmpty());
    }

    @Test
    void brancoComDigitosNaoEPermitido() {
        digitar("11");
        assertFalse(controller.votarBranco());
        assertFalse(controller.isBranco());
        assertFalse(controller.getAviso().isEmpty());
    }

    @Test
    void naoDigitaDepoisDeEscolherBrancoSemCorrigir() {
        controller.votarBranco();
        assertFalse(controller.digitarNumero('1'));
        controller.corrigir();
        assertTrue(controller.digitarNumero('1'));
    }

    @Test
    void corrigeLimpaNumero() {
        digitar("11");
        controller.corrigir();
        assertEquals("", controller.getNumeroDigitado());
    }

    @Test
    void reiniciarVoltaParaOPrimeiroCargo() {
        digitar("11111");
        controller.confirmarVoto();
        digitar("11");
        controller.confirmarVoto();
        assertTrue(controller.isVotacaoFinalizada());

        controller.reiniciarVotacao();
        assertFalse(controller.isVotacaoFinalizada());
        assertEquals(CargoEnum.VEREADOR, controller.getCargoAtual());
        assertEquals("", controller.getNumeroDigitado());
    }

    @Test
    void naoAceitaAcoesDepoisDoFim() {
        controller.votarBranco();
        controller.confirmarVoto();
        controller.votarBranco();
        controller.confirmarVoto();
        assertTrue(controller.isVotacaoFinalizada());

        assertFalse(controller.digitarNumero('1'));
        assertFalse(controller.votarBranco());
        assertFalse(controller.confirmarVoto());
        assertEquals(2, votosGravados().size());
    }
}
