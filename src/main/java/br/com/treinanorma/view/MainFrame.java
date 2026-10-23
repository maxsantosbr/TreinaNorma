package br.com.treinanorma.view;

import br.com.treinanorma.dao.Conexao;
import br.com.treinanorma.dao.RegistroDAO;
import br.com.treinanorma.model.Perfil;
import br.com.treinanorma.model.RegistroTreinamento;
import br.com.treinanorma.model.Usuario;
import br.com.treinanorma.util.Datas;
import br.com.treinanorma.util.Icones;
import br.com.treinanorma.util.Sessao;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D.Double;
import java.awt.image.AbstractMultiResolutionImage;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

public class MainFrame extends JFrame {
   public static final String VERSAO = "1.1.0";
   private static final Color AZUL = new Color(31, 78, 121);
   private static final Color AZUL_CLARO = new Color(46, 117, 182);
   private static final Color DESTAQUE = new Color(10, 167, 232);
   private static final Color FUNDO = new Color(246, 248, 251);
   private static final Color BORDA = new Color(219, 227, 238);
   private static final Color TEXTO = new Color(31, 41, 55);
   private static final Color SUAVE = new Color(107, 114, 128);
   private static final Color BEGE = new Color(238, 244, 250);
   private final Usuario usuario = Sessao.getUsuario();
   private final String nomeUsuario = this.usuario == null ? "Convidado" : this.usuario.getNome();
   private final Perfil perfil = this.usuario == null ? Perfil.CONSULTA : this.usuario.getPerfil();
   private JTable tabela;
   private TableRowSorter<DefaultTableModel> ordenador;
   private JTextField txtBusca;
   private MainFrame.BotaoJanela btnMaximizar;
   private JPanel host;
   private JPanel dashboard;
   private JPanel painelKpis;
   private JLabel lblBackup;
   private final RegistroDAO registroDAO = new RegistroDAO();

