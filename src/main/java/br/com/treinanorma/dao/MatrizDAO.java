package br.com.treinanorma.dao;

import br.com.treinanorma.model.MatrizItem;
import br.com.treinanorma.model.Norma;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MatrizDAO {

    public List<Norma> normas() {
        List<Norma> lista = new ArrayList<>();
        String sql = "SELECT * FROM norma ORDER BY codigo";
        try (Connection con = Conexao.abrir();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Norma n = new Norma();
                n.setCodigo(rs.getString("codigo"));
                n.setTitulo(rs.getString("titulo"));
                n.setTipo(rs.getString("tipo"));
                n.setSituacao(rs.getString("situacao"));
                n.setExigeTreinamento(rs.getString("exige_treinamento"));
                n.setObservacao(rs.getString("observacao"));
                lista.add(n);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao listar normas", e);
        }
        return lista;
    }

    public List<MatrizItem> itens() {
        List<MatrizItem> lista = new ArrayList<>();
        String sql = "SELECT * FROM matriz_treinamento ORDER BY norma, treinamento, criterio_valor";
        try (Connection con = Conexao.abrir();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(montar(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao listar matriz", e);
        }
        return lista;
    }

    public List<MatrizItem> itensDaNorma(String norma) {
        List<MatrizItem> lista = new ArrayList<>();
        String sql = "SELECT * FROM matriz_treinamento WHERE norma = ? ORDER BY treinamento, criterio_valor";
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, norma);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(montar(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao listar matriz da norma", e);
        }
        return lista;
    }

    public MatrizItem porId(int id) {
        String sql = "SELECT * FROM matriz_treinamento WHERE id = ?";
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? montar(rs) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao buscar item da matriz", e);
        }
    }

    public int semChFixa() {
        try (Connection con = Conexao.abrir();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT COUNT(*) FROM matriz_treinamento WHERE ch_minima IS NULL OR ch_minima <= 0")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao contar matriz sem CH", e);
        }
    }

    public int totalItens() {
        try (Connection con = Conexao.abrir();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM matriz_treinamento")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao contar matriz", e);
        }
    }

    private MatrizItem montar(ResultSet rs) throws SQLException {
        MatrizItem m = new MatrizItem();
        m.setId(rs.getInt("id"));
        m.setNorma(rs.getString("norma"));
        m.setTreinamento(rs.getString("treinamento"));
        m.setCriterioTipo(rs.getString("criterio_tipo"));
        m.setCriterioValor(rs.getString("criterio_valor"));
        double ch = rs.getDouble("ch_minima");
        m.setChMinima(rs.wasNull() ? null : ch);
        double pres = rs.getDouble("presencial_minimo");
        m.setPresencialMinimo(rs.wasNull() ? null : pres);
        int val = rs.getInt("validade_meses");
        m.setValidadeMeses(rs.wasNull() ? null : val);
        m.setRecorrencia(rs.getString("recorrencia"));
        m.setModalidade(rs.getString("modalidade"));
        m.setChFixada(rs.getString("ch_fixada"));
        m.setBaseLegal(rs.getString("base_legal"));
        m.setObservacao(rs.getString("observacao"));
        return m;
    }
}
