package br.com.treinanorma.dao;

import br.com.treinanorma.model.RegistroTreinamento;
import br.com.treinanorma.util.Datas;
import br.com.treinanorma.util.Hash;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RegistroDAO {

    private static final String BASE = "SELECT r.*, c.nome AS colaborador_nome, c.cpf AS colaborador_cpf, "
            + "c.cargo AS colaborador_cargo, i.nome AS instrutor_nome, "
            + "cert.numero AS numero_certificado, cert.hash AS hash_certificado, "
            + "cert.emitido_em AS emitido_em "
            + "FROM registro_treinamento r "
            + "JOIN colaborador c ON c.id = r.colaborador_id "
            + "LEFT JOIN instrutor i ON i.id = r.instrutor_id "
            + "LEFT JOIN certificado cert ON cert.registro_id = r.id ";

    public List<RegistroTreinamento> listar(String termo, String filtro) {
        List<RegistroTreinamento> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE).append("WHERE 1=1 ");
        if (termo != null && !termo.isBlank()) {
            sql.append("AND (c.nome LIKE ? OR c.cpf LIKE ? OR r.norma LIKE ? OR r.treinamento LIKE ?) ");
        }
        if ("VENCIDOS".equals(filtro)) {
            sql.append("AND r.validade IS NOT NULL AND r.validade < ? ");
        } else if ("VENCE30".equals(filtro)) {
            sql.append("AND r.validade IS NOT NULL AND r.validade BETWEEN ? AND ? ");
        } else if ("SEM_VALIDADE".equals(filtro)) {
            sql.append("AND r.validade IS NULL ");
        } else if ("CERTIFICADOS".equals(filtro)) {
            sql.append("AND cert.id IS NOT NULL ");
        }
        sql.append("ORDER BY r.data DESC, r.id DESC");
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int i = 1;
            if (termo != null && !termo.isBlank()) {
                String like = "%" + termo.trim() + "%";
                for (int k = 0; k < 4; k++) {
                    ps.setString(i++, like);
                }
            }
            String hoje = Datas.hojeIso();
            if ("VENCIDOS".equals(filtro)) {
                ps.setString(i++, hoje);
            } else if ("VENCE30".equals(filtro)) {
                ps.setString(i++, hoje);
                ps.setString(i++, LocalDate.now().plusDays(31).toString());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(montar(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao listar registros", e);
        }
        return lista;
    }

    public List<RegistroTreinamento> vencimentosProximos(int dias, int limite) {
        List<RegistroTreinamento> lista = new ArrayList<>();
        String sql = BASE + "WHERE r.validade IS NOT NULL AND r.validade <= ? "
                + "ORDER BY r.validade LIMIT ?";
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, LocalDate.now().plusDays(dias).toString());
            ps.setInt(2, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(montar(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao listar vencimentos", e);
        }
        return lista;
    }

    public List<RegistroTreinamento> porColaborador(int colaboradorId) {
        List<RegistroTreinamento> lista = new ArrayList<>();
        String sql = BASE + "WHERE r.colaborador_id = ? ORDER BY r.data DESC";
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, colaboradorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(montar(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao listar registros do colaborador", e);
        }
        return lista;
    }

    public RegistroTreinamento porId(int id) {
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(BASE + "WHERE r.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? montar(rs) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao buscar registro", e);
        }
    }

    public RegistroTreinamento emitirCertificado(int registroId, String usuario) {
        RegistroTreinamento r = porId(registroId);
        if (r == null) {
            throw new IllegalStateException("Registro não encontrado");
        }
        try (Connection con = Conexao.abrir()) {
            String existente = null;
            int via = 0;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT numero, via FROM certificado WHERE registro_id = ?")) {
                ps.setInt(1, registroId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        existente = rs.getString("numero");
                        via = rs.getInt("via");
                    }
                }
            }
            if (existente != null) {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE certificado SET via = via + 1, emitido_em = CURRENT_TIMESTAMP, "
                        + "emitido_por = ? WHERE registro_id = ?")) {
                    ps.setString(1, usuario);
                    ps.setInt(2, registroId);
                    ps.executeUpdate();
                }
                new AuditoriaDAO().registrar("CERTIFICADO", "2ª via emitida: " + existente);
                return porId(registroId);
            }

            String ano = String.valueOf(LocalDate.now().getYear());
            String numero;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) FROM certificado WHERE numero LIKE ?")) {
                ps.setString(1, "TN-" + ano + "-%");
                try (ResultSet rs = ps.executeQuery()) {
                    int seq = (rs.next() ? rs.getInt(1) : 0) + 1;
                    numero = "TN-" + ano + "-" + String.format("%06d", seq);
                }
            }
            String hash = Hash.sha256(registroId + "|" + numero + "|"
                    + r.getColaboradorCpf() + "|" + Datas.iso(r.getData()));
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO certificado (registro_id, numero, emitido_por, via, hash) "
                    + "VALUES (?,?,?,1,?)")) {
                ps.setInt(1, registroId);
                ps.setString(2, numero);
                ps.setString(3, usuario);
                ps.setString(4, hash);
                ps.executeUpdate();
            }
            new AuditoriaDAO().registrar("CERTIFICADO", "Certificado emitido: " + numero
                    + " para " + r.getColaboradorNome());
            return porId(registroId);
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao emitir certificado", e);
        }
    }

    public RegistroTreinamento buscarPorCodigo(String entrada) {
        if (entrada == null || entrada.isBlank()) {
            return null;
        }
        String texto = entrada.trim();
        String limpo = texto.replace("-", "").replace(" ", "").toLowerCase();
        String sql = BASE + "WHERE UPPER(cert.numero) = UPPER(?) OR cert.hash LIKE ? LIMIT 1";
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, texto);
            ps.setString(2, limpo + "%");
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? montar(rs) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao validar certificado", e);
        }
    }

    public int[] resumo() {
        int colaboradores = 0;
        int vencendo = 0;
        int vencidos = 0;
        int certificados = 0;
        try (Connection con = Conexao.abrir()) {
            colaboradores = contar(con, "SELECT COUNT(*) FROM colaborador WHERE ativo = 1", null, null);
            vencidos = contar(con,
                    "SELECT COUNT(*) FROM registro_treinamento WHERE validade IS NOT NULL AND validade < ?",
                    Datas.hojeIso(), null);
            vencendo = contar(con,
                    "SELECT COUNT(*) FROM registro_treinamento WHERE validade IS NOT NULL "
                    + "AND validade >= ? AND validade <= ?",
                    Datas.hojeIso(), LocalDate.now().plusDays(30).toString());
            certificados = contar(con,
                    "SELECT COUNT(*) FROM certificado WHERE emitido_em LIKE ?",
                    LocalDate.now().getYear() + "%", null);
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao montar resumo", e);
        }
        return new int[]{colaboradores, vencendo, vencidos, certificados};
    }

    private int contar(Connection con, String sql, String a, String b) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int i = 1;
            if (a != null) {
                ps.setString(i++, a);
            }
            if (b != null) {
                ps.setString(i++, b);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int totalRegistros() {
        try (Connection con = Conexao.abrir();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM registro_treinamento")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    private RegistroTreinamento montar(ResultSet rs) throws SQLException {
        RegistroTreinamento r = new RegistroTreinamento();
        r.setId(rs.getInt("id"));
        r.setColaboradorId(rs.getInt("colaborador_id"));
        r.setColaboradorNome(rs.getString("colaborador_nome"));
        r.setColaboradorCpf(rs.getString("colaborador_cpf"));
        r.setColaboradorCargo(rs.getString("colaborador_cargo"));
        int turma = rs.getInt("turma_id");
        r.setTurmaId(rs.wasNull() ? null : turma);
        r.setNorma(rs.getString("norma"));
        r.setTreinamento(rs.getString("treinamento"));
        r.setData(Datas.ler(rs.getString("data")));
        r.setDataFim(Datas.ler(rs.getString("data_fim")));
        r.setCh(rs.getDouble("ch"));
        int instrutor = rs.getInt("instrutor_id");
        r.setInstrutorId(rs.wasNull() ? null : instrutor);
        r.setInstrutorNome(rs.getString("instrutor_nome"));
        int meses = rs.getInt("validade_meses");
        r.setValidadeMeses(rs.wasNull() ? null : meses);
        r.setValidade(Datas.ler(rs.getString("validade")));
        r.setSituacao(rs.getString("situacao"));
        r.setNumeroCertificado(rs.getString("numero_certificado"));
        r.setEmitidoEm(br.com.treinanorma.util.Datas.lerDataHora(rs.getString("emitido_em")));
        String hash = rs.getString("hash_certificado");
        if (hash != null) {
            r.setCodigoVerificacao(Hash.codigoVerificacao(hash));
        }
        return r;
    }
}