   public MainFrame() {
      this.setTitle("TreinaNorma - Gestão de treinamentos de Normas Regulamentadoras. (v1.1.0)");
      this.setUndecorated(true);
      this.setDefaultCloseOperation(0);
      Icones.aplicarIcone(this);
      this.setMinimumSize(new Dimension(960, 640));
      Dimension tela = Toolkit.getDefaultToolkit().getScreenSize();
      this.setSize(Math.min(1200, tela.width - 80), Math.min(760, tela.height - 80));
      this.setLocationRelativeTo(null);
      this.montarTela();
      this.atualizarForma();
      this.addComponentListener(new ComponentAdapter() {
         @Override
         public void componentResized(ComponentEvent e) {
            MainFrame.this.atualizarForma();
         }
      });
      this.addWindowStateListener(e -> SwingUtilities.invokeLater(this::atualizarForma));
      this.addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent e) {
            MainFrame.this.sair();
         }
      });
   }

   private void montarTela() {
      JPanel raiz = new JPanel(new BorderLayout());
      raiz.setBackground(FUNDO);
      this.setContentPane(raiz);
      raiz.add(this.montarTopo(), "North");
      JPanel centro = new JPanel(new BorderLayout());
      centro.setBackground(FUNDO);
      centro.add(this.montarMenu(), "North");
      this.dashboard = this.montarConteudo();
      this.host = new JPanel(new BorderLayout());
      this.host.setBackground(FUNDO);
      this.host.add(this.dashboard, "Center");
      centro.add(this.host, "Center");
      raiz.add(centro, "Center");
      raiz.add(this.montarStatus(), "South");
   }

   private JPanel montarTopo() {
      JPanel topo = new JPanel(new BorderLayout(10, 0));
      topo.setBackground(AZUL);
      topo.setPreferredSize(new Dimension(0, 56));
      topo.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 10));
      JPanel marca = new JPanel();
      marca.setOpaque(false);
      marca.setLayout(new BoxLayout(marca, 0));
      marca.add(this.selo());
      marca.add(Box.createHorizontalStrut(10));
      JLabel titulo = new JLabel("TreinaNorma");
      titulo.setFont(new Font("Segoe UI", 1, 14));
      titulo.setForeground(Color.WHITE);
      marca.add(titulo);
      JLabel subtitulo = new JLabel(" - Gestão de treinamentos de Normas Regulamentadoras. (v1.1.0)");
      subtitulo.setFont(new Font("Segoe UI", 0, 12));
      subtitulo.setForeground(new Color(219, 234, 254));
      marca.add(subtitulo);
      topo.add(marca, "West");
      JPanel direita = new JPanel();
      direita.setOpaque(false);
      direita.setLayout(new BoxLayout(direita, 0));
      this.txtBusca = new JTextField();
      this.txtBusca.putClientProperty("JTextField.placeholderText", "Buscar colaborador, NR ou certificado...");
      this.txtBusca.putClientProperty("JTextField.showClearButton", Boolean.valueOf(true));
      this.txtBusca.setMaximumSize(new Dimension(200, 30));
      this.txtBusca.setPreferredSize(new Dimension(200, 30));
      this.txtBusca.setFont(new Font("Segoe UI", 0, 12));
      this.txtBusca.getDocument().addDocumentListener(new DocumentListener() {
         @Override
         public void insertUpdate(DocumentEvent e) {
            MainFrame.this.filtrar();
         }

         @Override
         public void removeUpdate(DocumentEvent e) {
            MainFrame.this.filtrar();
         }

         @Override
         public void changedUpdate(DocumentEvent e) {
            MainFrame.this.filtrar();
         }
      });
      direita.add(this.txtBusca);
      direita.add(Box.createHorizontalStrut(12));
      JLabel chip = new JLabel("  " + this.nomeUsuario + "  |  " + this.perfil + "  ");
      chip.setFont(new Font("Segoe UI", 0, 12));
      chip.setForeground(Color.WHITE);
      chip.setOpaque(true);
      chip.setBackground(new Color(255, 255, 255, 30));
      chip.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
      direita.add(chip);
      direita.add(Box.createHorizontalStrut(8));
      MainFrame.BotaoJanela btnMin = new MainFrame.BotaoJanela(MainFrame.BotaoJanela.Tipo.MINIMIZAR);
      btnMin.setToolTipText("Minimizar");
      btnMin.addActionListener(e -> this.setExtendedState(1));
      this.btnMaximizar = new MainFrame.BotaoJanela(MainFrame.BotaoJanela.Tipo.MAXIMIZAR);
      this.btnMaximizar.setToolTipText("Maximizar");
      this.btnMaximizar.addActionListener(e -> this.alternarMaximizar());
      MainFrame.BotaoJanela btnFechar = new MainFrame.BotaoJanela(MainFrame.BotaoJanela.Tipo.FECHAR);
      btnFechar.setToolTipText("Fechar");
      btnFechar.addActionListener(e -> this.sair());
      direita.add(btnMin);
      direita.add(this.btnMaximizar);
      direita.add(btnFechar);
      topo.add(direita, "East");
      this.instalarArraste(topo);
      return topo;
   }

   private JComponent selo() {
      final Image img = Icones.carregar("/icones/converterICO.png").getImage().getScaledInstance(26, 26, 4);
      JComponent selo = new JComponent() {
         @Override
         protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g.create();
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
      JMenu arquivo = this.menu("Arquivo");
      arquivo.add(this.item("Backup do banco", this::backup));
      arquivo.add(this.item("Restaurar backup", this::restaurar));
      arquivo.addSeparator();
      arquivo.add(this.item("Sair", this::sair));
      JMenu cadastros = this.menu("Cadastros");
      cadastros.add(this.item("Empresas / estabelecimentos", () -> this.abrirTela(new EmpresaPanel(this::mostrarDashboard))));
      cadastros.add(this.item("Colaboradores", () -> this.abrirTela(new ColaboradoresPanel(this::mostrarDashboard))));
      cadastros.add(this.item("Instrutores", () -> this.abrirTela(new InstrutoresPanel(this::mostrarDashboard))));
      JMenu treinamentos = this.menu("Treinamentos");
      treinamentos.add(this.item("Registrar treinamento", () -> this.abrirTela(new TurmasPanel(this::mostrarDashboard))));
      treinamentos.add(this.item("Emitir certificado", () -> this.abrirTela(new CertificadosPanel(this::mostrarDashboard))));
      treinamentos.add(this.item("Matriz de treinamento por NR", () -> this.abrirTela(new MatrizPanel(this::mostrarDashboard))));
      JMenu consultas = this.menu("Consultas");
      consultas.add(this.item("Vencimentos", () -> this.abrirTela(new VencimentosPanel(this::mostrarDashboard))));
      consultas.add(this.item("Histórico do colaborador", () -> this.abrirTela(new ColaboradoresPanel(this::mostrarDashboard))));
      consultas.add(this.item("Treinamentos por NR", () -> this.abrirTela(new CertificadosPanel(this::mostrarDashboard))));
      JMenu administracao = this.menu("Administração");
      administracao.add(this.item("Usuários", () -> this.abrirTela(new UsuariosPanel(this::mostrarDashboard))));
      administracao.add(this.item("Parâmetros do sistema", () -> this.abrirTela(new ParametrosPanel(this::mostrarDashboard))));
      JMenu conta = this.menu("Conta");
      conta.add(this.item("Trocar senha", () -> UsuariosPanel.trocarSenha(this)));
      conta.add(this.item("Encerrar sessão", this::encerrarSessao));
      JMenu ajuda = this.menu("Ajuda");
      ajuda.add(this.item("Manual", this::manual));
      ajuda.add(this.item("Sobre", this::sobre));
      barra.add(arquivo);
      barra.add(cadastros);
      barra.add(treinamentos);
      barra.add(consultas);
      if (this.perfil == Perfil.ADMIN) {
         barra.add(administracao);
      }

      barra.add(conta);
      barra.add(ajuda);
      return barra;
   }

   private JMenu menu(String titulo) {
      JMenu m = new JMenu(titulo);
      m.setFont(new Font("Segoe UI", 0, 13));
      m.setForeground(TEXTO);
      return m;
   }

   private JMenuItem item(String titulo, Runnable acao) {
      JMenuItem i = new JMenuItem(titulo);
      i.setFont(new Font("Segoe UI", 0, 13));
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
      titulos.setLayout(new BoxLayout(titulos, 1));
      JLabel titulo = new JLabel("Dashboard");
      titulo.setFont(new Font("Segoe UI", 1, 18));
      titulo.setForeground(TEXTO);
      JLabel legenda = new JLabel("Vigência dos treinamentos obrigatórios por colaborador e NR.");
      legenda.setFont(new Font("Segoe UI", 0, 12));
      legenda.setForeground(SUAVE);
      titulos.add(titulo);
      titulos.add(Box.createVerticalStrut(2));
      titulos.add(legenda);
      cabecalho.add(titulos, "West");
      JButton novo = new JButton("+ Novo treinamento");
      novo.setFont(new Font("Segoe UI", 1, 12));
      novo.setBackground(AZUL);
      novo.setForeground(Color.WHITE);
      novo.setFocusPainted(false);
      novo.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
      novo.setCursor(Cursor.getPredefinedCursor(12));
      novo.addActionListener(e -> this.abrirTela(new TurmasPanel(this::mostrarDashboard)));
      JPanel acao = new JPanel(new BorderLayout());
      acao.setOpaque(false);
      acao.add(novo, "North");
      cabecalho.add(acao, "East");
      conteudo.add(cabecalho, "North");
      JPanel centro = new JPanel(new BorderLayout(0, 14));
      centro.setOpaque(false);
      centro.add(this.montarKpis(), "North");
      centro.add(this.montarCorpo(), "Center");
      conteudo.add(centro, "Center");
      return conteudo;
   }

   private JPanel montarKpis() {
      this.painelKpis = new JPanel(new GridLayout(1, 4, 14, 0));
      this.painelKpis.setOpaque(false);
      this.atualizarKpis();
      return this.painelKpis;
   }

   private void atualizarKpis() {
      if (this.painelKpis != null) {
         int[] resumo;
         try {
            resumo = this.registroDAO.resumo();
         } catch (RuntimeException var3) {
            resumo = new int[]{0, 0, 0, 0};
         }

         this.painelKpis.removeAll();
         this.painelKpis.add(this.cartaoKpi("Colaboradores ativos", String.valueOf(resumo[0]), "cadastrados no sistema", new Color(21, 128, 61)));
         this.painelKpis.add(this.cartaoKpi("Vencem em 30 dias", String.valueOf(resumo[1]), "programar reciclagem", new Color(180, 83, 9)));
         this.painelKpis.add(this.cartaoKpi("Vencidos", String.valueOf(resumo[2]), "ação imediata", new Color(185, 28, 28)));
         this.painelKpis.add(this.cartaoKpi("Certificados no ano", String.valueOf(resumo[3]), "emitidos em " + LocalDate.now().getYear(), SUAVE));
         this.painelKpis.revalidate();
         this.painelKpis.repaint();
      }
   }

   private JPanel cartaoKpi(String rotulo, String valor, String rodape, Color corRodape) {
      MainFrame.Cartao c = new MainFrame.Cartao(new BorderLayout());
      c.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
      JLabel l = new JLabel(rotulo.toUpperCase());
      l.setFont(new Font("Segoe UI", 0, 11));
      l.setForeground(SUAVE);
      c.add(l, "North");
      JLabel v = new JLabel(valor);
      v.setFont(new Font("Segoe UI", 1, 26));
      v.setForeground(AZUL);
      c.add(v, "Center");
      JLabel r = new JLabel(rodape);
      r.setFont(new Font("Segoe UI", 0, 11));
      r.setForeground(corRodape);
      c.add(r, "South");
      return c;
   }

   private JPanel montarCorpo() {
      JPanel corpo = new JPanel(new GridBagLayout());
      corpo.setOpaque(false);
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridy = 0;
      gbc.weighty = 1.0;
      gbc.fill = 1;
      gbc.gridx = 0;
      gbc.weightx = 0.67;
      gbc.insets = new Insets(0, 0, 0, 14);
      corpo.add(this.montarTabela(), gbc);
      gbc.gridx = 1;
      gbc.weightx = 0.33;
      gbc.insets = new Insets(0, 0, 0, 0);
      corpo.add(this.montarLateral(), gbc);
      return corpo;
   }

   private JPanel montarTabela() {
      MainFrame.Cartao cartao = new MainFrame.Cartao(new BorderLayout());
      JPanel cab = new JPanel(new BorderLayout());
      cab.setOpaque(false);
      cab.setBorder(BorderFactory.createEmptyBorder(11, 14, 11, 14));
      JLabel t = new JLabel("Treinamentos vencendo");
      t.setFont(new Font("Segoe UI", 1, 12));
      t.setForeground(AZUL);
      cab.add(t, "West");
      JLabel ver = new JLabel("ver todos");
      ver.setFont(new Font("Segoe UI", 0, 11));
      ver.setForeground(AZUL_CLARO);
      ver.setCursor(Cursor.getPredefinedCursor(12));
      ver.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent e) {
            MainFrame.this.abrirTela(new VencimentosPanel(MainFrame.this::mostrarDashboard));
         }
      });
      cab.add(ver, "East");
      cab.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDA), BorderFactory.createEmptyBorder(11, 14, 11, 14)));
      cartao.add(cab, "North");
      String[] colunas = new String[]{"Colaborador", "NR / Treinamento", "Validade", "Status"};
      DefaultTableModel modelo = new DefaultTableModel(new Object[0][0], colunas) {
         @Override
         public boolean isCellEditable(int row, int column) {
            return false;
         }
      };
      this.tabela = new JTable(modelo);
      this.tabela.setFont(new Font("Segoe UI", 0, 12));
      this.tabela.setForeground(new Color(51, 65, 85));
      this.tabela.setRowHeight(34);
      this.tabela.setShowVerticalLines(false);
      this.tabela.setShowHorizontalLines(true);
      this.tabela.setGridColor(new Color(241, 245, 249));
      this.tabela.setIntercellSpacing(new Dimension(0, 1));
      this.tabela.setSelectionBackground(new Color(232, 242, 251));
      this.tabela.setSelectionForeground(AZUL);
      this.tabela.setFillsViewportHeight(true);
      this.tabela.setAutoResizeMode(2);
      this.tabela.setSelectionMode(0);
      this.tabela.setBorder(BorderFactory.createEmptyBorder());
      JTableHeader header = this.tabela.getTableHeader();
      header.setFont(new Font("Segoe UI", 1, 11));
      header.setBackground(BEGE);
      header.setForeground(AZUL);
      header.setReorderingAllowed(false);
      header.setPreferredSize(new Dimension(0, 32));
      header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDA));
      this.tabela.getColumnModel().getColumn(0).setPreferredWidth(170);
      this.tabela.getColumnModel().getColumn(1).setPreferredWidth(260);
      this.tabela.getColumnModel().getColumn(2).setPreferredWidth(95);
      this.tabela.getColumnModel().getColumn(3).setPreferredWidth(105);
      MainFrame.RenderizadorStatus status = new MainFrame.RenderizadorStatus();
      this.tabela.getColumnModel().getColumn(3).setCellRenderer(status);
      DefaultTableCellRenderer normal = new DefaultTableCellRenderer();
      normal.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

      for (int i = 0; i < 3; i++) {
         this.tabela.getColumnModel().getColumn(i).setCellRenderer(normal);
      }

      this.ordenador = new TableRowSorter<>(modelo);
      this.tabela.setRowSorter(this.ordenador);
      this.atualizarTabela();
      JScrollPane rolagem = new JScrollPane(this.tabela);
      rolagem.setBorder(BorderFactory.createEmptyBorder());
      rolagem.getViewport().setBackground(Color.WHITE);
      cartao.add(rolagem, "Center");
      JPanel envoltorio = new JPanel(new BorderLayout());
      envoltorio.setOpaque(false);
      envoltorio.add(cartao, "Center");
      return envoltorio;
   }

   private void atualizarTabela() {
      if (this.tabela != null) {
         DefaultTableModel modelo = (DefaultTableModel)this.tabela.getModel();
         modelo.setRowCount(0);

         try {
            List<RegistroTreinamento> vencidos = this.registroDAO.listar(null, "VENCIDOS");
            vencidos.sort((a, b) -> b.getValidade().compareTo((ChronoLocalDate)a.getValidade()));
            List<RegistroTreinamento> proximos = this.registroDAO.vencimentosProximos(30, 12);
            int adicionados = 0;

            for (RegistroTreinamento r : vencidos) {
               if (adicionados++ >= 12) {
                  break;
               }

               modelo.addRow(new Object[]{r.getColaboradorNome(), r.getNorma() + " - " + r.getTreinamento(), Datas.br(r.getValidade()), "Vencido"});
            }

            for (RegistroTreinamento r : proximos) {
               if (r.getValidade() != null && !r.getValidade().isBefore(LocalDate.now()) && adicionados++ < 12) {
                  long dias = ChronoUnit.DAYS.between(LocalDate.now(), r.getValidade());
                  modelo.addRow(
                     new Object[]{
                        r.getColaboradorNome(),
                        r.getNorma() + " - " + r.getTreinamento(),
                        Datas.br(r.getValidade()),
                        dias <= 5L ? "Vence em 5 d" : "Vence em " + dias + " d"
                     }
                  );
               }
            }
         } catch (RuntimeException var9) {
         }
      }
   }

   private JPanel montarLateral() {
      JPanel lateral = new JPanel(new BorderLayout(0, 14));
      lateral.setOpaque(false);
      lateral.add(this.montarAcoes(), "North");
      lateral.add(this.montarAvisos(), "Center");
      return lateral;
   }

   private JPanel montarAcoes() {
      MainFrame.Cartao cartao = new MainFrame.Cartao(new BorderLayout());
      cartao.add(this.tituloCartao("Ações rápidas"), "North");
      JPanel lista = new JPanel();
      lista.setOpaque(false);
      lista.setLayout(new BoxLayout(lista, 1));
      lista.add(this.linhaAcao("Registrar treinamento", "lançar execução e presença", false, () -> this.abrirTela(new TurmasPanel(this::mostrarDashboard))));
      lista.add(this.linhaAcao("Emitir certificado", "gerar PDF com base legal", false, () -> this.abrirTela(new CertificadosPanel(this::mostrarDashboard))));
      lista.add(this.linhaAcao("Importar colaboradores", "CSV ou cadastro manual", false, () -> this.abrirTela(new ColaboradoresPanel(this::mostrarDashboard))));
      cartao.add(lista, "Center");
      return cartao;
   }

   private JPanel linhaAcao(String titulo, String descricao, boolean alerta, final Runnable acao) {
      JPanel linha = new JPanel();
      linha.setOpaque(false);
      linha.setLayout(new BoxLayout(linha, 0));
      linha.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
      MainFrame.Ponto ponto = new MainFrame.Ponto(alerta ? new Color(245, 158, 11) : DESTAQUE);
      linha.add(ponto);
      linha.add(Box.createHorizontalStrut(8));
      JLabel texto = new JLabel("<html><b>" + titulo + "</b><br><font color='#6B7280'>" + descricao + "</font></html>");
      texto.setFont(new Font("Segoe UI", 0, 11));
      texto.setForeground(AZUL);
      texto.setCursor(Cursor.getPredefinedCursor(12));
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
      MainFrame.Cartao cartao = new MainFrame.Cartao(new BorderLayout());
      cartao.add(this.tituloCartao("Avisos"), "North");
      JPanel lista = new JPanel();
      lista.setOpaque(false);
      lista.setLayout(new BoxLayout(lista, 1));
      lista.setBorder(BorderFactory.createEmptyBorder(10, 14, 12, 14));
      lista.add(this.aviso("CIPA: mandato vence em 15 dias.", true));
      lista.add(Box.createVerticalStrut(6));
      lista.add(this.aviso("3 linhas da matriz sem CH fixada na NR (referência de mercado).", false));
      lista.add(Box.createVerticalStrut(6));
      lista.add(this.aviso("Treinamentos com validade nula seguem o PGR, não vencem.", false));
      cartao.add(lista, "Center");
      return cartao;
   }

   private JPanel aviso(String texto, boolean alerta) {
      JPanel linha = new JPanel();
      linha.setOpaque(false);
      linha.setLayout(new BoxLayout(linha, 0));
      linha.add(new MainFrame.Ponto(alerta ? new Color(245, 158, 11) : DESTAQUE));
      linha.add(Box.createHorizontalStrut(8));
      JLabel l = new JLabel("<html><body style='width:190px'>" + texto + "</body></html>");
      l.setFont(new Font("Segoe UI", 0, 11));
      l.setForeground(new Color(71, 85, 105));
      linha.add(l);
      linha.add(Box.createHorizontalGlue());
      return linha;
   }

   private JPanel tituloCartao(String texto) {
      JPanel cab = new JPanel(new BorderLayout());
      cab.setOpaque(false);
      cab.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDA), BorderFactory.createEmptyBorder(11, 14, 11, 14)));
      JLabel l = new JLabel(texto);
      l.setFont(new Font("Segoe UI", 1, 12));
      l.setForeground(AZUL);
      cab.add(l, "West");
      return cab;
   }

   private JPanel montarStatus() {
      JPanel status = new JPanel(new BorderLayout());
      status.setBackground(BEGE);
      status.setPreferredSize(new Dimension(0, 28));
      status.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDA), BorderFactory.createEmptyBorder(0, 14, 0, 14)));
      JPanel esquerda = new JPanel();
      esquerda.setOpaque(false);
      esquerda.setLayout(new BoxLayout(esquerda, 0));
      esquerda.add(new MainFrame.Ponto(new Color(34, 197, 94)));
      esquerda.add(Box.createHorizontalStrut(6));
      esquerda.add(this.textoStatus("SQLite conectado - treinanorma.db"));
      esquerda.add(Box.createHorizontalStrut(14));
      this.lblBackup = this.textoStatus("Nenhum backup gerado");
      esquerda.add(this.lblBackup);
      status.add(esquerda, "West");
      this.atualizarStatus();
      JPanel direita = new JPanel();
      direita.setOpaque(false);
      direita.setLayout(new BoxLayout(direita, 0));
      direita.add(this.textoStatus(this.nomeUsuario + " - " + this.perfil));
      direita.add(Box.createHorizontalStrut(14));
      direita.add(this.textoStatus(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
      status.add(direita, "East");
      return status;
   }

   private JLabel textoStatus(String texto) {
      JLabel l = new JLabel(texto);
      l.setFont(new Font("Segoe UI", 0, 11));
      l.setForeground(SUAVE);
      return l;
   }

   private void filtrar() {
      if (this.ordenador != null) {
         String termo = this.txtBusca.getText().trim();
         if (termo.isEmpty()) {
            this.ordenador.setRowFilter(null);
         } else {
            this.ordenador.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(termo)));
         }
      }
   }

   private void alternarMaximizar() {
      if ((this.getExtendedState() & 6) == 6) {
         this.setExtendedState(0);
      } else {
         this.setExtendedState(6);
      }

      this.atualizarForma();
   }

   private void atualizarForma() {
      boolean cheio = (this.getExtendedState() & 6) == 6;
      if (cheio) {
         this.setShape(null);
      } else {
         this.setShape(new Double(0.0, 0.0, (double)this.getWidth(), (double)this.getHeight(), 18.0, 18.0));
      }

      if (this.btnMaximizar != null) {
         this.btnMaximizar.setTipo(cheio ? MainFrame.BotaoJanela.Tipo.RESTAURAR : MainFrame.BotaoJanela.Tipo.MAXIMIZAR);
         this.btnMaximizar.setToolTipText(cheio ? "Restaurar" : "Maximizar");
      }
   }

   private boolean maximizado() {
      return (this.getExtendedState() & 6) == 6;
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
            if (!MainFrame.this.maximizado()) {
               Point naTela = e.getLocationOnScreen();
               MainFrame.this.setLocation(naTela.x - ponto.x, naTela.y - ponto.y);
            }
         }
      };
      this.instalarArraste(c, adaptador);
   }

   private void instalarArraste(Component c, MouseAdapter adaptador) {
      c.addMouseListener(adaptador);
      c.addMouseMotionListener(adaptador);
      if (c instanceof Container) {
         for (Component filho : ((Container)c).getComponents()) {
            this.instalarArraste(filho, adaptador);
         }
      }
   }

   private void abrirTela(JComponent tela) {
      this.host.removeAll();
      this.host.add(tela, "Center");
      this.host.revalidate();
      this.host.repaint();
   }

   private void mostrarDashboard() {
      this.host.removeAll();
      this.host.add(this.dashboard, "Center");
      this.host.revalidate();
      this.host.repaint();
      this.atualizarKpis();
      this.atualizarTabela();
      this.atualizarStatus();
   }

   private void atualizarStatus() {
      if (this.lblBackup != null) {
         File ultimo = this.ultimoBackup();
         this.lblBackup
            .setText(
               ultimo == null ? "Nenhum backup gerado" : "Último backup: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(ultimo.lastModified()))
            );
      }
   }

   private File pastaBackup() {
      File pasta = new File(System.getProperty("user.home") + File.separator + "TreinaNorma" + File.separator + "backups");
      pasta.mkdirs();
      return pasta;
   }

   private File ultimoBackup() {
      File[] arquivos = this.pastaBackup().listFiles((dir, nome) -> nome.endsWith(".db"));
      if (arquivos != null && arquivos.length != 0) {
         File ultimo = arquivos[0];

         for (File f : arquivos) {
            if (f.lastModified() > ultimo.lastModified()) {
               ultimo = f;
            }
         }

         return ultimo;
      } else {
         return null;
      }
   }

   private void backup() {
      try {
         File origem = new File(Conexao.caminho());
         if (!origem.exists()) {
            Ui.aviso(this, "Banco de dados não encontrado em " + origem.getAbsolutePath());
            return;
         }

         String nome = "treinanorma-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + ".db";
         File destino = new File(this.pastaBackup(), nome);
         Files.copy(origem.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
         this.atualizarStatus();
         if (Ui.confirma(this, "Backup criado em:\n" + destino.getAbsolutePath() + "\n\nDeseja abrir a pasta?")) {
            Desktop.getDesktop().open(destino.getParentFile());
         }
      } catch (Exception var4) {
         Ui.erro(this, "Falha ao gerar backup: " + var4.getMessage());
      }
   }

   private void restaurar() {
      JFileChooser escolha = new JFileChooser(this.pastaBackup());
      escolha.setDialogTitle("Selecionar backup (.db)");
      escolha.setFileFilter(new FileNameExtensionFilter("Banco SQLite", "db"));
      if (escolha.showOpenDialog(this) == 0) {
         if (Ui.confirma(
            this, "Restaurar " + escolha.getSelectedFile().getName() + "?\n\nOs dados atuais serão substituídos. Recomenda-se gerar um backup antes."
         )) {
            try {
               Files.copy(escolha.getSelectedFile().toPath(), new File(Conexao.caminho()).toPath(), StandardCopyOption.REPLACE_EXISTING);
               Ui.info(this, "Backup restaurado. O painel será atualizado.");
               this.mostrarDashboard();
            } catch (Exception var3) {
               Ui.erro(this, "Falha ao restaurar: " + var3.getMessage());
            }
         }
      }
   }

   private void manual() {
      Ui.info(
         this,
         "Fluxo recomendado:\n\n1. Cadastros > Empresas: dados do cabeçalho dos documentos.\n2. Cadastros > Colaboradores: cadastro manual ou importação .xlsx/.csv.\n   Use 'Foto e prévia do crachá...' para importar e enquadrar a foto 3x4.\n3. Cadastros > Instrutores: habilitações por NR (com validade).\n4. Treinamentos > Registrar treinamento: crie a turma, adicione os dias\n   (encontros) e selecione os participantes.\n5. Lance a chamada de cada dia e conclua a turma: só recebe registro quem\n   compareceu a todos os encontros; a validade conta do último dia.\n6. Treinamentos > Emitir certificado: PDF com número e código de verificação.\n7. Documentos da turma: atas por dia, ata consolidada e crachás (frente/verso, 54 x 86 mm, 4 por A4 paisagem).\n\nVencimentos nulos significam 'revisar conforme PGR'."
      );
   }

   private void sobre() {
      JDialog d = Ui.dialogo(this, "Sobre", 460, 380);
      JPanel corpo = new JPanel();
      corpo.setOpaque(false);
      corpo.setLayout(new BoxLayout(corpo, 1));
      corpo.setBorder(BorderFactory.createEmptyBorder(22, 24, 6, 24));
      JLabel imagem = new JLabel(this.logo(340));
      imagem.setAlignmentX(0.5F);
      corpo.add(imagem);
      corpo.add(Box.createVerticalStrut(22));
      corpo.add(this.centralizado("© " + LocalDate.now().getYear() + " - Todos os direitos reservados.", 12, 0, SUAVE));
      corpo.add(Box.createVerticalStrut(8));
      corpo.add(this.centralizado("Versão 1.1.0", 13, 1, AZUL));
      corpo.add(Box.createVerticalStrut(18));
      corpo.add(this.separador());
      corpo.add(Box.createVerticalStrut(16));
      corpo.add(this.centralizado("Maxwell Souza dos Santos", 13, 1, TEXTO));
      corpo.add(Box.createVerticalStrut(4));
      corpo.add(this.centralizado("Desenvolvedor", 12, 0, SUAVE));
      corpo.add(Box.createVerticalStrut(6));
      corpo.add(this.centralizado("CNPJ 54.718.287/0001-19", 11, 0, SUAVE));
      corpo.add(Box.createVerticalGlue());
      JButton ok = Ui.primario("OK");
      ok.setPreferredSize(new Dimension(120, 32));
      ok.setMaximumSize(new Dimension(120, 32));
      ok.setAlignmentX(0.5F);
      ok.addActionListener(e -> d.dispose());
      JPanel rodape = new JPanel();
      rodape.setOpaque(false);
      rodape.setLayout(new FlowLayout(1, 0, 12));
      rodape.add(ok);
      d.getRootPane().setDefaultButton(ok);
      d.add(corpo, "Center");
      d.add(rodape, "South");
      d.setVisible(true);
   }

   private JLabel centralizado(String texto, int tamanho, int estilo, Color cor) {
      JLabel l = new JLabel(texto);
      l.setFont(Ui.fonte(tamanho, estilo));
      l.setForeground(cor);
      l.setAlignmentX(0.5F);
      return l;
   }

   private JComponent separador() {
      JPanel linha = new JPanel();
      linha.setBackground(BORDA);
      linha.setPreferredSize(new Dimension(300, 1));
      linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
      linha.setAlignmentX(0.5F);
      return linha;
   }

   private Icon logo(int largura) {
      List<Image> variantes = new ArrayList<>();

      for (int escala = 1; escala <= 3; escala++) {
         BufferedImage versao = this.carregarLogo("/images/logo-" + escala + "x.png");
         if (versao == null) {
            variantes.clear();
            break;
         }

         variantes.add(versao);
      }

      if (variantes.size() != 3) {
         BufferedImage unica = this.carregarLogo("/images/logo-2x.png");
         return unica == null ? new ImageIcon() : new ImageIcon(unica.getScaledInstance(largura, -1, 16));
      } else {
         Image base = variantes.get(0);
         int altura = (int)Math.round((double)(largura * base.getHeight(null)) / (double)base.getWidth(null));
         return new MainFrame.IconeLogo(new MainFrame.LogoMulti(variantes), largura, altura);
      }
   }

   private BufferedImage carregarLogo(String caminho) {
      try {
         BufferedImage var3;
         try (InputStream entrada = MainFrame.class.getResourceAsStream(caminho)) {
            var3 = entrada == null ? null : ImageIO.read(entrada);
         }

         return var3;
      } catch (IOException var7) {
         return null;
      }
   }

   private void sair() {
      Sessao.encerrar();
      this.dispose();
      System.exit(0);
   }

   private void encerrarSessao() {
      Sessao.encerrar();
      this.dispose();
      new LoginFrame().setVisible(true);
   }

   private static class BotaoJanela extends JButton {
      private MainFrame.BotaoJanela.Tipo tipo;
      private final Color normal = new Color(255, 255, 255, 0);
      private final Color hover;

      BotaoJanela(MainFrame.BotaoJanela.Tipo tipo) {
         this.tipo = tipo;
         this.hover = tipo == MainFrame.BotaoJanela.Tipo.FECHAR ? new Color(192, 57, 43) : new Color(255, 255, 255, 30);
         this.setOpaque(false);
         this.setContentAreaFilled(false);
         this.setBorderPainted(false);
         this.setFocusPainted(false);
         this.setFocusable(false);
         this.setCursor(Cursor.getPredefinedCursor(12));
         this.setPreferredSize(new Dimension(30, 28));
         this.setMaximumSize(new Dimension(30, 28));
         this.setForeground(new Color(219, 234, 254));
         this.setBackground(this.normal);
         this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
               BotaoJanela.this.setBackground(BotaoJanela.this.hover);
               BotaoJanela.this.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
               BotaoJanela.this.setBackground(BotaoJanela.this.normal);
               BotaoJanela.this.repaint();
            }
         });
      }

      void setTipo(MainFrame.BotaoJanela.Tipo tipo) {
         this.tipo = tipo;
         this.repaint();
      }

      @Override
      protected void paintComponent(Graphics g) {
         Graphics2D g2 = (Graphics2D)g.create();
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         if (this.getBackground().getAlpha() > 0) {
            g2.setColor(this.getBackground());
            g2.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 8, 8);
         }

         g2.setColor(this.getForeground());
         g2.setStroke(new BasicStroke(1.4F, 1, 1));
         int cx = this.getWidth() / 2;
         int cy = this.getHeight() / 2;
         switch (this.tipo) {
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
         }

         g2.dispose();
      }

      static enum Tipo {
         MINIMIZAR,
         MAXIMIZAR,
         RESTAURAR,
         FECHAR;
      }
   }

   private static class Cartao extends JPanel {
      Cartao(LayoutManager layout) {
         super(layout);
         this.setOpaque(false);
      }

      @Override
      protected void paintComponent(Graphics g) {
         Graphics2D g2 = (Graphics2D)g.create();
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g2.setColor(Color.WHITE);
         g2.fillRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, 12, 12);
         g2.setColor(MainFrame.BORDA);
         g2.drawRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, 12, 12);
         g2.dispose();
         super.paintComponent(g);
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
         Graphics2D g2 = (Graphics2D)g.create();
         g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
         g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
         g2.drawImage(this.imagem, x, y, this.largura, this.altura, null);
         g2.dispose();
      }

      @Override
      public int getIconWidth() {
         return this.largura;
      }

      @Override
      public int getIconHeight() {
         return this.altura;
      }
   }

   private static final class LogoMulti extends AbstractMultiResolutionImage {
      private final List<Image> variantes;

      LogoMulti(List<Image> variantes) {
         this.variantes = List.copyOf(variantes);
      }

      @Override
      protected Image getBaseImage() {
         return this.variantes.get(0);
      }

      @Override
      public Image getResolutionVariant(double larguraDestino, double alturaDestino) {
         double fator = Math.max(larguraDestino / (double)this.getBaseImage().getWidth(null), alturaDestino / (double)this.getBaseImage().getHeight(null));
         Image escolhida = this.variantes.get(0);

         for (Image variante : this.variantes) {
            escolhida = variante;
            if ((double)variante.getWidth(null) / (double)this.getBaseImage().getWidth(null) >= fator) {
               break;
            }
         }

         return escolhida;
      }

      @Override
      public List<Image> getResolutionVariants() {
         return this.variantes;
      }
   }

   private static class Ponto extends JComponent {
      private final Color cor;

      Ponto(Color cor) {
         this.cor = cor;
         this.setPreferredSize(new Dimension(7, 7));
         this.setMaximumSize(new Dimension(7, 7));
         this.setMinimumSize(new Dimension(7, 7));
      }

      @Override
      protected void paintComponent(Graphics g) {
         Graphics2D g2 = (Graphics2D)g.create();
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g2.setColor(this.cor);
         g2.fillOval(0, 0, 7, 7);
         g2.dispose();
      }
   }

   private static class RenderizadorStatus extends DefaultTableCellRenderer {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object valor, boolean selecionado, boolean foco, int linha, int coluna) {
         JLabel l = (JLabel)super.getTableCellRendererComponent(table, valor, selecionado, foco, linha, coluna);
         String texto = String.valueOf(valor);
         Color fundo;
         Color frente;
         if (texto.equalsIgnoreCase("Vencido")) {
            fundo = new Color(254, 226, 226);
            frente = new Color(185, 28, 28);
         } else if (texto.equalsIgnoreCase("Vence em 5 d") || texto.equalsIgnoreCase("Vence em 2 d")) {
            fundo = new Color(254, 243, 199);
            frente = new Color(146, 64, 14);
         } else if (texto.endsWith(" d")) {
            fundo = new Color(224, 242, 254);
            frente = new Color(7, 89, 133);
         } else {
            fundo = new Color(220, 252, 231);
            frente = new Color(22, 101, 52);
         }

         l.setOpaque(true);
         l.setBackground(selecionado ? table.getSelectionBackground() : fundo);
         l.setForeground(selecionado ? table.getSelectionForeground() : frente);
         l.setFont(new Font("Segoe UI", 1, 11));
         l.setHorizontalAlignment(0);
         l.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
         return l;
      }
   }
}
