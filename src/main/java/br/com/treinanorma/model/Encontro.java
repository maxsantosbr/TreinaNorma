package br.com.treinanorma.model;

import java.time.LocalDate;

public class Encontro {

    private int id;
    private int turmaId;
    private int ordem;
    private LocalDate data;
    private String horaInicio;
    private String horaFim;
    private double ch;
    private boolean pratica;
    private String conteudo;

    public int getId() { return id; }
    public void setId(int v) { this.id = v; }
    public int getTurmaId() { return turmaId; }
    public void setTurmaId(int v) { this.turmaId = v; }
    public int getOrdem() { return ordem; }
    public void setOrdem(int v) { this.ordem = v; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate v) { this.data = v; }
    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String v) { this.horaInicio = v; }
    public String getHoraFim() { return horaFim; }
    public void setHoraFim(String v) { this.horaFim = v; }
    public double getCh() { return ch; }
    public void setCh(double v) { this.ch = v; }
    public boolean isPratica() { return pratica; }
    public void setPratica(boolean v) { this.pratica = v; }
    public String getConteudo() { return conteudo; }
    public void setConteudo(String v) { this.conteudo = v; }

    public String modalidade() {
        return pratica ? "Prática presencial" : "Teórica presencial";
    }
}
