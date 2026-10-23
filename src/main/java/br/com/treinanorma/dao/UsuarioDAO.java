package br.com.treinanorma.dao;

import br.com.treinanorma.model.Perfil;
import br.com.treinanorma.model.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

public class UsuarioDAO {

    public Usuario autenticar(String usuario, String senha) {
        String sql = "SELECT id, nome, usuario, senha_hash, perfil, deve_trocar_senha "
                + "FROM usuarios WHERE usuario = ? AND ativo = 1";

        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                String hash = rs.getString("senha_hash");
                if (!BCrypt.checkpw(senha, hash)) {
                    return null;
                }
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setNome(rs.getString("nome"));
                u.setUsuario(rs.getString("usuario"));
                u.setPerfil(Perfil.valueOf(rs.getString("perfil")));
                u.setDeveTrocarSenha(rs.getInt("deve_trocar_senha") == 1);
                return u;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao autenticar", e);
        }
    }

    public Usuario porLogin(String login) {
        String sql = "SELECT id, nome, usuario, perfil, ativo, deve_trocar_senha "
                + "FROM usuarios WHERE usuario = ? COLLATE NOCASE";
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, login == null ? "" : login.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setNome(rs.getString("nome"));
                u.setUsuario(rs.getString("usuario"));
                u.setPerfil(Perfil.valueOf(rs.getString("perfil")));
                u.setAtivo(rs.getInt("ativo") == 1);
                u.setDeveTrocarSenha(rs.getInt("deve_trocar_senha") == 1);
                return u;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao buscar usuário", e);
        }
    }

    public List<Usuario> listar(String termo) {
        List<Usuario> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM usuarios WHERE 1=1 ");
        if (termo != null && !termo.isBlank()) {
            sql.append("AND (nome LIKE ? OR usuario LIKE ?) ");
        }
        sql.append("ORDER BY nome");
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            if (termo != null && !termo.isBlank()) {
                String like = "%" + termo.trim() + "%";
                ps.setString(1, like);
                ps.setString(2, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Usuario u = new Usuario();
                    u.setId(rs.getInt("id"));
                    u.setNome(rs.getString("nome"));
                    u.setUsuario(rs.getString("usuario"));
                    u.setPerfil(Perfil.valueOf(rs.getString("perfil")));
                    u.setAtivo(rs.getInt("ativo") == 1);
                    u.setDeveTrocarSenha(rs.getInt("deve_trocar_senha") == 1);
                    lista.add(u);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao listar usuários", e);
        }
        return lista;
    }

    public void salvar(Usuario u, String senha) {
        if (u.getId() > 0) {
            atualizar(u, senha);
        } else {
            inserir(u, senha);
        }
    }

    private void inserir(Usuario u, String senha) {
        if (senha == null || senha.length() < 6) {
            throw new IllegalArgumentException("A senha deve ter ao menos 6 caracteres.");
        }
        String sql = "INSERT INTO usuarios (nome, usuario, senha_hash, perfil, ativo, deve_trocar_senha) "
                + "VALUES (?,?,?,?,?,?)";
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNome());
            ps.setString(2, u.getUsuario());
            ps.setString(3, BCrypt.hashpw(senha, BCrypt.gensalt(12)));
            ps.setString(4, u.getPerfil().name());
            ps.setInt(5, u.isAtivo() ? 1 : 0);
            ps.setInt(6, u.isDeveTrocarSenha() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    u.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException(mensagem(e), e);
        }
    }

    private void atualizar(Usuario u, String senha) {
        boolean troca = senha != null && !senha.isBlank();
        String sql = "UPDATE usuarios SET nome=?, usuario=?, perfil=?, ativo=?, deve_trocar_senha=?"
                + (troca ? ", senha_hash=?" : "") + " WHERE id=?";
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, u.getNome());
            ps.setString(i++, u.getUsuario());
            ps.setString(i++, u.getPerfil().name());
            ps.setInt(i++, u.isAtivo() ? 1 : 0);
            ps.setInt(i++, u.isDeveTrocarSenha() ? 1 : 0);
            if (troca) {
                ps.setString(i++, BCrypt.hashpw(senha, BCrypt.gensalt(12)));
            }
            ps.setInt(i, u.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(mensagem(e), e);
        }
    }

    private String mensagem(SQLException e) {
        String texto = e.getMessage() == null ? "" : e.getMessage();
        if (texto.contains("usuarios.usuario")) {
            return "Já existe um usuário com esse login.";
        }
        return "Erro ao salvar usuário";
    }

    public void trocarSenha(int id, String nova) {
        if (nova == null || nova.length() < 6) {
            throw new IllegalArgumentException("A senha deve ter ao menos 6 caracteres.");
        }
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE usuarios SET senha_hash = ?, deve_trocar_senha = 0 WHERE id = ?")) {
            ps.setString(1, BCrypt.hashpw(nova, BCrypt.gensalt(12)));
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao trocar senha", e);
        }
    }

    public void excluir(int id) {
        try (Connection con = Conexao.abrir()) {
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) FROM usuarios WHERE perfil = 'ADMIN' AND ativo = 1 AND id <> ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        throw new IllegalStateException("É necessário manter ao menos um administrador ativo.");
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM usuarios WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao excluir usuário", e);
        }
    }
}
