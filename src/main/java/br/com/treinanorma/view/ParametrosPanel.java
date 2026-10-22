package br.com.treinanorma.view;

import br.com.treinanorma.dao.ParametroDAO;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class ParametrosPanel extends TelaBase {

    private static final String[][] PADROES = {
        {"alerta_vencimento_dias", "30"},
        {"cidade_emissao", ""},
        {"texto_rodape_certificado", "Documento gerado pelo TreinaNorma."},
        {"backup_automatico", "nao"},
        {"pasta_documentos", ""},};

    private final ParametroDAO dao = new ParametroDAO();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Parâmetro", "Valor"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 1;
        }
    };
    private final JTable tabela = Ui.tabela("Parâmetro", "Valor");

    public ParametrosPanel(Runnable voltar) {
        super("Parâmetros do sistema", "Ajustes aplicados globalmente. Use com cautela.", voltar);
        tabela.setModel(modelo);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(240);
        acao(Ui.secundario("Recarregar")).addActionListener(e -> carregar());
        acao(Ui.primario("Salvar")).addActionListener(e -> salvar());
    }

    @Override
    protected void construir() {
        JPanel cartao = Ui.cartao(new BorderLayout(0, 8));
        cartao.add(Ui.subtitulo("Clique no valor para editar. Parâmetros desconhecidos são preservados."),
                BorderLayout.NORTH);
        cartao.add(Ui.rolagem(tabela), BorderLayout.CENTER);

        JTextField novaChave = Ui.campo("nova_chave");
        JTextField novoValor = Ui.campo("valor");
        novaChave.setPreferredSize(new Dimension(200, 28));
        novoValor.setPreferredSize(new Dimension(240, 28));
        JButton adicionar = Ui.secundario("Adicionar parâmetro");
        adicionar.addActionListener(e -> {
            String chave = Ui.texto(novaChave);
            if (chave.isEmpty()) {
                Ui.aviso(this, "Informe o nome do parâmetro.");
                return;
            }
            modelo.addRow(new Object[]{chave, Ui.texto(novoValor)});
            novaChave.setText("");
            novoValor.setText("");
        });
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        rodape.setOpaque(false);
        rodape.add(new JLabel("Novo:"));
        rodape.add(novaChave);
        rodape.add(novoValor);
        rodape.add(adicionar);
        cartao.add(rodape, BorderLayout.SOUTH);

        conteudo(cartao);
        carregar();
    }

    private void carregar() {
        try {
            Map<String, String> valores = dao.todos();
            for (String[] padrao : PADROES) {
                valores.putIfAbsent(padrao[0], padrao[1]);
            }
            modelo.setRowCount(0);
            for (Map.Entry<String, String> e : valores.entrySet()) {
                modelo.addRow(new Object[]{e.getKey(), e.getValue()});
            }
        } catch (RuntimeException e) {
            Ui.erro(this, e);
        }
    }

    private void salvar() {
        try {
            if (tabela.isEditing()) {
                tabela.getCellEditor().stopCellEditing();
            }
            Map<String, String> valores = new LinkedHashMap<>();
            for (int i = 0; i < modelo.getRowCount(); i++) {
                Object chave = modelo.getValueAt(i, 0);
                Object valor = modelo.getValueAt(i, 1);
                if (chave == null || chave.toString().isBlank()) {
                    continue;
                }
                valores.put(chave.toString().trim(), valor == null ? "" : valor.toString().trim());
            }
            dao.salvar(valores);
            Ui.info(this, "Parâmetros salvos.");
            carregar();
        } catch (RuntimeException e) {
            Ui.erro(this, e);
        }
    }
}
