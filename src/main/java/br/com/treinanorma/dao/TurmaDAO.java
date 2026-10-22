package br.com.treinanorma.dao;

import br.com.treinanorma.model.MatrizItem;
import br.com.treinanorma.model.Participante;
import br.com.treinanorma.model.Turma;
import br.com.treinanorma.util.Datas;
import br.com.treinanorma.util.Hash;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TurmaDAO {

    private static final String BASE = "SELECT t.*, i.nome AS instrutor_nome, "
            + "(SELECT COUNT(*) FROM turma_participante p WHERE p.turma_id = t.id) AS participantes "
            + "FROM turma t LEFT JOIN instrutor i ON i.id = t.instrutor_id ";

    public List<Turma> listar(String termo) {
        List<Turma> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE).append("WHERE 1=1 ");
        if (termo != null && !termo.isBlank()) {
            sql.append("AND (t.codigo LIKE ? OR t.norma LIKE ? OR t.treinamento LIKE ? "
                    + "OR t.local LIKE ? OR i.nome LIKE ?) ");
        }
        sql.append("ORDER BY t.data DESC, t.id DESC");
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            if (termo != null && !termo.isBlank()) {
                String like = "%" + termo.trim() + "%";
                for (int i = 1; i <= 5; i++) {
                    ps.setString(i, like);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(montar(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao listar turmas", e);
        }
        return lista;
    }

    public Turma porId(int id) {
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(BASE + "WHERE t.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? montar(rs) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao buscar turma", e);
        }
    }

    public String proximoCodigo(LocalDate data) {
        String prefixo = "T" + (data == null ? LocalDate.now() : data)
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        String sql = "SELECT COUNT(*) FROM turma WHERE codigo LIKE ?";
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, prefixo + "%");
            try (ResultSet rs = ps.executeQuery()) {
                int n = rs.next() ? rs.getInt(1) : 0;
                return prefixo + String.format("%03d", n + 1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao gerar código da turma", e);
        }
    }

    public int salvar(Turma t) {
        if (t.getId() > 0) {
            atualizar(t);
            return t.getId();
        }
        return inserir(t);
    }

    private int inserir(Turma t) {
        if (t.getCodigo() == null || t.getCodigo().isBlank()) {
            t.setCodigo(proximoCodigo(t.getData()));
        }
        String sql = "INSERT INTO turma (codigo, empresa_id, matriz_id, norma, treinamento, data, "
                + "hora_inicio, hora_fim, local, modalidade, ch_total, ch_presencial, instrutor_id, "
                + "conteudo, status, observacao, criado_por) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencher(ps, t);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    t.setId(rs.getInt(1));
                }
            }
            new AuditoriaDAO().registrar("TURMA", "Turma criada: " + t.getCodigo()
                    + " / " + t.getNorma() + " em " + Datas.br(t.getData()));
            return t.getId();
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao salvar turma: " + e.getMessage(), e);
        }
    }

    private void atualizar(Turma t) {
        String sql = "UPDATE turma SET matriz_id=?, norma=?, treinamento=?, data=?, hora_inicio=?, "
                + "hora_fim=?, local=?, modalidade=?, ch_total=?, ch_presencial=?, instrutor_id=?, "
                + "conteudo=?, status=?, observacao=? WHERE id=?";
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (t.getMatrizId() == null) {
                ps.setObject(1, null);
            } else {
                ps.setInt(1, t.getMatrizId());
            }
            ps.setString(2, t.getNorma());
            ps.setString(3, t.getTreinamento());
            ps.setString(4, Datas.iso(t.getData()));
            ps.setString(5, t.getHoraInicio());
            ps.setString(6, t.getHoraFim());
            ps.setString(7, t.getLocal());
            ps.setString(8, t.getModalidade());
            ps.setDouble(9, t.getChTotal());
            ps.setDouble(10, t.getChPresencial());
            if (t.getInstrutorId() == null) {
                ps.setObject(11, null);
            } else {
                ps.setInt(11, t.getInstrutorId());
            }
            ps.setString(12, t.getConteudo());
            ps.setString(13, t.getStatus());
            ps.setString(14, t.getObservacao());
            ps.setInt(15, t.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao atualizar turma", e);
        }
    }

    private void preencher(PreparedStatement ps, Turma t) throws SQLException {
        ps.setString(1, t.getCodigo());
        if (t.getEmpresaId() == null) {
            ps.setObject(2, null);
        } else {
            ps.setInt(2, t.getEmpresaId());
        }
        if (t.getMatrizId() == null) {
            ps.setObject(3, null);
        } else {
            ps.setInt(3, t.getMatrizId());
        }
        ps.setString(4, t.getNorma());
        ps.setString(5, t.getTreinamento());
        ps.setString(6, Datas.iso(t.getData()));
        ps.setString(7, t.getHoraInicio());
        ps.setString(8, t.getHoraFim());
        ps.setString(9, t.getLocal());
        ps.setString(10, t.getModalidade());
        ps.setDouble(11, t.getChTotal());
        ps.setDouble(12, t.getChPresencial());
        if (t.getInstrutorId() == null) {
            ps.setObject(13, null);
        } else {
            ps.setInt(13, t.getInstrutorId());
        }
        ps.setString(14, t.getConteudo());
        ps.setString(15, t.getStatus());
        ps.setString(16, t.getObservacao());
        ps.setString(17, t.getCriadoPor());
    }

    public void excluir(int id) {
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement("DELETE FROM turma WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            new AuditoriaDAO().registrar("TURMA", "Turma excluída: id " + id);
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao excluir turma", e);
        }
    }

    public List<Participante> participantes(int turmaId) {
        List<Participante> lista = new ArrayList<>();
        String sql = "SELECT p.*, c.nome, c.cpf, c.cargo, s.nome AS setor_nome "
                + "FROM turma_participante p "
                + "JOIN colaborador c ON c.id = p.colaborador_id "
                + "LEFT JOIN setor s ON s.id = c.setor_id "
                + "WHERE p.turma_id = ? ORDER BY c.nome";
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, turmaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Participante p = new Participante();
                    p.setId(rs.getInt("id"));
                    p.setTurmaId(rs.getInt("turma_id"));
                    p.setColaboradorId(rs.getInt("colaborador_id"));
                    p.setNome(rs.getString("nome"));
                    p.setCpf(rs.getString("cpf"));
                    p.setCargo(rs.getString("cargo"));
                    p.setSetor(rs.getString("setor_nome"));
                    p.setPresente(rs.getInt("presente") == 1);
                    int aprovado = rs.getInt("aprovado");
                    p.setAprovado(rs.wasNull() ? null : aprovado == 1);
                    p.setAssinatura(rs.getString("assinatura"));
                    p.setEletronica(rs.getInt("eletronica") == 1);
                    p.setObservacao(rs.getString("observacao"));
                    lista.add(p);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao listar participantes", e);
        }
        return lista;
    }

    public void adicionarParticipantes(int turmaId, List<Integer> colaboradores) {
        String sql = "INSERT OR IGNORE INTO turma_participante (turma_id, colaborador_id) VALUES (?,?)";
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (Integer id : colaboradores) {
                ps.setInt(1, turmaId);
                ps.setInt(2, id);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao adicionar participantes", e);
        }
    }

    public void removerParticipante(int turmaId, int colaboradorId) {
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(
                     "DELETE FROM turma_participante WHERE turma_id = ? AND colaborador_id = ?")) {
            ps.setInt(1, turmaId);
            ps.setInt(2, colaboradorId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao remover participante", e);
        }
    }

    public void salvarChamada(List<Participante> participantes) {
        String sql = "UPDATE turma_participante SET presente = ?, aprovado = ?, eletronica = ?, "
                + "observacao = ? WHERE id = ?";
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (Participante p : participantes) {
                ps.setInt(1, p.isPresente() ? 1 : 0);
                if (p.getAprovado() == null) {
                    ps.setObject(2, null);
                } else {
                    ps.setInt(2, p.getAprovado() ? 1 : 0);
                }
                ps.setInt(3, p.isEletronica() ? 1 : 0);
                ps.setString(4, p.getObservacao());
                ps.setInt(5, p.getId());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao salvar chamada", e);
        }
    }

    public int concluir(int turmaId, String usuario) {
        Turma t = porId(turmaId);
        if (t == null) {
            throw new IllegalStateException("Turma não encontrada");
        }
        List<Participante> lista = participantes(turmaId);
        Integer meses = null;
        if (t.getMatrizId() != null) {
            MatrizItem item = new MatrizDAO().porId(t.getMatrizId());
            if (item != null) {
                meses = item.getValidadeMeses();
            }
        }
        int gerados = 0;
        try (Connection con = Conexao.abrir()) {
            try (PreparedStatement del = con.prepareStatement(
                    "DELETE FROM registro_treinamento WHERE turma_id = ? "
                    + "AND id NOT IN (SELECT registro_id FROM certificado)")) {
                del.setInt(1, turmaId);
                del.executeUpdate();
            }
            String sql = "INSERT INTO registro_treinamento (colaborador_id, turma_id, norma, "
                    + "treinamento, data, ch, instrutor_id, validade_meses, validade, situacao) "
                    + "VALUES (?,?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (Participante p : lista) {
                    if (!p.isPresente() || Boolean.FALSE.equals(p.getAprovado())) {
                        continue;
                    }
                    LocalDate validade = Datas.validade(t.getData(), meses);
                    ps.setInt(1, p.getColaboradorId());
                    ps.setInt(2, turmaId);
                    ps.setString(3, t.getNorma());
                    ps.setString(4, t.getTreinamento());
                    ps.setString(5, Datas.iso(t.getData()));
                    ps.setDouble(6, t.getChTotal());
                    if (t.getInstrutorId() == null) {
                        ps.setObject(7, null);
                    } else {
                        ps.setInt(7, t.getInstrutorId());
                    }
                    if (meses == null) {
                        ps.setObject(8, null);
                    } else {
                        ps.setInt(8, meses);
                    }
                    ps.setString(9, Datas.iso(validade));
                    ps.setString(10, "VALIDO");
                    ps.addBatch();
                    gerados++;
                }
                ps.executeBatch();
            }
            String concluida = LocalDateTime.now().toString();
            String hash = Hash.sha256("ata|" + t.getId() + "|" + t.getCodigo() + "|"
                    + Datas.iso(t.getData()) + "|" + gerados + "|" + concluida);
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE turma SET status = 'CONCLUIDA', concluida_em = ?, hash_ata = ? WHERE id = ?")) {
                ps.setString(1, concluida);
                ps.setString(2, hash);
                ps.setInt(3, turmaId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao concluir turma", e);
        }
        new AuditoriaDAO().registrar("TURMA", "Turma concluída: " + t.getCodigo()
                + " (" + gerados + " registros) por " + usuario);
        return gerados;
    }

    private Turma montar(ResultSet rs) throws SQLException {
        Turma t = new Turma();
        t.setId(rs.getInt("id"));
        t.setCodigo(rs.getString("codigo"));
        int empresa = rs.getInt("empresa_id");
        t.setEmpresaId(rs.wasNull() ? null : empresa);
        int matriz = rs.getInt("matriz_id");
        t.setMatrizId(rs.wasNull() ? null : matriz);
        t.setNorma(rs.getString("norma"));
        t.setTreinamento(rs.getString("treinamento"));
        t.setData(Datas.ler(rs.getString("data")));
        t.setHoraInicio(rs.getString("hora_inicio"));
        t.setHoraFim(rs.getString("hora_fim"));
        t.setLocal(rs.getString("local"));
        t.setModalidade(rs.getString("modalidade"));
        t.setChTotal(rs.getDouble("ch_total"));
        t.setChPresencial(rs.getDouble("ch_presencial"));
        int instrutor = rs.getInt("instrutor_id");
        t.setInstrutorId(rs.wasNull() ? null : instrutor);
        t.setInstrutorNome(rs.getString("instrutor_nome"));
        t.setConteudo(rs.getString("conteudo"));
        t.setStatus(rs.getString("status"));
        t.setObservacao(rs.getString("observacao"));
        t.setCriadoPor(rs.getString("criado_por"));
        t.setCriadoEm(rs.getString("criado_em"));
        t.setConcluidaEm(rs.getString("concluida_em"));
        t.setHashAta(rs.getString("hash_ata"));
        t.setParticipantes(rs.getInt("participantes"));
        return t;
    }
}
