package br.edu.unijorge;

import br.edu.unijorge.controller.UrnaController;
import br.edu.unijorge.service.CandidatoRepository;
import br.edu.unijorge.service.FirebaseService;
import br.edu.unijorge.view.UrnaView;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // O Firebase é iniciado fora da thread da interface (pode demorar na primeira conexão)
        FirebaseService firebaseService = new FirebaseService();

        SwingUtilities.invokeLater(() -> {
            String sessao = args.length > 0 ? args[0] : perguntarSessao();

            CandidatoRepository candidatoRepository = new CandidatoRepository();
            UrnaController controller = new UrnaController(candidatoRepository, firebaseService, sessao);

            new UrnaView(controller).setVisible(true);
        });
    }

    private static String perguntarSessao() {
        String sessao = JOptionPane.showInputDialog(null,
                "Identificação desta sessão (ex.: escola-turma-data):", "teste-interno");
        return (sessao == null || sessao.isBlank()) ? "teste-interno" : sessao.trim();
    }
}
