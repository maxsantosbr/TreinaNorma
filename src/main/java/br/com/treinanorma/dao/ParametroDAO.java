package br.com.treinanorma.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ParametroDAO {

    public Map<String, String> todos() {
        Map<String, String> mapa = new LinkedHashMap<>();
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT chave, valor FROM parametro ORDER BY chave");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                mapa.put(rs.getString("chave"), rs.getString("valor"));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao ler parâmetros", e);
        }
        return mapa;
    }

    public void salvar(Map<String, String> mapa) {
        try (Connection con = Conexao.abrir()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO parametro (chave, valor) VALUES (?,?) "
                    + "ON CONFLICT(chave) DO UPDATE SET valor = excluded.valor")) {
                for (Map.Entry<String, String> e : mapa.entrySet()) {
                    ps.setString(1, e.getKey());
                    ps.setString(2, e.getValue());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            con.commit();
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao salvar parâmetros", e);
        }
    }
}
