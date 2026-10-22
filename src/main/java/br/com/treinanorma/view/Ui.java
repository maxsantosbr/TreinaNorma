package br.com.treinanorma.view;

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
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public final class Ui {

    public static final Color AZUL = new Color(0x1F, 0x4E, 0x79);
    public static final Color AZUL_CLARO = new Color(0x2E, 0x75, 0xB6);
    public static final Color DESTAQUE = new Color(0x0A, 0xA7, 0xE8);
    public static final Color FUNDO = new Color(0xF6, 0xF8, 0xFB);
    public static final Color BORDA = new Color(0xDB, 0xE3, 0xEE);
    public static final Color TEXTO = new Color(0x1F, 0x29, 0x37);
    public static final Color SUAVE = new Color(0x6B, 0x72, 0x80);
    public static final Color BEGE = new Color(0xEE, 0xF4, 0xFA);
    public static final Color VERDE = new Color(0x15, 0x80, 0x3D);
    public static final Color VERMELHO = new Color(0xB9, 0x1C, 0x1C);
    public static final Color AMBAR = new Color(0xB4, 0x53, 0x09);

    private Ui() {
    }

    public static Font fonte(int tamanho, int estilo) {
        return new Font("Segoe UI", estilo, tamanho);
    }

    public static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(fonte(18, Font.BOLD));
        l.setForeground(TEXTO);
        return l;
    }

    public static JLabel subtitulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(fonte(12, Font.PLAIN));
        l.setForeground(SUAVE);
        return l;
    }

    public static JLabel rotulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(fonte(11, Font.BOLD));
        l.setForeground(SUAVE);
        return l;
    }

    public static JLabel valor(String texto) {
        JLabel l = new JLabel(texto == null ? "" : texto);
        l.setFont(fonte(12, Font.PLAIN));
        l.setForeground(TEXTO);
        return l;
    }

    /** Texto de ajuda permanente exibido abaixo de um campo (11px, cinza). */
    public static JLabel ajuda(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(SUAVE);
        return l;
    }

    /** Ícone "?" em círculo: tooltip no mouse e detalhes ao clicar (acessível por teclado). */
    public static JComponent dica(String texto) {
        return new Dica(texto, texto);
    }

    /** Ícone "?" com resumo no tooltip e texto completo no clique. */
    public static JComponent dica(String resumo, String detalhe) {
        return new Dica(resumo, detalhe);
    }

    public static JTextField campo() {
        return campo(null);
    }

    public static JTextField campo(String placeholder) {
        JTextField f = new JTextField();
        f.setFont(fonte(12, Font.PLAIN));
        if (placeholder != null) {
            f.putClientProperty("JTextField.placeholderText", placeholder);
        }
        f.putClientProperty("JTextField.showClearButton", true);
        return f;
    }

    public static JPasswordField senha() {
        return senha(null);
    }

    public static JPasswordField senha(String placeholder) {
        JPasswordField f = new JPasswordField();
        f.setFont(fonte(12, Font.PLAIN));
        if (placeholder != null) {
            f.putClientProperty("JTextField.placeholderText", placeholder);
        }
        return f;
    }

    public static JComboBox<String> combo(String... itens) {
        JComboBox<String> c = new JComboBox<>(itens);
        c.setFont(fonte(12, Font.PLAIN));
        return c;
    }

    public static JButton primario(String texto) {
        return botao(texto, AZUL, Color.WHITE, true);
    }

    public static JButton secundario(String texto) {
        return botao(texto, Color.WHITE, AZUL, false);
    }

    public static JButton perigo(String texto) {
        return botao(texto, Color.WHITE, VERMELHO, false);
    }

    private static JButton botao(String texto, Color fundo, Color frente, boolean solido) {
        JButton b = new JButton(texto);
        b.setFont(fonte(12, Font.BOLD));
        b.setFocusPainted(false);
        b.setBackground(fundo);
        b.setForeground(frente);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
                solido ? BorderFactory.createEmptyBorder() : BorderFactory.createLineBorder(BORDA),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        return b;
    }

    public static JButton link(String texto) {
        JButton b = new JButton(texto);
        b.setFont(fonte(11, Font.PLAIN));
        b.setForeground(AZUL_CLARO);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JPanel linha(Component... componentes) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        for (int i = 0; i < componentes.length; i++) {
            if (i > 0) {
                p.add(Box.createHorizontalStrut(8));
            }
            p.add(componentes[i]);
        }
        return p;
    }

    public static JPanel linhaEsquerda(Component... componentes) {
        JPanel p = linha(componentes);
        p.add(Box.createHorizontalGlue());
        return p;
    }

    public static JPanel cartao(java.awt.LayoutManager layout) {
        Cartao c = new Cartao(layout);
        c.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        return c;
    }

    public static JTable tabela(String... colunas) {
        DefaultTableModel modelo = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable t = new JTable(modelo);
        t.setFont(fonte(12, Font.PLAIN));
        t.setForeground(new Color(0x33, 0x41, 0x55));
        t.setRowHeight(32);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(new Color(0xF1, 0xF5, 0xF9));
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setSelectionBackground(new Color(0xE8, 0xF2, 0xFB));
        t.setSelectionForeground(AZUL);
        t.setFillsViewportHeight(true);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setBorder(BorderFactory.createEmptyBorder());
        JTableHeader header = t.getTableHeader();
        header.setFont(fonte(11, Font.BOLD));
        header.setBackground(BEGE);
        header.setForeground(AZUL);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 30));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDA));
        DefaultTableCellRenderer renderizador = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object valor, boolean sel,
                    boolean foco, int linha, int coluna) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, valor, sel, foco, linha, coluna);
                l.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                l.setToolTipText(valor == null ? null : valor.toString());
                return l;
            }
        };
        t.setDefaultRenderer(Object.class, renderizador);
        return t;
    }

    public static JScrollPane rolagem(JTable tabela) {
        JScrollPane rolagem = new JScrollPane(tabela);
        rolagem.setBorder(BorderFactory.createEmptyBorder());
        rolagem.getViewport().setBackground(Color.WHITE);
        return rolagem;
    }

    public static void info(Component pai, String mensagem) {
        JOptionPane.showMessageDialog(pai, mensagem, "TreinaNorma", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void aviso(Component pai, String mensagem) {
        JOptionPane.showMessageDialog(pai, mensagem, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    public static void erro(Component pai, String mensagem) {
        JOptionPane.showMessageDialog(pai, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    public static void erro(Component pai, Throwable e) {
        erro(pai, e.getMessage() == null ? e.toString() : e.getMessage());
    }

    /** Pergunta se deve abrir a pasta; com arquivo, abre com o item selecionado. */
    public static void perguntaAbrirLocal(Component pai, String mensagem, File alvo) {
        Object[] opcoes = {"Abrir local", "Não"};
        int escolha = JOptionPane.showOptionDialog(pai, mensagem, "TreinaNorma",
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                opcoes, opcoes[1]);
        if (escolha == 0) {
            abrirLocal(pai, alvo);
        }
    }

    public static void abrirLocal(Component pai, File alvo) {
        try {
            if (alvo != null && alvo.isFile()) {
                new ProcessBuilder("explorer.exe", "/select,\"" + alvo.getAbsolutePath() + "\"").start();
                return;
            }
            File pasta = alvo == null ? null : (alvo.isDirectory() ? alvo : alvo.getParentFile());
            java.awt.Desktop.getDesktop().open(pasta);
        } catch (Exception e) {
            info(pai, "Arquivo em:\n" + (alvo == null ? "" : alvo.getAbsolutePath()));
        }
    }

    public static boolean confirma(Component pai, String mensagem) {
        return JOptionPane.showConfirmDialog(pai, mensagem, "Confirmação",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public static JDialog dialogo(Component pai, String titulo, int largura, int altura) {
        Window janela = pai == null ? null : javax.swing.SwingUtilities.getWindowAncestor(pai);
        JDialog d = new JDialog(janela, titulo, JDialog.ModalityType.APPLICATION_MODAL);
        d.setSize(largura, altura);
        d.setLocationRelativeTo(pai);
        d.setLayout(new BorderLayout());
        return d;
    }

    public static JPanel formulario() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        return p;
    }

    public static void campo(JPanel form, GridBagConstraints gbc, int x, int y,
            String rotulo, JComponent componente, int largura) {
        campo(form, gbc, x, y, rotulo, componente, largura, null);
    }

    public static void campo(JPanel form, GridBagConstraints gbc, int x, int y,
            String rotulo, JComponent componente, int largura, JComponent ajuda) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = largura;
        gbc.weightx = largura;
        JPanel bloco = new JPanel(new BorderLayout(0, 3));
        bloco.setOpaque(false);
        bloco.add(ajuda == null ? rotulo(rotulo) : linhaEsquerda(rotulo(rotulo), ajuda),
                BorderLayout.NORTH);
        bloco.add(componente, BorderLayout.CENTER);
        form.add(bloco, gbc);
    }

    public static GridBagConstraints gbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        return gbc;
    }

    public static void preencher(DefaultTableModel modelo, Object[]... linhas) {
        modelo.setRowCount(0);
        for (Object[] linha : linhas) {
            modelo.addRow(linha);
        }
    }

    public static String texto(JTextField campo) {
        return campo.getText() == null ? "" : campo.getText().trim();
    }

    public static class Selo extends JLabel {

        public Selo(String texto, Color fundo, Color frente) {
            super(texto);
            setFont(fonte(10, Font.BOLD));
            setForeground(frente);
            setOpaque(true);
            setBackground(fundo);
            setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
            setHorizontalAlignment(SwingConstants.CENTER);
        }
    }

    public static class Cartao extends JPanel {

        public Cartao(java.awt.LayoutManager layout) {
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
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class Dica extends JButton {

        public Dica(String resumo, String detalhe) {
            super("?");
            setFont(fonte(10, Font.BOLD));
            setForeground(SUAVE);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);
            setMargin(new Insets(0, 0, 0, 0));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            Dimension tamanho = new Dimension(18, 18);
            setPreferredSize(tamanho);
            setMinimumSize(tamanho);
            setMaximumSize(tamanho);
            setToolTipText(resumo);
            getAccessibleContext().setAccessibleName("Ajuda");
            getAccessibleContext().setAccessibleDescription(detalhe);
            addActionListener(e -> JOptionPane.showMessageDialog(this, detalhe, "Ajuda",
                    JOptionPane.INFORMATION_MESSAGE));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0x9A, 0xA3, 0xB2));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawOval(1, 1, getWidth() - 3, getHeight() - 3);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
