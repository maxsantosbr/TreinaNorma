package br.com.treinanorma.view;

import br.com.treinanorma.dao.InstrutorDAO;
import br.com.treinanorma.dao.MatrizDAO;
import br.com.treinanorma.model.Instrutor;
import br.com.treinanorma.model.Norma;
import br.com.treinanorma.util.Cpf;
import br.com.treinanorma.util.Datas;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class InstrutoresPanel extends TelaBase {

    private static final String AJUDA_TIPO_RESUMO = "Interno = quadro da empresa; externo = prestador "
            + "contratado. Clique para detalhes.";
    private static final String AJUDA_TIPO = "<html><body style='width:340px'>"
            + "<b>Interno</b> &mdash; instrutor do quadro da própria empresa (empregado ou sócio), "
            + "que ministra o treinamento diretamente.<br><br>"
            + "<b>Externo</b> &mdash; instrutor contratado: autônomo, consultoria ou empresa de "
            + "treinamento. Mantenha contrato e comprovantes de qualificação arquivados.<br><br>"
            + "É uma informação cadastral, usada em relatórios; não altera a validade nem o "
            + "conteúdo do certificado."
            + "</body></html>";

    private final InstrutorDAO dao = new InstrutorDAO();
    private final MatrizDAO matriz = new MatrizDAO();
    private final JTextField busca = Ui.campo("Buscar instrutor por nome, CPF ou registro...");
    private final JTable tabela = Ui.tabela("Nome", "CPF", "Formação", "Registro", "Tipo", "NRs habilitadas", "Status");
    private final TableRowSorter<DefaultTableModel> ordenador;
    private final List<Instrutor> lista = new ArrayList<>();

    public InstrutoresPanel(Runnable voltar) {
        super("Instrutores", "Habilitações por NR controlam quem pode assinar ata e certificado.", voltar);
        ordenador = new TableRowSorter<>((DefaultTableModel) tabela.getModel());
        tabela.setRowSorter(ordenador);
        acao(Ui.primario("+ Novo instrutor")).addActionListener(e -> editar(null));
    }

    @Override
    protected void construir() {
        JPanel cartao = Ui.cartao(new BorderLayout(0, 10));
        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        topo.add(busca, BorderLayout.WEST);
        busca.setPreferredSize(new Dimension(320, 30));
        busca.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filtrar();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filtrar();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filtrar();
            }
        });
        topo.add(Ui.subtitulo("Duplo clique para editar."), BorderLayout.EAST);
        cartao.add(topo, BorderLayout.NORTH);
        cartao.add(Ui.rolagem(tabela), BorderLayout.CENTER);

        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        rodape.setOpaque(false);
        JButton editar = Ui.secundario("Editar");
        editar.addActionListener(e -> editar(selecionado()));
        JButton excluir = Ui.perigo("Excluir");
        excluir.addActionListener(e -> excluir(selecionado()));
        rodape.add(editar);
        rodape.add(excluir);
        cartao.add(rodape, BorderLayout.SOUTH);

        tabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editar(selecionado());
                }
            }
        });

        conteudo(cartao);
        carregar();
    }

    private void carregar() {
        try {
            lista.clear();
            lista.addAll(dao.listar(null));
            DefaultTableModel modelo = (DefaultTableModel) tabela.getModel();
            modelo.setRowCount(0);
            for (Instrutor i : lista) {
                StringBuilder hab = new StringBuilder();
                for (Instrutor.Habilitacao h : i.getHabilitacoes()) {
                    if (hab.length() > 0) {
                        hab.append(", ");
                    }
                    hab.append(h.getNorma());
                    if (h.vencida()) {
                        hab.append(" (vencida)");
                    }
                }
                modelo.addRow(new Object[]{i.getNome(), Cpf.formatar(i.getCpf()),
                    nz(i.getFormacao()), nz(i.getRegistro()), i.getTipo(),
                    hab.length() == 0 ? "Nenhuma" : hab.toString(),
                    i.isAtivo() ? "Ativo" : "Inativo"});
            }
        } catch (RuntimeException e) {
            Ui.erro(this, e);
        }
    }

    private void filtrar() {
        String termo = Ui.texto(busca);
        ordenador.setRowFilter(termo.isEmpty() ? null
                : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(termo)));
    }

    private Instrutor selecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            Ui.aviso(this, "Selecione um instrutor.");
            return null;
        }
        return lista.get(tabela.convertRowIndexToModel(linha));
    }

    private void editar(Instrutor instrutor) {
        Instrutor alvo = instrutor == null ? new Instrutor() : instrutor;
        JDialog d = Ui.dialogo(this, instrutor == null ? "Novo instrutor" : "Editar instrutor", 720, 560);

        JTextField nome = Ui.campo();
        JTextField cpf = Ui.campo("000.000.000-00");
        JTextField formacao = Ui.campo();
        JTextField registro = Ui.campo();
        JComboBox<String> tipo = Ui.combo("INTERNO", "EXTERNO");
        JTextField email = Ui.campo();
        JTextField telefone = Ui.campo();
        JCheckBox ativo = new JCheckBox("Instrutor ativo");
        ativo.setOpaque(false);
        ativo.setFont(Ui.fonte(12, Font.PLAIN));
        ativo.setSelected(instrutor == null || instrutor.isAtivo());

        nome.setText(nz(alvo.getNome()));
        cpf.setText(Cpf.formatar(alvo.getCpf()));
        formacao.setText(nz(alvo.getFormacao()));
        registro.setText(nz(alvo.getRegistro()));
        tipo.setSelectedItem(alvo.getTipo());
        email.setText(nz(alvo.getEmail()));
        telefone.setText(nz(alvo.getTelefone()));

        JPanel form = Ui.formulario();
        GridBagConstraints gbc = Ui.gbc();
        Ui.campo(form, gbc, 0, 0, "Nome *", nome, 2);
        Ui.campo(form, gbc, 0, 1, "CPF", cpf, 1);
        Ui.campo(form, gbc, 1, 1, "Tipo", tipo, 1,
                Ui.dica(AJUDA_TIPO_RESUMO, AJUDA_TIPO));
        Ui.campo(form, gbc, 0, 2, "Formação", formacao, 2);
        Ui.campo(form, gbc, 0, 3, "Registro profissional", registro, 1);
        Ui.campo(form, gbc, 1, 3, "Telefone", telefone, 1);
        Ui.campo(form, gbc, 0, 4, "E-mail", email, 2);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        form.add(ativo, gbc);

        DefaultListModel<String> modeloHab = new DefaultListModel<>();
        List<Instrutor.Habilitacao> habilitacoes = new ArrayList<>(alvo.getHabilitacoes());
        for (Instrutor.Habilitacao h : habilitacoes) {
            modeloHab.addElement(rotuloHabilitacao(h));
        }
        JList<String> listaHab = new JList<>(modeloHab);
        listaHab.setFont(Ui.fonte(12, Font.PLAIN));
        listaHab.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaHab.setVisibleRowCount(4);
        JScrollPane rolagemHab = new JScrollPane(listaHab);
        rolagemHab.setPreferredSize(new Dimension(300, 92));

        List<Norma> normas = matriz.normas();
        String[] opcoes = new String[normas.size()];
        for (int i = 0; i < normas.size(); i++) {
            opcoes[i] = normas.get(i).getCodigo();
        }
        JComboBox<String> comboNorma = Ui.combo(opcoes);
        JTextField validadeHab = Ui.campo("dd/mm/aaaa");
        validadeHab.setPreferredSize(new Dimension(110, 28));
        JButton adicionar = Ui.secundario("Adicionar");
        JButton remover = Ui.link("Remover selecionada");
        remover.setEnabled(false);
        listaHab.addListSelectionListener(e -> remover.setEnabled(listaHab.getSelectedIndex() >= 0));

        adicionar.addActionListener(e -> {
            String norma = (String) comboNorma.getSelectedItem();
            LocalDate validade = Datas.ler(validadeHab.getText());
            if (!validadeHab.getText().isBlank() && validade == null) {
                Ui.aviso(d, "Validade inválida. Use dd/mm/aaaa.");
                return;
            }
            for (Instrutor.Habilitacao h : habilitacoes) {
                if (h.getNorma().equals(norma)) {
                    Ui.aviso(d, "Habilitação para " + norma + " já adicionada.");
                    return;
                }
            }
            Instrutor.Habilitacao h = new Instrutor.Habilitacao(norma, validade);
            habilitacoes.add(h);
            modeloHab.addElement(rotuloHabilitacao(h));
            validadeHab.setText("");
        });
        remover.addActionListener(e -> {
            int i = listaHab.getSelectedIndex();
            if (i >= 0) {
                habilitacoes.remove(i);
                modeloHab.remove(i);
            }
        });

        JPanel blocoHab = new JPanel(new BorderLayout(0, 6));
        blocoHab.setOpaque(false);
        blocoHab.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Ui.BORDA),
                BorderFactory.createEmptyBorder(10, 0, 0, 0)));
        blocoHab.add(Ui.titulo("Habilitações por NR"), BorderLayout.NORTH);
        JPanel listaHabPainel = new JPanel(new BorderLayout(0, 6));
        listaHabPainel.setOpaque(false);
        listaHabPainel.add(rolagemHab, BorderLayout.CENTER);
        JPanel acoesHab = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        acoesHab.setOpaque(false);
        acoesHab.add(remover);
        listaHabPainel.add(acoesHab, BorderLayout.SOUTH);
        JPanel centroHab = new JPanel(new BorderLayout(10, 0));
        centroHab.setOpaque(false);
        centroHab.add(listaHabPainel, BorderLayout.CENTER);
        JPanel controles = new JPanel();
        controles.setOpaque(false);
        controles.setLayout(new javax.swing.BoxLayout(controles, javax.swing.BoxLayout.Y_AXIS));
        JLabel rotuloHab = Ui.subtitulo("NR / validade (opcional)");
        rotuloHab.setAlignmentX(LEFT_ALIGNMENT);
        controles.add(rotuloHab);
        controles.add(javax.swing.Box.createVerticalStrut(4));
        controles.add(Ui.linha(comboNorma, validadeHab));
        controles.add(javax.swing.Box.createVerticalStrut(8));
        adicionar.setAlignmentX(LEFT_ALIGNMENT);
        controles.add(adicionar);
        controles.add(javax.swing.Box.createVerticalGlue());
        centroHab.add(controles, BorderLayout.EAST);
        blocoHab.add(centroHab, BorderLayout.CENTER);

        JPanel corpo = new JPanel(new BorderLayout(0, 10));
        corpo.setOpaque(false);
        corpo.setBorder(BorderFactory.createEmptyBorder(12, 14, 4, 14));
        corpo.add(form, BorderLayout.NORTH);
        corpo.add(blocoHab, BorderLayout.CENTER);

        JButton salvar = Ui.primario("Salvar");
        JButton cancelar = Ui.secundario("Cancelar");
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        rodape.add(cancelar);
        rodape.add(salvar);

        salvar.addActionListener(e -> {
            try {
                if (Ui.texto(nome).isEmpty()) {
                    Ui.aviso(d, "Informe o nome do instrutor.");
                    return;
                }
                String documento = Cpf.limpar(cpf.getText());
                if (!documento.isEmpty() && !Cpf.valido(documento)) {
                    Ui.aviso(d, "CPF inválido.");
                    return;
                }
                alvo.setNome(Ui.texto(nome));
                alvo.setCpf(documento);
                alvo.setFormacao(Ui.texto(formacao));
                alvo.setRegistro(Ui.texto(registro));
                alvo.setTipo((String) tipo.getSelectedItem());
                alvo.setEmail(Ui.texto(email));
                alvo.setTelefone(Ui.texto(telefone));
                alvo.setAtivo(ativo.isSelected());
                alvo.getHabilitacoes().clear();
                alvo.getHabilitacoes().addAll(habilitacoes);
                dao.salvar(alvo);
                d.dispose();
                carregar();
            } catch (RuntimeException ex) {
                Ui.erro(d, ex);
            }
        });
        cancelar.addActionListener(e -> d.dispose());

        d.add(corpo, BorderLayout.CENTER);
        d.add(rodape, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private String rotuloHabilitacao(Instrutor.Habilitacao h) {
        return h.getNorma() + " - validade "
                + (h.getValidade() == null ? "indeterminada" : Datas.br(h.getValidade()))
                + (h.vencida() ? "  [VENCIDA]" : "");
    }

    private void excluir(Instrutor i) {
        if (i == null) {
            return;
        }
        if (!Ui.confirma(this, "Excluir o instrutor " + i.getNome() + "?")) {
            return;
        }
        try {
            dao.excluir(i.getId());
            carregar();
        } catch (RuntimeException e) {
            Ui.erro(this, e);
        }
    }

    private String nz(String valor) {
        return valor == null ? "" : valor;
    }
}
