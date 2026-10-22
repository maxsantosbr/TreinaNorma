package br.com.treinanorma.view;

import br.com.treinanorma.dao.RegistroDAO;
import br.com.treinanorma.dao.Conexao;
import br.com.treinanorma.model.Perfil;
import br.com.treinanorma.model.RegistroTreinamento;
import br.com.treinanorma.model.Usuario;
import br.com.treinanorma.util.Icones;
import br.com.treinanorma.util.Sessao;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

public class MainFrame extends JFrame {

    public static final String VERSAO = "1.0.0";

    private static final Color AZUL = new Color(0x1F, 0x4E, 0x79);
    private static final Color AZUL_CLARO = new Color(0x2E, 0x75, 0xB6);
    private static final Color DESTAQUE = new Color(0x0A, 0xA7, 0xE8);
    private static final Color FUNDO = new Color(0xF6, 0xF8, 0xFB);
    private static final Color BORDA = new Color(0xDB, 0xE3, 0xEE);
    private static final Color TEXTO = new Color(0x1F, 0x29, 0x37);
    private static final Color SUAVE = new Color(0x6B, 0x72, 0x80);
    private static final Color BEGE = new Color(0xEE, 0xF4, 0xFA);

    private final Usuario usuario = Sessao.getUsuario();
    private final String nomeUsuario = usuario == null ? "Convidado" : usuario.getNome();
    private final Perfil perfil = usuario == null ? Perfil.CONSULTA : usuario.getPerfil();

    private JTable tabela;
    private TableRowSorter<DefaultTableModel> ordenador;
    private JTextField txtBusca;
    private BotaoJanela btnMaximizar;
    private JPanel host;
    private JPanel dashboard;
    private JPanel painelKpis;
    private JLabel lblBackup;
    private final RegistroDAO registroDAO = new RegistroDAO();

    public MainFrame() {
        setTitle("TreinaNorma - Gestão de treinamentos de Normas Regulamentadoras. (v" + VERSAO + ")");
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        Icones.aplicarIcone(this);

        setMinimumSize(new Dimension(960, 640));
        Dimension tela = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(1200, tela.width - 80), Math.min(760, tela.height - 80));
        setLocationRelativeTo(null);

