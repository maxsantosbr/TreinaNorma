package br.com.treinanorma.model;

public class SolicitacaoSenha {

    private int id;
    private String protocolo;
    private Integer usuarioId;
    private String login;
    private String status;
    private String criadoEm;
    private Integer atendidoPor;
    private String atendidoPorNome;
    private String atendidoEm;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getProtocolo() { return protocolo; }
    public void setProtocolo(String protocolo) { this.protocolo = protocolo; }
    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCriadoEm() { return criadoEm; }
    public void setCriadoEm(String criadoEm) { this.criadoEm = criadoEm; }
    public Integer getAtendidoPor() { return atendidoPor; }
    public void setAtendidoPor(Integer atendidoPor) { this.atendidoPor = atendidoPor; }
    public String getAtendidoPorNome() { return atendidoPorNome; }
    public void setAtendidoPorNome(String atendidoPorNome) { this.atendidoPorNome = atendidoPorNome; }
    public String getAtendidoEm() { return atendidoEm; }
    public void setAtendidoEm(String atendidoEm) { this.atendidoEm = atendidoEm; }

    public boolean isPendente() {
        return "PENDENTE".equals(status);
    }
}
