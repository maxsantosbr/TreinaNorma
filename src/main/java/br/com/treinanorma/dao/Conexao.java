package br.com.treinanorma.dao;

import br.com.treinanorma.util.Csv;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

public final class Conexao {

    private static final String PASTA =
            System.getProperty("user.home") + File.separator + "TreinaNorma";
    private static final String ARQUIVO = PASTA + File.separator + "treinanorma.db";
    private static final String URL = "jdbc:sqlite:" + ARQUIVO;

    private Conexao() {
    }

    public static String pasta() {
        return PASTA;
    }

    public static String caminho() {
        return ARQUIVO;
    }

    public static Connection abrir() throws SQLException {
        new File(PASTA).mkdirs();
        Connection con = DriverManager.getConnection(URL);
        try (Statement st = con.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        criarTabelas(con);
        criarAdminSeVazio(con);
        carregarNormas(con);
        carregarMatriz(con);
        return con;
    }

    private static void criarTabelas(Connection con) throws SQLException {
        String[] sql = {
            """
            CREATE TABLE IF NOT EXISTS usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                usuario TEXT NOT NULL UNIQUE,
                senha_hash TEXT NOT NULL,
                perfil TEXT NOT NULL DEFAULT 'CONSULTA'
                    CHECK (perfil IN ('ADMIN','INSTRUTOR','CONSULTA')),
                ativo INTEGER NOT NULL DEFAULT 1,
                deve_trocar_senha INTEGER NOT NULL DEFAULT 1,
                criado_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS solicitacao_senha (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                protocolo TEXT UNIQUE,
                usuario_id INTEGER REFERENCES usuarios(id) ON DELETE SET NULL,
                login TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'PENDENTE'
                    CHECK (status IN ('PENDENTE','ATENDIDA','CANCELADA')),
                criado_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                atendido_por INTEGER REFERENCES usuarios(id) ON DELETE SET NULL,
                atendido_em TEXT
            )
            """,
            """
            CREATE UNIQUE INDEX IF NOT EXISTS idx_solicitacao_senha_pendente
                ON solicitacao_senha(login COLLATE NOCASE)
                WHERE status = 'PENDENTE'
            """,
            """
            CREATE INDEX IF NOT EXISTS idx_solicitacao_senha_status
                ON solicitacao_senha(status, criado_em)
            """,
            """
            CREATE TABLE IF NOT EXISTS empresa (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                razao_social TEXT NOT NULL,
                nome_fantasia TEXT,
                cnpj TEXT,
                cnae TEXT,
                grau_risco INTEGER,
                endereco TEXT,
                numero TEXT,
                complemento TEXT,
                bairro TEXT,
                cidade TEXT,
                uf TEXT,
                cep TEXT,
                telefone TEXT,
                email TEXT,
                logo TEXT,
                assinatura TEXT,
                responsavel_nome TEXT,
                responsavel_registro TEXT,
                responsavel_profissao TEXT,
                criado_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS estabelecimento (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                empresa_id INTEGER NOT NULL REFERENCES empresa(id) ON DELETE CASCADE,
                nome TEXT NOT NULL,
                cnpj TEXT,
                cnae TEXT,
                grau_risco INTEGER,
                endereco TEXT,
                cidade TEXT,
                uf TEXT,
                ativo INTEGER NOT NULL DEFAULT 1
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS setor (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                estabelecimento_id INTEGER REFERENCES estabelecimento(id) ON DELETE CASCADE,
                nome TEXT NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS colaborador (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                empresa_id INTEGER REFERENCES empresa(id) ON DELETE CASCADE,
                estabelecimento_id INTEGER REFERENCES estabelecimento(id) ON DELETE SET NULL,
                setor_id INTEGER REFERENCES setor(id) ON DELETE SET NULL,
                nome TEXT NOT NULL,
                cpf TEXT NOT NULL UNIQUE,
                matricula TEXT,
                cargo TEXT,
                admissao TEXT,
                demissao TEXT,
                email TEXT,
                telefone TEXT,
                foto TEXT,
                ativo INTEGER NOT NULL DEFAULT 1,
                criado_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS colaborador_exposicao (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                colaborador_id INTEGER NOT NULL REFERENCES colaborador(id) ON DELETE CASCADE,
                tipo TEXT NOT NULL,
                UNIQUE (colaborador_id, tipo)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS instrutor (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                cpf TEXT UNIQUE,
                formacao TEXT,
                registro TEXT,
                tipo TEXT NOT NULL DEFAULT 'INTERNO',
                email TEXT,
                telefone TEXT,
                assinatura TEXT,
                ativo INTEGER NOT NULL DEFAULT 1,
                criado_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS instrutor_habilitacao (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                instrutor_id INTEGER NOT NULL REFERENCES instrutor(id) ON DELETE CASCADE,
                norma TEXT NOT NULL,
                validade TEXT,
                UNIQUE (instrutor_id, norma)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS norma (
                codigo TEXT PRIMARY KEY,
                titulo TEXT NOT NULL,
                tipo TEXT,
                situacao TEXT,
                exige_treinamento TEXT,
                observacao TEXT
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS matriz_treinamento (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                norma TEXT NOT NULL,
                treinamento TEXT NOT NULL,
                criterio_tipo TEXT,
                criterio_valor TEXT,
                ch_minima REAL,
                presencial_minimo REAL,
                validade_meses INTEGER,
                recorrencia TEXT,
                modalidade TEXT,
                ch_fixada TEXT,
                base_legal TEXT,
                observacao TEXT
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS turma (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                codigo TEXT NOT NULL UNIQUE,
                empresa_id INTEGER REFERENCES empresa(id) ON DELETE SET NULL,
                matriz_id INTEGER REFERENCES matriz_treinamento(id) ON DELETE SET NULL,
                norma TEXT NOT NULL,
                treinamento TEXT NOT NULL,
                data TEXT NOT NULL,
                hora_inicio TEXT,
                hora_fim TEXT,
                local TEXT,
                modalidade TEXT,
                ch_total REAL NOT NULL DEFAULT 0,
                ch_presencial REAL NOT NULL DEFAULT 0,
                instrutor_id INTEGER REFERENCES instrutor(id) ON DELETE SET NULL,
                conteudo TEXT,
                status TEXT NOT NULL DEFAULT 'AGENDADA'
                    CHECK (status IN ('RASCUNHO','AGENDADA','EM_EXECUCAO','CONCLUIDA','CANCELADA')),
                observacao TEXT,
                criado_por TEXT,
                criado_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                concluida_em TEXT,
                hash_ata TEXT
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS turma_participante (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                turma_id INTEGER NOT NULL REFERENCES turma(id) ON DELETE CASCADE,
                colaborador_id INTEGER NOT NULL REFERENCES colaborador(id) ON DELETE CASCADE,
                presente INTEGER NOT NULL DEFAULT 0,
                aprovado INTEGER,
                assinatura TEXT,
                eletronica INTEGER NOT NULL DEFAULT 0,
                observacao TEXT,
                UNIQUE (turma_id, colaborador_id)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS turma_encontro (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                turma_id INTEGER NOT NULL REFERENCES turma(id) ON DELETE CASCADE,
                ordem INTEGER NOT NULL DEFAULT 1,
                data TEXT NOT NULL,
                hora_inicio TEXT,
                hora_fim TEXT,
                ch REAL NOT NULL DEFAULT 0,
                pratica INTEGER NOT NULL DEFAULT 0,
                conteudo TEXT
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS turma_encontro_presenca (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                encontro_id INTEGER NOT NULL REFERENCES turma_encontro(id) ON DELETE CASCADE,
                participante_id INTEGER NOT NULL REFERENCES turma_participante(id) ON DELETE CASCADE,
                presente INTEGER NOT NULL DEFAULT 0,
                UNIQUE (encontro_id, participante_id)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS registro_treinamento (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                colaborador_id INTEGER NOT NULL REFERENCES colaborador(id) ON DELETE CASCADE,
                turma_id INTEGER REFERENCES turma(id) ON DELETE SET NULL,
                norma TEXT NOT NULL,
                treinamento TEXT NOT NULL,
                data TEXT NOT NULL,
                ch REAL NOT NULL DEFAULT 0,
                instrutor_id INTEGER REFERENCES instrutor(id) ON DELETE SET NULL,
                validade_meses INTEGER,
                validade TEXT,
                situacao TEXT NOT NULL DEFAULT 'VALIDO',
                criado_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS certificado (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                registro_id INTEGER NOT NULL UNIQUE REFERENCES registro_treinamento(id) ON DELETE CASCADE,
                numero TEXT NOT NULL UNIQUE,
                emitido_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                emitido_por TEXT,
                via INTEGER NOT NULL DEFAULT 1,
                hash TEXT
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS cracha_lote (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                turma_id INTEGER REFERENCES turma(id) ON DELETE SET NULL,
                gerado_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                arquivo TEXT
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS documento (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                tipo TEXT NOT NULL,
                referencia TEXT,
                arquivo TEXT,
                hash TEXT,
                criado_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS auditoria (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                usuario TEXT,
                acao TEXT NOT NULL,
                detalhe TEXT,
                criado_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS parametro (
                chave TEXT PRIMARY KEY,
                valor TEXT
            )
            """,
            "CREATE INDEX IF NOT EXISTS ix_colaborador_nome ON colaborador (nome)",
            "CREATE INDEX IF NOT EXISTS ix_turma_data ON turma (data)",
            "CREATE INDEX IF NOT EXISTS ix_registro_colab ON registro_treinamento (colaborador_id, norma)",
            "CREATE INDEX IF NOT EXISTS ix_participante_turma ON turma_participante (turma_id)",
            "CREATE INDEX IF NOT EXISTS ix_encontro_turma ON turma_encontro (turma_id, data)",
            "CREATE INDEX IF NOT EXISTS ix_encontro_presenca ON turma_encontro_presenca (encontro_id)"
        };
        try (Statement st = con.createStatement()) {
            for (String comando : sql) {
                st.execute(comando);
            }
        }
        garantirColuna(con, "registro_treinamento", "data_fim", "TEXT");
    }

    private static void garantirColuna(Connection con, String tabela, String coluna, String tipo)
            throws SQLException {
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("PRAGMA table_info(" + tabela + ")")) {
            while (rs.next()) {
                if (coluna.equalsIgnoreCase(rs.getString("name"))) {
                    return;
                }
            }
        }
        try (Statement st = con.createStatement()) {
            st.execute("ALTER TABLE " + tabela + " ADD COLUMN " + coluna + " " + tipo);
        }
    }

