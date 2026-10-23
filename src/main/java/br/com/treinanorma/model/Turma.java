package br.com.treinanorma.model;

import java.time.LocalDate;

public class Turma {

    private int id;
    private String codigo;
    private Integer empresaId;
    private Integer matrizId;
    private String norma;
    private String treinamento;
    private LocalDate data;
    private LocalDate dataFim;
    private String horaInicio;
    private String horaFim;
    private String local;
    private String modalidade;
    private double chTotal;
    private double chPresencial;
    private Integer instrutorId;
    private String instrutorNome;
    private String conteudo;
    private String status = "AGENDADA";
    private String observacao;
    private String criadoPor;
    private String criadoEm;
    private String concluidaEm;
    private String hashAta;
    private int participantes;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String v) { this.codigo = v; }
    public Integer getEmpresaId() { return empresaId; }
    public void setEmpresaId(Integer v) { this.empresaId = v; }
    public Integer getMatrizId() { return matrizId; }
    public void setMatrizId(Integer v) { this.matrizId = v; }
    public String getNorma() { return norma; }
    public void setNorma(String v) { this.norma = v; }
    public String getTreinamento() { return treinamento; }
    public void setTreinamento(String v) { this.treinamento = v; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate v) { this.data = v; }
    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate v) { this.dataFim = v; }
    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String v) { this.horaInicio = v; }
    public String getHoraFim() { return horaFim; }
    public void setHoraFim(String v) { this.horaFim = v; }
    public String getLocal() { return local; }
    public void setLocal(String v) { this.local = v; }
    public String getModalidade() { return modalidade; }
    public void setModalidade(String v) { this.modalidade = v; }
    public double getChTotal() { return chTotal; }
    public void setChTotal(double v) { this.chTotal = v; }
    public double getChPresencial() { return chPresencial; }
    public void setChPresencial(double v) { this.chPresencial = v; }
    public Integer getInstrutorId() { return instrutorId; }
    public void setInstrutorId(Integer v) { this.instrutorId = v; }
    public String getInstrutorNome() { return instrutorNome; }
    public void setInstrutorNome(String v) { this.instrutorNome = v; }
    public String getConteudo() { return conteudo; }
    public void setConteudo(String v) { this.conteudo = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String v) { this.observacao = v; }
    public String getCriadoPor() { return criadoPor; }
    public void setCriadoPor(String v) { this.criadoPor = v; }
    public String getCriadoEm() { return criadoEm; }
    public void setCriadoEm(String v) { this.criadoEm = v; }
    public String getConcluidaEm() { return concluidaEm; }
    public void setConcluidaEm(String v) { this.concluidaEm = v; }
    public String getHashAta() { return hashAta; }
    public void setHashAta(String v) { this.hashAta = v; }
    public int getParticipantes() { return participantes; }
    public void setParticipantes(int v) { this.participantes = v; }

    public boolean concluida() {
        return "CONCLUIDA".equals(status);
    }
}
