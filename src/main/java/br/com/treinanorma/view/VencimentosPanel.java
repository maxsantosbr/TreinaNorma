package br.com.treinanorma.view;

import br.com.treinanorma.dao.RegistroDAO;
import br.com.treinanorma.model.RegistroTreinamento;
import br.com.treinanorma.util.Datas;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class VencimentosPanel extends TelaBase {

    private final RegistroDAO dao = new RegistroDAO();
    private final JTextField busca = Ui.campo("Filtrar por colaborador, NR ou treinamento...");
    private final JComboBox<String> janela = Ui.combo("Vencidos e próximos 30 dias",
            "Vencidos", "Próximos 60 dias", "Próximos 90 dias", "Todos os registros");
    private final JTable tabela = Ui.tabela("Colaborador", "NR / Treinamento", "Realizado",
            "Validade", "Dias restantes", "Situação");
    private final TableRowSorter<DefaultTableModel> ordenador;
    private final List<RegistroTreinamento> lista = new ArrayList<>();

    public VencimentosPanel(Runnable voltar) {
        super("Vencimentos", "Priorize reciclagens: vencidos primeiro, depois o que vence em 30 dias.", voltar);
        ordenador = new TableRowSorter<>((DefaultTableModel) tabela.getModel());
        tabela.setRowSorter(ordenador);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(260);
    }

    @Override
    protected void construir() {
        janela.addActionListener(e -> carregar());
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

        JPanel cartao = Ui.cartao(new BorderLayout(0, 10));
        JPanel topo = new JPanel(new BorderLayout(10, 0));
        topo.setOpaque(false);
        topo.add(busca, BorderLayout.CENTER);
        janela.setPreferredSize(new Dimension(220, 30));
        topo.add(janela, BorderLayout.EAST);
        cartao.add(topo, BorderLayout.NORTH);
        cartao.add(Ui.rolagem(tabela), BorderLayout.CENTER);
        conteudo(cartao);
        carregar();
    }

    private void carregar() {
        try {
            int dias;
            switch (janela.getSelectedIndex()) {
                case 1:
                    dias = 0;
                    break;
                case 2:
                    dias = 60;
                    break;
                case 3:
                    dias = 90;
                    break;
                case 4:
                    dias = 3650;
                    break;
                default:
                    dias = 30;
            }
            boolean somenteVencidos = janela.getSelectedIndex() == 1;
            lista.clear();
            List<RegistroTreinamento> base = dias >= 3650 ? dao.listar(null, null)
                    : dao.vencimentosProximos(dias, 500);
            for (RegistroTreinamento r : base) {
                if (somenteVencidos && (r.getValidade() == null
                        || !r.getValidade().isBefore(LocalDate.now()))) {
                    continue;
                }
                lista.add(r);
            }
            DefaultTableModel modelo = (DefaultTableModel) tabela.getModel();
            modelo.setRowCount(0);
            for (RegistroTreinamento r : lista) {
                long restantes = r.getValidade() == null ? Long.MIN_VALUE
                        : ChronoUnit.DAYS.between(LocalDate.now(), r.getValidade());
                modelo.addRow(new Object[]{r.getColaboradorNome(),
                    r.getNorma() + " - " + r.getTreinamento(),
                    Datas.br(r.getData()),
                    r.getValidade() == null ? "Revisar PGR" : Datas.br(r.getValidade()),
                    r.getValidade() == null ? "-" : String.valueOf(restantes),
                    situacao(restantes, r)});
            }
        } catch (RuntimeException e) {
            Ui.erro(this, e);
        }
    }

    private String situacao(long dias, RegistroTreinamento r) {
        if (r.getValidade() == null) {
            return "Revisar PGR";
        }
        if (dias < 0) {
            return "Vencido";
        }
        if (dias <= 30) {
            return "Vence em " + dias + " d";
        }
        return "Válido";
    }

    private void filtrar() {
        String termo = Ui.texto(busca);
        ordenador.setRowFilter(termo.isEmpty() ? null
                : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(termo)));
    }
}
