package br.com.treinanorma.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RegistroTreinamento {

    private int id;
    private int colaboradorId;
    private String colaboradorNome;
    private String colaboradorCpf;
    private String colaboradorCargo;
    private Integer turmaId;
    private String norma;
    private String treinamento;
    private LocalDate data;
    private LocalDate dataFim;
    private double ch;
    private Integer instrutorId;
    private String instrutorNome;
    private Integer validadeMeses;
    private LocalDate validade;
    private String situacao = "VALIDO";
    private String numeroCertificado;
    private String codigoVerificacao;
    private LocalDateTime emitidoEm;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getColaboradorId() { return colaboradorId; }
    public void setColaboradorId(int v) { this.colaboradorId = v; }
    public String getColaboradorNome() { return colaboradorNome; }
    public void setColaboradorNome(String v) { this.colaboradorNome = v; }
    public String getColaboradorCpf() { return colaboradorCpf; }
    public void setColaboradorCpf(String v) { this.colaboradorCpf = v; }
    public String getColaboradorCargo() { return colaboradorCargo; }
    public void setColaboradorCargo(String v) { this.colaboradorCargo = v; }
    public Integer getTurmaId() { return turmaId; }
    public void setTurmaId(Integer v) { this.turmaId = v; }
    public String getNorma() { return norma; }
    public void setNorma(String v) { this.norma = v; }
    public String getTreinamento() { return treinamento; }
    public void setTreinamento(String v) { this.treinamento = v; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate v) { this.data = v; }
    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate v) { this.dataFim = v; }
    public double getCh() { return ch; }
    public void setCh(double v) { this.ch = v; }
    public Integer getInstrutorId() { return instrutorId; }
    public void setInstrutorId(Integer v) { this.instrutorId = v; }
    public String getInstrutorNome() { return instrutorNome; }
    public void setInstrutorNome(String v) { this.instrutorNome = v; }
    public Integer getValidadeMeses() { return validadeMeses; }
    public void setValidadeMeses(Integer v) { this.validadeMeses = v; }
    public LocalDate getValidade() { return validade; }
    public void setValidade(LocalDate v) { this.validade = v; }
    public String getSituacao() { return situacao; }
    public void setSituacao(String v) { this.situacao = v; }
    public String getNumeroCertificado() { return numeroCertificado; }
    public void setNumeroCertificado(String v) { this.numeroCertificado = v; }
    public String getCodigoVerificacao() { return codigoVerificacao; }
    public void setCodigoVerificacao(String v) { this.codigoVerificacao = v; }
    public LocalDateTime getEmitidoEm() { return emitidoEm; }
    public void setEmitidoEm(LocalDateTime v) { this.emitidoEm = v; }

    public String situacaoCalculada() {
        if (validade == null) {
            return "REVISAR PGR";
        }
        LocalDate hoje = LocalDate.now();
        if (validade.isBefore(hoje)) {
            return "VENCIDO";
        }
        if (validade.isBefore(hoje.plusDays(31))) {
            return "VENCE 30D";
        }
        return "VALIDO";
    }
}
