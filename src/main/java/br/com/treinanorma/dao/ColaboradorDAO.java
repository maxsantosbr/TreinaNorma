package br.com.treinanorma.dao;

import br.com.treinanorma.model.Colaborador;
import br.com.treinanorma.util.Cpf;
import br.com.treinanorma.util.Datas;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ColaboradorDAO {

    private static final String BASE = "SELECT c.*, s.nome AS setor_nome, e.nome AS estab_nome "
            + "FROM colaborador c "
            + "LEFT JOIN setor s ON s.id = c.setor_id "
            + "LEFT JOIN estabelecimento e ON e.id = c.estabelecimento_id ";

    public List<Colaborador> listar(String termo, boolean somenteAtivos) {
        List<Colaborador> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE).append("WHERE 1=1 ");
        if (somenteAtivos) {
            sql.append("AND c.ativo = 1 ");
        }
        if (termo != null && !termo.isBlank()) {
            sql.append("AND (c.nome LIKE ? OR c.cpf LIKE ? OR c.matricula LIKE ? OR c.cargo LIKE ?) ");
        }
        sql.append("ORDER BY c.nome");
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            if (termo != null && !termo.isBlank()) {
                String like = "%" + termo.trim() + "%";
                for (int i = 1; i <= 4; i++) {
                    ps.setString(i, like);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(montar(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao listar colaboradores", e);
        }
        return lista;
    }

    public List<Colaborador> ativos() {
        return listar(null, true);
    }

    public Colaborador porId(int id) {
        String sql = BASE + "WHERE c.id = ?";
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? montar(rs) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao buscar colaborador", e);
        }
    }

    public Colaborador porCpf(String cpf) {
        String sql = BASE + "WHERE c.cpf = ?";
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, Cpf.limpar(cpf));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? montar(rs) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao buscar colaborador por CPF", e);
        }
    }

    public void salvar(Colaborador c) {
        if (c.getId() > 0) {
            atualizar(c);
        } else {
            inserir(c);
        }
    }

    private void inserir(Colaborador c) {
        String sql = "INSERT INTO colaborador (empresa_id, estabelecimento_id, setor_id, nome, cpf, "
                + "matricula, cargo, admissao, demissao, email, telefone, foto, ativo) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencher(ps, c);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    c.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException(mensagem(e), e);
        }
    }

    private void atualizar(Colaborador c) {
        String sql = "UPDATE colaborador SET empresa_id=?, estabelecimento_id=?, setor_id=?, nome=?, "
                + "cpf=?, matricula=?, cargo=?, admissao=?, demissao=?, email=?, telefone=?, foto=?, "
                + "ativo=? WHERE id=?";
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            preencher(ps, c);
            ps.setInt(14, c.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(mensagem(e), e);
        }
    }

    private String mensagem(SQLException e) {
        String texto = e.getMessage() == null ? "" : e.getMessage();
        if (texto.contains("colaborador.cpf")) {
            return "Já existe um colaborador com esse CPF.";
        }
        return "Erro ao salvar colaborador";
    }

    private void preencher(PreparedStatement ps, Colaborador c) throws SQLException {
        if (c.getEmpresaId() == null) {
            ps.setObject(1, null);
        } else {
            ps.setInt(1, c.getEmpresaId());
        }
        if (c.getEstabelecimentoId() == null) {
            ps.setObject(2, null);
        } else {
            ps.setInt(2, c.getEstabelecimentoId());
        }
        if (c.getSetorId() == null) {
            ps.setObject(3, null);
        } else {
            ps.setInt(3, c.getSetorId());
        }
        ps.setString(4, c.getNome());
        ps.setString(5, Cpf.limpar(c.getCpf()));
        ps.setString(6, c.getMatricula());
        ps.setString(7, c.getCargo());
        ps.setString(8, Datas.iso(c.getAdmissao()));
        ps.setString(9, Datas.iso(c.getDemissao()));
        ps.setString(10, c.getEmail());
        ps.setString(11, c.getTelefone());
        ps.setString(12, c.getFoto());
        ps.setInt(13, c.isAtivo() ? 1 : 0);
    }

    public void excluir(int id) {
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement("DELETE FROM colaborador WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao excluir colaborador", e);
        }
    }

    public int contarAtivos() {
        try (Connection con = Conexao.abrir();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM colaborador WHERE ativo = 1")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao contar colaboradores", e);
        }
    }

    public int contar() {
        try (Connection con = Conexao.abrir();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM colaborador")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao contar colaboradores", e);
        }
    }

    private Colaborador montar(ResultSet rs) throws SQLException {
        Colaborador c = new Colaborador();
        c.setId(rs.getInt("id"));
        int empresa = rs.getInt("empresa_id");
        c.setEmpresaId(rs.wasNull() ? null : empresa);
        int estab = rs.getInt("estabelecimento_id");
        c.setEstabelecimentoId(rs.wasNull() ? null : estab);
        int setor = rs.getInt("setor_id");
        c.setSetorId(rs.wasNull() ? null : setor);
        c.setSetorNome(rs.getString("setor_nome"));
        c.setEstabelecimentoNome(rs.getString("estab_nome"));
        c.setNome(rs.getString("nome"));
        c.setCpf(rs.getString("cpf"));
        c.setMatricula(rs.getString("matricula"));
        c.setCargo(rs.getString("cargo"));
        c.setAdmissao(Datas.ler(rs.getString("admissao")));
        c.setDemissao(Datas.ler(rs.getString("demissao")));
        c.setEmail(rs.getString("email"));
        c.setTelefone(rs.getString("telefone"));
        c.setFoto(rs.getString("foto"));
        c.setAtivo(rs.getInt("ativo") == 1);
        return c;
    }
}
