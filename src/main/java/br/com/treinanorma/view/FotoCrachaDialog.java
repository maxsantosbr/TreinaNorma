package br.com.treinanorma.view;

import br.com.treinanorma.dao.RegistroDAO;
import br.com.treinanorma.model.Colaborador;
import br.com.treinanorma.model.Empresa;
import br.com.treinanorma.model.RegistroTreinamento;
import br.com.treinanorma.util.Fotos;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.TransferHandler;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Tela de seleção, recorte e prévia da foto no crachá. */
public final class FotoCrachaDialog {

    private final Component pai;
    private final Colaborador colaborador;
    private final Empresa empresa;
    private final String fotoInicial;
    private final List<RegistroTreinamento> registros;
    private final JDialog dialogo;
    private final FotoPreview preview = new FotoPreview();
    private final CrachaPreview cracha;
    private final JLabel arquivoNome = new JLabel();
    private final JLabel arquivoInfo = new JLabel();
    private final JLabel status = new JLabel();
    private final JLabel zoomValor = new JLabel("100%");
    private final JSlider zoom = new JSlider(100, 300, 100);
    private final JButton remover;

    private BufferedImage imagemAtual;
    private File origemAtual;
    private boolean carregando;
    private boolean ajustandoZoom;
    private boolean dirty;
    private boolean alterou;
    private String resultado;

    public FotoCrachaDialog(Component pai, Colaborador colaborador, Empresa empresa, String fotoInicial) {
        this.pai = pai;
        this.colaborador = colaborador == null ? new Colaborador() : colaborador;
        this.empresa = empresa;
        this.fotoInicial = fotoInicial == null ? "" : fotoInicial;
        this.registros = carregarRegistros();
        this.cracha = new CrachaPreview(this.colaborador, this.empresa, this.registros);
        this.dialogo = Ui.dialogo(pai, "Foto e prévia do crachá", 1160, 720);
        this.remover = Ui.perigo("Remover foto");
        montar();
        carregarInicial();
        preview.setAoMudar(() -> {
            if (carregando) {
                return;
            }
            dirty = true;
            sincronizarZoom();
            atualizarCracha();
        });
    }

    public String abrir() {
        dialogo.setVisible(true);
        return resultado;
    }

    public boolean isAlterou() {
        return alterou;
    }

    private void montar() {
        JPanel raiz = new JPanel(new BorderLayout(0, 0));
        raiz.setBackground(Ui.FUNDO);
        raiz.setBorder(BorderFactory.createEmptyBorder(14, 18, 12, 18));

        raiz.add(cabecalho(), BorderLayout.NORTH);
        raiz.add(corpo(), BorderLayout.CENTER);
        raiz.add(rodape(), BorderLayout.SOUTH);
        dialogo.add(raiz, BorderLayout.CENTER);
    }

