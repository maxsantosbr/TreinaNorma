package br.com.treinanorma.view;

import br.com.treinanorma.dao.EmpresaDAO;
import br.com.treinanorma.model.Empresa;
import br.com.treinanorma.util.Ufs;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

public class EmpresaPanel extends TelaBase {

    private static final long LIMITE_LOGO = 2L * 1024 * 1024;
    private static final int LARGURA_PREVIA = 108;
    private static final int ALTURA_PREVIA = 36;
    private static final String AJUDA_LOGO = "PNG (aceita fundo transparente) ou JPG, até 2 MB e "
            + "no mínimo 120×40 px.\nRecomendado 600×200 px (proporção 3:1).\n"
            + "A imagem entra no cabeçalho da ata e do certificado e no crachá, com até "
            + "38 mm de largura, sempre na proporção original.";

    private final EmpresaDAO dao = new EmpresaDAO();
    private Empresa empresa;

    private final JTextField razao = Ui.campo();
    private final JTextField fantasia = Ui.campo();
    private final JTextField cnpj = Ui.campo("00.000.000/0000-00");
    private final JTextField cnae = Ui.campo();
    private final JComboBox<String> grau = Ui.combo("Não informado", "1", "2", "3", "4");
    private final JTextField endereco = Ui.campo();
    private final JTextField numero = Ui.campo();
    private final JTextField complemento = Ui.campo();
    private final JTextField bairro = Ui.campo();
    private final JTextField cidade = Ui.campo();
    private final JComboBox<String> uf = Ui.combo(Ufs.opcoes());
    private final JTextField cep = Ui.campo();
    private final JTextField telefone = Ui.campo();
    private final JTextField email = Ui.campo();
    private final JTextField responsavelNome = Ui.campo();
    private final JTextField responsavelProfissao = Ui.campo();
    private final JTextField responsavelRegistro = Ui.campo();
    private final JLabel logoNome = Ui.subtitulo("Nenhuma imagem selecionada");
    private final JLabel logoPrevia = new JLabel();
    private String logo;
    private final JLabel consulta = Ui.subtitulo("");

    public EmpresaPanel(Runnable voltar) {
        super("Empresa e estabelecimento", "Dados usados no cabeçalho da ata, do certificado e do crachá.", voltar);
        acao(Ui.secundario("Recarregar")).addActionListener(e -> carregar());
        acao(Ui.primario("Salvar")).addActionListener(e -> salvar());
    }

