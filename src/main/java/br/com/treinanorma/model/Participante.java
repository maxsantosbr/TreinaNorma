package br.com.treinanorma.model;

public class Participante {

    private int id;
    private int turmaId;
    private int colaboradorId;
    private String nome;
    private String cpf;
    private String cargo;
    private String setor;
    private boolean presente;
    private Boolean aprovado;
    private String assinatura;
    private boolean eletronica;
    private String observacao;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTurmaId() { return turmaId; }
    public void setTurmaId(int v) { this.turmaId = v; }
    public int getColaboradorId() { return colaboradorId; }
    public void setColaboradorId(int v) { this.colaboradorId = v; }
    public String getNome() { return nome; }
    public void setNome(String v) { this.nome = v; }
    public String getCpf() { return cpf; }
    public void setCpf(String v) { this.cpf = v; }
    public String getCargo() { return cargo; }
    public void setCargo(String v) { this.cargo = v; }
    public String getSetor() { return setor; }
    public void setSetor(String v) { this.setor = v; }
    public boolean isPresente() { return presente; }
    public void setPresente(boolean v) { this.presente = v; }
    public Boolean getAprovado() { return aprovado; }
    public void setAprovado(Boolean v) { this.aprovado = v; }
    public String getAssinatura() { return assinatura; }
    public void setAssinatura(String v) { this.assinatura = v; }
    public boolean isEletronica() { return eletronica; }
    public void setEletronica(boolean v) { this.eletronica = v; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String v) { this.observacao = v; }
}
