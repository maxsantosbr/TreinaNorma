package br.com.treinanorma.model;

public class Norma {

    private String codigo;
    private String titulo;
    private String tipo;
    private String situacao;
    private String exigeTreinamento;
    private String observacao;

    public String getCodigo() { return codigo; }
    public void setCodigo(String v) { this.codigo = v; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String v) { this.titulo = v; }
    public String getTipo() { return tipo; }
    public void setTipo(String v) { this.tipo = v; }
    public String getSituacao() { return situacao; }
    public void setSituacao(String v) { this.situacao = v; }
    public String getExigeTreinamento() { return exigeTreinamento; }
    public void setExigeTreinamento(String v) { this.exigeTreinamento = v; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String v) { this.observacao = v; }

    @Override
    public String toString() {
        return codigo + " - " + titulo;
    }
}