    private static void criarAdminSeVazio(Connection con) throws SQLException {
        String sql = "INSERT INTO usuarios (nome, usuario, senha_hash, perfil, "
                + "ativo, deve_trocar_senha) "
                + "SELECT 'Administrador', 'admin', ?, 'ADMIN', 1, 1 "
                + "WHERE NOT EXISTS (SELECT 1 FROM usuarios)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, BCrypt.hashpw("admin123", BCrypt.gensalt(12)));
            ps.executeUpdate();
        }
    }

    private static void carregarNormas(Connection con) throws SQLException {
        if (contar(con, "norma") > 0) {
            return;
        }
        try (InputStream in = Conexao.class.getResourceAsStream("/dados/normas.csv")) {
            if (in == null) {
                return;
            }
            List<String[]> linhas = Csv.ler(in, ';');
            String sql = "INSERT OR REPLACE INTO norma (codigo, titulo, tipo, situacao, "
                    + "exige_treinamento, observacao) VALUES (?,?,?,?,?,?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (int i = 1; i < linhas.size(); i++) {
                    String[] l = linhas.get(i);
                    ps.setString(1, l[0]);
                    ps.setString(2, l[1]);
                    ps.setString(3, l[2]);
                    ps.setString(4, l[3]);
                    ps.setString(5, l[4]);
                    ps.setString(6, l.length > 5 ? l[5] : null);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        } catch (Exception e) {
            throw new SQLException("Falha ao carregar normas", e);
        }
    }

    private static void carregarMatriz(Connection con) throws SQLException {
        if (contar(con, "matriz_treinamento") > 0) {
            return;
        }
        try (InputStream in = Conexao.class.getResourceAsStream("/dados/matriz.csv")) {
            if (in == null) {
                return;
            }
            List<String[]> linhas = Csv.ler(in, ';');
            String sql = "INSERT INTO matriz_treinamento (norma, treinamento, criterio_tipo, "
                    + "criterio_valor, ch_minima, presencial_minimo, validade_meses, recorrencia, "
                    + "modalidade, ch_fixada, base_legal, observacao) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (int i = 1; i < linhas.size(); i++) {
                    String[] l = linhas.get(i);
                    ps.setString(1, l[0]);
                    ps.setString(2, l[2]);
                    ps.setString(3, l[3]);
                    ps.setString(4, vazio(l[4]));
                    ps.setObject(5, numero(l[5]));
                    ps.setObject(6, numero(l[6]));
                    ps.setObject(7, inteiro(l[7]));
                    ps.setString(8, vazio(l[8]));
                    ps.setString(9, vazio(l[9]));
                    ps.setString(10, vazio(l[10]));
                    ps.setString(11, vazio(l[11]));
                    ps.setString(12, vazio(l[12]));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        } catch (Exception e) {
            throw new SQLException("Falha ao carregar matriz", e);
        }
    }

    private static int contar(Connection con, String tabela) throws SQLException {
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + tabela)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static String vazio(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }

    private static Double numero(String v) {
        String t = vazio(v);
        if (t == null) {
            return null;
        }
        try {
            return Double.valueOf(t.replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer inteiro(String v) {
        Double d = numero(v);
        return d == null ? null : d.intValue();
    }
}
