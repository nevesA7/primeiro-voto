package br.edu.unijorge.view;

import br.edu.unijorge.controller.UrnaController;
import br.edu.unijorge.model.Voto;
import br.edu.unijorge.service.ApuracaoService;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/** Apuração real: lê os votos da sessão (Firestore ou memória) e mostra a contagem. */
public class ResultadoView extends JFrame {

    private final UrnaController controller;
    private final ApuracaoService apuracao = new ApuracaoService();

    private final JTextField txtSessao = new JTextField(18);
    private final JTextArea txtArea = new JTextArea();
    private final JButton btnAtualizar = new JButton("Atualizar");
    private final JButton btnExportar = new JButton("Exportar CSV");
    private List<Voto> ultimosVotos = List.of();

    public ResultadoView(UrnaController controller) {
        this.controller = controller;

        setTitle("Resultado da Simulação de Votação - Primeiro Voto");
        setSize(640, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JLabel lblTitulo = new JLabel("Apuração dos Votos (Simulação)", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        txtSessao.setText(controller.getSessaoId());
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        barra.add(new JLabel("Sessão:"));
        barra.add(txtSessao);
        barra.add(btnAtualizar);
        barra.add(btnExportar);

        JPanel topo = new JPanel(new BorderLayout());
        topo.add(lblTitulo, BorderLayout.NORTH);
        topo.add(barra, BorderLayout.SOUTH);
        add(topo, BorderLayout.NORTH);

        txtArea.setEditable(false);
        txtArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        add(new JScrollPane(txtArea), BorderLayout.CENTER);

        btnAtualizar.addActionListener(e -> atualizar());
        btnExportar.addActionListener(e -> exportar());
        txtSessao.addActionListener(e -> atualizar());

        atualizar();
    }

    /** Busca os votos fora da thread da interface (a rede pode demorar). */
    private void atualizar() {
        final String sessao = txtSessao.getText().trim();
        btnAtualizar.setEnabled(false);
        txtArea.setText("Carregando votos da sessão \"" + sessao + "\"...");

        new SwingWorker<List<Voto>, Void>() {
            @Override
            protected List<Voto> doInBackground() {
                return controller.getVotoRepository().listarPorSessao(sessao);
            }

            @Override
            protected void done() {
                try {
                    ultimosVotos = get();
                    txtArea.setText(apuracao.gerarRelatorio(sessao, ultimosVotos,
                            controller.getCandidatoRepository().getTodosCandidatos()));
                    txtArea.setCaretPosition(0);
                } catch (InterruptedException | ExecutionException e) {
                    txtArea.setText("Não foi possível carregar os votos: " + e.getMessage());
                } finally {
                    btnAtualizar.setEnabled(true);
                }
            }
        }.execute();
    }

    private void exportar() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("votos-" + txtSessao.getText().trim() + ".csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            apuracao.exportarCsv(ultimosVotos, chooser.getSelectedFile().toPath());
            JOptionPane.showMessageDialog(this, "Arquivo salvo com sucesso.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
