package br.com.treinanorma.view;

import br.com.treinanorma.dao.MatrizDAO;
import br.com.treinanorma.model.MatrizItem;
import br.com.treinanorma.model.Norma;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class MatrizPanel extends TelaBase {

    private final MatrizDAO dao = new MatrizDAO();
    private final JTextField busca = Ui.campo("Filtrar por NR, treinamento, modalidade ou base legal...");
    private final JComboBox<String> norma = Ui.combo("Todas as NRs");
    private final JTable tabela = Ui.tabela("NR", "Treinamento", "Critério", "CH mín.", "Presencial",
            "Validade", "Recorrência", "Modalidade", "Base legal");
    private final JLabel resumo = Ui.subtitulo("");
    private List<MatrizItem> itens = new ArrayList<>();

    public MatrizPanel(Runnable voltar) {
        super("Matriz de treinamento", "Parâmetros legais por NR: carga horária, periodicidade e base legal.", voltar);
    }

    @Override
    protected void construir() {
        tabela.getColumnModel().getColumn(0).setPreferredWidth(58);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(210);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(130);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(66);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(74);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(72);
        tabela.getColumnModel().getColumn(6).setPreferredWidth(115);
        tabela.getColumnModel().getColumn(7).setPreferredWidth(104);
        tabela.getColumnModel().getColumn(8).setPreferredWidth(190);
        busca.setPreferredSize(new Dimension(320, 30));
        norma.addActionListener(e -> carregar());
        busca.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                carregar();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                carregar();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                carregar();
            }
        });

        JPanel cartao = Ui.cartao(new BorderLayout(0, 10));
        JPanel topo = new JPanel(new BorderLayout(10, 0));
        topo.setOpaque(false);
        topo.add(Ui.linha(busca, norma), BorderLayout.WEST);
        topo.add(resumo, BorderLayout.EAST);
        cartao.add(topo, BorderLayout.NORTH);
        cartao.add(Ui.rolagem(tabela), BorderLayout.CENTER);

        JLabel nota = Ui.subtitulo("Validade vazia = definida pelo PGR (a NR não fixa prazo). "
                + "NR-05 usa grau de risco para dimensionar a CIPA.");
        nota.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 0, 2));
        cartao.add(nota, BorderLayout.SOUTH);

        conteudo(cartao);
        carregarNormas();
        carregar();
    }

    private void carregarNormas() {
        try {
            norma.removeAllItems();
            norma.addItem("Todas as NRs");
            for (Norma n : dao.normas()) {
                norma.addItem(n.getCodigo());
            }
        } catch (RuntimeException e) {
            Ui.erro(this, e);
        }
    }

    private void carregar() {
        try {
            itens = dao.itens();
            String filtro = Ui.texto(busca).toLowerCase();
            String nr = (String) norma.getSelectedItem();
            DefaultTableModel modelo = (DefaultTableModel) tabela.getModel();
            modelo.setRowCount(0);
            int total = 0;
            for (MatrizItem i : itens) {
                if (nr != null && !"Todas as NRs".equals(nr) && !nr.equals(i.getNorma())) {
                    continue;
                }
                String linha = (i.getNorma() + " " + i.getTreinamento() + " "
                        + nz(i.getModalidade()) + " " + nz(i.getBaseLegal())).toLowerCase();
                if (!filtro.isEmpty() && !linha.contains(filtro)) {
                    continue;
                }
                modelo.addRow(new Object[]{i.getNorma(), i.getTreinamento(),
                    criterio(i),
                    i.getChMinima() == null ? "a definir" : numero(i.getChMinima()) + " h",
                    i.getPresencialMinimo() == null ? "-" : numero(i.getPresencialMinimo()) + " h",
                    i.getValidadeMeses() == null ? "PGR" : i.getValidadeMeses() + " meses",
                    maiusculaInicial(nz(i.getRecorrencia())),
                    modalidade(i.getModalidade()),
                    nz(i.getBaseLegal())});
                total++;
            }
            resumo.setText(total + " linha(s) de " + itens.size() + " na matriz");
        } catch (RuntimeException e) {
            Ui.erro(this, e);
        }
    }

    private String criterio(MatrizItem i) {
        if (i.getCriterioTipo() == null || i.getCriterioTipo().isBlank()) {
            return "";
        }
        String tipo = i.getCriterioTipo().replace('_', ' ').toLowerCase();
        tipo = maiusculaInicial(tipo);
        return i.getCriterioValor() == null ? tipo : tipo + " " + i.getCriterioValor();
    }

    private String modalidade(String valor) {
        if (valor == null) {
            return "";
        }
        switch (valor.toUpperCase()) {
            case "A_DEFINIR":
                return "A definir";
            case "SEMIPRESENCIAL":
                return "Semipresencial";
            case "EAD":
                return "EaD";
            case "PRESENCIAL":
                return "Presencial";
            default:
                return maiusculaInicial(valor.toLowerCase().replace('_', ' '));
        }
    }

    private String maiusculaInicial(String valor) {
        if (valor == null || valor.isBlank()) {
            return "";
        }
        return Character.toUpperCase(valor.charAt(0)) + valor.substring(1).toLowerCase();
    }

    private String numero(Double valor) {
        return valor == Math.rint(valor) ? String.valueOf(valor.intValue())
                : String.valueOf(valor).replace('.', ',');
    }

    private String nz(String valor) {
        return valor == null ? "" : valor;
    }
}