        montarTela();
        atualizarForma();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                atualizarForma();
            }
        });

        addWindowStateListener(e -> SwingUtilities.invokeLater(this::atualizarForma));

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                sair();
            }
        });
    }

    private void montarTela() {
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(FUNDO);
        setContentPane(raiz);

        raiz.add(montarTopo(), BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout());
        centro.setBackground(FUNDO);
        centro.add(montarMenu(), BorderLayout.NORTH);
        dashboard = montarConteudo();
        host = new JPanel(new BorderLayout());
        host.setBackground(FUNDO);
        host.add(dashboard, BorderLayout.CENTER);
        centro.add(host, BorderLayout.CENTER);
        raiz.add(centro, BorderLayout.CENTER);

        raiz.add(montarStatus(), BorderLayout.SOUTH);
    }

    private JPanel montarTopo() {
        JPanel topo = new JPanel(new BorderLayout(10, 0));
        topo.setBackground(AZUL);
        topo.setPreferredSize(new Dimension(0, 56));
        topo.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 10));

        JPanel marca = new JPanel();
        marca.setOpaque(false);
        marca.setLayout(new BoxLayout(marca, BoxLayout.X_AXIS));
        marca.add(selo());
        marca.add(Box.createHorizontalStrut(10));

        JLabel titulo = new JLabel("TreinaNorma");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titulo.setForeground(Color.WHITE);
        marca.add(titulo);

        JLabel subtitulo = new JLabel(" - Gestão de treinamentos de Normas Regulamentadoras. (v"
                + VERSAO + ")");
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitulo.setForeground(new Color(0xDB, 0xEA, 0xFE));
        marca.add(subtitulo);

        topo.add(marca, BorderLayout.WEST);

        JPanel direita = new JPanel();
        direita.setOpaque(false);
        direita.setLayout(new BoxLayout(direita, BoxLayout.X_AXIS));

        txtBusca = new JTextField();
        txtBusca.putClientProperty("JTextField.placeholderText", "Buscar colaborador, NR ou certificado...");
        txtBusca.putClientProperty("JTextField.showClearButton", true);
        txtBusca.setMaximumSize(new Dimension(200, 30));
        txtBusca.setPreferredSize(new Dimension(200, 30));
        txtBusca.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtBusca.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filtrar();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filtrar();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filtrar();
            }
        });
        direita.add(txtBusca);
        direita.add(Box.createHorizontalStrut(12));

        JLabel chip = new JLabel("  " + nomeUsuario + "  |  " + perfil + "  ");
        chip.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chip.setForeground(Color.WHITE);
        chip.setOpaque(true);
        chip.setBackground(new Color(255, 255, 255, 30));
        chip.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        direita.add(chip);
        direita.add(Box.createHorizontalStrut(8));

        BotaoJanela btnMin = new BotaoJanela(BotaoJanela.Tipo.MINIMIZAR);
        btnMin.setToolTipText("Minimizar");
        btnMin.addActionListener(e -> setExtendedState(JFrame.ICONIFIED));

        btnMaximizar = new BotaoJanela(BotaoJanela.Tipo.MAXIMIZAR);
        btnMaximizar.setToolTipText("Maximizar");
        btnMaximizar.addActionListener(e -> alternarMaximizar());

        BotaoJanela btnFechar = new BotaoJanela(BotaoJanela.Tipo.FECHAR);
        btnFechar.setToolTipText("Fechar");

        btnFechar.addActionListener(e -> sair());

        direita.add(btnMin);
        direita.add(btnMaximizar);
        direita.add(btnFechar);

        topo.add(direita, BorderLayout.EAST);

        instalarArraste(topo);
        return topo;
    }

    private JComponent selo() {
        final java.awt.Image img = Icones.carregar("/icones/converterICO.png").getImage()
                .getScaledInstance(26, 26, java.awt.Image.SCALE_SMOOTH);
        JComponent selo = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.drawImage(img, 0, 0, this);
                g2.dispose();
            }
        };
        selo.setPreferredSize(new Dimension(26, 26));
        selo.setMaximumSize(new Dimension(26, 26));
        selo.setMinimumSize(new Dimension(26, 26));
        return selo;
    }

    private JMenuBar montarMenu() {
        JMenuBar barra = new JMenuBar();
        barra.setBackground(Color.WHITE);
        barra.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDA));
        barra.setPreferredSize(new Dimension(0, 38));

        JMenu arquivo = menu("Arquivo");
        arquivo.add(item("Backup do banco", this::backup));
        arquivo.add(item("Restaurar backup", this::restaurar));
        arquivo.addSeparator();
        arquivo.add(item("Sair", this::sair));

        JMenu cadastros = menu("Cadastros");
        cadastros.add(item("Colaboradores", () -> abrirTela(new ColaboradoresPanel(this::mostrarDashboard))));
        cadastros.add(item("Empresas / estabelecimentos", () -> abrirTela(new EmpresaPanel(this::mostrarDashboard))));
        cadastros.add(item("Instrutores", () -> abrirTela(new InstrutoresPanel(this::mostrarDashboard))));

        JMenu treinamentos = menu("Treinamentos");
        treinamentos.add(item("Registrar treinamento", () -> abrirTela(new TurmasPanel(this::mostrarDashboard))));
        treinamentos.add(item("Emitir certificado", () -> abrirTela(new CertificadosPanel(this::mostrarDashboard))));
        treinamentos.add(item("Matriz de treinamento por NR", () -> abrirTela(new MatrizPanel(this::mostrarDashboard))));

        JMenu consultas = menu("Consultas");
        consultas.add(item("Vencimentos", () -> abrirTela(new VencimentosPanel(this::mostrarDashboard))));
        consultas.add(item("Histórico do colaborador", () -> abrirTela(new ColaboradoresPanel(this::mostrarDashboard))));
        consultas.add(item("Treinamentos por NR", () -> abrirTela(new CertificadosPanel(this::mostrarDashboard))));

        JMenu administracao = menu("Administração");
        administracao.add(item("Usuários", () -> abrirTela(new UsuariosPanel(this::mostrarDashboard))));
        administracao.add(item("Parâmetros do sistema", () -> abrirTela(new ParametrosPanel(this::mostrarDashboard))));

        JMenu conta = menu("Conta");
        conta.add(item("Trocar senha", () -> UsuariosPanel.trocarSenha(this)));
        conta.add(item("Encerrar sessão", this::encerrarSessao));

        JMenu ajuda = menu("Ajuda");
        ajuda.add(item("Manual", this::manual));
        ajuda.add(item("Sobre", this::sobre));

        barra.add(arquivo);
        barra.add(cadastros);
        barra.add(treinamentos);
        barra.add(consultas);
        if (perfil == Perfil.ADMIN) {
            barra.add(administracao);
        }
        barra.add(conta);
        barra.add(ajuda);
        return barra;
    }

    private JMenu menu(String titulo) {
        JMenu m = new JMenu(titulo);
        m.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        m.setForeground(TEXTO);
        return m;
    }

    private JMenuItem item(String titulo, Runnable acao) {
        JMenuItem i = new JMenuItem(titulo);
        i.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        i.addActionListener(e -> acao.run());
        return i;
    }

    private JPanel montarConteudo() {
        JPanel conteudo = new JPanel(new BorderLayout());
        conteudo.setBackground(FUNDO);
        conteudo.setBorder(BorderFactory.createEmptyBorder(18, 20, 22, 20));

        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setOpaque(false);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JPanel titulos = new JPanel();
        titulos.setOpaque(false);
        titulos.setLayout(new BoxLayout(titulos, BoxLayout.Y_AXIS));
        JLabel titulo = new JLabel("Dashboard");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(TEXTO);
        JLabel legenda = new JLabel("Vigência dos treinamentos obrigatórios por colaborador e NR.");
        legenda.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        legenda.setForeground(SUAVE);
        titulos.add(titulo);
        titulos.add(Box.createVerticalStrut(2));
        titulos.add(legenda);
        cabecalho.add(titulos, BorderLayout.WEST);

        JButton novo = new JButton("+ Novo treinamento");
        novo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        novo.setBackground(AZUL);
        novo.setForeground(Color.WHITE);
        novo.setFocusPainted(false);
        novo.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        novo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        novo.addActionListener(e -> abrirTela(new TurmasPanel(this::mostrarDashboard)));

        JPanel acao = new JPanel(new BorderLayout());
        acao.setOpaque(false);
        acao.add(novo, BorderLayout.NORTH);
        cabecalho.add(acao, BorderLayout.EAST);

        conteudo.add(cabecalho, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 14));
        centro.setOpaque(false);
        centro.add(montarKpis(), BorderLayout.NORTH);
        centro.add(montarCorpo(), BorderLayout.CENTER);
        conteudo.add(centro, BorderLayout.CENTER);

        return conteudo;
    }

    private JPanel montarKpis() {
        painelKpis = new JPanel(new GridLayout(1, 4, 14, 0));
        painelKpis.setOpaque(false);
        atualizarKpis();
        return painelKpis;
    }

    private void atualizarKpis() {
        if (painelKpis == null) {
            return;
        }
        int[] resumo;
        try {
            resumo = registroDAO.resumo();
        } catch (RuntimeException e) {
            resumo = new int[]{0, 0, 0, 0};
        }
        painelKpis.removeAll();
        painelKpis.add(cartaoKpi("Colaboradores ativos", String.valueOf(resumo[0]),
                "cadastrados no sistema", new Color(0x15, 0x80, 0x3D)));
        painelKpis.add(cartaoKpi("Vencem em 30 dias", String.valueOf(resumo[1]),
                "programar reciclagem", new Color(0xB4, 0x53, 0x09)));
        painelKpis.add(cartaoKpi("Vencidos", String.valueOf(resumo[2]),
                "ação imediata", new Color(0xB9, 0x1C, 0x1C)));
        painelKpis.add(cartaoKpi("Certificados no ano", String.valueOf(resumo[3]),
                "emitidos em " + LocalDate.now().getYear(), SUAVE));
        painelKpis.revalidate();
        painelKpis.repaint();
    }

    private JPanel cartaoKpi(String rotulo, String valor, String rodape, Color corRodape) {
        Cartao c = new Cartao(new BorderLayout());
        c.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel l = new JLabel(rotulo.toUpperCase());
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(SUAVE);
        c.add(l, BorderLayout.NORTH);

        JLabel v = new JLabel(valor);
        v.setFont(new Font("Segoe UI", Font.BOLD, 26));
        v.setForeground(AZUL);
        c.add(v, BorderLayout.CENTER);

        JLabel r = new JLabel(rodape);
        r.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        r.setForeground(corRodape);
        c.add(r, BorderLayout.SOUTH);

        return c;
    }

    private JPanel montarCorpo() {
        JPanel corpo = new JPanel(new GridBagLayout());
        corpo.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;

        gbc.gridx = 0;
        gbc.weightx = 0.67;
        gbc.insets = new Insets(0, 0, 0, 14);
        corpo.add(montarTabela(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.33;
        gbc.insets = new Insets(0, 0, 0, 0);
        corpo.add(montarLateral(), gbc);

        return corpo;
    }

    private JPanel montarTabela() {
        Cartao cartao = new Cartao(new BorderLayout());

        JPanel cab = new JPanel(new BorderLayout());
        cab.setOpaque(false);
        cab.setBorder(BorderFactory.createEmptyBorder(11, 14, 11, 14));
        JLabel t = new JLabel("Treinamentos vencendo");
        t.setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.setForeground(AZUL);
        cab.add(t, BorderLayout.WEST);
        JLabel ver = new JLabel("ver todos");
        ver.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        ver.setForeground(AZUL_CLARO);
        ver.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        ver.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                abrirTela(new VencimentosPanel(MainFrame.this::mostrarDashboard));
            }
        });
        cab.add(ver, BorderLayout.EAST);
        cab.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDA),
                BorderFactory.createEmptyBorder(11, 14, 11, 14)));
        cartao.add(cab, BorderLayout.NORTH);

        String[] colunas = {"Colaborador", "NR / Treinamento", "Validade", "Status"};

        DefaultTableModel modelo = new DefaultTableModel(new Object[0][0], colunas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabela = new JTable(modelo);
        tabela.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabela.setForeground(new Color(0x33, 0x41, 0x55));
        tabela.setRowHeight(34);
        tabela.setShowVerticalLines(false);
        tabela.setShowHorizontalLines(true);
        tabela.setGridColor(new Color(0xF1, 0xF5, 0xF9));
        tabela.setIntercellSpacing(new Dimension(0, 1));
        tabela.setSelectionBackground(new Color(0xE8, 0xF2, 0xFB));
        tabela.setSelectionForeground(AZUL);
        tabela.setFillsViewportHeight(true);
        tabela.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setBorder(BorderFactory.createEmptyBorder());

        JTableHeader header = tabela.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setBackground(BEGE);
        header.setForeground(AZUL);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 32));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDA));

        tabela.getColumnModel().getColumn(0).setPreferredWidth(170);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(260);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(95);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(105);

        RenderizadorStatus status = new RenderizadorStatus();
        tabela.getColumnModel().getColumn(3).setCellRenderer(status);
        DefaultTableCellRenderer normal = new DefaultTableCellRenderer();
        normal.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        for (int i = 0; i < 3; i++) {
            tabela.getColumnModel().getColumn(i).setCellRenderer(normal);
        }

        ordenador = new TableRowSorter<>(modelo);
        tabela.setRowSorter(ordenador);
        atualizarTabela();

        JScrollPane rolagem = new JScrollPane(tabela);
        rolagem.setBorder(BorderFactory.createEmptyBorder());
        rolagem.getViewport().setBackground(Color.WHITE);
        cartao.add(rolagem, BorderLayout.CENTER);

        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setOpaque(false);
        envoltorio.add(cartao, BorderLayout.CENTER);
        return envoltorio;
    }

    private void atualizarTabela() {
        if (tabela == null) {
            return;
        }
        DefaultTableModel modelo = (DefaultTableModel) tabela.getModel();
        modelo.setRowCount(0);
        try {
            List<RegistroTreinamento> vencidos = registroDAO.listar(null, "VENCIDOS");
            vencidos.sort((a, b) -> b.getValidade().compareTo(a.getValidade()));
            List<RegistroTreinamento> proximos = registroDAO.vencimentosProximos(30, 12);
            int adicionados = 0;
            for (RegistroTreinamento r : vencidos) {
                if (adicionados++ >= 12) {
                    break;
                }
                modelo.addRow(new Object[]{r.getColaboradorNome(),
                    r.getNorma() + " - " + r.getTreinamento(),
                    br.com.treinanorma.util.Datas.br(r.getValidade()), "Vencido"});
            }
            for (RegistroTreinamento r : proximos) {
                if (r.getValidade() == null || r.getValidade().isBefore(LocalDate.now())
                        || adicionados++ >= 12) {
                    continue;
                }
                long dias = ChronoUnit.DAYS.between(LocalDate.now(), r.getValidade());
                modelo.addRow(new Object[]{r.getColaboradorNome(),
                    r.getNorma() + " - " + r.getTreinamento(),
                    br.com.treinanorma.util.Datas.br(r.getValidade()),
                    dias <= 5 ? "Vence em 5 d" : "Vence em " + dias + " d"});
            }
        } catch (RuntimeException e) {
            // banco indisponível: dashboard permanece vazio
        }
    }

    private JPanel montarLateral() {
        JPanel lateral = new JPanel(new BorderLayout(0, 14));
        lateral.setOpaque(false);
        lateral.add(montarAcoes(), BorderLayout.NORTH);
        lateral.add(montarAvisos(), BorderLayout.CENTER);
        return lateral;
    }

    private JPanel montarAcoes() {
        Cartao cartao = new Cartao(new BorderLayout());
        cartao.add(tituloCartao("Ações rápidas"), BorderLayout.NORTH);

        JPanel lista = new JPanel();
        lista.setOpaque(false);
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        lista.add(linhaAcao("Registrar treinamento", "lançar execução e presença", false,
                () -> abrirTela(new TurmasPanel(this::mostrarDashboard))));
        lista.add(linhaAcao("Emitir certificado", "gerar PDF com base legal", false,
                () -> abrirTela(new CertificadosPanel(this::mostrarDashboard))));
        lista.add(linhaAcao("Importar colaboradores", "CSV ou cadastro manual", false,
                () -> abrirTela(new ColaboradoresPanel(this::mostrarDashboard))));
        cartao.add(lista, BorderLayout.CENTER);
        return cartao;
    }

    private JPanel linhaAcao(String titulo, String descricao, boolean alerta, Runnable acao) {
        JPanel linha = new JPanel();
        linha.setOpaque(false);
        linha.setLayout(new BoxLayout(linha, BoxLayout.X_AXIS));
        linha.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));

        Ponto ponto = new Ponto(alerta ? new Color(0xF5, 0x9E, 0x0B) : DESTAQUE);
        linha.add(ponto);
        linha.add(Box.createHorizontalStrut(8));

        JLabel texto = new JLabel("<html><b>" + titulo + "</b><br><font color='#6B7280'>" + descricao + "</font></html>");
        texto.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        texto.setForeground(AZUL);
        texto.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        texto.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                acao.run();
            }
        });
        linha.add(texto);
        linha.add(Box.createHorizontalGlue());
        return linha;
    }

    private JPanel montarAvisos() {
        Cartao cartao = new Cartao(new BorderLayout());
        cartao.add(tituloCartao("Avisos"), BorderLayout.NORTH);

        JPanel lista = new JPanel();
        lista.setOpaque(false);
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        lista.setBorder(BorderFactory.createEmptyBorder(10, 14, 12, 14));
        lista.add(aviso("CIPA: mandato vence em 15 dias.", true));
        lista.add(Box.createVerticalStrut(6));
        lista.add(aviso("3 linhas da matriz sem CH fixada na NR (referência de mercado).", false));
        lista.add(Box.createVerticalStrut(6));
        lista.add(aviso("Treinamentos com validade nula seguem o PGR, não vencem.", false));
        cartao.add(lista, BorderLayout.CENTER);
        return cartao;
    }

    private JPanel aviso(String texto, boolean alerta) {
        JPanel linha = new JPanel();
        linha.setOpaque(false);
        linha.setLayout(new BoxLayout(linha, BoxLayout.X_AXIS));
        linha.add(new Ponto(alerta ? new Color(0xF5, 0x9E, 0x0B) : DESTAQUE));
        linha.add(Box.createHorizontalStrut(8));
        JLabel l = new JLabel("<html><body style='width:190px'>" + texto + "</body></html>");
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(new Color(0x47, 0x55, 0x69));
        linha.add(l);
        linha.add(Box.createHorizontalGlue());
        return linha;
    }

    private JPanel tituloCartao(String texto) {
        JPanel cab = new JPanel(new BorderLayout());
        cab.setOpaque(false);
        cab.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDA),
                BorderFactory.createEmptyBorder(11, 14, 11, 14)));
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(AZUL);
        cab.add(l, BorderLayout.WEST);
        return cab;
    }

    private JPanel montarStatus() {
        JPanel status = new JPanel(new BorderLayout());
        status.setBackground(BEGE);
        status.setPreferredSize(new Dimension(0, 28));
        status.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDA),
                BorderFactory.createEmptyBorder(0, 14, 0, 14)));

        JPanel esquerda = new JPanel();
        esquerda.setOpaque(false);
        esquerda.setLayout(new BoxLayout(esquerda, BoxLayout.X_AXIS));
        esquerda.add(new Ponto(new Color(0x22, 0xC5, 0x5E)));
        esquerda.add(Box.createHorizontalStrut(6));
        esquerda.add(textoStatus("SQLite conectado - treinanorma.db"));
        esquerda.add(Box.createHorizontalStrut(14));
        lblBackup = textoStatus("Nenhum backup gerado");
        esquerda.add(lblBackup);
        status.add(esquerda, BorderLayout.WEST);
        atualizarStatus();

        JPanel direita = new JPanel();
        direita.setOpaque(false);
        direita.setLayout(new BoxLayout(direita, BoxLayout.X_AXIS));
        direita.add(textoStatus(nomeUsuario + " - " + perfil));
        direita.add(Box.createHorizontalStrut(14));
        direita.add(textoStatus(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        status.add(direita, BorderLayout.EAST);

        return status;
    }

    private JLabel textoStatus(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(SUAVE);
        return l;
    }

    private void filtrar() {
        if (ordenador == null) {
            return;
        }
        String termo = txtBusca.getText().trim();
        if (termo.isEmpty()) {
            ordenador.setRowFilter(null);
        } else {
            ordenador.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(termo)));
        }
    }

    private void alternarMaximizar() {
        if ((getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
            setExtendedState(JFrame.NORMAL);
        } else {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
        atualizarForma();
    }

    private void atualizarForma() {
        boolean cheio = (getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH;
        if (cheio) {
            setShape(null);
        } else {
            setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 18, 18));
        }
        if (btnMaximizar != null) {
            btnMaximizar.setTipo(cheio ? BotaoJanela.Tipo.RESTAURAR : BotaoJanela.Tipo.MAXIMIZAR);
            btnMaximizar.setToolTipText(cheio ? "Restaurar" : "Maximizar");
        }
    }

    private boolean maximizado() {
        return (getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH;
    }

    private void instalarArraste(Component c) {
        final Point ponto = new Point();
        MouseAdapter adaptador = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                ponto.setLocation(e.getPoint());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (maximizado()) {
                    return;
                }
                Point naTela = e.getLocationOnScreen();
                setLocation(naTela.x - ponto.x, naTela.y - ponto.y);
            }
        };
        instalarArraste(c, adaptador);
    }

    private void instalarArraste(Component c, MouseAdapter adaptador) {
        c.addMouseListener(adaptador);
        c.addMouseMotionListener(adaptador);
        if (c instanceof java.awt.Container) {
            for (Component filho : ((java.awt.Container) c).getComponents()) {
                instalarArraste(filho, adaptador);
            }
        }
    }

    private void abrirTela(JComponent tela) {
        host.removeAll();
        host.add(tela, BorderLayout.CENTER);
        host.revalidate();
        host.repaint();
    }

    private void mostrarDashboard() {
        host.removeAll();
        host.add(dashboard, BorderLayout.CENTER);
        host.revalidate();
        host.repaint();
        atualizarKpis();
        atualizarTabela();
        atualizarStatus();
    }

    private void atualizarStatus() {
        if (lblBackup == null) {
            return;
        }
        File ultimo = ultimoBackup();
        lblBackup.setText(ultimo == null ? "Nenhum backup gerado"
                : "Último backup: " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm")
                        .format(new java.util.Date(ultimo.lastModified())));
    }

    private File pastaBackup() {
        File pasta = new File(System.getProperty("user.home") + File.separator
                + "TreinaNorma" + File.separator + "backups");
        pasta.mkdirs();
        return pasta;
    }

    private File ultimoBackup() {
        File[] arquivos = pastaBackup().listFiles((dir, nome) -> nome.endsWith(".db"));
        if (arquivos == null || arquivos.length == 0) {
            return null;
        }
        File ultimo = arquivos[0];
        for (File f : arquivos) {
            if (f.lastModified() > ultimo.lastModified()) {
                ultimo = f;
            }
        }
        return ultimo;
    }

    private void backup() {
        try {
            File origem = new File(Conexao.caminho());
            if (!origem.exists()) {
                Ui.aviso(this, "Banco de dados não encontrado em " + origem.getAbsolutePath());
                return;
            }
            String nome = "treinanorma-"
                    + new java.text.SimpleDateFormat("yyyyMMdd-HHmmss").format(new java.util.Date())
                    + ".db";
            File destino = new File(pastaBackup(), nome);
            Files.copy(origem.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
            atualizarStatus();
            if (Ui.confirma(this, "Backup criado em:\n" + destino.getAbsolutePath()
                    + "\n\nDeseja abrir a pasta?")) {
                java.awt.Desktop.getDesktop().open(destino.getParentFile());
            }
        } catch (Exception e) {
            Ui.erro(this, "Falha ao gerar backup: " + e.getMessage());
        }
    }

    private void restaurar() {
        javax.swing.JFileChooser escolha = new javax.swing.JFileChooser(pastaBackup());
        escolha.setDialogTitle("Selecionar backup (.db)");
        escolha.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Banco SQLite", "db"));
        if (escolha.showOpenDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) {
            return;
        }
        if (!Ui.confirma(this, "Restaurar " + escolha.getSelectedFile().getName()
                + "?\n\nOs dados atuais serão substituídos. Recomenda-se gerar um backup antes.")) {
            return;
        }
        try {
            Files.copy(escolha.getSelectedFile().toPath(), new File(Conexao.caminho()).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            Ui.info(this, "Backup restaurado. O painel será atualizado.");
            mostrarDashboard();
        } catch (Exception e) {
            Ui.erro(this, "Falha ao restaurar: " + e.getMessage());
        }
    }

    private void manual() {
        Ui.info(this, "Fluxo recomendado:\n\n"
                + "1. Cadastros > Empresas: dados do cabeçalho dos documentos.\n"
                + "2. Cadastros > Instrutores: habilitações por NR (com validade).\n"
                + "3. Cadastros > Colaboradores: cadastro manual ou importação .xlsx/.csv.\n"
                + "4. Treinamentos > Registrar treinamento: crie a turma, selecione participantes\n"
                + "   e lance a chamada.\n"
                + "5. Conclua a turma para gerar os registros com validade da matriz.\n"
                + "6. Treinamentos > Emitir certificado: PDF com número e código de verificação.\n"
                + "7. Documentos da turma: ata de presença e crachás.\n\n"
                + "Vencimentos nulos significam 'revisar conforme PGR'.");
    }

    private void sobre() {
        JDialog d = Ui.dialogo(this, "Sobre", 460, 380);
        JPanel corpo = new JPanel();
        corpo.setOpaque(false);
        corpo.setLayout(new BoxLayout(corpo, BoxLayout.Y_AXIS));
        corpo.setBorder(BorderFactory.createEmptyBorder(22, 24, 6, 24));

        JLabel imagem = new JLabel(logo(340));
        imagem.setAlignmentX(Component.CENTER_ALIGNMENT);
        corpo.add(imagem);

        corpo.add(Box.createVerticalStrut(22));
        corpo.add(centralizado("© " + LocalDate.now().getYear()
                + " - Todos os direitos reservados.", 12, Font.PLAIN, SUAVE));
        corpo.add(Box.createVerticalStrut(8));
        corpo.add(centralizado("Versão " + VERSAO, 13, Font.BOLD, AZUL));

        corpo.add(Box.createVerticalStrut(18));
        corpo.add(separador());
        corpo.add(Box.createVerticalStrut(16));
        corpo.add(centralizado("Maxwell Souza dos Santos", 13, Font.BOLD, TEXTO));
        corpo.add(Box.createVerticalStrut(4));
        corpo.add(centralizado("Desenvolvedor", 12, Font.PLAIN, SUAVE));
        corpo.add(Box.createVerticalStrut(6));
        corpo.add(centralizado("CNPJ 54.718.287/0001-19", 11, Font.PLAIN, SUAVE));
        corpo.add(Box.createVerticalGlue());

        JButton ok = Ui.primario("OK");
        ok.setPreferredSize(new Dimension(120, 32));
        ok.setMaximumSize(new Dimension(120, 32));
        ok.setAlignmentX(Component.CENTER_ALIGNMENT);
        ok.addActionListener(e -> d.dispose());
        JPanel rodape = new JPanel();
        rodape.setOpaque(false);
        rodape.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 12));
        rodape.add(ok);

        d.getRootPane().setDefaultButton(ok);
        d.add(corpo, BorderLayout.CENTER);
        d.add(rodape, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private JLabel centralizado(String texto, int tamanho, int estilo, Color cor) {
        JLabel l = new JLabel(texto);
        l.setFont(Ui.fonte(tamanho, estilo));
        l.setForeground(cor);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private JComponent separador() {
        JPanel linha = new JPanel();
        linha.setBackground(BORDA);
        linha.setPreferredSize(new Dimension(300, 1));
        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        linha.setAlignmentX(Component.CENTER_ALIGNMENT);
        return linha;
    }

    private Icon logo(int largura) {
        List<Image> variantes = new java.util.ArrayList<>();
        for (int escala = 1; escala <= 3; escala++) {
            BufferedImage versao = carregarLogo("/images/logo-" + escala + "x.png");
            if (versao == null) {
                variantes.clear();
                break;
            }
            variantes.add(versao);
        }
        if (variantes.size() != 3) {
            BufferedImage unica = carregarLogo("/images/logo-2x.png");
            if (unica == null) {
                return new ImageIcon();
            }
            return new ImageIcon(unica.getScaledInstance(largura, -1, Image.SCALE_AREA_AVERAGING));
        }
        Image base = variantes.get(0);
        int altura = (int) Math.round(largura * base.getHeight(null)
                / (double) base.getWidth(null));
        return new IconeLogo(new LogoMulti(variantes), largura, altura);
    }

    private BufferedImage carregarLogo(String caminho) {
        try (java.io.InputStream entrada = MainFrame.class.getResourceAsStream(caminho)) {
            return entrada == null ? null : ImageIO.read(entrada);
        } catch (IOException e) {
            return null;
        }
    }

    private static final class LogoMulti extends java.awt.image.AbstractMultiResolutionImage {

        private final List<Image> variantes;

        LogoMulti(List<Image> variantes) {
            this.variantes = List.copyOf(variantes);
        }

        @Override
        protected Image getBaseImage() {
            return variantes.get(0);
        }

        @Override
        public Image getResolutionVariant(double larguraDestino, double alturaDestino) {
            double fator = Math.max(larguraDestino / getBaseImage().getWidth(null),
                    alturaDestino / getBaseImage().getHeight(null));
            Image escolhida = variantes.get(0);
            for (Image variante : variantes) {
                escolhida = variante;
                if (variante.getWidth(null) / (double) getBaseImage().getWidth(null) >= fator) {
                    break;
                }
            }
            return escolhida;
        }

        @Override
        public List<Image> getResolutionVariants() {
            return variantes;
        }
    }

    private static final class IconeLogo implements Icon {

        private final Image imagem;
        private final int largura;
        private final int altura;

        IconeLogo(Image imagem, int largura, int altura) {
            this.imagem = imagem;
            this.largura = largura;
            this.altura = altura;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                    RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g2.drawImage(imagem, x, y, largura, altura, null);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return largura;
        }

        @Override
        public int getIconHeight() {
            return altura;
        }
    }

    private void sair() {
        Sessao.encerrar();
        dispose();
        System.exit(0);
    }

    private void encerrarSessao() {
        Sessao.encerrar();
        dispose();
        new LoginFrame().setVisible(true);
    }

    private static class BotaoJanela extends JButton {

        enum Tipo {
            MINIMIZAR, MAXIMIZAR, RESTAURAR, FECHAR
        }

        private Tipo tipo;
        private final Color normal = new Color(255, 255, 255, 0);
        private final Color hover;

        BotaoJanela(Tipo tipo) {
            this.tipo = tipo;
            this.hover = tipo == Tipo.FECHAR ? new Color(0xC0, 0x39, 0x2B) : new Color(255, 255, 255, 30);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFocusable(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(30, 28));
            setMaximumSize(new Dimension(30, 28));
            setForeground(new Color(0xDB, 0xEA, 0xFE));
            setBackground(normal);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(hover);
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(normal);
                    repaint();
                }
            });
        }

        void setTipo(Tipo tipo) {
            this.tipo = tipo;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getBackground().getAlpha() > 0) {
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            }
            g2.setColor(getForeground());
            g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            switch (tipo) {
                case MINIMIZAR:
                    g2.drawLine(cx - 5, cy + 3, cx + 5, cy + 3);
                    break;
                case MAXIMIZAR:
                    g2.drawRect(cx - 5, cy - 5, 10, 10);
                    break;
                case RESTAURAR:
                    g2.drawRect(cx - 6, cy - 4, 9, 9);
                    g2.drawLine(cx - 3, cy - 4, cx - 3, cy - 7);
                    g2.drawLine(cx - 3, cy - 7, cx + 5, cy - 7);
                    g2.drawLine(cx + 5, cy - 7, cx + 5, cy + 2);
                    g2.drawLine(cx + 5, cy + 2, cx + 3, cy + 2);
                    break;
                case FECHAR:
                    g2.drawLine(cx - 5, cy - 5, cx + 5, cy + 5);
                    g2.drawLine(cx - 5, cy + 5, cx + 5, cy - 5);
                    break;
            }
            g2.dispose();
        }
    }

    private static class Cartao extends JPanel {

        Cartao(java.awt.LayoutManager layout) {
            super(layout);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g2.setColor(BORDA);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class Ponto extends JComponent {

        private final Color cor;

        Ponto(Color cor) {
            this.cor = cor;
            setPreferredSize(new Dimension(7, 7));
            setMaximumSize(new Dimension(7, 7));
            setMinimumSize(new Dimension(7, 7));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(cor);
            g2.fillOval(0, 0, 7, 7);
            g2.dispose();
        }
    }

    private static class RenderizadorStatus extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object valor, boolean selecionado,
                boolean foco, int linha, int coluna) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, valor, selecionado, foco, linha, coluna);
            String texto = String.valueOf(valor);
            Color fundo;
            Color frente;
            if (texto.equalsIgnoreCase("Vencido")) {
                fundo = new Color(0xFE, 0xE2, 0xE2);
                frente = new Color(0xB9, 0x1C, 0x1C);
            } else if (texto.equalsIgnoreCase("Vence em 5 d") || texto.equalsIgnoreCase("Vence em 2 d")) {
                fundo = new Color(0xFE, 0xF3, 0xC7);
                frente = new Color(0x92, 0x40, 0x0E);
            } else if (texto.endsWith(" d")) {
                fundo = new Color(0xE0, 0xF2, 0xFE);
                frente = new Color(0x07, 0x59, 0x85);
            } else {
                fundo = new Color(0xDC, 0xFC, 0xE7);
                frente = new Color(0x16, 0x65, 0x34);
            }
            l.setOpaque(true);
            l.setBackground(selecionado ? table.getSelectionBackground() : fundo);
            l.setForeground(selecionado ? table.getSelectionForeground() : frente);
            l.setFont(new Font("Segoe UI", Font.BOLD, 11));
            l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            return l;
        }
    }
}
