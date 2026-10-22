package br.com.treinanorma.view;

import br.com.treinanorma.dao.UsuarioDAO;
import br.com.treinanorma.model.Perfil;
import br.com.treinanorma.model.Usuario;
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
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class UsuariosPanel extends TelaBase {

    private final UsuarioDAO dao = new UsuarioDAO();
    private final JTextField busca = Ui.campo("Buscar por nome ou login...");
    private final JTable tabela = Ui.tabela("Nome", "Login", "Perfil", "Status", "Troca pendente");
    private final TableRowSorter<DefaultTableModel> ordenador;
    private final List<Usuario> lista = new ArrayList<>();

    public UsuariosPanel(Runnable voltar) {
        super("Usuários", "Somente administradores gerenciam acessos e perfis.", voltar);
        ordenador = new TableRowSorter<>((DefaultTableModel) tabela.getModel());
        tabela.setRowSorter(ordenador);
        acao(Ui.primario("+ Novo usuário")).addActionListener(e -> editar(null));
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

    private void editar(Usuario usuario) {
        Usuario alvo = usuario == null ? new Usuario() : usuario;
        JDialog d = Ui.dialogo(this, usuario == null ? "Novo usuário" : "Editar usuário", 480, 380);
        JTextField nome = Ui.campo();
        JTextField login = Ui.campo();
        javax.swing.JComboBox<String> perfil = Ui.combo("ADMIN", "INSTRUTOR", "CONSULTA");
        JPasswordField senha = Ui.senha(usuario == null ? "mínimo 6 caracteres" : "deixe vazio para manter");
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
        trocar.setSelected(usuario != null && alvo.isDeveTrocarSenha());

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

        JPanel corpo = new JPanel(new BorderLayout());
        corpo.setOpaque(false);
        corpo.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 14, 4, 14));
        corpo.add(form, BorderLayout.NORTH);

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
                if (usuario == null && nova.length() < 6) {
                    Ui.aviso(d, "A senha deve ter ao menos 6 caracteres.");
                    return;
                }
                Usuario atual = Sessao.getUsuario();
                if (usuario != null && atual != null && alvo.getId() == atual.getId()
                        && !alvo.isAtivo()) {
                    Ui.aviso(d, "Você não pode desativar o próprio usuário.");
                    return;
                }
                dao.salvar(alvo, nova);
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
