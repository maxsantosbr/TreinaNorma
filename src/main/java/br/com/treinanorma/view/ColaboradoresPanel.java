package br.com.treinanorma.view;

import br.com.treinanorma.dao.ColaboradorDAO;
import br.com.treinanorma.dao.EmpresaDAO;
import br.com.treinanorma.dao.RegistroDAO;
import br.com.treinanorma.dao.SetorDAO;
import br.com.treinanorma.model.Colaborador;
import br.com.treinanorma.model.Empresa;
import br.com.treinanorma.model.RegistroTreinamento;
import br.com.treinanorma.util.Cpf;
import br.com.treinanorma.util.Datas;
import br.com.treinanorma.util.Excel;
import br.com.treinanorma.util.Fotos;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class ColaboradoresPanel extends TelaBase {

    private final ColaboradorDAO dao = new ColaboradorDAO();
    private final RegistroDAO registros = new RegistroDAO();
    private final SetorDAO setores = new SetorDAO();

    private final JTextField busca = Ui.campo("Buscar por nome, CPF, matrícula ou cargo...");
    private final JTable tabela = Ui.tabela("Nome", "CPF", "Cargo", "Setor", "Matrícula", "Status");
    private final TableRowSorter<DefaultTableModel> ordenador;
    private final List<Colaborador> lista = new ArrayList<>();

    public ColaboradoresPanel(Runnable voltar) {
        super("Colaboradores", "Cadastro, importação por planilha e histórico de treinamentos.", voltar);
        ordenador = new TableRowSorter<>((DefaultTableModel) tabela.getModel());
        tabela.setRowSorter(ordenador);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(240);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(120);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(150);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(120);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(90);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(80);
        acao(Ui.secundario("Modelo de planilha")).addActionListener(e -> gerarModelo());
        acao(Ui.secundario("Importar")).addActionListener(e -> importar());
        acao(Ui.primario("+ Novo colaborador")).addActionListener(e -> editar(null));
    }

    @Override
    protected void construir() {
        JPanel cartao = Ui.cartao(new BorderLayout(0, 10));
        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        topo.add(busca, BorderLayout.WEST);
        busca.setPreferredSize(new Dimension(340, 30));
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
        JLabel dica = Ui.subtitulo("Duplo clique para editar. Importe .xlsx ou .csv com cabeçalho.");
        topo.add(dica, BorderLayout.EAST);
        cartao.add(topo, BorderLayout.NORTH);
        cartao.add(Ui.rolagem(tabela), BorderLayout.CENTER);

        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        rodape.setOpaque(false);
        JButton btnEditar = Ui.secundario("Editar");
        btnEditar.addActionListener(e -> editar(selecionado()));
        JButton btnHist = Ui.secundario("Histórico");
        btnHist.addActionListener(e -> historico(selecionado()));
        JButton btnExcluir = Ui.perigo("Excluir");
        btnExcluir.addActionListener(e -> excluir(selecionado()));
        rodape.add(btnEditar);
        rodape.add(btnHist);
        rodape.add(btnExcluir);
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
            lista.addAll(dao.listar(null, false));
            DefaultTableModel modelo = (DefaultTableModel) tabela.getModel();
            modelo.setRowCount(0);
            for (Colaborador c : lista) {
                modelo.addRow(new Object[]{c.getNome(), Cpf.formatar(c.getCpf()),
                    c.getCargo() == null ? "" : c.getCargo(),
                    c.getSetorNome() == null ? "" : c.getSetorNome(),
                    c.getMatricula() == null ? "" : c.getMatricula(),
                    c.isAtivo() ? "Ativo" : "Inativo"});
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

    private Colaborador selecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            Ui.aviso(this, "Selecione um colaborador.");
            return null;
        }
        return lista.get(tabela.convertRowIndexToModel(linha));
    }

    private void editar(Colaborador c) {
        Colaborador alvo = c == null ? new Colaborador() : c;
        JDialog d = Ui.dialogo(this, c == null ? "Novo colaborador" : "Editar colaborador", 580, 620);

        JPanel form = Ui.formulario();
        GridBagConstraints gbc = Ui.gbc();
        JTextField nome = Ui.campo();
        JTextField cpf = Ui.campo("000.000.000-00");
        JTextField cargo = Ui.campo();
        JTextField setor = Ui.campo();
        JTextField matricula = Ui.campo();
        JTextField admissao = Ui.campo("dd/mm/aaaa");
        JTextField email = Ui.campo();
        JTextField telefone = Ui.campo();
        JCheckBox ativo = new JCheckBox("Colaborador ativo");
        ativo.setOpaque(false);
        ativo.setFont(Ui.fonte(12, Font.PLAIN));
        ativo.setForeground(Ui.TEXTO);
        ativo.setSelected(c == null || c.isAtivo());

        nome.setText(nz(alvo.getNome()));
        cpf.setText(Cpf.formatar(alvo.getCpf()));
        cargo.setText(nz(alvo.getCargo()));
        setor.setText(nz(alvo.getSetorNome()));
        matricula.setText(nz(alvo.getMatricula()));
        admissao.setText(Datas.br(alvo.getAdmissao()));
        email.setText(nz(alvo.getEmail()));
        telefone.setText(nz(alvo.getTelefone()));

        final String original = nz(alvo.getFoto());
        final String[] foto = {original};
        final boolean[] salvou = {false};

        JLabel miniatura = new JLabel("", JLabel.CENTER);
        miniatura.setOpaque(true);
        miniatura.setBackground(new java.awt.Color(0xF4, 0xF7, 0xFA));
        miniatura.setBorder(BorderFactory.createLineBorder(Ui.BORDA));
        miniatura.setPreferredSize(new Dimension(54, 72));
        miniatura.setMinimumSize(new Dimension(54, 72));

        JLabel statusFoto = new JLabel();
        statusFoto.setFont(Ui.fonte(10, Font.BOLD));

        JButton btnFoto = Ui.secundario("Foto e prévia do crachá...");
        JPanel infoFoto = new JPanel(new BorderLayout(0, 4));
        infoFoto.setOpaque(false);
        infoFoto.add(statusFoto, BorderLayout.NORTH);
        infoFoto.add(Ui.ajuda("Usada no crachá e nas próximas impressões."), BorderLayout.CENTER);
        infoFoto.add(Ui.linhaEsquerda(btnFoto), BorderLayout.SOUTH);

        JPanel painelFoto = new JPanel(new BorderLayout(10, 0));
        painelFoto.setOpaque(false);
        painelFoto.add(miniatura, BorderLayout.WEST);
        painelFoto.add(infoFoto, BorderLayout.CENTER);

        Runnable atualizarFoto = () -> {
            BufferedImage imagem = Fotos.carregar(foto[0]);
            if (imagem == null) {
                miniatura.setIcon(null);
                miniatura.setText("3x4");
                miniatura.setFont(Ui.fonte(9, Font.PLAIN));
                miniatura.setForeground(Ui.SUAVE);
                statusFoto.setText("Sem foto");
                statusFoto.setForeground(Ui.SUAVE);
            } else {
                miniatura.setText("");
                miniatura.setIcon(new ImageIcon(Fotos.recortar3x4(imagem, 48, 64)));
                statusFoto.setText("Com foto");
                statusFoto.setForeground(Ui.VERDE);
            }
        };
        atualizarFoto.run();

        Runnable limparNova = () -> {
            if (!salvou[0] && !foto[0].equals(original)) {
                if (!foto[0].isBlank()) {
                    Fotos.remover(foto[0]);
                }
                foto[0] = original;
            }
        };

        btnFoto.addActionListener(e -> {
            try {
                Colaborador visual = new Colaborador();
                visual.setId(alvo.getId());
                visual.setNome(Ui.texto(nome));
                visual.setCargo(Ui.texto(cargo));
                visual.setCpf(Cpf.limpar(cpf.getText()));
                visual.setFoto(foto[0]);
                Empresa empresa = new EmpresaDAO().obter();
                FotoCrachaDialog dialog = new FotoCrachaDialog(d, visual, empresa, foto[0]);
                String nova = dialog.abrir();
                if (dialog.isAlterou()) {
                    if (!Objects.equals(foto[0], nova) && !foto[0].isBlank()
                            && !foto[0].equals(original)) {
                        Fotos.remover(foto[0]);
                    }
                    foto[0] = nova == null ? "" : nova;
                    atualizarFoto.run();
                }
            } catch (RuntimeException ex) {
                Ui.erro(d, ex);
            }
        });

        Ui.campo(form, gbc, 0, 0, "Nome completo *", nome, 2);
        Ui.campo(form, gbc, 0, 1, "CPF *", cpf, 1);
        Ui.campo(form, gbc, 1, 1, "Matrícula", matricula, 1);
        Ui.campo(form, gbc, 0, 2, "Cargo / função", cargo, 1);
        Ui.campo(form, gbc, 1, 2, "Setor", setor, 1);
        Ui.campo(form, gbc, 0, 3, "Admissão", admissao, 1);
        Ui.campo(form, gbc, 1, 3, "Telefone", telefone, 1);
        Ui.campo(form, gbc, 0, 4, "E-mail", email, 2);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        form.add(ativo, gbc);
        Ui.campo(form, gbc, 0, 6, "Foto do crachá", painelFoto, 2);

        JPanel centro = new JPanel(new BorderLayout());
        centro.setOpaque(false);
        centro.setBorder(BorderFactory.createEmptyBorder(12, 14, 4, 14));
        centro.add(form, BorderLayout.NORTH);

        JButton salvar = Ui.primario("Salvar");
        JButton cancelar = Ui.secundario("Cancelar");
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        rodape.add(cancelar);
        rodape.add(salvar);

        salvar.addActionListener(e -> {
            try {
                if (Ui.texto(nome).isEmpty()) {
                    Ui.aviso(d, "Informe o nome.");
                    return;
                }
                String documento = Cpf.limpar(cpf.getText());
                if (!Cpf.valido(documento)) {
                    Ui.aviso(d, "CPF inválido.");
                    return;
                }
                LocalDate dataAdmissao = Datas.ler(admissao.getText());
                if (!admissao.getText().isBlank() && dataAdmissao == null) {
                    Ui.aviso(d, "Data de admissão inválida. Use dd/mm/aaaa.");
                    return;
                }
                Colaborador existente = dao.porCpf(documento);
                if (existente != null && existente.getId() != alvo.getId()) {
                    Ui.aviso(d, "Já existe um colaborador com esse CPF: " + existente.getNome());
                    return;
                }
                alvo.setNome(Ui.texto(nome));
                alvo.setCpf(documento);
                alvo.setCargo(Ui.texto(cargo));
                alvo.setMatricula(Ui.texto(matricula));
                alvo.setAdmissao(dataAdmissao);
                alvo.setEmail(Ui.texto(email));
                alvo.setTelefone(Ui.texto(telefone));
                alvo.setAtivo(ativo.isSelected());
                alvo.setFoto(foto[0].isBlank() ? null : foto[0]);
                Integer setorId = setores.garantir(Ui.texto(setor));
                alvo.setSetorId(setorId);
                if (alvo.getEmpresaId() == null) {
                    alvo.setEmpresaId(1);
                }
                dao.salvar(alvo);
                if (!original.isBlank() && !original.equals(foto[0])) {
                    Fotos.remover(original);
                }
                salvou[0] = true;
                d.dispose();
                carregar();
            } catch (RuntimeException ex) {
                Ui.erro(d, ex);
            }
        });
        cancelar.addActionListener(e -> {
            limparNova.run();
            d.dispose();
        });
        d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        d.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                limparNova.run();
            }
        });

        d.add(centro, BorderLayout.CENTER);
        d.add(rodape, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private void excluir(Colaborador c) {
        if (c == null) {
            return;
        }
        if (!Ui.confirma(this, "Excluir o colaborador " + c.getNome()
                + "?\nO histórico de treinamentos também será removido.")) {
            return;
        }
        try {
            dao.excluir(c.getId());
            Fotos.remover(c.getFoto());
            carregar();
        } catch (RuntimeException e) {
            Ui.erro(this, e);
        }
    }

    private void historico(Colaborador c) {
        if (c == null) {
            return;
        }
        try {
            List<RegistroTreinamento> listaReg = registros.porColaborador(c.getId());
            JTable t = Ui.tabela("Norma", "Treinamento", "Data", "CH", "Validade", "Certificado");
            DefaultTableModel modelo = (DefaultTableModel) t.getModel();
            for (RegistroTreinamento r : listaReg) {
                modelo.addRow(new Object[]{r.getNorma(), r.getTreinamento(), Datas.br(r.getData()),
                    r.getCh() + " h", r.getValidade() == null ? "Revisar PGR" : Datas.br(r.getValidade()),
                    r.getNumeroCertificado() == null ? "" : r.getNumeroCertificado()});
            }
            JDialog d = Ui.dialogo(this, "Histórico - " + c.getNome(), 760, 420);
            JPanel corpo = new JPanel(new BorderLayout(0, 8));
            corpo.setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));
            corpo.add(Ui.subtitulo(listaReg.size() + " treinamento(s) registrado(s)."), BorderLayout.NORTH);
            corpo.add(Ui.rolagem(t), BorderLayout.CENTER);
            d.add(corpo);
            d.setVisible(true);
        } catch (RuntimeException e) {
            Ui.erro(this, e);
        }
    }

    private void importar() {
        JFileChooser escolha = new JFileChooser();
        escolha.setDialogTitle("Importar colaboradores");
        escolha.setFileFilter(new FileNameExtensionFilter("Planilhas (.xlsx, .csv)", "xlsx", "csv"));
        if (escolha.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File arquivo = escolha.getSelectedFile();
        try {
            List<List<String>> linhas = Excel.ler(arquivo);
            if (linhas.isEmpty()) {
                Ui.aviso(this, "Arquivo vazio.");
                return;
            }
            int inicio = 0;
            Map<String, Integer> colunas = new HashMap<>();
            String primeira = String.join(";", linhas.get(0)).toLowerCase();
            if (primeira.contains("nome") || primeira.contains("cpf")) {
                for (int i = 0; i < linhas.get(0).size(); i++) {
                    colunas.put(linhas.get(0).get(i).trim().toLowerCase(), i);
                }
                inicio = 1;
            } else {
                colunas.put("nome", 0);
                colunas.put("cpf", 1);
                colunas.put("cargo", 2);
                colunas.put("setor", 3);
                colunas.put("matricula", 4);
                colunas.put("admissao", 5);
                colunas.put("email", 6);
                colunas.put("telefone", 7);
            }
            int novos = 0;
            int atualizados = 0;
            int ignorados = 0;
            List<String> problemas = new ArrayList<>();
            for (int i = inicio; i < linhas.size(); i++) {
                List<String> linha = linhas.get(i);
                String nome = valor(linha, colunas, "nome");
                String documento = Cpf.limpar(valor(linha, colunas, "cpf"));
                if (nome.isBlank() || documento.isBlank()) {
                    ignorados++;
                    continue;
                }
                if (!Cpf.valido(documento)) {
                    ignorados++;
                    problemas.add("Linha " + (i + 1) + ": CPF inválido (" + documento + ")");
                    continue;
                }
                Colaborador c = dao.porCpf(documento);
                boolean novo = c == null;
                if (novo) {
                    c = new Colaborador();
                    c.setEmpresaId(1);
                }
                c.setNome(nome);
                c.setCpf(documento);
                String cargo = valor(linha, colunas, "cargo");
                String setor = valor(linha, colunas, "setor");
                String matricula = valor(linha, colunas, "matricula");
                String email = valor(linha, colunas, "email");
                String telefone = valor(linha, colunas, "telefone");
                String data = valor(linha, colunas, "admissao");
                if (!cargo.isBlank()) {
                    c.setCargo(cargo);
                }
                if (!matricula.isBlank()) {
                    c.setMatricula(matricula);
                }
                if (!email.isBlank()) {
                    c.setEmail(email);
                }
                if (!telefone.isBlank()) {
                    c.setTelefone(telefone);
                }
                LocalDate admissao = Datas.ler(data);
                if (admissao != null) {
                    c.setAdmissao(admissao);
                }
                Integer setorId = setores.garantir(setor);
                if (setorId != null) {
                    c.setSetorId(setorId);
                }
                dao.salvar(c);
                if (novo) {
                    novos++;
                } else {
                    atualizados++;
                }
            }
            StringBuilder msg = new StringBuilder("Importação concluída.\n\nNovos: " + novos
                    + "\nAtualizados: " + atualizados + "\nIgnorados: " + ignorados);
            if (!problemas.isEmpty()) {
                msg.append("\n\nOcorrências:\n");
                for (int i = 0; i < Math.min(8, problemas.size()); i++) {
                    msg.append("- ").append(problemas.get(i)).append('\n');
                }
                if (problemas.size() > 8) {
                    msg.append("- ... e mais ").append(problemas.size() - 8);
                }
            }
            Ui.info(this, msg.toString());
            carregar();
        } catch (Exception e) {
            Ui.erro(this, "Falha ao importar: " + e.getMessage());
        }
    }

    private String valor(List<String> linha, Map<String, Integer> colunas, String chave) {
        Integer indice = colunas.get(chave);
        if (indice == null || indice >= linha.size()) {
            return "";
        }
        String v = linha.get(indice);
        return v == null ? "" : v.trim();
    }

    private void gerarModelo() {
        JFileChooser escolha = new JFileChooser();
        escolha.setDialogTitle("Salvar modelo de importação");
        escolha.setSelectedFile(new File("modelo-colaboradores.xlsx"));
        if (escolha.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File destino = escolha.getSelectedFile();
        if (!destino.getName().toLowerCase().endsWith(".xlsx")) {
            destino = new File(destino.getAbsolutePath() + ".xlsx");
        }
        try {
            List<String> cabecalho = List.of("Nome", "CPF", "Cargo", "Setor", "Matricula",
                    "Admissao", "Email", "Telefone");
            List<List<String>> exemplo = List.of(List.of("Maria Oliveira Santos", "529.982.247-25",
                    "Eletricista", "Manutenção", "M-002", "01/02/2026", "maria@empresa.com.br",
                    "(19) 99999-0000"));
            Excel.gravar(destino, cabecalho, exemplo);
            if (Ui.confirma(this, "Modelo gravado em:\n" + destino.getAbsolutePath()
                    + "\n\nDeseja abrir a pasta?")) {
                java.awt.Desktop.getDesktop().open(destino.getParentFile());
            }
        } catch (Exception e) {
            Ui.erro(this, "Falha ao gerar modelo: " + e.getMessage());
        }
    }

    private String nz(String valor) {
        return valor == null ? "" : valor;
    }
}
