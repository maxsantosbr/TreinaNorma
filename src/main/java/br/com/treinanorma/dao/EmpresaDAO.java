package br.com.treinanorma.dao;

import br.com.treinanorma.model.Empresa;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class EmpresaDAO {

    public Empresa obter() {
        String sql = "SELECT * FROM empresa ORDER BY id LIMIT 1";
        try (Connection con = Conexao.abrir();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? montar(rs) : null;
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao carregar empresa", e);
        }
    }

    public void salvar(Empresa e) {
        if (e.getId() > 0) {
            atualizar(e);
        } else {
            inserir(e);
        }
        new AuditoriaDAO().registrar("EMPRESA", "Dados da empresa atualizados: " + e.getRazaoSocial());
    }

    private void inserir(Empresa e) {
        String sql = "INSERT INTO empresa (razao_social, nome_fantasia, cnpj, cnae, grau_risco, "
                + "endereco, numero, complemento, bairro, cidade, uf, cep, telefone, email, logo, "
                + "assinatura, responsavel_nome, responsavel_registro, responsavel_profissao) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencher(ps, e);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    e.setId(rs.getInt(1));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao salvar empresa", ex);
        }
    }

    private void atualizar(Empresa e) {
        String sql = "UPDATE empresa SET razao_social=?, nome_fantasia=?, cnpj=?, cnae=?, grau_risco=?, "
                + "endereco=?, numero=?, complemento=?, bairro=?, cidade=?, uf=?, cep=?, telefone=?, "
                + "email=?, logo=?, assinatura=?, responsavel_nome=?, responsavel_registro=?, "
                + "responsavel_profissao=? WHERE id=?";
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            preencher(ps, e);
            ps.setInt(20, e.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Erro ao atualizar empresa", ex);
        }
    }

    private void preencher(PreparedStatement ps, Empresa e) throws SQLException {
        ps.setString(1, e.getRazaoSocial());
        ps.setString(2, e.getNomeFantasia());
        ps.setString(3, e.getCnpj());
        ps.setString(4, e.getCnae());
        if (e.getGrauRisco() == null) {
            ps.setObject(5, null);
        } else {
            ps.setInt(5, e.getGrauRisco());
        }
        ps.setString(6, e.getEndereco());
        ps.setString(7, e.getNumero());
        ps.setString(8, e.getComplemento());
        ps.setString(9, e.getBairro());
        ps.setString(10, e.getCidade());
        ps.setString(11, e.getUf());
        ps.setString(12, e.getCep());
        ps.setString(13, e.getTelefone());
        ps.setString(14, e.getEmail());
        ps.setString(15, e.getLogo());
        ps.setString(16, e.getAssinatura());
        ps.setString(17, e.getResponsavelNome());
        ps.setString(18, e.getResponsavelRegistro());
        ps.setString(19, e.getResponsavelProfissao());
    }

    private Empresa montar(ResultSet rs) throws SQLException {
        Empresa e = new Empresa();
        e.setId(rs.getInt("id"));
        e.setRazaoSocial(rs.getString("razao_social"));
        e.setNomeFantasia(rs.getString("nome_fantasia"));
        e.setCnpj(rs.getString("cnpj"));
        e.setCnae(rs.getString("cnae"));
        int gr = rs.getInt("grau_risco");
        e.setGrauRisco(rs.wasNull() ? null : gr);
        e.setEndereco(rs.getString("endereco"));
        e.setNumero(rs.getString("numero"));
        e.setComplemento(rs.getString("complemento"));
        e.setBairro(rs.getString("bairro"));
        e.setCidade(rs.getString("cidade"));
        e.setUf(rs.getString("uf"));
        e.setCep(rs.getString("cep"));
        e.setTelefone(rs.getString("telefone"));
        e.setEmail(rs.getString("email"));
        e.setLogo(rs.getString("logo"));
        e.setAssinatura(rs.getString("assinatura"));
        e.setResponsavelNome(rs.getString("responsavel_nome"));
        e.setResponsavelRegistro(rs.getString("responsavel_registro"));
        e.setResponsavelProfissao(rs.getString("responsavel_profissao"));
        return e;
    }
}
