package br.com.treinanorma.dao;

import br.com.treinanorma.util.Sessao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AuditoriaDAO {

    public void registrar(String acao, String detalhe) {
        String usuario = "sistema";
        try {
            if (Sessao.getUsuario() != null) {
                usuario = Sessao.getUsuario().getUsuario();
            }
        } catch (RuntimeException e) {
            usuario = "sistema";
        }
        String sql = "INSERT INTO auditoria (usuario, acao, detalhe) VALUES (?,?,?)";
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, acao);
            ps.setString(3, detalhe);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Falha ao registrar auditoria: " + e.getMessage());
        }
    }

    public List<String[]> listar(int limite) {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT criado_em, usuario, acao, detalhe FROM auditoria ORDER BY id DESC LIMIT ?";
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new String[]{rs.getString(1), rs.getString(2),
                        rs.getString(3), rs.getString(4)});
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao listar auditoria", e);
        }
        return lista;
    }

    public int total() {
        try (Connection con = Conexao.abrir();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM auditoria")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }
}
