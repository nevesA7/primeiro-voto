package br.edu.unijorge.view;

import br.edu.unijorge.controller.UrnaController;
import br.edu.unijorge.model.Candidato;
import br.edu.unijorge.model.CandidatoMajoritario;
import br.edu.unijorge.util.SomUrna;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UrnaView extends JFrame {

    private static final int MAX_CAIXAS = 5;
    private static final int TEMPO_REINICIO_MS = 4000;

    private final UrnaController controller;
    private final Map<String, ImageIcon> cacheImagens = new HashMap<>();
    private final Timer timerReinicio;

    private JLabel lblTopo;
    private JLabel lblCargo;
    private JLabel lblNome;
    private JLabel lblPartido;
    private JLabel lblVice;
    private JLabel lblGrande;
    private JLabel lblMensagem;
    private JLabel lblFoto;
    private JLabel lblFotoVice;
    private JLabel lblRodape;
    private JPanel panelCaixas;
    private final JLabel[] caixas = new JLabel[MAX_CAIXAS];

    public UrnaView(UrnaController controller) {
        this.controller = controller;
        this.timerReinicio = new Timer(TEMPO_REINICIO_MS, e -> {
            controller.reiniciarVotacao();
            atualizarTela();
        });
        this.timerReinicio.setRepeats(false);

        configurarJanela();
        criarMenu();
        inicializarComponentes();
        configurarAtalhosDeTeclado();
        atualizarTela();
    }

    // ------------------------------------------------------------------ montagem

    private void configurarJanela() {
        setTitle("Simulador de Urna Eletrônica - Primeiro Voto");
        setSize(900, 560);
        setMinimumSize(new Dimension(820, 520));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.encerrar(); // espera gravar os votos pendentes
                dispose();
                System.exit(0);
            }
        });
    }

    private void criarMenu() {
        int atalho = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

        JMenuItem itemApuracao = new JMenuItem("Ver apuração");
        itemApuracao.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, atalho));
        itemApuracao.addActionListener(e -> new ResultadoView(controller).setVisible(true));

        JMenuItem itemSessao = new JMenuItem("Trocar sessão (turma/oficina)...");
        itemSessao.addActionListener(e -> trocarSessao());

        JMenuItem itemQuiz = new JMenuItem("Atividade: Verdadeiro ou Falso");
        itemQuiz.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, atalho));
        itemQuiz.addActionListener(e -> new QuizDesinformacaoDialog(this).setVisible(true));

        JMenuItem itemSair = new JMenuItem("Sair");
        itemSair.addActionListener(e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));

        JMenu menu = new JMenu("Oficina");
        menu.add(itemApuracao);
        menu.add(itemSessao);
        menu.add(itemQuiz);
        menu.addSeparator();
        menu.add(itemSair);

        JMenuBar barra = new JMenuBar();
        barra.add(menu);
        setJMenuBar(barra);
    }

    private void inicializarComponentes() {
        // --- Tela da urna ---
        JPanel panelEcra = new JPanel(new BorderLayout(10, 10));
        panelEcra.setBackground(new Color(240, 245, 240));
        panelEcra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY, 3),
                BorderFactory.createEmptyBorder(15, 15, 10, 15)));

        lblTopo = new JLabel("SEU VOTO PARA");
        lblTopo.setFont(new Font("Arial", Font.PLAIN, 13));

        lblCargo = new JLabel(" ");
        lblCargo.setFont(new Font("Arial", Font.BOLD, 24));

        panelCaixas = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panelCaixas.setOpaque(false);
        for (int i = 0; i < MAX_CAIXAS; i++) {
            JLabel c = new JLabel(" ", SwingConstants.CENTER);
            c.setPreferredSize(new Dimension(38, 46));
            c.setFont(new Font("Monospaced", Font.BOLD, 28));
            c.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            caixas[i] = c;
            panelCaixas.add(c);
        }

        lblNome = criarRotulo(15, Font.PLAIN);
        lblPartido = criarRotulo(15, Font.PLAIN);
        lblVice = criarRotulo(14, Font.PLAIN);

        lblGrande = new JLabel(" ");
        lblGrande.setFont(new Font("Arial", Font.BOLD, 34));

        lblMensagem = new JLabel(" ");
        lblMensagem.setFont(new Font("Arial", Font.BOLD, 13));

        JPanel panelTexto = new JPanel();
        panelTexto.setLayout(new BoxLayout(panelTexto, BoxLayout.Y_AXIS));
        panelTexto.setOpaque(false);
        for (JComponent c : new JComponent[]{lblTopo, lblCargo, panelCaixas, lblNome, lblPartido, lblVice, lblGrande, lblMensagem}) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        panelTexto.add(lblTopo);
        panelTexto.add(lblCargo);
        panelTexto.add(Box.createVerticalStrut(12));
        panelTexto.add(panelCaixas);
        panelTexto.add(Box.createVerticalStrut(14));
        panelTexto.add(lblNome);
        panelTexto.add(Box.createVerticalStrut(5));
        panelTexto.add(lblPartido);
        panelTexto.add(Box.createVerticalStrut(5));
        panelTexto.add(lblVice);
        panelTexto.add(Box.createVerticalStrut(8));
        panelTexto.add(lblGrande);
        panelTexto.add(Box.createVerticalGlue());
        panelTexto.add(lblMensagem);
        panelEcra.add(panelTexto, BorderLayout.CENTER);

        // Fotos
        lblFoto = criarMoldura(130, 160);
        lblFotoVice = criarMoldura(65, 80);
        JPanel panelFotos = new JPanel();
        panelFotos.setLayout(new BoxLayout(panelFotos, BoxLayout.Y_AXIS));
        panelFotos.setOpaque(false);
        lblFoto.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblFotoVice.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelFotos.add(lblFoto);
        panelFotos.add(Box.createVerticalStrut(8));
        panelFotos.add(lblFotoVice);
        panelEcra.add(panelFotos, BorderLayout.EAST);

        // Rodapé (legenda como na urna real + status do armazenamento)
        lblRodape = new JLabel(" ");
        lblRodape.setFont(new Font("Arial", Font.PLAIN, 11));
        lblRodape.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
        panelEcra.add(lblRodape, BorderLayout.SOUTH);

        add(panelEcra, BorderLayout.CENTER);

        // --- Teclado ---
        JPanel panelTeclado = new JPanel(new BorderLayout(5, 5));
        panelTeclado.setBackground(Color.BLACK);
        panelTeclado.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panelTeclado.setPreferredSize(new Dimension(320, 0));

        JPanel panelNumeros = new JPanel(new GridLayout(4, 3, 8, 8));
        panelNumeros.setBackground(Color.BLACK);
        for (int i = 1; i <= 9; i++) {
            panelNumeros.add(criarBotaoNumerico((char) ('0' + i)));
        }
        panelNumeros.add(new JLabel(""));
        panelNumeros.add(criarBotaoNumerico('0'));
        panelNumeros.add(new JLabel(""));
        panelTeclado.add(panelNumeros, BorderLayout.CENTER);

        JPanel panelAcoes = new JPanel(new GridLayout(1, 3, 6, 6));
        panelAcoes.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        panelAcoes.setBackground(Color.BLACK);
        panelAcoes.add(criarBotaoAcao("BRANCO", Color.WHITE, Color.BLACK, e -> branco()));
        panelAcoes.add(criarBotaoAcao("CORRIGE", new Color(230, 81, 0), Color.WHITE, e -> corrigir()));
        panelAcoes.add(criarBotaoAcao("CONFIRMA", new Color(46, 125, 50), Color.WHITE, e -> confirmar()));
        panelTeclado.add(panelAcoes, BorderLayout.SOUTH);

        add(panelTeclado, BorderLayout.EAST);
    }

    private JLabel criarRotulo(int tamanho, int estilo) {
        JLabel l = new JLabel(" ");
        l.setFont(new Font("Arial", estilo, tamanho));
        return l;
    }

    private JLabel criarMoldura(int largura, int altura) {
        JLabel l = new JLabel("", SwingConstants.CENTER);
        l.setPreferredSize(new Dimension(largura, altura));
        l.setMinimumSize(new Dimension(largura, altura));
        l.setMaximumSize(new Dimension(largura, altura));
        l.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        l.setFont(new Font("Arial", Font.ITALIC, 11));
        return l;
    }

    private JButton criarBotaoNumerico(char digito) {
        JButton btn = new JButton(String.valueOf(digito));
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setBackground(Color.DARK_GRAY);
        btn.setForeground(Color.WHITE);
        btn.setFocusable(false);
        btn.addActionListener(e -> digitar(digito));
        return btn;
    }

    private JButton criarBotaoAcao(String texto, Color fundo, Color letra, java.awt.event.ActionListener acao) {
        JButton btn = new JButton(texto);
        btn.setBackground(fundo);
        btn.setForeground(letra);
        btn.setFont(new Font("Arial", Font.BOLD, 11));
        btn.setMargin(new Insets(10, 2, 10, 2));
        btn.setFocusable(false);
        btn.addActionListener(acao);
        return btn;
    }

    /** Teclado físico: 0-9 (linha de cima e numérico), Enter = confirma, Backspace/Esc = corrige, B = branco. */
    private void configurarAtalhosDeTeclado() {
        JRootPane raiz = getRootPane();
        InputMap im = raiz.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = raiz.getActionMap();

        for (int i = 0; i <= 9; i++) {
            final char d = (char) ('0' + i);
            String nome = "digito" + i;
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_0 + i, 0), nome);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0 + i, 0), nome);
            am.put(nome, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    digitar(d);
                }
            });
        }
        mapear(im, am, KeyEvent.VK_ENTER, "confirma", this::confirmar);
        mapear(im, am, KeyEvent.VK_BACK_SPACE, "corrige", this::corrigir);
        mapear(im, am, KeyEvent.VK_ESCAPE, "corrige2", this::corrigir);
        mapear(im, am, KeyEvent.VK_B, "branco", this::branco);
    }

    private void mapear(InputMap im, ActionMap am, int tecla, String nome, Runnable acao) {
        im.put(KeyStroke.getKeyStroke(tecla, 0), nome);
        am.put(nome, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                acao.run();
            }
        });
    }

    // -------------------------------------------------------------------- ações

    private void digitar(char d) {
        if (controller.digitarNumero(d)) {
            SomUrna.tecla();
        } else if (!controller.getAviso().isEmpty()) {
            SomUrna.erro();
        }
        atualizarTela();
    }

    private void corrigir() {
        controller.corrigir();
        atualizarTela();
    }

    private void branco() {
        if (controller.votarBranco()) {
            SomUrna.tecla();
        } else if (!controller.getAviso().isEmpty()) {
            SomUrna.erro();
        }
        atualizarTela();
    }

    private void confirmar() {
        if (controller.confirmarVoto()) {
            if (controller.isVotacaoFinalizada()) {
                SomUrna.fim();
                timerReinicio.restart(); // volta sozinha para o próximo eleitor
            } else {
                SomUrna.confirma();
            }
        } else if (!controller.getAviso().isEmpty()) {
            SomUrna.erro();
        }
        atualizarTela();
    }

    private void trocarSessao() {
        String nova = JOptionPane.showInputDialog(this,
                "Identificação da sessão (ex.: escola-turma-data):", controller.getSessaoId());
        if (nova != null && !nova.isBlank()) {
            controller.setSessaoId(nova.trim());
            atualizarTela();
        }
    }

    // ------------------------------------------------------------------- exibição

    private void atualizarTela() {
        lblRodape.setText("<html>Aperte a tecla VERDE para CONFIRMAR este voto | LARANJA para CORRIGIR<br>"
                + "Sessão: " + controller.getSessaoId()
                + " &nbsp;&bull;&nbsp; " + controller.getStatusArmazenamento() + "</html>");

        if (controller.isVotacaoFinalizada()) {
            mostrarFim();
            return;
        }

        lblTopo.setText("SEU VOTO PARA");
        lblCargo.setText(controller.getCargoAtual().getDescricao().toUpperCase());
        lblMensagem.setForeground(new Color(180, 0, 0));
        lblGrande.setForeground(Color.BLACK);

        int qtd = controller.getCargoAtual().getQtdDigitos();

        if (controller.isBranco()) {
            panelCaixas.setVisible(false);
            limparCandidato();
            lblGrande.setText("VOTO EM BRANCO");
            lblMensagem.setForeground(Color.DARK_GRAY);
            lblMensagem.setText("CONFIRMA para votar em branco ou CORRIGE para voltar.");
            return;
        }

        panelCaixas.setVisible(true);
        String num = controller.getNumeroDigitado();
        for (int i = 0; i < MAX_CAIXAS; i++) {
            caixas[i].setVisible(i < qtd);
            caixas[i].setText(i < num.length() ? String.valueOf(num.charAt(i)) : " ");
        }

        Optional<Candidato> opt = controller.buscarCandidatoAtual();
        if (opt.isPresent()) {
            mostrarCandidato(opt.get());
            lblGrande.setText(" ");
            lblMensagem.setForeground(new Color(0, 90, 0));
            lblMensagem.setText("CONFIRMA para votar ou CORRIGE para corrigir.");
        } else {
            limparCandidato();
            if (controller.isNumeroCompleto()) {
                lblGrande.setText("VOTO NULO");
                lblGrande.setForeground(new Color(180, 0, 0));
                lblMensagem.setText("NÚMERO ERRADO. CONFIRMA para votar nulo ou CORRIGE.");
            } else {
                lblGrande.setText(" ");
                lblMensagem.setForeground(Color.DARK_GRAY);
                lblMensagem.setText("Digite o número do candidato.");
            }
        }

        if (!controller.getAviso().isEmpty()) {
            lblMensagem.setForeground(new Color(180, 0, 0));
            lblMensagem.setText(controller.getAviso());
        }
    }

    private void mostrarCandidato(Candidato c) {
        lblNome.setText("Nome: " + c.getNome());
        lblPartido.setText("Partido: " + c.getPartidoFicticio() + " (" + c.getSiglaPartido() + ")");
        definirFoto(lblFoto, c.getCaminhoFoto(), 130, 160);

        if (c instanceof CandidatoMajoritario) {
            CandidatoMajoritario maj = (CandidatoMajoritario) c;
            lblVice.setText("Vice-Prefeito: " + maj.getNomeVice());
            lblFotoVice.setVisible(true);
            definirFoto(lblFotoVice, maj.getCaminhoFotoVice(), 65, 80);
        } else {
            lblVice.setText(" ");
            lblFotoVice.setVisible(false);
        }
    }

    private void limparCandidato() {
        lblNome.setText(" ");
        lblPartido.setText(" ");
        lblVice.setText(" ");
        lblFoto.setVisible(true);
        lblFoto.setIcon(null);
        lblFoto.setText("");
        lblFotoVice.setIcon(null);
        lblFotoVice.setText("");
        lblFotoVice.setVisible(false);
    }

    private void mostrarFim() {
        lblTopo.setText(" ");
        lblCargo.setText(" ");
        panelCaixas.setVisible(false);
        limparCandidato();
        lblGrande.setForeground(Color.BLACK);
        lblGrande.setText("FIM");
        lblNome.setText(" ");
        lblMensagem.setForeground(new Color(0, 90, 0));
        lblMensagem.setText("Voto registrado. Próximo eleitor em instantes...");
        lblFoto.setVisible(false);
    }

    private void definirFoto(JLabel destino, String caminho, int largura, int altura) {
        destino.setVisible(true);
        ImageIcon icone = cacheImagens.computeIfAbsent(caminho + largura, k -> carregarImagem(caminho, largura, altura));
        if (icone != null) {
            destino.setText("");
            destino.setIcon(icone);
        } else {
            destino.setIcon(null);
            destino.setText("SEM FOTO");
        }
    }

    private ImageIcon carregarImagem(String caminho, int largura, int altura) {
        URL url = getClass().getResource(caminho);
        if (url == null) {
            return null;
        }
        Image img = new ImageIcon(url).getImage().getScaledInstance(largura, altura, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }
}
