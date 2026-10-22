package br.com.treinanorma.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SetorDAO {

    public List<String> listar() {
        List<String> lista = new ArrayList<>();
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement("SELECT nome FROM setor ORDER BY nome");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(rs.getString(1));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao listar setores", e);
        }
        return lista;
    }

    public Integer garantir(String nome) {
        if (nome == null || nome.isBlank()) {
            return null;
        }
        String limpo = nome.trim();
        try (Connection con = Conexao.abrir()) {
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT id FROM setor WHERE UPPER(nome) = UPPER(?) LIMIT 1")) {
                ps.setString(1, limpo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO setor (nome) VALUES (?)", java.sql.Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, limpo);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    return rs.next() ? rs.getInt(1) : null;
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao registrar setor", e);
        }
    }
}
