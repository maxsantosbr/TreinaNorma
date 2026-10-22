package br.com.treinanorma.model;

public class MatrizItem {

    private int id;
    private String norma;
    private String treinamento;
    private String criterioTipo;
    private String criterioValor;
    private Double chMinima;
    private Double presencialMinimo;
    private Integer validadeMeses;
    private String recorrencia;
    private String modalidade;
    private String chFixada;
    private String baseLegal;
    private String observacao;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNorma() { return norma; }
    public void setNorma(String v) { this.norma = v; }
    public String getTreinamento() { return treinamento; }
    public void setTreinamento(String v) { this.treinamento = v; }
    public String getCriterioTipo() { return criterioTipo; }
    public void setCriterioTipo(String v) { this.criterioTipo = v; }
    public String getCriterioValor() { return criterioValor; }
    public void setCriterioValor(String v) { this.criterioValor = v; }
    public Double getChMinima() { return chMinima; }
    public void setChMinima(Double v) { this.chMinima = v; }
    public Double getPresencialMinimo() { return presencialMinimo; }
    public void setPresencialMinimo(Double v) { this.presencialMinimo = v; }
    public Integer getValidadeMeses() { return validadeMeses; }
    public void setValidadeMeses(Integer v) { this.validadeMeses = v; }
    public String getRecorrencia() { return recorrencia; }
    public void setRecorrencia(String v) { this.recorrencia = v; }
    public String getModalidade() { return modalidade; }
    public void setModalidade(String v) { this.modalidade = v; }
    public String getChFixada() { return chFixada; }
    public void setChFixada(String v) { this.chFixada = v; }
    public String getBaseLegal() { return baseLegal; }
    public void setBaseLegal(String v) { this.baseLegal = v; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String v) { this.observacao = v; }

    @Override
    public String toString() {
        String ch = chMinima == null ? "CH a definir" : fmt(chMinima) + " h";
        return norma + " - " + treinamento + " (" + ch + ")";
    }

    private String fmt(Double v) {
        return v == Math.rint(v) ? String.valueOf(v.intValue()) : String.valueOf(v);
    }
}
