package br.com.treinanorma.view;

import br.com.treinanorma.dao.EmpresaDAO;
import br.com.treinanorma.dao.InstrutorDAO;
import br.com.treinanorma.dao.RegistroDAO;
import br.com.treinanorma.dao.TurmaDAO;
import br.com.treinanorma.model.Instrutor;
import br.com.treinanorma.model.RegistroTreinamento;
import br.com.treinanorma.model.Turma;
import br.com.treinanorma.util.Cpf;
import br.com.treinanorma.util.Datas;
import br.com.treinanorma.util.Documentos;
import br.com.treinanorma.util.Sessao;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class CertificadosPanel extends TelaBase {

    private final RegistroDAO dao = new RegistroDAO();
    private final TurmaDAO turmas = new TurmaDAO();
    private final EmpresaDAO empresas = new EmpresaDAO();
    private final InstrutorDAO instrutores = new InstrutorDAO();

    private final JTextField busca = Ui.campo("Buscar por colaborador, CPF, NR ou certificado...");
    private final JComboBox<String> filtro = Ui.combo("Todos", "Vencidos", "Vencem em 30 dias",
            "Sem validade (PGR)", "Já certificados");
    private final JTextField de = Ui.campo("dd/mm/aaaa");
    private final JTextField ate = Ui.campo("dd/mm/aaaa");
    private final Modelo modelo = new Modelo();
    private final JTable tabela = Ui.tabela("", "Colaborador", "NR / Treinamento", "Data", "Validade",
            "Situação", "Certificado", "Código");
    private final TableRowSorter<DefaultTableModel> ordenador;
    private final List<RegistroTreinamento> lista = new ArrayList<>();
    private final List<Integer> ids = new ArrayList<>();
    private final Set<Integer> marcados = new HashSet<>();
    private final JButton lote = Ui.primario("Emitir selecionados");

    public CertificadosPanel(Runnable voltar) {
        super("Certificados", "Emissão em PDF, segunda via e validação por código de verificação.", voltar);
        tabela.setModel(modelo);
        ordenador = new TableRowSorter<DefaultTableModel>(modelo);
        ordenador.setSortable(0, false);
        tabela.setRowSorter(ordenador);
        tabela.getColumnModel().getColumn(0).setHeaderValue("Sel.");
        tabela.getColumnModel().getColumn(0).setMaxWidth(46);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(46);
        tabela.getColumnModel().getColumn(0).setResizable(false);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(150);
        tabela.getTableHeader().setToolTipText(
                "Clique no cabeçalho Sel. para marcar/desmarcar todos os visíveis");
        tabela.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (tabela.getTableHeader().columnAtPoint(e.getPoint()) != 0) {
                    return;
                }
                marcarVisiveis(!todosVisiveisMarcados());
            }
        });
        tabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    return;
                }
                int v = tabela.rowAtPoint(e.getPoint());
                int coluna = tabela.columnAtPoint(e.getPoint());
                if (v >= 0 && coluna >= 0 && tabela.convertColumnIndexToModel(coluna) == 0) {
                    alternar(tabela.convertRowIndexToModel(v), null);
                }
            }
        });
        tabela.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("SPACE"), "alternar");
        tabela.getActionMap().put("alternar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int v : tabela.getSelectedRows()) {
                    alternar(tabela.convertRowIndexToModel(v), null);
                }
            }
        });
        lote.setEnabled(false);
        acao(Ui.secundario("Validar código")).addActionListener(e -> validar());
    }

    @Override
    protected void construir() {
        tabela.getColumnModel().getColumn(1).setPreferredWidth(190);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(250);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(88);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(96);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(95);
        tabela.getColumnModel().getColumn(6).setPreferredWidth(118);
        tabela.getColumnModel().getColumn(7).setPreferredWidth(118);

        JPanel cartao = Ui.cartao(new BorderLayout(0, 10));
        JPanel topo = new JPanel(new BorderLayout(0, 8));
        topo.setOpaque(false);
        JPanel linhaBusca = new JPanel(new BorderLayout(10, 0));
        linhaBusca.setOpaque(false);
        linhaBusca.add(busca, BorderLayout.CENTER);
        filtro.setPreferredSize(new Dimension(170, 30));
        filtro.addActionListener(e -> carregar());
        linhaBusca.add(filtro, BorderLayout.EAST);

        JPanel linhaPeriodo = Ui.linhaEsquerda(Ui.rotulo("Período"),
                de, new JLabel("a"), ate,
                Ui.ajuda("vazio = todas as datas \u00b7 mesma data nos dois campos = um dia"));
        de.setPreferredSize(new Dimension(98, 30));
        ate.setPreferredSize(new Dimension(98, 30));

        DocumentListener aoMudarPeriodo = new DocumentListener() {
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
        };
        de.getDocument().addDocumentListener(aoMudarPeriodo);
        ate.getDocument().addDocumentListener(aoMudarPeriodo);
        busca.getDocument().addDocumentListener(aoMudarPeriodo);

        topo.add(linhaBusca, BorderLayout.NORTH);
        topo.add(linhaPeriodo, BorderLayout.WEST);
        cartao.add(topo, BorderLayout.NORTH);
        cartao.add(Ui.rolagem(tabela), BorderLayout.CENTER);

        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        rodape.setOpaque(false);
        lote.addActionListener(e -> emitirSelecionados());
        JButton segundaVia = Ui.secundario("Emitir / 2ª via");
        segundaVia.addActionListener(e -> emitir());
        JButton abrir = Ui.secundario("Abrir PDF");
        abrir.addActionListener(e -> abrirSelecionado());
        rodape.add(lote);
        rodape.add(segundaVia);
        rodape.add(abrir);
        cartao.add(rodape, BorderLayout.SOUTH);

        conteudo(cartao);
        carregar();
    }

    private String filtroChave() {
        int i = filtro.getSelectedIndex();
        switch (i) {
            case 1:
                return "VENCIDOS";
            case 2:
                return "VENCE30";
            case 3:
                return "SEM_VALIDADE";
            case 4:
                return "CERTIFICADOS";
            default:
                return null;
        }
    }

    private String periodo(RegistroTreinamento r) {
        if (r.getData() == null) {
            return "";
        }
        if (r.getDataFim() != null && !r.getDataFim().equals(r.getData())) {
            return Datas.br(r.getData()) + " a " + Datas.br(r.getDataFim());
        }
        return Datas.br(r.getData());
    }
    private void carregar() {
        try {
            lista.clear();
            lista.addAll(dao.listar(null, filtroChave()));
            modelo.setRowCount(0);
            ids.clear();
            for (RegistroTreinamento r : lista) {
                modelo.addRow(new Object[]{marcados.contains(r.getId()),
                    r.getColaboradorNome(),
                    r.getNorma() + " - " + r.getTreinamento(),
                    periodo(r),
                    r.getValidade() == null ? "Revisar PGR" : Datas.br(r.getValidade()),
                    situacao(r),
                    r.getNumeroCertificado() == null ? "Não emitido" : r.getNumeroCertificado(),
                    r.getCodigoVerificacao() == null ? "" : r.getCodigoVerificacao()});
                ids.add(r.getId());
            }
            filtrar();
            atualizarSelecao();
        } catch (RuntimeException e) {
            Ui.erro(this, e);
        }
    }

    private String situacao(RegistroTreinamento r) {
        if (r.getValidade() == null) {
            return "Revisar PGR";
        }
        LocalDate hoje = LocalDate.now();
        if (r.getValidade().isBefore(hoje)) {
            return "Vencido";
        }
        long dias = ChronoUnit.DAYS.between(hoje, r.getValidade());
        if (dias <= 30) {
            return "Vence em " + dias + " d";
        }
        return "Válido";
    }

    private void filtrar() {
        List<RowFilter<Object, Object>> filtros = new ArrayList<>();
        String termo = Ui.texto(busca);
        if (!termo.isEmpty()) {
            filtros.add(RowFilter.regexFilter("(?i)" + Pattern.quote(termo)));
        }
        LocalDate inicio = data(de);
        LocalDate fim = data(ate);
        if (inicio != null || fim != null) {
            filtros.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<? extends Object, ? extends Object> e) {
                    int linha = (Integer) e.getIdentifier();
                    if (linha < 0 || linha >= lista.size()) {
                        return true;
                    }
                    LocalDate data = lista.get(linha).getData();
                    if (data == null) {
                        return false;
                    }
                    return (inicio == null || !data.isBefore(inicio))
                            && (fim == null || !data.isAfter(fim));
                }
            });
        }
        ordenador.setRowFilter(filtros.isEmpty() ? null : RowFilter.andFilter(filtros));
    }

    private LocalDate data(JTextField campo) {
        String texto = Ui.texto(campo);
        boolean vazio = texto.isEmpty();
        LocalDate valor = Datas.ler(texto);
        boolean invalida = !vazio && valor == null;
        campo.setForeground(invalida ? Ui.VERMELHO : new java.awt.Color(0x33, 0x41, 0x55));
        campo.setToolTipText(invalida ? "Data inválida. Use dd/mm/aaaa." : null);
        return valor;
    }

    private void alternar(int linhaModelo, Boolean valor) {
        if (linhaModelo < 0 || linhaModelo >= ids.size()) {
            return;
        }
        boolean atual = Boolean.TRUE.equals(modelo.getValueAt(linhaModelo, 0));
        boolean novo = valor == null ? !atual : valor;
        modelo.setValueAt(novo, linhaModelo, 0);
        int id = ids.get(linhaModelo);
        if (novo) {
            marcados.add(id);
        } else {
            marcados.remove(id);
        }
        atualizarSelecao();
    }

    private void marcarVisiveis(boolean marcar) {
        for (int v = 0; v < tabela.getRowCount(); v++) {
            int m = tabela.convertRowIndexToModel(v);
            if (m < 0 || m >= ids.size()) {
                continue;
            }
            modelo.setValueAt(marcar, m, 0);
            int id = ids.get(m);
            if (marcar) {
                marcados.add(id);
            } else {
                marcados.remove(id);
            }
        }
        atualizarSelecao();
    }

    private boolean todosVisiveisMarcados() {
        if (tabela.getRowCount() == 0) {
            return false;
        }
        for (int v = 0; v < tabela.getRowCount(); v++) {
            int m = tabela.convertRowIndexToModel(v);
            if (!Boolean.TRUE.equals(modelo.getValueAt(m, 0))) {
                return false;
            }
        }
        return true;
    }

    private void atualizarSelecao() {
        int n = 0;
        for (int i = 0; i < modelo.getRowCount(); i++) {
            if (Boolean.TRUE.equals(modelo.getValueAt(i, 0))) {
                n++;
            }
        }
        lote.setText(n == 0 ? "Emitir selecionados" : "Emitir selecionados (" + n + ")");
        lote.setEnabled(n > 0);
    }

    private List<RegistroTreinamento> alvos() {
        List<RegistroTreinamento> alvos = new ArrayList<>();
        for (RegistroTreinamento r : lista) {
            if (marcados.contains(r.getId())) {
                alvos.add(r);
            }
        }
        return alvos;
    }

    private RegistroTreinamento selecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            Ui.aviso(this, "Selecione um registro de treinamento.");
            return null;
        }
        return lista.get(tabela.convertRowIndexToModel(linha));
    }

    private void emitirSelecionados() {
        List<RegistroTreinamento> alvos = alvos();
        if (alvos.isEmpty()) {
            Ui.aviso(this, "Marque ao menos um registro na coluna \u2713.");
            return;
        }
        int novos = 0;
        for (RegistroTreinamento r : alvos) {
            if (r.getNumeroCertificado() == null) {
                novos++;
            }
        }
        int reimpressos = alvos.size() - novos;
        StringBuilder resumo = new StringBuilder();
        resumo.append(novos).append(novos == 1 ? " certificado novo" : " certificados novos");
        if (reimpressos > 0) {
            resumo.append(" e ").append(reimpressos)
                    .append(reimpressos == 1 ? " reimpressão de PDF" : " reimpressões de PDF")
                    .append(" (sem nova via)");
        }
        StringBuilder nomes = new StringBuilder();
        for (int i = 0; i < Math.min(alvos.size(), 8); i++) {
            RegistroTreinamento r = alvos.get(i);
            nomes.append("\n\u2022 ").append(r.getColaboradorNome()).append(" \u2013 ")
                    .append(r.getNorma());
        }
        if (alvos.size() > 8) {
            nomes.append("\n\u2022 ... e mais ").append(alvos.size() - 8);
        }
        if (!Ui.confirma(this, "Emitir " + resumo + "?" + nomes + "\n\nPasta: "
                + Documentos.pasta().getAbsolutePath())) {
            return;
        }

        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
        int ok = 0;
        File ultimoGerado = null;
        List<String> falhas = new ArrayList<>();
        try {
            for (RegistroTreinamento r : alvos) {
                RegistroTreinamento atual = r;
                try {
                    if (atual.getNumeroCertificado() == null) {
                        atual = dao.emitirCertificado(atual.getId(), Sessao.nome());
                    }
                    Turma turma = atual.getTurmaId() == null ? null : turmas.porId(atual.getTurmaId());
                    Instrutor instrutor = atual.getInstrutorId() == null ? null
                            : instrutores.porId(atual.getInstrutorId());
                    boolean reimpressao = atual.getNumeroCertificado() != null && atual == r;
                    ultimoGerado = Documentos.certificado(atual, turma, empresas.obter(), instrutor,
                            reimpressao ? java.time.LocalDateTime.now() : null);
                    ok++;
                } catch (Exception ex) {
                    String motivo = ex.getMessage() == null ? ex.toString() : ex.getMessage();
                    if (motivo.toLowerCase().contains("usado por outro processo")
                            || motivo.toLowerCase().contains("used by another process")) {
                        motivo = "o PDF está aberto em outro programa. Feche-o e tente novamente.";
                    }
                    falhas.add(atual.getColaboradorNome() + ": " + motivo);
                }
            }
        } finally {
            setCursor(java.awt.Cursor.getDefaultCursor());
        }
        marcados.clear();
        carregar();

        StringBuilder fim = new StringBuilder(ok + (ok == 1 ? " PDF gerado" : " PDFs gerados")
                + " em " + Documentos.pasta().getAbsolutePath());
        if (!falhas.isEmpty()) {
            fim.append("\n\nFalhas (").append(falhas.size()).append("):\n")
                    .append(String.join("\n", falhas.subList(0, Math.min(falhas.size(), 8))));
            Ui.aviso(this, fim.toString());
            if (ok > 0) {
                Ui.perguntaAbrirLocal(this, "Abrir o local dos PDFs gerados?",
                        ok == 1 ? ultimoGerado : Documentos.pasta());
            }
            return;
        }
        Ui.perguntaAbrirLocal(this, fim.toString(), ok == 1 ? ultimoGerado : Documentos.pasta());
    }

    private void emitir() {
        RegistroTreinamento r = selecionado();
        if (r == null) {
            return;
        }
        try {
            RegistroTreinamento emitido = dao.emitirCertificado(r.getId(), Sessao.nome());
            Turma turma = emitido.getTurmaId() == null ? null : turmas.porId(emitido.getTurmaId());
            Instrutor instrutor = emitido.getInstrutorId() == null ? null
                    : instrutores.porId(emitido.getInstrutorId());
            File pdf = Documentos.certificado(emitido, turma, empresas.obter(), instrutor);
            carregar();
            Ui.perguntaAbrirLocal(this, "Certificado " + emitido.getNumeroCertificado() + " gerado.\n"
                    + "Código de verificação: " + emitido.getCodigoVerificacao() + "\n\n"
                    + pdf.getAbsolutePath(), pdf);
        } catch (Exception e) {
            Ui.erro(this, "Falha ao gerar PDF: " + e.getMessage());
        }
    }

    private void abrirSelecionado() {
        RegistroTreinamento r = selecionado();
        if (r == null) {
            return;
        }
        if (r.getNumeroCertificado() == null) {
            Ui.aviso(this, "Certificado ainda não emitido. Use Emitir / 2ª via.");
            return;
        }
        File pdf = new File(Documentos.pasta(), "certificado-" + r.getNumeroCertificado() + ".pdf");
        if (!pdf.exists()) {
            Ui.aviso(this, "O PDF não está na pasta de documentos. Emita novamente.");
            return;
        }
        abrir(pdf);
    }

    private void validar() {
        JDialog d = Ui.dialogo(this, "Validar certificado", 480, 320);
        JPanel corpo = new JPanel();
        corpo.setLayout(new javax.swing.BoxLayout(corpo, javax.swing.BoxLayout.Y_AXIS));
        corpo.setBorder(BorderFactory.createEmptyBorder(14, 16, 12, 16));
        JLabel instrucao = Ui.subtitulo(
                "Informe o número (TN-AAAA-000000) ou o código de verificação.");
        instrucao.setAlignmentX(LEFT_ALIGNMENT);
        corpo.add(instrucao);
        corpo.add(javax.swing.Box.createVerticalStrut(8));
        JTextField codigo = Ui.campo("TN-2026-000001 ou AAAA-BBBB-CCCC");
        codigo.setAlignmentX(LEFT_ALIGNMENT);
        codigo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        corpo.add(codigo);
        corpo.add(javax.swing.Box.createVerticalStrut(12));
        JLabel resultado = Ui.subtitulo(" ");
        resultado.setFont(Ui.fonte(12, Font.PLAIN));
        resultado.setAlignmentX(LEFT_ALIGNMENT);
        corpo.add(resultado);
        corpo.add(javax.swing.Box.createVerticalGlue());

        JButton consultar = Ui.primario("Consultar");
        JButton fechar = Ui.secundario("Fechar");
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        rodape.add(fechar);
        rodape.add(consultar);
        consultar.addActionListener(e -> {
            try {
                RegistroTreinamento r = dao.buscarPorCodigo(codigo.getText());
                if (r == null) {
                    resultado.setForeground(Ui.VERMELHO);
                    resultado.setText("Certificado não encontrado.");
                    return;
                }
                resultado.setForeground(Ui.VERDE);
                resultado.setText("<html><b>Certificado válido</b><br>" + r.getColaboradorNome()
                        + " - CPF " + Cpf.formatar(r.getColaboradorCpf()) + "<br>"
                        + r.getNorma() + " - " + r.getTreinamento() + "<br>"
                        + "Realizado em " + periodo(r) + " | CH " + r.getCh() + " h"
                        + (r.getValidade() == null ? " | validade pelo PGR"
                        : " | válido até " + Datas.br(r.getValidade())) + "<br>"
                        + r.getNumeroCertificado() + " | " + r.getCodigoVerificacao() + "</html>");
            } catch (RuntimeException ex) {
                Ui.erro(d, ex);
            }
        });
        fechar.addActionListener(e -> d.dispose());
        codigo.addActionListener(e -> consultar.doClick());

        d.add(corpo, BorderLayout.CENTER);
        d.add(rodape, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private void abrir(File arquivo) {
        try {
            java.awt.Desktop.getDesktop().open(arquivo);
        } catch (Exception e) {
            Ui.info(this, "Arquivo gerado em:\n" + arquivo.getAbsolutePath());
        }
    }


    private static final class Modelo extends DefaultTableModel {

        Modelo() {
            super(new Object[]{"", "Colaborador", "NR / Treinamento", "Data", "Validade",
                "Situação", "Certificado", "Código"}, 0);
        }

        @Override
        public Class<?> getColumnClass(int coluna) {
            return coluna == 0 ? Boolean.class : String.class;
        }

        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return false;
        }
    }
}
