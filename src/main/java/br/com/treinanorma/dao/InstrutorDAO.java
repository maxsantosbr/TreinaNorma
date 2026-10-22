package br.com.treinanorma.dao;

import br.com.treinanorma.model.Instrutor;
import br.com.treinanorma.util.Cpf;
import br.com.treinanorma.util.Datas;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class InstrutorDAO {

    public List<Instrutor> listar(String termo) {
        List<Instrutor> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM instrutor WHERE 1=1 ");
        if (termo != null && !termo.isBlank()) {
            sql.append("AND (nome LIKE ? OR cpf LIKE ? OR registro LIKE ?) ");
        }
        sql.append("ORDER BY nome");
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            if (termo != null && !termo.isBlank()) {
                String like = "%" + termo.trim() + "%";
                for (int i = 1; i <= 3; i++) {
                    ps.setString(i, like);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(montar(rs, con));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao listar instrutores", e);
        }
        return lista;
    }

    public Instrutor porId(int id) {
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM instrutor WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? montar(rs, con) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao buscar instrutor", e);
        }
    }

    public List<Instrutor> habilitadosPara(String norma) {
        List<Instrutor> lista = new ArrayList<>();
        String sql = "SELECT i.* FROM instrutor i "
                + "JOIN instrutor_habilitacao h ON h.instrutor_id = i.id "
                + "WHERE i.ativo = 1 AND h.norma = ? "
                + "AND (h.validade IS NULL OR h.validade = '' OR h.validade >= ?) ORDER BY i.nome";
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, norma);
            ps.setString(2, Datas.hojeIso());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(montar(rs, con));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao listar instrutores habilitados", e);
        }
        return lista;
    }

    public void salvar(Instrutor i) {
        int id = i.getId();
        if (id > 0) {
            atualizar(i);
        } else {
            id = inserir(i);
            i.setId(id);
        }
        salvarHabilitacoes(i);
    }

    private int inserir(Instrutor i) {
        String sql = "INSERT INTO instrutor (nome, cpf, formacao, registro, tipo, email, telefone, "
                + "assinatura, ativo) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencher(ps, i);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException(mensagem(e), e);
        }
    }

    private void atualizar(Instrutor i) {
        String sql = "UPDATE instrutor SET nome=?, cpf=?, formacao=?, registro=?, tipo=?, email=?, "
                + "telefone=?, assinatura=?, ativo=? WHERE id=?";
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            preencher(ps, i);
            ps.setInt(10, i.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(mensagem(e), e);
        }
    }

    private String mensagem(SQLException e) {
        String texto = e.getMessage() == null ? "" : e.getMessage();
        if (texto.contains("instrutor.cpf")) {
            return "Já existe um instrutor com esse CPF.";
        }
        return "Erro ao salvar instrutor";
    }

    private void preencher(PreparedStatement ps, Instrutor i) throws SQLException {
        ps.setString(1, i.getNome());
        String cpf = Cpf.limpar(i.getCpf());
        ps.setString(2, cpf.isEmpty() ? null : cpf);
        ps.setString(3, i.getFormacao());
        ps.setString(4, i.getRegistro());
        ps.setString(5, i.getTipo());
        ps.setString(6, i.getEmail());
        ps.setString(7, i.getTelefone());
        ps.setString(8, i.getAssinatura());
        ps.setInt(9, i.isAtivo() ? 1 : 0);
    }

    private void salvarHabilitacoes(Instrutor i) {
        try (Connection con = Conexao.abrir()) {
            try (PreparedStatement del = con.prepareStatement(
                    "DELETE FROM instrutor_habilitacao WHERE instrutor_id = ?")) {
                del.setInt(1, i.getId());
                del.executeUpdate();
            }
            try (PreparedStatement ins = con.prepareStatement(
                    "INSERT INTO instrutor_habilitacao (instrutor_id, norma, validade) VALUES (?,?,?)")) {
                for (Instrutor.Habilitacao h : i.getHabilitacoes()) {
                    ins.setInt(1, i.getId());
                    ins.setString(2, h.getNorma());
                    ins.setString(3, Datas.iso(h.getValidade()));
                    ins.addBatch();
                }
                ins.executeBatch();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao salvar habilitações do instrutor", e);
        }
    }

    public void excluir(int id) {
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement("DELETE FROM instrutor WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao excluir instrutor", e);
        }
    }

    private Instrutor montar(ResultSet rs, Connection con) throws SQLException {
        Instrutor i = new Instrutor();
        i.setId(rs.getInt("id"));
        i.setNome(rs.getString("nome"));
        i.setCpf(rs.getString("cpf"));
        i.setFormacao(rs.getString("formacao"));
        i.setRegistro(rs.getString("registro"));
        i.setTipo(rs.getString("tipo"));
        i.setEmail(rs.getString("email"));
        i.setTelefone(rs.getString("telefone"));
        i.setAssinatura(rs.getString("assinatura"));
        i.setAtivo(rs.getInt("ativo") == 1);
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT norma, validade FROM instrutor_habilitacao WHERE instrutor_id = ? ORDER BY norma")) {
            ps.setInt(1, i.getId());
            try (ResultSet r = ps.executeQuery()) {
                while (r.next()) {
                    i.getHabilitacoes().add(new Instrutor.Habilitacao(
                            r.getString("norma"), Datas.ler(r.getString("validade"))));
                }
            }
        }
        return i;
    }
}
