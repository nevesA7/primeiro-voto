package br.edu.unijorge.view;

import javax.swing.*;
import java.awt.*;

/** Atividade de apoio da oficina: verdadeiro ou falso sobre urna eletrônica e desinformação. */
public class QuizDesinformacaoDialog extends JDialog {

    private static final String[][] PERGUNTAS = {
            {"A urna eletrônica brasileira fica conectada à internet durante a votação.", "F",
                    "Falso. A urna não é conectada à internet nem a nenhuma rede durante a votação."},
            {"Um áudio que circula sem fonte, com voz parecida com a de uma autoridade, prova que ela disse aquilo.", "F",
                    "Falso. Vozes podem ser clonadas com inteligência artificial. Procure a fonte original antes de acreditar ou compartilhar."},
            {"Mensagens que pedem \"compartilhe urgente, antes que apaguem!\" merecem desconfiança.", "V",
                    "Verdadeiro. Pressa e medo são táticas comuns para fazer a desinformação se espalhar sem checagem."},
            {"O voto é secreto: ninguém consegue ligar o seu voto ao seu nome.", "V",
                    "Verdadeiro. O sigilo do voto é uma garantia do processo eleitoral."},
            {"Se uma imagem ou vídeo parece muito real, então ele é verdadeiro.", "F",
                    "Falso. Deepfakes e imagens geradas por IA podem parecer reais. Observe detalhes estranhos e busque a origem."},
            {"Antes de compartilhar uma notícia, vale procurar o mesmo fato em veículos confiáveis ou agências de checagem.", "V",
                    "Verdadeiro. Conferir em mais de uma fonte confiável é o hábito mais simples e eficaz."}
    };

    private int atual = 0;
    private int acertos = 0;
    private boolean respondida = false;

    private final JLabel lblProgresso = new JLabel();
    private final JTextArea txtPergunta = new JTextArea();
    private final JTextArea txtFeedback = new JTextArea();
    private final JButton btnVerdadeiro = new JButton("VERDADEIRO");
    private final JButton btnFalso = new JButton("FALSO");
    private final JButton btnProxima = new JButton("Próxima");

    public QuizDesinformacaoDialog(Frame dono) {
        super(dono, "Verdadeiro ou Falso? Eleições e desinformação", true);
        setSize(560, 380);
        setLocationRelativeTo(dono);
        setLayout(new BorderLayout(10, 10));

        lblProgresso.setFont(new Font("Arial", Font.BOLD, 13));
        lblProgresso.setBorder(BorderFactory.createEmptyBorder(10, 14, 0, 14));
        add(lblProgresso, BorderLayout.NORTH);

        configurarTexto(txtPergunta, new Font("Arial", Font.BOLD, 17));
        configurarTexto(txtFeedback, new Font("Arial", Font.PLAIN, 14));

        JPanel centro = new JPanel(new GridLayout(2, 1, 8, 8));
        centro.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        centro.add(txtPergunta);
        centro.add(txtFeedback);
        add(centro, BorderLayout.CENTER);

        btnVerdadeiro.addActionListener(e -> responder(true));
        btnFalso.addActionListener(e -> responder(false));
        btnProxima.addActionListener(e -> proxima());

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        botoes.add(btnVerdadeiro);
        botoes.add(btnFalso);
        botoes.add(btnProxima);
        add(botoes, BorderLayout.SOUTH);

        mostrarPergunta();
    }

    private void configurarTexto(JTextArea area, Font fonte) {
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setOpaque(false);
        area.setFont(fonte);
    }

    private void mostrarPergunta() {
        respondida = false;
        lblProgresso.setText("Pergunta " + (atual + 1) + " de " + PERGUNTAS.length);
        txtPergunta.setText(PERGUNTAS[atual][0]);
        txtFeedback.setText("");
        btnVerdadeiro.setEnabled(true);
        btnFalso.setEnabled(true);
        btnProxima.setEnabled(false);
        btnProxima.setText(atual == PERGUNTAS.length - 1 ? "Ver resultado" : "Próxima");
    }

    private void responder(boolean disseVerdadeiro) {
        if (respondida) {
            return;
        }
        respondida = true;
        boolean gabaritoVerdadeiro = "V".equals(PERGUNTAS[atual][1]);
        boolean acertou = disseVerdadeiro == gabaritoVerdadeiro;
        if (acertou) {
            acertos++;
        }
        txtFeedback.setText((acertou ? "Acertou! " : "Quase! ") + PERGUNTAS[atual][2]);
        btnVerdadeiro.setEnabled(false);
        btnFalso.setEnabled(false);
        btnProxima.setEnabled(true);
    }

    private void proxima() {
        if (atual < PERGUNTAS.length - 1) {
            atual++;
            mostrarPergunta();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Você acertou " + acertos + " de " + PERGUNTAS.length + ".\n"
                            + "Na dúvida: confira a fonte, desconfie de pressa e compartilhe com cuidado.",
                    "Resultado", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }
    }
}
