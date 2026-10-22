package br.com.treinanorma.view;

import br.com.treinanorma.dao.ColaboradorDAO;
import br.com.treinanorma.dao.EmpresaDAO;
import br.com.treinanorma.dao.InstrutorDAO;
import br.com.treinanorma.dao.MatrizDAO;
import br.com.treinanorma.dao.TurmaDAO;
import br.com.treinanorma.model.Colaborador;
import br.com.treinanorma.model.Empresa;
import br.com.treinanorma.model.Instrutor;
import br.com.treinanorma.model.MatrizItem;
import br.com.treinanorma.model.Participante;
import br.com.treinanorma.model.Turma;
import br.com.treinanorma.util.Datas;
import br.com.treinanorma.util.Documentos;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class TurmasPanel extends TelaBase {

    private final TurmaDAO dao = new TurmaDAO();
    private final MatrizDAO matriz = new MatrizDAO();
    private final InstrutorDAO instrutores = new InstrutorDAO();
    private final ColaboradorDAO colaboradores = new ColaboradorDAO();
    private final EmpresaDAO empresas = new EmpresaDAO();

    private final JTextField busca = Ui.campo("Buscar por código, NR, treinamento, local ou instrutor...");
    private final JTable tabela = Ui.tabela("Código", "Data", "Norma", "Treinamento", "Instrutor", "Participantes", "Status");
    private final TableRowSorter<DefaultTableModel> ordenador;
    private final List<Turma> lista = new ArrayList<>();

    public TurmasPanel(Runnable voltar) {
        super("Turmas", "Execução coletiva de treinamento, chamada e geração da ata.", voltar);
        ordenador = new TableRowSorter<>((DefaultTableModel) tabela.getModel());
        tabela.setRowSorter(ordenador);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(260);
        acao(Ui.primario("+ Nova turma")).addActionListener(e -> editar(null));
    }

    @Override
    protected void construir() {
        JPanel cartao = Ui.cartao(new BorderLayout(0, 10));
        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        topo.add(busca, BorderLayout.WEST);
        busca.setPreferredSize(new Dimension(360, 30));
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
        topo.add(Ui.subtitulo("Fluxo: participantes > chamada > concluir > certificados."), BorderLayout.EAST);
        cartao.add(topo, BorderLayout.NORTH);
        cartao.add(Ui.rolagem(tabela), BorderLayout.CENTER);

        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        rodape.setOpaque(false);
        JButton editar = Ui.secundario("Editar");
        editar.addActionListener(e -> editar(selecionado()));
        JButton participantes = Ui.secundario("Participantes");
        participantes.addActionListener(e -> participantes(selecionado()));
        JButton chamada = Ui.secundario("Chamada");
        chamada.addActionListener(e -> chamada(selecionado()));
        JButton concluir = Ui.secundario("Concluir turma");
        concluir.addActionListener(e -> concluir(selecionado()));
        JButton documentos = Ui.secundario("Documentos");
        documentos.addActionListener(e -> documentos(selecionado()));
        JButton excluir = Ui.perigo("Excluir");
        excluir.addActionListener(e -> excluir(selecionado()));
        rodape.add(editar);
        rodape.add(participantes);
        rodape.add(chamada);
        rodape.add(concluir);
        rodape.add(documentos);
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
            for (Turma t : lista) {
                modelo.addRow(new Object[]{t.getCodigo(), Datas.br(t.getData()), t.getNorma(),
                    t.getTreinamento(), nz(t.getInstrutorNome()), t.getParticipantes(),
                    status(t.getStatus())});
            }
        } catch (RuntimeException e) {
            Ui.erro(this, e);
        }
    }

    private String status(String valor) {
        if ("CONCLUIDA".equals(valor)) {
            return "Concluída";
        }
        if ("CANCELADA".equals(valor)) {
            return "Cancelada";
        }
        return "Agendada";
    }

    private void filtrar() {
        String termo = Ui.texto(busca);
        ordenador.setRowFilter(termo.isEmpty() ? null
                : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(termo)));
    }

    private Turma selecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            Ui.aviso(this, "Selecione uma turma.");
            return null;
        }
        return lista.get(tabela.convertRowIndexToModel(linha));
    }

    private void editar(Turma turma) {
        Turma alvo = turma == null ? new Turma() : turma;
        JDialog d = Ui.dialogo(this, turma == null ? "Nova turma" : "Editar turma", 760, 640);

        List<MatrizItem> itens = matriz.itens();
        Map<String, MatrizItem> porRotulo = new LinkedHashMap<>();
        for (MatrizItem i : itens) {
            porRotulo.put(i.toString(), i);
        }
        JComboBox<String> comboMatriz = Ui.combo(porRotulo.keySet().toArray(new String[0]));
        JTextField data = Ui.campo("dd/mm/aaaa");
        JTextField inicio = Ui.campo("08:00");
        JTextField fim = Ui.campo("17:00");
        JTextField local = Ui.campo();
        JComboBox<String> modalidade = Ui.combo("Presencial", "Semipresencial", "EAD");
        JComboBox<String> comboInstrutor = Ui.combo();
        JTextField chTotal = Ui.campo("8");
        JTextField chPresencial = Ui.campo("8");
        JTextArea conteudo = new JTextArea(4, 20);
        conteudo.setFont(Ui.fonte(12, Font.PLAIN));
        conteudo.setLineWrap(true);
        conteudo.setWrapStyleWord(true);
        JLabel baseLegal = Ui.subtitulo("Selecione o treinamento da matriz.");
        JLabel validade = Ui.subtitulo("");

        if (turma != null) {
            for (MatrizItem i : itens) {
                if (i.getId() == (alvo.getMatrizId() == null ? -1 : alvo.getMatrizId())) {
                    comboMatriz.setSelectedItem(i.toString());
                    break;
                }
            }
            data.setText(Datas.br(alvo.getData()));
            inicio.setText(nz(alvo.getHoraInicio()));
            fim.setText(nz(alvo.getHoraFim()));
            local.setText(nz(alvo.getLocal()));
            modalidade.setSelectedItem(nz(alvo.getModalidade()).isEmpty() ? "Presencial" : alvo.getModalidade());
            chTotal.setText(numero(alvo.getChTotal()));
            chPresencial.setText(numero(alvo.getChPresencial()));
            conteudo.setText(nz(alvo.getConteudo()));
        } else {
            data.setText(Datas.br(LocalDate.now()));
        }

        Runnable atualizarInstrutores = () -> {
            MatrizItem item = porRotulo.get((String) comboMatriz.getSelectedItem());
            comboInstrutor.removeAllItems();
            comboInstrutor.addItem("(sem instrutor)");
            if (item != null) {
                for (Instrutor i : instrutores.habilitadosPara(item.getNorma())) {
                    comboInstrutor.addItem(i.getNome());
                }
            }
            if (alvo.getInstrutorId() != null) {
                Instrutor i = instrutores.porId(alvo.getInstrutorId());
                if (i != null) {
                    comboInstrutor.setSelectedItem(i.getNome());
                }
            }
        };

        java.util.function.Consumer<MatrizItem> aplicar = item -> {
            if (item == null) {
                return;
            }
            boolean preencherCh = turma == null || alvo.getMatrizId() == null
                    || alvo.getMatrizId() != item.getId();
            if (preencherCh) {
                chTotal.setText(item.getChMinima() == null ? "" : numero(item.getChMinima()));
                chPresencial.setText(item.getPresencialMinimo() == null ? ""
                        : numero(item.getPresencialMinimo()));
                if (item.getModalidade() != null && !item.getModalidade().isBlank()) {
                    modalidade.setSelectedItem(item.getModalidade());
                }
            }
            baseLegal.setText("Base legal: " + nz(item.getBaseLegal()));
            validade.setText(item.getValidadeMeses() == null
                    ? "Validade definida pelo PGR (a NR não fixa prazo)."
                    : "Validade: " + item.getValidadeMeses() + " meses a partir da data da turma.");
            atualizarInstrutores.run();
        };
        comboMatriz.addActionListener(e ->
                aplicar.accept(porRotulo.get((String) comboMatriz.getSelectedItem())));
        aplicar.accept(porRotulo.get((String) comboMatriz.getSelectedItem()));

        JPanel form = Ui.formulario();
        GridBagConstraints gbc = Ui.gbc();
        Ui.campo(form, gbc, 0, 0, "Treinamento da matriz *", comboMatriz, 4);
        Ui.campo(form, gbc, 0, 1, "Data *", data, 1);
        Ui.campo(form, gbc, 1, 1, "Início", inicio, 1);
        Ui.campo(form, gbc, 2, 1, "Fim", fim, 1);
        Ui.campo(form, gbc, 3, 1, "Modalidade", modalidade, 1);
        Ui.campo(form, gbc, 0, 2, "Local", local, 2);
        Ui.campo(form, gbc, 2, 2, "Instrutor", comboInstrutor, 2);
        Ui.campo(form, gbc, 0, 3, "CH total (h)", chTotal, 1);
        Ui.campo(form, gbc, 1, 3, "CH presencial (h)", chPresencial, 1);
        JPanel blocoConteudo = new JPanel(new BorderLayout(0, 3));
        blocoConteudo.setOpaque(false);
        blocoConteudo.add(Ui.rotulo("Conteúdo programático"), BorderLayout.NORTH);
        JScrollPane rolagemConteudo = new JScrollPane(conteudo);
        rolagemConteudo.setPreferredSize(new Dimension(300, 90));
        blocoConteudo.add(rolagemConteudo, BorderLayout.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 4;
        form.add(blocoConteudo, gbc);
        gbc.gridy = 5;
        form.add(baseLegal, gbc);
        gbc.gridy = 6;
        form.add(validade, gbc);

        JPanel corpo = new JPanel(new BorderLayout(0, 6));
        corpo.setOpaque(false);
        corpo.setBorder(BorderFactory.createEmptyBorder(12, 14, 4, 14));
        corpo.add(form, BorderLayout.NORTH);

        JButton salvar = Ui.primario("Salvar turma");
        JButton cancelar = Ui.secundario("Cancelar");
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        rodape.add(cancelar);
        rodape.add(salvar);

        salvar.addActionListener(e -> {
            try {
                MatrizItem item = porRotulo.get((String) comboMatriz.getSelectedItem());
                if (item == null) {
                    Ui.aviso(d, "Selecione o treinamento da matriz.");
                    return;
                }
                LocalDate dia = Datas.ler(data.getText());
                if (dia == null) {
                    Ui.aviso(d, "Data inválida. Use dd/mm/aaaa.");
                    return;
                }
                double total = numero(chTotal.getText());
                double presencial = numero(chPresencial.getText());
                if (total <= 0) {
                    Ui.aviso(d, "Informe a carga horária total.");
                    return;
                }
                if (presencial > total) {
                    Ui.aviso(d, "A carga presencial não pode ser maior que a total.");
                    return;
                }
                alvo.setMatrizId(item.getId());
                alvo.setEmpresaId(1);
                alvo.setNorma(item.getNorma());
                alvo.setTreinamento(item.getTreinamento());
                alvo.setData(dia);
                alvo.setHoraInicio(Ui.texto(inicio));
                alvo.setHoraFim(Ui.texto(fim));
                alvo.setLocal(Ui.texto(local));
                alvo.setModalidade((String) modalidade.getSelectedItem());
                alvo.setChTotal(total);
                alvo.setChPresencial(presencial);
                String nomeInstrutor = (String) comboInstrutor.getSelectedItem();
                alvo.setInstrutorId(null);
                if (nomeInstrutor != null && !nomeInstrutor.startsWith("(")) {
                    for (Instrutor i : instrutores.listar(nomeInstrutor)) {
                        if (i.getNome().equals(nomeInstrutor)) {
                            alvo.setInstrutorId(i.getId());
                            break;
                        }
                    }
                }
                alvo.setConteudo(conteudo.getText());
                if (turma == null) {
                    alvo.setCriadoPor(br.com.treinanorma.util.Sessao.nome());
                }
                if (turma != null && "CONCLUIDA".equals(alvo.getStatus())) {
                    Ui.aviso(d, "Turma concluída não pode ser alterada. Crie uma nova turma de reciclagem.");
                    return;
                }
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

    private void participantes(Turma turma) {
        if (turma == null) {
            return;
        }
        try {
            List<Colaborador> todos = colaboradores.ativos();
            List<Participante> atuais = dao.participantes(turma.getId());
            DefaultListModel<String> modelo = new DefaultListModel<>();
            List<Colaborador> ordem = new ArrayList<>();
            for (Colaborador c : todos) {
                if (turma.getInstrutorId() != null) {
                    Instrutor i = instrutores.porId(turma.getInstrutorId());
                    if (i != null && i.getCpf() != null && i.getCpf().equals(c.getCpf())) {
                        continue;
                    }
                }
                ordem.add(c);
                modelo.addElement(c.getNome() + "  -  " + c.getCpf()
                        + (c.getCargo() == null ? "" : "  -  " + c.getCargo()));
            }
            JList<String> listaColab = new JList<>(modelo);
            listaColab.setFont(Ui.fonte(12, Font.PLAIN));
            listaColab.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            List<Integer> selecionados = new ArrayList<>();
            for (Participante p : atuais) {
                for (int i = 0; i < ordem.size(); i++) {
                    if (ordem.get(i).getId() == p.getColaboradorId()) {
                        selecionados.add(i);
                    }
                }
            }
            int[] indices = selecionados.stream().mapToInt(Integer::intValue).toArray();
            listaColab.setSelectedIndices(indices);

            JDialog d = Ui.dialogo(this, "Participantes - " + turma.getCodigo(), 620, 520);
            JPanel corpo = new JPanel(new BorderLayout(0, 8));
            corpo.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
            corpo.add(Ui.subtitulo("Selecione os colaboradores (Ctrl para vários). "
                    + "Somente presentes e aprovados geram certificado."), BorderLayout.NORTH);
            JScrollPane rolagem = new JScrollPane(listaColab);
            corpo.add(rolagem, BorderLayout.CENTER);

            JButton salvar = Ui.primario("Salvar seleção");
            JButton cancelar = Ui.secundario("Cancelar");
            JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
            rodape.add(cancelar);
            rodape.add(salvar);

            salvar.addActionListener(e -> {
                try {
                    List<Integer> escolhidos = new ArrayList<>();
                    for (int i : listaColab.getSelectedIndices()) {
                        escolhidos.add(ordem.get(i).getId());
                    }
                    List<Integer> atuaisIds = new ArrayList<>();
                    for (Participante p : atuais) {
                        atuaisIds.add(p.getColaboradorId());
                    }
                    List<Integer> adicionar = new ArrayList<>(escolhidos);
                    adicionar.removeAll(atuaisIds);
                    List<Integer> remover = new ArrayList<>(atuaisIds);
                    remover.removeAll(escolhidos);
                    if (!adicionar.isEmpty()) {
                        dao.adicionarParticipantes(turma.getId(), adicionar);
                    }
                    for (Integer id : remover) {
                        dao.removerParticipante(turma.getId(), id);
                    }
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
        } catch (RuntimeException e) {
            Ui.erro(this, e);
        }
    }

    private void chamada(Turma turma) {
        if (turma == null) {
            return;
        }
        try {
            List<Participante> participantes = dao.participantes(turma.getId());
            if (participantes.isEmpty()) {
                Ui.aviso(this, "Adicione participantes antes de lançar a chamada.");
                return;
            }
            DefaultTableModel modelo = new DefaultTableModel(
                    new Object[]{"Nome", "CPF", "Presente", "Aprovado"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column >= 2;
                }

                @Override
                public Class<?> getColumnClass(int column) {
                    return column >= 2 ? Boolean.class : String.class;
                }
            };
            for (Participante p : participantes) {
                modelo.addRow(new Object[]{p.getNome(), p.getCpf(),
                    p.isPresente(), p.getAprovado() == null ? Boolean.TRUE : p.getAprovado()});
            }
            JTable t = Ui.tabela("Nome", "CPF", "Presente", "Aprovado");
            t.setModel(modelo);
            t.getColumnModel().getColumn(0).setPreferredWidth(260);
            t.getColumnModel().getColumn(2).setPreferredWidth(70);
            t.getColumnModel().getColumn(3).setPreferredWidth(70);

            JDialog d = Ui.dialogo(this, "Chamada - " + turma.getCodigo(), 680, 480);
            JPanel corpo = new JPanel(new BorderLayout(0, 8));
            corpo.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
            corpo.add(Ui.subtitulo("Marque presença e aprovação. A preventiva pode ser lançada depois."),
                    BorderLayout.NORTH);
            corpo.add(Ui.rolagem(t), BorderLayout.CENTER);

            JButton salvar = Ui.primario("Salvar chamada");
            JButton cancelar = Ui.secundario("Cancelar");
            JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
            rodape.add(cancelar);
            rodape.add(salvar);
            salvar.addActionListener(e -> {
                try {
                    for (int i = 0; i < participantes.size(); i++) {
                        Participante p = participantes.get(i);
                        p.setPresente(Boolean.TRUE.equals(modelo.getValueAt(i, 2)));
                        Object aprovado = modelo.getValueAt(i, 3);
                        p.setAprovado(aprovado == null ? null : Boolean.TRUE.equals(aprovado));
                        p.setEletronica(true);
                    }
                    dao.salvarChamada(participantes);
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
        } catch (RuntimeException e) {
            Ui.erro(this, e);
        }
    }

    private void concluir(Turma turma) {
        if (turma == null) {
            return;
        }
        if ("CONCLUIDA".equals(turma.getStatus())) {
            Ui.info(this, "Turma já concluída em " + turma.getConcluidaEm() + ".");
            return;
        }
        try {
            List<Participante> participantes = dao.participantes(turma.getId());
            int presentes = 0;
            for (Participante p : participantes) {
                if (p.isPresente()) {
                    presentes++;
                }
            }
            if (presentes == 0) {
                Ui.aviso(this, "Nenhum participante presente. Lance a chamada antes de concluir.");
                return;
            }
            if (!Ui.confirma(this, "Concluir a turma " + turma.getCodigo() + "?\n\n"
                    + presentes + " participante(s) presente(s) gerarão registro de treinamento "
                    + "com validade calculada pela matriz.\nA ata será selada com hash de integridade.")) {
                return;
            }
            int gerados = dao.concluir(turma.getId(), br.com.treinanorma.util.Sessao.nome());
            carregar();
            if (Ui.confirma(this, gerados + " registro(s) gerado(s).\n\nDeseja gerar a ata em PDF agora?")) {
                Turma cheia = dao.porId(turma.getId());
                File ata = Documentos.ata(cheia, empresas.obter(), instrutor(cheia),
                        dao.participantes(cheia.getId()));
                abrir(ata);
            }
        } catch (Exception e) {
            Ui.erro(this, "Falha ao gerar documento: " + e.getMessage());
        }
    }

    private void documentos(Turma turma) {
        if (turma == null) {
            return;
        }
        JDialog d = Ui.dialogo(this, "Documentos - " + turma.getCodigo(), 460, 260);
        JPanel corpo = new JPanel();
        corpo.setLayout(new javax.swing.BoxLayout(corpo, javax.swing.BoxLayout.Y_AXIS));
        corpo.setBorder(BorderFactory.createEmptyBorder(16, 18, 12, 18));
        corpo.add(Ui.subtitulo("Arquivos gerados em " + Documentos.pasta().getAbsolutePath()));
        corpo.add(javax.swing.Box.createVerticalStrut(10));
        JButton ata = Ui.primario("Ata de presença (A4)");
        JButton frentes = Ui.secundario("Crachás - frentes (A4, 2x5)");
        JButton versos = Ui.secundario("Crachás - versos");
        JButton pasta = Ui.link("Abrir pasta de documentos");
        ata.setAlignmentX(LEFT_ALIGNMENT);
        frentes.setAlignmentX(LEFT_ALIGNMENT);
        versos.setAlignmentX(LEFT_ALIGNMENT);
        pasta.setAlignmentX(LEFT_ALIGNMENT);
        corpo.add(ata);
        corpo.add(javax.swing.Box.createVerticalStrut(6));
        corpo.add(frentes);
        corpo.add(javax.swing.Box.createVerticalStrut(6));
        corpo.add(versos);
        corpo.add(javax.swing.Box.createVerticalStrut(10));
        corpo.add(pasta);

        ata.addActionListener(e -> {
            try {
                Turma cheia = dao.porId(turma.getId());
                File f = Documentos.ata(cheia, empresas.obter(), instrutor(cheia),
                        dao.participantes(cheia.getId()));
                abrir(f);
            } catch (Exception ex) {
                Ui.erro(d, "Falha ao gerar a ata: " + ex.getMessage());
            }
        });
        frentes.addActionListener(e -> {
            try {
                List<Participante> ps = dao.participantes(turma.getId());
                if (ps.isEmpty()) {
                    Ui.aviso(d, "Sem participantes.");
                    return;
                }
                abrir(Documentos.crachasFrentes(turma, empresas.obter(), ps));
            } catch (Exception ex) {
                Ui.erro(d, "Falha ao gerar crachás: " + ex.getMessage());
            }
        });
        versos.addActionListener(e -> {
            try {
                List<Participante> ps = dao.participantes(turma.getId());
                if (ps.isEmpty()) {
                    Ui.aviso(d, "Sem participantes.");
                    return;
                }
                abrir(Documentos.crachasVersos(turma, empresas.obter(), ps));
            } catch (Exception ex) {
                Ui.erro(d, "Falha ao gerar crachás: " + ex.getMessage());
            }
        });
        pasta.addActionListener(e -> {
            try {
                java.awt.Desktop.getDesktop().open(Documentos.pasta());
            } catch (Exception ex) {
                Ui.erro(d, ex);
            }
        });

        d.add(corpo, BorderLayout.CENTER);
        d.setVisible(true);
    }

    private Instrutor instrutor(Turma t) {
        return t.getInstrutorId() == null ? null : instrutores.porId(t.getInstrutorId());
    }

    private void abrir(File arquivo) {
        try {
            java.awt.Desktop.getDesktop().open(arquivo);
        } catch (Exception e) {
            Ui.info(this, "Arquivo gerado em:\n" + arquivo.getAbsolutePath());
        }
    }

    private void excluir(Turma t) {
        if (t == null) {
            return;
        }
        if ("CONCLUIDA".equals(t.getStatus())) {
            Ui.aviso(this, "Turma concluída não pode ser excluída (rastreabilidade).");
            return;
        }
        if (!Ui.confirma(this, "Excluir a turma " + t.getCodigo() + "?")) {
            return;
        }
        try {
            dao.excluir(t.getId());
            carregar();
        } catch (RuntimeException e) {
            Ui.erro(this, e);
        }
    }

    private double numero(String valor) {
        if (valor == null || valor.isBlank()) {
            return 0;
        }
        try {
            return Double.parseDouble(valor.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String numero(double valor) {
        return valor == Math.rint(valor) ? String.valueOf((int) valor) : String.valueOf(valor);
    }

    private String nz(String valor) {
        return valor == null ? "" : valor;
    }
}
