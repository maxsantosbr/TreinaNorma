package br.com.treinanorma.dao;

import br.com.treinanorma.model.Encontro;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TurmaDAO {

    private static final String BASE = "SELECT t.*, i.nome AS instrutor_nome, "
            + "(SELECT COUNT(*) FROM turma_participante p WHERE p.turma_id = t.id) AS participantes, "
            + "(SELECT MIN(e.data) FROM turma_encontro e WHERE e.turma_id = t.id) AS primeira_data, "
            + "(SELECT MAX(e.data) FROM turma_encontro e WHERE e.turma_id = t.id) AS ultima_data "
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

    public List<Encontro> encontros(int turmaId) {
        List<Encontro> lista = new ArrayList<>();
        String sql = "SELECT * FROM turma_encontro WHERE turma_id = ? ORDER BY data, ordem, id";
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, turmaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Encontro e = new Encontro();
                    e.setId(rs.getInt("id"));
                    e.setTurmaId(rs.getInt("turma_id"));
                    e.setOrdem(rs.getInt("ordem"));
                    e.setData(Datas.ler(rs.getString("data")));
                    e.setHoraInicio(rs.getString("hora_inicio"));
                    e.setHoraFim(rs.getString("hora_fim"));
                    e.setCh(rs.getDouble("ch"));
                    e.setPratica(rs.getInt("pratica") == 1);
                    e.setConteudo(rs.getString("conteudo"));
                    lista.add(e);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao listar encontros", e);
        }
        return lista;
    }

    public void salvarEncontros(int turmaId, List<Encontro> lista) {
        if (lista == null || lista.isEmpty()) {
            throw new IllegalStateException("A turma precisa de pelo menos um dia de treinamento.");
        }
        List<Encontro> ordenada = new ArrayList<>(lista);
        ordenada.sort((a, b) -> {
            if (a.getData() == null && b.getData() == null) {
                return Integer.compare(a.getOrdem(), b.getOrdem());
            }
            if (a.getData() == null) {
                return 1;
            }
            if (b.getData() == null) {
                return -1;
            }
            int c = a.getData().compareTo(b.getData());
            return c != 0 ? c : Integer.compare(a.getOrdem(), b.getOrdem());
        });
        try (Connection con = Conexao.abrir()) {
            con.setAutoCommit(false);
            try {
                Set<Integer> existentes = new HashSet<>();
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT id FROM turma_encontro WHERE turma_id = ?")) {
                    ps.setInt(1, turmaId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            existentes.add(rs.getInt(1));
                        }
                    }
                }
                Set<Integer> manter = new HashSet<>();
                for (Encontro e : ordenada) {
                    if (e.getId() > 0 && existentes.contains(e.getId())) {
                        manter.add(e.getId());
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM turma_encontro WHERE turma_id = ? AND id = ?")) {
                    for (Integer id : existentes) {
                        if (!manter.contains(id)) {
                            ps.setInt(1, turmaId);
                            ps.setInt(2, id);
                            ps.addBatch();
                        }
                    }
                    ps.executeBatch();
                }
                String insert = "INSERT INTO turma_encontro (turma_id, ordem, data, hora_inicio, "
                        + "hora_fim, ch, pratica, conteudo) VALUES (?,?,?,?,?,?,?,?)";
                String update = "UPDATE turma_encontro SET ordem=?, data=?, hora_inicio=?, hora_fim=?, "
                        + "ch=?, pratica=?, conteudo=? WHERE id=? AND turma_id=?";
                try (PreparedStatement ins = con.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);
                     PreparedStatement upd = con.prepareStatement(update)) {
                    for (int i = 0; i < ordenada.size(); i++) {
                        Encontro e = ordenada.get(i);
                        e.setOrdem(i + 1);
                        if (e.getId() > 0 && manter.contains(e.getId())) {
                            upd.setInt(1, i + 1);
                            upd.setString(2, Datas.iso(e.getData()));
                            upd.setString(3, e.getHoraInicio());
                            upd.setString(4, e.getHoraFim());
                            upd.setDouble(5, e.getCh());
                            upd.setInt(6, e.isPratica() ? 1 : 0);
                            upd.setString(7, e.getConteudo());
                            upd.setInt(8, e.getId());
                            upd.setInt(9, turmaId);
                            upd.addBatch();
                        } else {
                            ins.setInt(1, turmaId);
                            ins.setInt(2, i + 1);
                            ins.setString(3, Datas.iso(e.getData()));
                            ins.setString(4, e.getHoraInicio());
                            ins.setString(5, e.getHoraFim());
                            ins.setDouble(6, e.getCh());
                            ins.setInt(7, e.isPratica() ? 1 : 0);
                            ins.setString(8, e.getConteudo());
                            ins.executeUpdate();
                            try (ResultSet rs = ins.getGeneratedKeys()) {
                                if (rs.next()) {
                                    e.setId(rs.getInt(1));
                                }
                            }
                        }
                    }
                    upd.executeBatch();
                }
                double total = 0;
                double pratica = 0;
                for (Encontro e : ordenada) {
                    total += e.getCh();
                    if (e.isPratica()) {
                        pratica += e.getCh();
                    }
                }
                Encontro primeiro = ordenada.get(0);
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE turma SET data=?, hora_inicio=?, hora_fim=?, ch_total=?, "
                        + "ch_presencial=? WHERE id=?")) {
                    ps.setString(1, Datas.iso(primeiro.getData()));
                    ps.setString(2, primeiro.getHoraInicio());
                    ps.setString(3, primeiro.getHoraFim());
                    ps.setDouble(4, total);
                    ps.setDouble(5, pratica);
                    ps.setInt(6, turmaId);
                    ps.executeUpdate();
                }
                sincronizarPresenca(con, turmaId);
                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao salvar encontros", e);
        }
    }

    public Map<Integer, Set<Integer>> presencas(int turmaId) {
        Map<Integer, Set<Integer>> mapa = new HashMap<>();
        String sql = "SELECT ep.encontro_id, ep.participante_id FROM turma_encontro_presenca ep "
                + "JOIN turma_encontro e ON e.id = ep.encontro_id "
                + "WHERE e.turma_id = ? AND ep.presente = 1";
        try (Connection con = Conexao.abrir()) {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, turmaId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        mapa.computeIfAbsent(rs.getInt("encontro_id"), k -> new HashSet<>())
                                .add(rs.getInt("participante_id"));
                    }
                }
            }
            if (mapa.isEmpty()) {
                List<Integer> dias = new ArrayList<>();
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT id FROM turma_encontro WHERE turma_id = ? ORDER BY ordem")) {
                    ps.setInt(1, turmaId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            dias.add(rs.getInt(1));
                        }
                    }
                }
                Set<Integer> legado = new HashSet<>();
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT id FROM turma_participante WHERE turma_id = ? AND presente = 1")) {
                    ps.setInt(1, turmaId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            legado.add(rs.getInt(1));
                        }
                    }
                }
                if (!legado.isEmpty()) {
                    for (Integer dia : dias) {
                        mapa.put(dia, new HashSet<>(legado));
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao listar presenças", e);
        }
        return mapa;
    }
    public void salvarPresencas(int encontroId, Map<Integer, Boolean> presencas) {
        String sql = "INSERT INTO turma_encontro_presenca (encontro_id, participante_id, presente) "
                + "VALUES (?,?,?) ON CONFLICT (encontro_id, participante_id) "
                + "DO UPDATE SET presente = excluded.presente";
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (Map.Entry<Integer, Boolean> par : presencas.entrySet()) {
                ps.setInt(1, encontroId);
                ps.setInt(2, par.getKey());
                ps.setInt(3, Boolean.TRUE.equals(par.getValue()) ? 1 : 0);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao salvar presenças", e);
        }
    }

    public void sincronizarPresenca(int turmaId) {
        try (Connection con = Conexao.abrir()) {
            sincronizarPresenca(con, turmaId);
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao sincronizar presença", e);
        }
    }

    private void sincronizarPresenca(Connection con, int turmaId) throws SQLException {
        int total = 0;
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT COUNT(*) FROM turma_encontro WHERE turma_id = ?")) {
            ps.setInt(1, turmaId);
            try (ResultSet rs = ps.executeQuery()) {
                total = rs.next() ? rs.getInt(1) : 0;
            }
        }
        if (total == 0) {
            return;
        }
        String sql = "UPDATE turma_participante SET presente = CASE WHEN ("
                + "SELECT COUNT(*) FROM turma_encontro_presenca ep "
                + "JOIN turma_encontro e ON e.id = ep.encontro_id "
                + "WHERE e.turma_id = ? AND ep.participante_id = turma_participante.id "
                + "AND ep.presente = 1) >= ? THEN 1 ELSE 0 END WHERE turma_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, turmaId);
            ps.setInt(2, total);
            ps.setInt(3, turmaId);
            ps.executeUpdate();
        }
    }

    public void salvarAprovacao(List<Participante> participantes) {
        String sql = "UPDATE turma_participante SET aprovado = ?, eletronica = ?, "
                + "observacao = ? WHERE id = ?";
        try (Connection con = Conexao.abrir();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (Participante p : participantes) {
                if (p.getAprovado() == null) {
                    ps.setObject(1, null);
                } else {
                    ps.setInt(1, p.getAprovado() ? 1 : 0);
                }
                ps.setInt(2, p.isEletronica() ? 1 : 0);
                ps.setString(3, p.getObservacao());
                ps.setInt(4, p.getId());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao salvar aprovação", e);
        }
    }

    public int concluir(int turmaId, String usuario) {
        Turma t = porId(turmaId);
        if (t == null) {
            throw new IllegalStateException("Turma não encontrada");
        }
        List<Encontro> encontros = encontros(turmaId);
        LocalDate fim = encontros.isEmpty() ? t.getData()
                : encontros.get(encontros.size() - 1).getData();
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
            sincronizarPresenca(con, turmaId);
            try (PreparedStatement del = con.prepareStatement(
                    "DELETE FROM registro_treinamento WHERE turma_id = ? "
                    + "AND id NOT IN (SELECT registro_id FROM certificado)")) {
                del.setInt(1, turmaId);
                del.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT id, presente FROM turma_participante WHERE turma_id = ?")) {
                ps.setInt(1, turmaId);
                try (ResultSet rs = ps.executeQuery()) {
                    Map<Integer, Boolean> presenca = new HashMap<>();
                    while (rs.next()) {
                        presenca.put(rs.getInt("id"), rs.getInt("presente") == 1);
                    }
                    for (Participante p : lista) {
                        p.setPresente(Boolean.TRUE.equals(presenca.get(p.getId())));
                    }
                }
            }
            String sql = "INSERT INTO registro_treinamento (colaborador_id, turma_id, norma, "
                    + "treinamento, data, data_fim, ch, instrutor_id, validade_meses, validade, situacao) "
                    + "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (Participante p : lista) {
                    if (!p.isPresente() || Boolean.FALSE.equals(p.getAprovado())) {
                        continue;
                    }
                    LocalDate validade = Datas.validade(fim, meses);
                    ps.setInt(1, p.getColaboradorId());
                    ps.setInt(2, turmaId);
                    ps.setString(3, t.getNorma());
                    ps.setString(4, t.getTreinamento());
                    ps.setString(5, Datas.iso(t.getData()));
                    ps.setString(6, Datas.iso(fim));
                    ps.setDouble(7, t.getChTotal());
                    if (t.getInstrutorId() == null) {
                        ps.setObject(8, null);
                    } else {
                        ps.setInt(8, t.getInstrutorId());
                    }
                    if (meses == null) {
                        ps.setObject(9, null);
                    } else {
                        ps.setInt(9, meses);
                    }
                    ps.setString(10, Datas.iso(validade));
                    ps.setString(11, "VALIDO");
                    ps.addBatch();
                    gerados++;
                }
                ps.executeBatch();
            }
            String concluida = LocalDateTime.now().toString();
            String hash = Hash.sha256("ata|" + t.getId() + "|" + t.getCodigo() + "|"
                    + Datas.iso(t.getData()) + "|" + Datas.iso(fim) + "|" + gerados + "|" + concluida);
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
        t.setDataFim(Datas.ler(rs.getString("ultima_data")));
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