    @Override
    protected void construir() {
        JPanel form = Ui.formulario();
        GridBagConstraints gbc = Ui.gbc();
        int y = 0;

        form.add(secao("Identificação"), restricao(gbc, 0, y++, 4));

        Ui.campo(form, gbc, 0, y, "Razão social *", razao, 2);
        Ui.campo(form, gbc, 2, y++, "Nome fantasia", fantasia, 2);
        Ui.campo(form, gbc, 0, y, "CNPJ", cnpj, 1);
        Ui.campo(form, gbc, 1, y, "CNAE principal", cnae, 1);
        Ui.campo(form, gbc, 2, y, "Grau de risco", grau, 1);
        Ui.campo(form, gbc, 3, y++, "Telefone", telefone, 1);
        Ui.campo(form, gbc, 0, y, "E-mail", email, 4);

        form.add(secao("Endereço"), restricao(gbc, 0, ++y, 4));
        y++;
        Ui.campo(form, gbc, 0, y, "Logradouro", endereco, 2);
        Ui.campo(form, gbc, 2, y, "Número", numero, 1);
        Ui.campo(form, gbc, 3, y++, "Complemento", complemento, 1);
        Ui.campo(form, gbc, 0, y, "Bairro", bairro, 1);
        Ui.campo(form, gbc, 1, y, "Cidade", cidade, 1);
        Ui.campo(form, gbc, 2, y, "UF", uf, 1);
        Ui.campo(form, gbc, 3, y++, "CEP", cep, 1);

        form.add(secao("Responsável técnico"), restricao(gbc, 0, ++y, 4));
        y++;
        Ui.campo(form, gbc, 0, y, "Nome", responsavelNome, 2);
        Ui.campo(form, gbc, 2, y++, "Profissão", responsavelProfissao, 2);
        Ui.campo(form, gbc, 0, y, "Registro profissional", responsavelRegistro, 2);

        JButton escolher = Ui.secundario("Escolher imagem");
        escolher.addActionListener(e -> escolherLogo());
        JButton remover = Ui.link("remover");
        remover.addActionListener(e -> limparLogo());
        JPanel botoesLogo = Ui.linhaEsquerda(escolher, remover);

        JPanel textosLogo = new JPanel();
        textosLogo.setOpaque(false);
        textosLogo.setLayout(new BoxLayout(textosLogo, BoxLayout.Y_AXIS));
        botoesLogo.setAlignmentX(LEFT_ALIGNMENT);
        JLabel ajudaLogo = Ui.ajuda("PNG ou JPG · até 2 MB · recomendado 600×200 px");
        ajudaLogo.setAlignmentX(LEFT_ALIGNMENT);
        logoNome.setAlignmentX(LEFT_ALIGNMENT);
        textosLogo.add(botoesLogo);
        textosLogo.add(Box.createVerticalStrut(4));
        textosLogo.add(ajudaLogo);
        textosLogo.add(Box.createVerticalStrut(2));
        textosLogo.add(logoNome);

        logoPrevia.setPreferredSize(new Dimension(LARGURA_PREVIA, ALTURA_PREVIA));
        logoPrevia.setMinimumSize(new Dimension(LARGURA_PREVIA, ALTURA_PREVIA));
        logoPrevia.setHorizontalAlignment(JLabel.CENTER);
        logoPrevia.setVerticalAlignment(JLabel.CENTER);
        logoPrevia.setOpaque(true);
        logoPrevia.setBackground(Color.WHITE);
        logoPrevia.setBorder(BorderFactory.createLineBorder(Ui.BORDA));
        logoPrevia.setToolTipText("Pré-visualização do logotipo");

        JPanel escolhaLogo = new JPanel(new BorderLayout(12, 0));
        escolhaLogo.setOpaque(false);
        JPanel previaTopo = new JPanel(new BorderLayout());
        previaTopo.setOpaque(false);
        previaTopo.add(logoPrevia, BorderLayout.NORTH);
        escolhaLogo.add(previaTopo, BorderLayout.WEST);
        escolhaLogo.add(textosLogo, BorderLayout.CENTER);
        escolhaLogo.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        JPanel blocoLogo = new JPanel(new BorderLayout(0, 3));
        blocoLogo.setOpaque(false);
        blocoLogo.add(Ui.linhaEsquerda(Ui.rotulo("Logotipo"), Ui.dica(AJUDA_LOGO)),
                BorderLayout.NORTH);
        blocoLogo.add(escolhaLogo, BorderLayout.CENTER);
        form.add(blocoLogo, restricao(gbc, 2, y++, 2));

        JPanel cartao = Ui.cartao(new BorderLayout());
        cartao.add(form, BorderLayout.CENTER);
        cartao.add(consulta, BorderLayout.SOUTH);
        consulta.setBorder(BorderFactory.createEmptyBorder(10, 4, 0, 0));

        JScrollPane rolagem = new JScrollPane(cartao);
        rolagem.setBorder(BorderFactory.createEmptyBorder());
        rolagem.getViewport().setBackground(Ui.FUNDO);
        rolagem.getVerticalScrollBar().setUnitIncrement(18);
        conteudo(rolagem);

        carregar();
    }

