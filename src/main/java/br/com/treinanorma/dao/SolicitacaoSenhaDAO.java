package br.com.treinanorma.dao;

import br.com.treinanorma.model.SolicitacaoSenha;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SolicitacaoSenhaDAO {

    public String registrar(String login) {
        String texto = login == null ? "" : login.trim();
        if (texto.isEmpty()) {
            throw new IllegalArgumentException("Informe o login.");
        }
        try (Connection con = Conexao.abrir()) {
            con.setAutoCommit(false);
            try {
                String existente = pendente(con, texto);
                if (existente != null) {
                    con.commit();
                    return existente;
                }

                Integer usuarioId = usuarioId(con, texto);
                int id;
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO solicitacao_senha (usuario_id, login, status) VALUES (?, ?, 'PENDENTE')",
                        Statement.RETURN_GENERATED_KEYS)) {
                    if (usuarioId == null) {
                        ps.setNull(1, Types.INTEGER);
                    } else {
                        ps.setInt(1, usuarioId);
                    }
                    ps.setString(2, texto);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new IllegalStateException("Não foi possível gerar o protocolo.");
                        }
                        id = rs.getInt(1);
                    }
                }

                String protocolo = "RS-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                        + "-" + String.format("%06d", id);
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE solicitacao_senha SET protocolo = ? WHERE id = ?")) {
                    ps.setString(1, protocolo);
                    ps.setInt(2, id);
                    ps.executeUpdate();
                }
                con.commit();
                return protocolo;
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao registrar solicitação de senha", e);
        }
    }

    private String pendente(Connection con, String login) throws SQLException {
        String sql = "SELECT protocolo FROM solicitacao_senha "
                + "WHERE login = ? COLLATE NOCASE AND status = 'PENDENTE' "
                + "ORDER BY id DESC LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    private Integer usuarioId(Connection con, String login) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT id FROM usuarios WHERE usuario = ? COLLATE NOCASE")) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    public List<SolicitacaoSenha> listar() {
        List<SolicitacaoSenha> lista = new ArrayList<>();
        String sql = "SELECT s.id, s.protocolo, s.usuario_id, s.login, s.status, s.criado_em, "
                + "s.atendido_por, u.nome AS atendido_por_nome, s.atendido_em "
                + "FROM solicitacao_senha s "
                + "LEFT JOIN usuarios u ON u.id = s.atendido_por "
                + "ORDER BY CASE s.status WHEN 'PENDENTE' THEN 0 ELSE 1 END, "
                + "s.criado_em DESC, s.id DESC";
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                SolicitacaoSenha s = new SolicitacaoSenha();
                s.setId(rs.getInt("id"));
                s.setProtocolo(rs.getString("protocolo"));
                int usuarioId = rs.getInt("usuario_id");
                if (!rs.wasNull()) {
                    s.setUsuarioId(usuarioId);
                }
                s.setLogin(rs.getString("login"));
                s.setStatus(rs.getString("status"));
                s.setCriadoEm(rs.getString("criado_em"));
                int atendidoPor = rs.getInt("atendido_por");
                if (!rs.wasNull()) {
                    s.setAtendidoPor(atendidoPor);
                }
                s.setAtendidoPorNome(rs.getString("atendido_por_nome"));
                s.setAtendidoEm(rs.getString("atendido_em"));
                lista.add(s);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao listar solicitações de senha", e);
        }
        return lista;
    }

    public int pendentes() {
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT COUNT(*) FROM solicitacao_senha WHERE status = 'PENDENTE'");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao contar solicitações de senha", e);
        }
    }

    public boolean atender(int id, Integer adminId) {
        return encerrar(id, adminId, "ATENDIDA");
    }

    public boolean cancelar(int id, Integer adminId) {
        return encerrar(id, adminId, "CANCELADA");
    }

    private boolean encerrar(int id, Integer adminId, String status) {
        String sql = "UPDATE solicitacao_senha SET status = ?, atendido_por = ?, "
                + "atendido_em = CURRENT_TIMESTAMP WHERE id = ? AND status = 'PENDENTE'";
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            if (adminId == null) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, adminId);
            }
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao encerrar solicitação de senha", e);
        }
    }
}
