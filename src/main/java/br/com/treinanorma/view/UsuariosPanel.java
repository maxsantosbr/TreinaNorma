package br.com.treinanorma.view;

import br.com.treinanorma.dao.SolicitacaoSenhaDAO;
import br.com.treinanorma.dao.UsuarioDAO;
import br.com.treinanorma.model.Perfil;
import br.com.treinanorma.model.SolicitacaoSenha;
import br.com.treinanorma.model.Usuario;
import br.com.treinanorma.util.Datas;
import br.com.treinanorma.util.Sessao;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class UsuariosPanel extends TelaBase {

    private final UsuarioDAO dao = new UsuarioDAO();
    private final SolicitacaoSenhaDAO solicitacaoDAO = new SolicitacaoSenhaDAO();
    private final JTextField busca = Ui.campo("Buscar por nome ou login...");
    private final JTable tabela = Ui.tabela("Nome", "Login", "Perfil", "Status", "Troca pendente");
    private final TableRowSorter<DefaultTableModel> ordenador;
    private final List<Usuario> lista = new ArrayList<>();
    private final JButton btnSolicitacoes = Ui.secundario("Solicitações");

    public UsuariosPanel(Runnable voltar) {
        super("Usuários", "Somente administradores gerenciam acessos e perfis.", voltar);
        ordenador = new TableRowSorter<>((DefaultTableModel) tabela.getModel());
        tabela.setRowSorter(ordenador);
        acao(Ui.primario("+ Novo usuário")).addActionListener(e -> editar(null));
        btnSolicitacoes.addActionListener(e -> abrirSolicitacoes());
        acao(btnSolicitacoes);
    }

    @Override
    protected void construir() {
        JPanel cartao = Ui.cartao(new BorderLayout(0, 10));
        busca.setPreferredSize(new Dimension(300, 30));
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
        cartao.add(Ui.linhaEsquerda(busca), BorderLayout.NORTH);
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
            for (Usuario u : lista) {
                modelo.addRow(new Object[]{u.getNome(), u.getUsuario(), u.getPerfil(),
                    u.isAtivo() ? "Ativo" : "Inativo", u.isDeveTrocarSenha() ? "Sim" : "Não"});
            }
            int pendentes = solicitacaoDAO.pendentes();
            btnSolicitacoes.setText(pendentes > 0
                    ? "Solicitações (" + pendentes + ")" : "Solicitações");
            btnSolicitacoes.setToolTipText(pendentes > 0
                    ? pendentes + " solicitação(ões) de senha pendente(s)"
                    : "Histórico de solicitações de senha");
        } catch (RuntimeException e) {
            Ui.erro(this, e);
        }
    }

    private void filtrar() {
        String termo = Ui.texto(busca);
        ordenador.setRowFilter(termo.isEmpty() ? null
                : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(termo)));
    }

    private Usuario selecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            Ui.aviso(this, "Selecione um usuário.");
            return null;
        }
        return lista.get(tabela.convertRowIndexToModel(linha));
    }

    private void abrirSolicitacoes() {
        JDialog d = Ui.dialogo(this, "Solicitações de senha", 940, 560);
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"Protocolo", "Login", "Solicitado em", "Status", "Atendido por", "Atendido em"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tabelaSol = Ui.tabela("Protocolo", "Login", "Solicitado em", "Status",
                "Atendido por", "Atendido em");
        tabelaSol.setModel(modelo);
        tabelaSol.getColumnModel().getColumn(0).setPreferredWidth(150);
        tabelaSol.getColumnModel().getColumn(1).setPreferredWidth(140);
        tabelaSol.getColumnModel().getColumn(2).setPreferredWidth(150);
        tabelaSol.getColumnModel().getColumn(3).setPreferredWidth(90);
        tabelaSol.getColumnModel().getColumn(4).setPreferredWidth(150);
        tabelaSol.getColumnModel().getColumn(5).setPreferredWidth(150);
        TableRowSorter<DefaultTableModel> ordenadorSol = new TableRowSorter<>(modelo);
        tabelaSol.setRowSorter(ordenadorSol);

        JComboBox<String> filtro = Ui.combo("Pendentes", "Todas", "Atendidas", "Canceladas");
        filtro.setPreferredSize(new Dimension(160, 30));
        JLabel resumo = new JLabel();
        resumo.setFont(Ui.fonte(11, Font.PLAIN));
        resumo.setForeground(Ui.SUAVE);

        List<SolicitacaoSenha> visiveis = new ArrayList<>();
        JButton redefinir = Ui.primario("Redefinir senha");
        JButton cancelar = Ui.perigo("Cancelar solicitação");
        JButton fechar = Ui.secundario("Fechar");

        Runnable atualizarBotoes = () -> {
            int linha = tabelaSol.getSelectedRow();
            boolean pendente = false;
            if (linha >= 0) {
                int indice = tabelaSol.convertRowIndexToModel(linha);
                if (indice >= 0 && indice < visiveis.size()) {
                    pendente = visiveis.get(indice).isPendente();
                }
            }
            redefinir.setEnabled(pendente);
            cancelar.setEnabled(pendente);
        };

        Runnable carregar = () -> {
            try {
                List<SolicitacaoSenha> todas = solicitacaoDAO.listar();
                String escolha = (String) filtro.getSelectedItem();
                if (escolha == null) {
                    escolha = "Todas";
                }
                visiveis.clear();
                modelo.setRowCount(0);
                for (SolicitacaoSenha s : todas) {
                    if ("Pendentes".equals(escolha) && !s.isPendente()) {
                        continue;
                    }
                    if ("Atendidas".equals(escolha) && !"ATENDIDA".equals(s.getStatus())) {
                        continue;
                    }
                    if ("Canceladas".equals(escolha) && !"CANCELADA".equals(s.getStatus())) {
                        continue;
                    }
                    visiveis.add(s);
                    modelo.addRow(new Object[]{s.getProtocolo(), s.getLogin(),
                        dataHora(s.getCriadoEm()), rotuloStatus(s.getStatus()),
                        nz(s.getAtendidoPorNome()), dataHora(s.getAtendidoEm())});
                }
                resumo.setText(visiveis.size() + " solicitação(ões) • "
                        + solicitacaoDAO.pendentes() + " pendente(s)");
                atualizarBotoes.run();
            } catch (RuntimeException ex) {
                Ui.erro(d, ex);
            }
        };

        filtro.addActionListener(e -> carregar.run());
        tabelaSol.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                atualizarBotoes.run();
            }
        });
        tabelaSol.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && redefinir.isEnabled()) {
                    redefinir.doClick();
                }
            }
        });

        redefinir.addActionListener(e -> {
            SolicitacaoSenha s = solicitacaoSelecionada(tabelaSol, visiveis);
            if (s == null) {
                return;
            }
            if (!s.isPendente()) {
                Ui.aviso(d, "Esta solicitação já foi encerrada.");
                return;
            }
            try {
                Usuario u = new UsuarioDAO().porLogin(s.getLogin());
                if (u == null) {
                    Ui.aviso(d, "O login " + s.getLogin()
                            + " não está cadastrado.\n\nCadastre o usuário ou cancele a solicitação.");
                    return;
                }
                editar(u, s);
                carregar();
                carregar.run();
            } catch (RuntimeException ex) {
                Ui.erro(d, ex);
            }
        });

        cancelar.addActionListener(e -> {
            SolicitacaoSenha s = solicitacaoSelecionada(tabelaSol, visiveis);
            if (s == null) {
                return;
            }
            if (!s.isPendente()) {
                Ui.aviso(d, "Esta solicitação já foi encerrada.");
                return;
            }
            if (!Ui.confirma(d, "Cancelar a solicitação " + s.getProtocolo()
                    + " do login " + s.getLogin() + "?")) {
                return;
            }
            try {
                Usuario admin = Sessao.getUsuario();
                solicitacaoDAO.cancelar(s.getId(), admin == null ? null : admin.getId());
                carregar();
                carregar.run();
            } catch (RuntimeException ex) {
                Ui.erro(d, ex);
            }
        });
        fechar.addActionListener(e -> d.dispose());

        JPanel topo = new JPanel(new BorderLayout(8, 0));
        topo.setOpaque(false);
        topo.add(filtro, BorderLayout.WEST);
        topo.add(resumo, BorderLayout.EAST);

        JPanel corpo = new JPanel(new BorderLayout(0, 8));
        corpo.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 14, 10, 14));
        corpo.add(topo, BorderLayout.NORTH);
        corpo.add(Ui.rolagem(tabelaSol), BorderLayout.CENTER);

        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        rodape.add(fechar);
        rodape.add(cancelar);
        rodape.add(redefinir);

        d.add(corpo, BorderLayout.CENTER);
        d.add(rodape, BorderLayout.SOUTH);
        filtro.setSelectedItem(solicitacaoDAO.pendentes() > 0 ? "Pendentes" : "Todas");
        carregar.run();
        atualizarBotoes.run();
        d.setVisible(true);
    }

    private SolicitacaoSenha solicitacaoSelecionada(JTable tabela, List<SolicitacaoSenha> visiveis) {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            Ui.aviso(this, "Selecione uma solicitação.");
            return null;
        }
        int indice = tabela.convertRowIndexToModel(linha);
        if (indice < 0 || indice >= visiveis.size()) {
            return null;
        }
        return visiveis.get(indice);
    }

    private String rotuloStatus(String status) {
        if ("PENDENTE".equals(status)) {
            return "Pendente";
        }
        if ("ATENDIDA".equals(status)) {
            return "Atendida";
        }
        if ("CANCELADA".equals(status)) {
            return "Cancelada";
        }
        return nz(status);
    }

    private String dataHora(String valor) {
        java.time.LocalDateTime data = Datas.lerDataHora(valor);
        return data == null ? nz(valor) : Datas.brHora(data);
    }

    private void editar(Usuario usuario) {
        editar(usuario, null);
    }

    private void editar(Usuario usuario, SolicitacaoSenha solicitacao) {
        Usuario alvo = usuario == null ? new Usuario() : usuario;
        String titulo = solicitacao == null
                ? (usuario == null ? "Novo usuário" : "Editar usuário")
                : "Redefinir senha - " + solicitacao.getProtocolo();
        JDialog d = Ui.dialogo(this, titulo, 480, solicitacao == null ? 380 : 450);
        JTextField nome = Ui.campo();
        JTextField login = Ui.campo();
        javax.swing.JComboBox<String> perfil = Ui.combo("ADMIN", "INSTRUTOR", "CONSULTA");
        JPasswordField senha = Ui.senha(solicitacao != null
                ? "senha temporária (mínimo 6 caracteres)"
                : (usuario == null ? "mínimo 6 caracteres" : "deixe vazio para manter"));
        JCheckBox ativo = new JCheckBox("Usuário ativo");
        JCheckBox trocar = new JCheckBox("Exigir troca de senha no próximo acesso");
        ativo.setOpaque(false);
        trocar.setOpaque(false);
        ativo.setFont(Ui.fonte(12, Font.PLAIN));
        trocar.setFont(Ui.fonte(12, Font.PLAIN));

        nome.setText(nz(alvo.getNome()));
        login.setText(nz(alvo.getUsuario()));
        if (alvo.getPerfil() != null) {
            perfil.setSelectedItem(alvo.getPerfil().name());
        }
        ativo.setSelected(usuario == null || alvo.isAtivo());
        trocar.setSelected(solicitacao != null
                || (usuario != null && alvo.isDeveTrocarSenha()));
        if (solicitacao != null) {
            trocar.setEnabled(false);
            trocar.setToolTipText("Obrigatório para solicitações de redefinição.");
        }

        JPanel form = Ui.formulario();
        GridBagConstraints gbc = Ui.gbc();
        Ui.campo(form, gbc, 0, 0, "Nome *", nome, 2);
        Ui.campo(form, gbc, 0, 1, "Login *", login, 1);
        Ui.campo(form, gbc, 1, 1, "Perfil", perfil, 1);
        Ui.campo(form, gbc, 0, 2, usuario == null ? "Senha *" : "Nova senha", senha, 2);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        form.add(ativo, gbc);
        gbc.gridy = 4;
        form.add(trocar, gbc);

        JPanel corpo = new JPanel(new BorderLayout(0, 8));
        corpo.setOpaque(false);
        corpo.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 14, 4, 14));
        if (solicitacao != null) {
            JTextArea aviso = new JTextArea(
                    "Defina uma senha temporária. O usuário deverá cadastrar uma nova senha "
                    + "no próximo acesso.");
            aviso.setEditable(false);
            aviso.setLineWrap(true);
            aviso.setWrapStyleWord(true);
            aviso.setRows(2);
            aviso.setOpaque(false);
            aviso.setFont(Ui.fonte(12, Font.PLAIN));
            aviso.setForeground(Ui.SUAVE);
            aviso.setBorder(null);
            corpo.add(aviso, BorderLayout.NORTH);
        }
        corpo.add(form, BorderLayout.CENTER);

        JButton salvar = Ui.primario("Salvar");
        JButton cancelar = Ui.secundario("Cancelar");
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        rodape.add(cancelar);
        rodape.add(salvar);

        salvar.addActionListener(e -> {
            try {
                if (Ui.texto(nome).isEmpty() || Ui.texto(login).isEmpty()) {
                    Ui.aviso(d, "Informe nome e login.");
                    return;
                }
                alvo.setNome(Ui.texto(nome));
                alvo.setUsuario(Ui.texto(login).toLowerCase());
                alvo.setPerfil(Perfil.valueOf((String) perfil.getSelectedItem()));
                alvo.setAtivo(ativo.isSelected());
                alvo.setDeveTrocarSenha(trocar.isSelected());
                String nova = new String(senha.getPassword());
                if ((usuario == null || solicitacao != null) && nova.length() < 6) {
                    Ui.aviso(d, solicitacao != null
                            ? "Defina a senha temporária com ao menos 6 caracteres."
                            : "A senha deve ter ao menos 6 caracteres.");
                    return;
                }
                Usuario atual = Sessao.getUsuario();
                if (usuario != null && atual != null && alvo.getId() == atual.getId()
                        && !alvo.isAtivo()) {
                    Ui.aviso(d, "Você não pode desativar o próprio usuário.");
                    return;
                }
                if (solicitacao != null) {
                    alvo.setDeveTrocarSenha(true);
                }
                dao.salvar(alvo, nova);
                if (solicitacao != null) {
                    solicitacaoDAO.atender(solicitacao.getId(),
                            atual == null ? null : atual.getId());
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
    }

    private void excluir(Usuario u) {
        if (u == null) {
            return;
        }
        Usuario atual = Sessao.getUsuario();
        if (atual != null && atual.getId() == u.getId()) {
            Ui.aviso(this, "Você não pode excluir o próprio usuário.");
            return;
        }
        if (!Ui.confirma(this, "Excluir o usuário " + u.getNome() + " (" + u.getUsuario() + ")?")) {
            return;
        }
        try {
            dao.excluir(u.getId());
            carregar();
        } catch (RuntimeException e) {
            Ui.erro(this, e);
        }
    }

    public static void trocarSenha(java.awt.Component pai) {
        Usuario atual = Sessao.getUsuario();
        if (atual == null) {
            Ui.aviso(pai, "Sessão encerrada.");
            return;
        }
        JDialog d = Ui.dialogo(pai, "Trocar senha", 420, 320);
        JPasswordField senhaAtual = Ui.senha();
        JPasswordField nova = Ui.senha("mínimo 6 caracteres");
        JPasswordField confirmar = Ui.senha();
        JPanel form = Ui.formulario();
        GridBagConstraints gbc = Ui.gbc();
        Ui.campo(form, gbc, 0, 0, "Senha atual", senhaAtual, 1);
        Ui.campo(form, gbc, 0, 1, "Nova senha", nova, 1);
        Ui.campo(form, gbc, 0, 2, "Confirmar nova senha", confirmar, 1);

        JPanel corpo = new JPanel(new BorderLayout());
        corpo.setOpaque(false);
        corpo.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 16, 6, 16));
        corpo.add(Ui.subtitulo("Usuário: " + atual.getUsuario()), BorderLayout.NORTH);
        corpo.add(form, BorderLayout.CENTER);

        JButton salvar = Ui.primario("Alterar senha");
        JButton cancelar = Ui.secundario("Cancelar");
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        rodape.add(cancelar);
        rodape.add(salvar);

        salvar.addActionListener(e -> {
            try {
                String senhaAtualTexto = new String(senhaAtual.getPassword());
                String novaTexto = new String(nova.getPassword());
                if (new UsuarioDAO().autenticar(atual.getUsuario(), senhaAtualTexto) == null) {
                    Ui.aviso(d, "Senha atual incorreta.");
                    return;
                }
                if (novaTexto.length() < 6) {
                    Ui.aviso(d, "A nova senha deve ter ao menos 6 caracteres.");
                    return;
                }
                if (!novaTexto.equals(new String(confirmar.getPassword()))) {
                    Ui.aviso(d, "A confirmação não confere.");
                    return;
                }
                if (novaTexto.equals(senhaAtualTexto)) {
                    Ui.aviso(d, "A nova senha deve ser diferente da atual.");
                    return;
                }
                new UsuarioDAO().trocarSenha(atual.getId(), novaTexto);
                atual.setDeveTrocarSenha(false);
                d.dispose();
                Ui.info(pai, "Senha alterada com sucesso.");
            } catch (RuntimeException ex) {
                Ui.erro(d, ex);
            }
        });
        cancelar.addActionListener(e -> d.dispose());

        d.add(corpo, BorderLayout.CENTER);
        d.add(rodape, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private String nz(String valor) {
        return valor == null ? "" : valor;
    }
}