    private JPanel cabecalho() {
        JPanel cab = new JPanel(new BorderLayout(0, 0));
        cab.setOpaque(false);
        cab.setBorder(BorderFactory.createEmptyBorder(0, 2, 12, 2));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));

        JLabel sobretitulo = new JLabel("CADASTRO DE COLABORADOR");
        sobretitulo.setFont(Ui.fonte(10, Font.BOLD));
        sobretitulo.setForeground(Ui.AZUL_CLARO);
        sobretitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titulo = Ui.titulo("Foto e prévia do crachá");
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = Ui.subtitulo(descricaoColaborador());
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        textos.add(sobretitulo);
        textos.add(Box.createVerticalStrut(3));
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(4));
        textos.add(sub);
        cab.add(textos, BorderLayout.WEST);
        return cab;
    }

    private JPanel corpo() {
        JPanel corpo = new JPanel(new java.awt.GridBagLayout());
        corpo.setOpaque(false);
        corpo.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.insets = new java.awt.Insets(0, 0, 0, 10);
        corpo.add(cardFoto(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.insets = new java.awt.Insets(0, 0, 0, 0);
        corpo.add(cardPrevia(), gbc);
        return corpo;
    }

    private JPanel cardFoto() {
        JPanel card = Ui.cartao(new BorderLayout(0, 10));
        card.setPreferredSize(new Dimension(410, 560));

        JPanel topo = new JPanel(new BorderLayout(8, 0));
        topo.setOpaque(false);
        topo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Ui.BORDA),
                BorderFactory.createEmptyBorder(0, 0, 9, 0)));

        JPanel titulo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titulo.setOpaque(false);
        JLabel passo = new JLabel("1", JLabel.CENTER);
        passo.setFont(Ui.fonte(11, Font.BOLD));
        passo.setForeground(Color.WHITE);
        passo.setBackground(Ui.AZUL);
        passo.setOpaque(true);
        passo.setPreferredSize(new Dimension(22, 22));
        JLabel nome = new JLabel("Foto do colaborador");
        nome.setFont(Ui.fonte(13, Font.BOLD));
        nome.setForeground(Ui.TEXTO);
        titulo.add(passo);
        titulo.add(nome);

        status.setFont(Ui.fonte(9, Font.BOLD));
        status.setOpaque(true);
        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        topo.add(titulo, BorderLayout.WEST);
        topo.add(status, BorderLayout.EAST);
        card.add(topo, BorderLayout.NORTH);

        JPanel conteudo = new JPanel(new BorderLayout(0, 10));
        conteudo.setOpaque(false);

        JPanel trabalho = new JPanel(new BorderLayout(14, 0));
        trabalho.setOpaque(false);
        trabalho.add(preview, BorderLayout.WEST);
        trabalho.add(controles(), BorderLayout.CENTER);

        JPanel sul = new JPanel(new BorderLayout(0, 7));
        sul.setOpaque(false);
        sul.add(dropzone(), BorderLayout.NORTH);
        JLabel dica = Ui.ajuda("<html>A imagem é recortada no formato 3 × 4, redimensionada e copiada "
                + "para a pasta de fotos do TreinaNorma.</html>");
        sul.add(dica, BorderLayout.SOUTH);

        conteudo.add(trabalho, BorderLayout.CENTER);
        conteudo.add(sul, BorderLayout.SOUTH);
        card.add(conteudo, BorderLayout.CENTER);
        return card;
    }

    private JPanel controles() {
        JPanel controles = new JPanel();
        controles.setOpaque(false);
        controles.setLayout(new BoxLayout(controles, BoxLayout.Y_AXIS));

        JPanel arquivo = new JPanel(new BorderLayout(0, 2));
        arquivo.setOpaque(true);
        arquivo.setBackground(new Color(0xF8, 0xFA, 0xFD));
        arquivo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Ui.BORDA),
                BorderFactory.createEmptyBorder(8, 9, 8, 9)));
        arquivoNome.setFont(Ui.fonte(10, Font.BOLD));
        arquivoNome.setForeground(new Color(0x2B, 0x3C, 0x52));
        arquivoInfo.setFont(Ui.fonte(9, Font.PLAIN));
        arquivoInfo.setForeground(Ui.SUAVE);
        arquivo.add(arquivoNome, BorderLayout.NORTH);
        arquivo.add(arquivoInfo, BorderLayout.CENTER);
        arquivo.setAlignmentX(Component.LEFT_ALIGNMENT);
        arquivo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        controles.add(arquivo);
        controles.add(Box.createVerticalStrut(8));

        JButton selecionar = Ui.primario("Selecionar foto");
        selecionar.setAlignmentX(Component.LEFT_ALIGNMENT);
        selecionar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        selecionar.addActionListener(e -> escolherFoto());
        controles.add(selecionar);
        controles.add(Box.createVerticalStrut(6));

        remover.setAlignmentX(Component.LEFT_ALIGNMENT);
        remover.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        remover.addActionListener(e -> removerFoto());
        controles.add(remover);
        controles.add(Box.createVerticalStrut(10));

        JSeparator separador = new JSeparator();
        separador.setForeground(Ui.BORDA);
        separador.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        controles.add(separador);
        controles.add(Box.createVerticalStrut(9));

        JPanel rotuloZoom = new JPanel(new BorderLayout());
        rotuloZoom.setOpaque(false);
        JLabel rotulo = new JLabel("Enquadramento");
        rotulo.setFont(Ui.fonte(10, Font.BOLD));
        rotulo.setForeground(new Color(0x52, 0x61, 0x76));
        zoomValor.setFont(Ui.fonte(10, Font.PLAIN));
        zoomValor.setForeground(Ui.SUAVE);
        rotuloZoom.add(rotulo, BorderLayout.WEST);
        rotuloZoom.add(zoomValor, BorderLayout.EAST);
        rotuloZoom.setAlignmentX(Component.LEFT_ALIGNMENT);
        rotuloZoom.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        controles.add(rotuloZoom);

        zoom.setOpaque(false);
        zoom.setAlignmentX(Component.LEFT_ALIGNMENT);
        zoom.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        zoom.addChangeListener(e -> {
            if (ajustandoZoom) {
                return;
            }
            zoomValor.setText(zoom.getValue() + "%");
            preview.setZoom(zoom.getValue() / 100.0);
        });
        controles.add(zoom);
        controles.add(Box.createVerticalStrut(4));

        JPanel mini = new JPanel(new GridLayout(1, 2, 6, 0));
        mini.setOpaque(false);
        mini.setAlignmentX(Component.LEFT_ALIGNMENT);
        mini.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        JButton centralizar = Ui.secundario("Centralizar");
        JButton restaurar = Ui.secundario("Restaurar");
        centralizar.setFont(Ui.fonte(10, Font.PLAIN));
        restaurar.setFont(Ui.fonte(10, Font.PLAIN));
        centralizar.addActionListener(e -> preview.centralizar());
        restaurar.addActionListener(e -> {
            ajustandoZoom = true;
            zoom.setValue(100);
            zoomValor.setText("100%");
            ajustandoZoom = false;
            preview.restaurar();
        });
        mini.add(centralizar);
        mini.add(restaurar);
        controles.add(mini);
        controles.add(Box.createVerticalGlue());
        return controles;
    }

    private JPanel dropzone() {
        JPanel drop = new JPanel(new BorderLayout(9, 0));
        drop.setOpaque(true);
        drop.setBackground(new Color(0xFA, 0xFC, 0xFE));
        drop.setBorder(BorderFactory.createDashedBorder(new Color(0xC4, 0xD3, 0xE3),
                1.2f, 4f, 3f, true));
        drop.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        drop.setPreferredSize(new Dimension(0, 52));

        JLabel icone = new JLabel("↑", JLabel.CENTER);
        icone.setFont(Ui.fonte(18, Font.BOLD));
        icone.setForeground(Ui.AZUL_CLARO);
        icone.setPreferredSize(new Dimension(28, 28));
        icone.setOpaque(true);
        icone.setBackground(new Color(0xEA, 0xF4, 0xFC));
        drop.add(icone, BorderLayout.WEST);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        JLabel titulo = new JLabel("Arraste uma foto aqui ou clique para procurar");
        titulo.setFont(Ui.fonte(10, Font.BOLD));
        titulo.setForeground(new Color(0x34, 0x46, 0x5D));
        JLabel sub = new JLabel("JPG, PNG ou BMP · até 10 MB · recomendado 3 × 4");
        sub.setFont(Ui.fonte(9, Font.PLAIN));
        sub.setForeground(Ui.SUAVE);
        textos.add(titulo);
        textos.add(sub);
        drop.add(textos, BorderLayout.CENTER);

        drop.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                escolherFoto();
            }
        });
        instalarArrasto(drop);
        instalarArrasto(preview);
        return drop;
    }

    private JPanel cardPrevia() {
        JPanel card = Ui.cartao(new BorderLayout(0, 8));
        card.setPreferredSize(new Dimension(680, 560));

        JPanel topo = new JPanel(new BorderLayout(8, 0));
        topo.setOpaque(false);
        topo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Ui.BORDA),
                BorderFactory.createEmptyBorder(0, 0, 9, 0)));

        JPanel titulo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titulo.setOpaque(false);
        JLabel passo = new JLabel("2", JLabel.CENTER);
        passo.setFont(Ui.fonte(11, Font.BOLD));
        passo.setForeground(Color.WHITE);
        passo.setBackground(Ui.AZUL);
        passo.setOpaque(true);
        passo.setPreferredSize(new Dimension(22, 22));
        JLabel nome = new JLabel("Prévia no crachá");
        nome.setFont(Ui.fonte(13, Font.BOLD));
        nome.setForeground(Ui.TEXTO);
        titulo.add(passo);
        titulo.add(nome);

        JLabel atualiza = new JLabel("Atualiza automaticamente");
        atualiza.setFont(Ui.fonte(9, Font.BOLD));
        atualiza.setForeground(new Color(0x08, 0x6D, 0x9B));
        atualiza.setOpaque(true);
        atualiza.setBackground(new Color(0xEA, 0xF6, 0xFD));
        atualiza.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        topo.add(titulo, BorderLayout.WEST);
        topo.add(atualiza, BorderLayout.EAST);
        card.add(topo, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 0));
        centro.setOpaque(false);
        centro.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        centro.add(cracha, BorderLayout.CENTER);

        JPanel sul = new JPanel(new BorderLayout(8, 0));
        sul.setOpaque(false);
        JLabel medidas = Ui.ajuda("Peça aberta 108 × 86 mm · dobrada 54 × 86 mm");
        JLabel legenda = Ui.ajuda("A prévia usa o mesmo layout do PDF.");
        sul.add(medidas, BorderLayout.WEST);
        sul.add(legenda, BorderLayout.EAST);
        centro.add(sul, BorderLayout.SOUTH);

        card.add(centro, BorderLayout.CENTER);
        return card;
    }

    private JPanel rodape() {
        JPanel rodape = new JPanel(new BorderLayout(12, 0));
        rodape.setOpaque(false);
        rodape.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Ui.BORDA),
                BorderFactory.createEmptyBorder(12, 0, 0, 0)));

        JLabel nota = Ui.ajuda("A foto será salva no cadastro e reutilizada em todos os crachás deste colaborador.");
        rodape.add(nota, BorderLayout.WEST);

        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acoes.setOpaque(false);
        JButton cancelar = Ui.secundario("Cancelar");
        JButton salvar = Ui.primario("Salvar foto");
        cancelar.addActionListener(e -> {
            alterou = false;
            resultado = fotoInicial;
            dialogo.dispose();
        });
        salvar.addActionListener(e -> salvarFoto());
        acoes.add(cancelar);
        acoes.add(salvar);
        rodape.add(acoes, BorderLayout.EAST);
        return rodape;
    }

    private void carregarInicial() {
        carregando = true;
        atualizarStatus(false);
        atualizarInfo(null, null);
        if (!fotoInicial.isBlank()) {
            BufferedImage imagem = Fotos.carregar(fotoInicial);
            if (imagem != null) {
                imagemAtual = imagem;
                origemAtual = Fotos.arquivo(fotoInicial);
                preview.setImagem(imagem);
                atualizarInfo(origemAtual, imagem);
                atualizarStatus(true);
                atualizarCracha();
                resultado = fotoInicial;
            }
        }
        dirty = false;
        carregando = false;
    }

    private void escolherFoto() {
        JFileChooser escolha = new JFileChooser();
        escolha.setDialogTitle("Selecionar foto do colaborador");
        escolha.setAcceptAllFileFilterUsed(false);
        escolha.setFileFilter(new FileNameExtensionFilter(
                "Imagens (JPG, PNG, BMP)", "jpg", "jpeg", "png", "bmp"));
        if (escolha.showOpenDialog(dialogo) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        aceitarArquivo(escolha.getSelectedFile());
    }

    private void aceitarArquivo(File arquivo) {
        if (arquivo == null || !arquivo.isFile()) {
            Ui.aviso(dialogo, "Selecione um arquivo de imagem válido.");
            return;
        }
        try {
            BufferedImage imagem = Fotos.ler(arquivo);
            imagemAtual = imagem;
            origemAtual = arquivo;
            dirty = true;
            preview.setImagem(imagem);
            atualizarInfo(arquivo, imagem);
            atualizarStatus(true);
            ajustandoZoom = true;
            zoom.setValue(100);
            zoomValor.setText("100%");
            ajustandoZoom = false;
            atualizarCracha();
        } catch (IOException ex) {
            Ui.erro(dialogo, ex.getMessage());
        }
    }

    private void removerFoto() {
        if (imagemAtual == null) {
            return;
        }
        if (!Ui.confirma(dialogo, "Remover a foto deste colaborador?")) {
            return;
        }
        imagemAtual = null;
        origemAtual = null;
        dirty = true;
        preview.setImagem(null);
        atualizarInfo(null, null);
        atualizarStatus(false);
        ajustandoZoom = true;
        zoom.setValue(100);
        zoomValor.setText("100%");
        ajustandoZoom = false;
        atualizarCracha();
    }

    private void salvarFoto() {
        try {
            if (imagemAtual == null) {
                resultado = null;
                alterou = !fotoInicial.isBlank();
            } else if (!dirty) {
                resultado = fotoInicial;
                alterou = false;
            } else {
                resultado = Fotos.salvar(preview.recortar(600, 800));
                alterou = true;
            }
            dialogo.dispose();
        } catch (IOException ex) {
            Ui.erro(dialogo, "Não foi possível salvar a foto: " + ex.getMessage());
        }
    }

    private void atualizarCracha() {
        cracha.setFoto(imagemAtual == null ? null : preview.recortar(300, 400));
    }

    private void sincronizarZoom() {
        int valor = (int) Math.round(preview.getZoom() * 100);
        valor = Math.max(100, Math.min(300, valor));
        if (zoom.getValue() != valor) {
            ajustandoZoom = true;
            zoom.setValue(valor);
            zoomValor.setText(valor + "%");
            ajustandoZoom = false;
        }
    }

    private void atualizarStatus(boolean temFoto) {
        status.setText(temFoto ? "Com foto" : "Sem foto");
        status.setBackground(temFoto ? new Color(0xE8, 0xF7, 0xEE) : new Color(0xF1, 0xF5, 0xF9));
        status.setForeground(temFoto ? Ui.VERDE : Ui.SUAVE);
        remover.setEnabled(temFoto);
    }

    private void atualizarInfo(File arquivo, BufferedImage imagem) {
        if (arquivo == null || imagem == null) {
            arquivoNome.setText("Nenhuma foto selecionada");
            arquivoNome.setToolTipText(null);
            arquivoInfo.setText("JPG, PNG ou BMP · até 10 MB");
            return;
        }
        String nome = arquivo.getName();
        String ext = nome.contains(".") ? nome.substring(nome.lastIndexOf('.') + 1).toUpperCase() : "IMG";
        String tamanho = arquivo.length() < 1024 * 1024
                ? String.format("%.0f KB", arquivo.length() / 1024.0)
                : String.format("%.1f MB", arquivo.length() / (1024.0 * 1024.0));
        arquivoNome.setText(nome);
        arquivoNome.setToolTipText(arquivo.getAbsolutePath());
        arquivoInfo.setText(String.format("%s · %s · %d × %d px",
                ext, tamanho, imagem.getWidth(), imagem.getHeight()));
    }

    private List<RegistroTreinamento> carregarRegistros() {
        if (colaborador == null || colaborador.getId() <= 0) {
            return Collections.emptyList();
        }
        try {
            return new RegistroDAO().porColaborador(colaborador.getId());
        } catch (RuntimeException e) {
            return Collections.emptyList();
        }
    }

    private String descricaoColaborador() {
        String nome = valor(colaborador.getNome(), "Novo colaborador");
        String cargo = valor(colaborador.getCargo(), "função não informada");
        String cpf = valor(colaborador.getCpf(), "CPF não informado");
        return nome + " · " + cargo + " · CPF " + cpf;
    }

    private void instalarArrasto(javax.swing.JComponent alvo) {
        alvo.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                try {
                    @SuppressWarnings("unchecked")
                    List<File> arquivos = (List<File>) support.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);
                    if (arquivos != null && !arquivos.isEmpty()) {
                        aceitarArquivo(arquivos.get(0));
                        return true;
                    }
                } catch (Exception ex) {
                    Ui.erro(dialogo, "Não foi possível ler o arquivo arrastado.");
                }
                return false;
            }
        });
    }

    private static String valor(String texto, String padrao) {
        return texto == null || texto.isBlank() ? padrao : texto;
    }

    private static String dataCurta(LocalDate data) {
        return data == null ? "" : data.format(DateTimeFormatter.ofPattern("dd/MM/yy"));
    }

    private static Font fonte(float tamanho, int estilo) {
        return Ui.fonte(1, estilo).deriveFont(tamanho);
    }

    /** Prévia 3x4 com recorte, zoom e arraste. */
    private static final class FotoPreview extends JPanel {

        private BufferedImage imagem;
        private double zoom = 1.0;
        private double offsetX;
        private double offsetY;
        private Point arraste;
        private double inicioX;
        private double inicioY;
        private Runnable aoMudar;

        FotoPreview() {
            setOpaque(true);
            setBackground(new Color(0xE9, 0xF0, 0xF7));
            setPreferredSize(new Dimension(196, 261));
            setMinimumSize(new Dimension(196, 261));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Arraste para ajustar o enquadramento");

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    arraste = e.getPoint();
                    inicioX = offsetX;
                    inicioY = offsetY;
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    arraste = null;
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (arraste == null || imagem == null) {
                        return;
                    }
                    offsetX = inicioX + (e.getX() - arraste.x);
                    offsetY = inicioY + (e.getY() - arraste.y);
                    limitarOffsets();
                    repaint();
                    notificar();
                }
            });
            addMouseWheelListener((MouseWheelEvent e) -> {
                if (imagem == null) {
                    return;
                }
                double fator = e.getWheelRotation() < 0 ? 1.1 : 0.9;
                setZoom(zoom * fator);
            });
            addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override
                public void componentResized(java.awt.event.ComponentEvent e) {
                    limitarOffsets();
                    repaint();
                }
            });
        }

        void setAoMudar(Runnable aoMudar) {
            this.aoMudar = aoMudar;
        }

        void setImagem(BufferedImage imagem) {
            this.imagem = imagem;
            this.zoom = 1.0;
            this.offsetX = 0;
            this.offsetY = 0;
            repaint();
            notificar();
        }

        double getZoom() {
            return zoom;
        }

        void setZoom(double zoom) {
            this.zoom = Math.max(1.0, Math.min(3.0, zoom));
            limitarOffsets();
            repaint();
            notificar();
        }

        void centralizar() {
            offsetX = 0;
            offsetY = 0;
            repaint();
            notificar();
        }

        void restaurar() {
            zoom = 1.0;
            offsetX = 0;
            offsetY = 0;
            repaint();
            notificar();
        }

        private void notificar() {
            if (aoMudar != null) {
                aoMudar.run();
            }
        }

        private int larguraView() {
            return getWidth() > 0 ? getWidth() : 196;
        }

        private int alturaView() {
            return getHeight() > 0 ? getHeight() : 261;
        }

        private double escala() {
            if (imagem == null) {
                return 1;
            }
            double base = Math.max(larguraView() / (double) imagem.getWidth(),
                    alturaView() / (double) imagem.getHeight());
            return base * zoom;
        }

        private void limitarOffsets() {
            if (imagem == null) {
                offsetX = 0;
                offsetY = 0;
                return;
            }
            double largura = imagem.getWidth() * escala();
            double altura = imagem.getHeight() * escala();
            double maxX = Math.max(0, (largura - larguraView()) / 2.0);
            double maxY = Math.max(0, (altura - alturaView()) / 2.0);
            offsetX = Math.max(-maxX, Math.min(maxX, offsetX));
            offsetY = Math.max(-maxY, Math.min(maxY, offsetY));
        }

        BufferedImage recortar(int largura, int altura) {
            if (imagem == null) {
                return null;
            }
            int vw = larguraView();
            int vh = alturaView();
            double escala = escala();
            int dw = (int) Math.round(imagem.getWidth() * escala);
            int dh = (int) Math.round(imagem.getHeight() * escala);
            int x = (int) Math.round(vw / 2.0 - dw / 2.0 + offsetX);
            int y = (int) Math.round(vh / 2.0 - dh / 2.0 + offsetY);

            BufferedImage destino = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = destino.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, largura, altura);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.scale(largura / (double) vw, altura / (double) vh);
            g.drawImage(imagem, x, y, dw, dh, null);
            g.dispose();
            return destino;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int vw = getWidth();
            int vh = getHeight();
            g2.setColor(getBackground());
            g2.fillRect(0, 0, vw, vh);

            if (imagem == null) {
                g2.setColor(new Color(0xC9, 0xD6, 0xE5));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1, 1, vw - 3, vh - 3, 10, 10);
                int cx = vw / 2;
                int cy = vh / 2 - 16;
                g2.setColor(new Color(0xB4, 0xC4, 0xD6));
                g2.fillOval(cx - 17, cy - 25, 34, 34);
                g2.fillRoundRect(cx - 30, cy + 10, 60, 34, 16, 16);
                g2.setColor(Ui.SUAVE);
                g2.setFont(Ui.fonte(10, Font.PLAIN));
                String texto = "Foto 3x4";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(texto, cx - fm.stringWidth(texto) / 2, vh - 30);
            } else {
                double escala = escala();
                int dw = (int) Math.round(imagem.getWidth() * escala);
                int dh = (int) Math.round(imagem.getHeight() * escala);
                int x = (int) Math.round(vw / 2.0 - dw / 2.0 + offsetX);
                int y = (int) Math.round(vh / 2.0 - dh / 2.0 + offsetY);
                Shape clip = g2.getClip();
                g2.clipRect(0, 0, vw, vh);
                g2.drawImage(imagem, x, y, dw, dh, null);
                g2.setClip(clip);
            }

            g2.setColor(new Color(255, 255, 255, 165));
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(vw / 3, 0, vw / 3, vh);
            g2.drawLine(vw * 2 / 3, 0, vw * 2 / 3, vh);
            g2.drawLine(0, vh / 3, vw, vh / 3);
            g2.drawLine(0, vh * 2 / 3, vw, vh * 2 / 3);
            g2.setColor(new Color(31, 78, 121, 55));
            g2.drawRect(0, 0, vw - 1, vh - 1);
            g2.dispose();
        }
    }

    /** Prévia simplificada do crachá frente/verso, no mesmo layout do PDF. */
    private static final class CrachaPreview extends JPanel {

        private static final Color AZUL = new Color(31, 78, 121);
        private static final Color CINZA = new Color(107, 114, 128);
        private static final Color GRAFITE = new Color(31, 41, 55);
        private static final Color BORDA = new Color(201, 214, 229);

        private final Colaborador colaborador;
        private final Empresa empresa;
        private final List<RegistroTreinamento> registros;
        private final BufferedImage logo;
        private BufferedImage foto;

        CrachaPreview(Colaborador colaborador, Empresa empresa, List<RegistroTreinamento> registros) {
            this.colaborador = colaborador == null ? new Colaborador() : colaborador;
            this.empresa = empresa;
            this.registros = registros == null ? new ArrayList<>() : registros;
            this.logo = empresa == null ? null : Fotos.carregar(empresa.getLogo());
            setOpaque(true);
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(500, 410));
        }

        void setFoto(BufferedImage foto) {
            this.foto = foto;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int margem = 10;
            int alturaLegenda = 20;
            double larguraMm = 108;
            double alturaMm = 86;
            double disponivelW = getWidth() - margem * 2;
            double disponivelH = getHeight() - margem * 2 - alturaLegenda;
            double escala = Math.min(disponivelW / larguraMm, disponivelH / alturaMm);
            double largura = larguraMm * escala;
            double altura = alturaMm * escala;
            double x = (getWidth() - largura) / 2.0;
            double y = margem;

            AffineTransform original = g2.getTransform();
            g2.translate(x, y);
            g2.scale(escala, escala);

            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, 108, 86);
            g2.setColor(new Color(0x7C, 0x87, 0x96));
            g2.setStroke(new BasicStroke(0.25f));
            g2.drawRect(0, 0, 108, 86);

            frente(g2);
            verso(g2);

            g2.setColor(new Color(0x7C, 0x87, 0x96));
            g2.setStroke(new BasicStroke(0.3f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 10f, new float[]{1.2f, 0.8f}, 0f));
            g2.drawLine(54, 0, 54, 86);
            g2.setTransform(original);

            g2.setFont(Ui.fonte(9, Font.BOLD));
            g2.setColor(new Color(0x7A, 0x87, 0x98));
            String frente = "FRENTE";
            String verso = "VERSO";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(frente, (float) (x + largura / 4.0 - fm.stringWidth(frente) / 2.0),
                    (float) (y + altura + 14));
            g2.drawString(verso, (float) (x + largura * 3 / 4.0 - fm.stringWidth(verso) / 2.0),
                    (float) (y + altura + 14));
            g2.dispose();
        }

        private void frente(Graphics2D g) {
            double topoY = 3.5;
            if (logo != null) {
                desenharCover(g, logo, 9, topoY, 36, 10);
            } else if (empresa != null && !vazio(empresa.nomeExibicao())) {
                centro(g, empresa.nomeExibicao(), 2.55f, Font.BOLD, AZUL, 6.2, 2, 50);
                centro(g, "CNPJ " + valor(empresa.getCnpj(), ""), 1.55f,
                        Font.PLAIN, CINZA, 9.1, 2, 50);
            } else {
                centro(g, "Informações/logo da empresa", 1.7f, Font.PLAIN, CINZA, 6.5, 2, 50);
            }

            double px = 12;
            double py = 13.5;
            double pw = 30;
            double ph = 40;
            if (foto != null) {
                desenharCover(g, foto, px, py, pw, ph);
            } else {
                g.setColor(new Color(0xF4, 0xF7, 0xFA));
                g.fillRect((int) px, (int) py, (int) pw, (int) ph);
                g.setColor(CINZA);
                centro(g, "Foto 3x4", 2.0f, Font.PLAIN, CINZA, py + ph / 2 + 0.8, px, pw);
            }
            g.setColor(BORDA);
            g.setStroke(new BasicStroke(0.2f));
            g.drawRect((int) px, (int) py, (int) pw, (int) ph);

            String nome = vazio(colaborador.getNome())
                    ? "Nome do Colaborador" : colaborador.getNome().toUpperCase();
            centro(g, nome, 3.0f, Font.BOLD, new Color(0x11, 0x18, 0x27), 59.8, 2, 50);
            String cargo = vazio(colaborador.getCargo()) ? "Função" : colaborador.getCargo();
            centro(g, cargo, 2.2f, Font.PLAIN, new Color(0x37, 0x41, 0x51), 67.8, 2, 50);
        }

        private void verso(Graphics2D g) {
            double x = 57;
            double largura = 48;
            double[] colunas = {24, 12, 12};
            double[] posicoes = {x, x + 24, x + 36};
            double y = 4.4;

            g.setFont(fonte(1.65f, Font.BOLD));
            g.setColor(AZUL);
            esquerda(g, "Treinamentos", 1.65f, Font.BOLD, AZUL, y, posicoes[0]);
            centro(g, "Finalizado", 1.65f, Font.BOLD, AZUL, y, posicoes[1], colunas[1]);
            centro(g, "Vencimento", 1.65f, Font.BOLD, AZUL, y, posicoes[2], colunas[2]);
            g.setColor(AZUL);
            g.setStroke(new BasicStroke(0.18f));
            g.drawLine((int) x, (int) (y + 1.3), (int) (x + largura), (int) (y + 1.3));

            y = 10.1;
            int limite = Math.min(12, registros.size());
            for (int i = 0; i < limite; i++) {
                RegistroTreinamento r = registros.get(i);
                esquerda(g, valor(r.getNorma(), ""), 1.85f, Font.BOLD, GRAFITE, y, posicoes[0]);
                LocalDate feito = r.getDataFim() != null ? r.getDataFim() : r.getData();
                centro(g, dataCurta(feito), 1.75f, Font.PLAIN, GRAFITE, y, posicoes[1], colunas[1]);
                String validade = r.getValidade() == null ? "PGR" : dataCurta(r.getValidade());
                centro(g, validade, 1.75f, Font.PLAIN, GRAFITE, y, posicoes[2], colunas[2]);
                g.setColor(new Color(0xDD, 0xE5, 0xEF));
                g.setStroke(new BasicStroke(0.15f));
                g.drawLine((int) x, (int) (y + 1.15), (int) (x + largura), (int) (y + 1.15));
                y += 3.8;
            }
            if (registros.isEmpty()) {
                centro(g, "Nenhum treinamento registrado.", 1.7f, Font.PLAIN, CINZA, 10.5, x, largura);
            } else if (registros.size() > limite) {
                esquerda(g, "+ " + (registros.size() - limite) + " outro(s) treinamento(s)",
                        1.55f, Font.PLAIN, CINZA, y, x);
            }
        }

        private void desenharCover(Graphics2D g, BufferedImage imagem, double x, double y,
                double largura, double altura) {
            double escala = Math.max(largura / imagem.getWidth(), altura / imagem.getHeight());
            double dw = imagem.getWidth() * escala;
            double dh = imagem.getHeight() * escala;
            double dx = x + (largura - dw) / 2.0;
            double dy = y + (altura - dh) / 2.0;
            Shape clip = g.getClip();
            g.clip(new Rectangle2D.Double(x, y, largura, altura));
            g.drawImage(imagem, new AffineTransform(dw / imagem.getWidth(), 0, 0,
                    dh / imagem.getHeight(), dx, dy), null);
            g.setClip(clip);
        }

        private void centro(Graphics2D g, String texto, float tamanho, int estilo, Color cor,
                double y, double x, double largura) {
            String t = texto == null ? "" : texto;
            Font f = fonte(tamanho, estilo);
            g.setFont(f);
            FontMetrics fm = g.getFontMetrics();
            while (fm.stringWidth(t) > largura && tamanho > 1.2f) {
                tamanho -= 0.15f;
                f = fonte(tamanho, estilo);
                g.setFont(f);
                fm = g.getFontMetrics();
            }
            g.setColor(cor);
            g.drawString(t, (float) (x + (largura - fm.stringWidth(t)) / 2.0), (float) y);
        }

        private void esquerda(Graphics2D g, String texto, float tamanho, int estilo, Color cor,
                double y, double x) {
            String t = texto == null ? "" : texto;
            g.setFont(fonte(tamanho, estilo));
            g.setColor(cor);
            g.drawString(t, (float) x, (float) y);
        }

        private static boolean vazio(String texto) {
            return texto == null || texto.isBlank();
        }

        private static String valor(String texto, String padrao) {
            return texto == null || texto.isBlank() ? padrao : texto;
        }
    }
}