    private JLabel secao(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Ui.fonte(12, Font.BOLD));
        l.setForeground(Ui.AZUL);
        l.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Ui.BORDA),
                BorderFactory.createEmptyBorder(10, 0, 6, 0)));
        return l;
    }

    private GridBagConstraints restricao(GridBagConstraints gbc, int x, int y, int largura) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = largura;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private void carregar() {
        try {
            empresa = dao.obter();
            if (empresa == null) {
                empresa = new Empresa();
            }
            razao.setText(texto(empresa.getRazaoSocial()));
            fantasia.setText(texto(empresa.getNomeFantasia()));
            cnpj.setText(texto(empresa.getCnpj()));
            cnae.setText(texto(empresa.getCnae()));
            grau.setSelectedItem(empresa.getGrauRisco() == null ? "Não informado"
                    : String.valueOf(empresa.getGrauRisco()));
            endereco.setText(texto(empresa.getEndereco()));
            numero.setText(texto(empresa.getNumero()));
            complemento.setText(texto(empresa.getComplemento()));
            bairro.setText(texto(empresa.getBairro()));
            cidade.setText(texto(empresa.getCidade()));
            String siglaUf = Ufs.sigla(empresa.getUf());
            uf.setSelectedItem(siglaUf == null ? Ufs.VAZIO : siglaUf);
            cep.setText(texto(empresa.getCep()));
            telefone.setText(texto(empresa.getTelefone()));
            email.setText(texto(empresa.getEmail()));
            responsavelNome.setText(texto(empresa.getResponsavelNome()));
            responsavelProfissao.setText(texto(empresa.getResponsavelProfissao()));
            responsavelRegistro.setText(texto(empresa.getResponsavelRegistro()));
            logo = empresa.getLogo();
            mostrarLogo(false);
            consulta.setText(empresa.getId() > 0
                    ? "Cadastro nº " + empresa.getId() + " - alterações são registradas na auditoria."
                    : "Nenhuma empresa cadastrada. Preencha e clique em Salvar.");
        } catch (RuntimeException e) {
            Ui.erro(this, e);
        }
    }

    private String texto(String valor) {
        return valor == null ? "" : valor;
    }

    private void escolherLogo() {
        JFileChooser escolha = new JFileChooser();
        escolha.setDialogTitle("Selecionar logotipo");
        escolha.setFileFilter(new FileNameExtensionFilter("Imagens (PNG, JPG)", "png", "jpg", "jpeg"));
        escolha.setAcceptAllFileFilterUsed(false);
        if (escolha.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File arquivo = escolha.getSelectedFile();
        String nome = arquivo.getName().toLowerCase(Locale.ROOT);
        if (!nome.endsWith(".png") && !nome.endsWith(".jpg") && !nome.endsWith(".jpeg")) {
            Ui.aviso(this, "Formato não suportado. Selecione um arquivo PNG ou JPG.");
            return;
        }
        long tamanho = arquivo.length();
        if (tamanho > LIMITE_LOGO) {
            Ui.aviso(this, String.format(Locale.ROOT,
                    "A imagem tem %.1f MB e o limite é 2 MB.\n"
                    + "Reduza a imagem (recomendado 600×200 px) e tente novamente.",
                    tamanho / 1048576.0));
            return;
        }
        BufferedImage imagem = lerImagem(arquivo);
        if (imagem == null) {
            Ui.aviso(this, "Não foi possível ler a imagem. O arquivo pode estar corrompido.");
            return;
        }
        if (imagem.getWidth() < 120 || imagem.getHeight() < 40) {
            Ui.aviso(this, "Imagem muito pequena (" + imagem.getWidth() + "×"
                    + imagem.getHeight() + " px).\nUse no mínimo 120×40 px; "
                    + "o recomendado é 600×200 px.");
            return;
        }
        logo = arquivo.getAbsolutePath();
        mostrarLogo(false);
    }

    private void limparLogo() {
        logo = null;
        mostrarLogo(false);
    }

    private void mostrarLogo(boolean avisarErro) {
        logoPrevia.setIcon(null);
        logoPrevia.setText(null);
        if (logo == null || logo.isBlank()) {
            logoPrevia.setText("sem imagem");
            logoPrevia.setFont(Ui.fonte(10, Font.PLAIN));
            logoPrevia.setForeground(Ui.SUAVE);
            logoNome.setText("Nenhuma imagem selecionada");
            logoNome.setForeground(Ui.SUAVE);
            return;
        }
        File arquivo = new File(logo);
        BufferedImage imagem = lerImagem(arquivo);
        if (imagem == null) {
            logoPrevia.setText("indisponível");
            logoPrevia.setFont(Ui.fonte(10, Font.PLAIN));
            logoPrevia.setForeground(Ui.VERMELHO);
            logoNome.setText(arquivo.getName() + " · arquivo não encontrado");
            logoNome.setForeground(Ui.VERMELHO);
            if (avisarErro) {
                Ui.aviso(this, "O logotipo salvo não foi encontrado:\n" + arquivo.getAbsolutePath());
            }
            return;
        }
        logoPrevia.setText(null);
        logoPrevia.setIcon(new ImageIcon(miniatura(imagem)));
        logoNome.setText(arquivo.getName() + " · " + imagem.getWidth() + "×"
                + imagem.getHeight() + " px · " + Math.max(1, arquivo.length() / 1024) + " KB");
        logoNome.setForeground(Ui.VERDE);
    }

    private BufferedImage lerImagem(File arquivo) {
        if (arquivo == null || !arquivo.isFile()) {
            return null;
        }
        try {
            return ImageIO.read(arquivo);
        } catch (IOException e) {
            return null;
        }
    }

    private Image miniatura(Image original) {
        double fator = Math.min((double) LARGURA_PREVIA / original.getWidth(null),
                (double) ALTURA_PREVIA / original.getHeight(null));
        int largura = Math.max(1, (int) Math.round(original.getWidth(null) * fator));
        int altura = Math.max(1, (int) Math.round(original.getHeight(null) * fator));
        BufferedImage saida = new BufferedImage(LARGURA_PREVIA, ALTURA_PREVIA,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = saida.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(original, (LARGURA_PREVIA - largura) / 2, (ALTURA_PREVIA - altura) / 2,
                largura, altura, null);
        g.dispose();
        return saida;
    }

    private void salvar() {
        if (Ui.texto(razao).isEmpty()) {
            Ui.aviso(this, "Informe a razão social.");
            razao.requestFocus();
            return;
        }
        try {
            empresa.setRazaoSocial(Ui.texto(razao));
            empresa.setNomeFantasia(Ui.texto(fantasia));
            empresa.setCnpj(Ui.texto(cnpj));
            empresa.setCnae(Ui.texto(cnae));
            String g = (String) grau.getSelectedItem();
            empresa.setGrauRisco("Não informado".equals(g) ? null : Integer.valueOf(g));
            empresa.setEndereco(Ui.texto(endereco));
            empresa.setNumero(Ui.texto(numero));
            empresa.setComplemento(Ui.texto(complemento));
            empresa.setBairro(Ui.texto(bairro));
            empresa.setCidade(Ui.texto(cidade));
            empresa.setUf(Ufs.sigla((String) uf.getSelectedItem()));
            empresa.setCep(Ui.texto(cep));
            empresa.setTelefone(Ui.texto(telefone));
            empresa.setEmail(Ui.texto(email));
            empresa.setLogo(logo);
            empresa.setResponsavelNome(Ui.texto(responsavelNome));
            empresa.setResponsavelProfissao(Ui.texto(responsavelProfissao));
            empresa.setResponsavelRegistro(Ui.texto(responsavelRegistro));
            dao.salvar(empresa);
            carregar();
            Ui.info(this, "Dados da empresa salvos.");
        } catch (RuntimeException e) {
            Ui.erro(this, e);
        }
    }
}
