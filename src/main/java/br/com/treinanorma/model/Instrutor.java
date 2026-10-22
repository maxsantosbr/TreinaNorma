package br.com.treinanorma.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Instrutor {

    public static class Habilitacao {

        private String norma;
        private LocalDate validade;

        public Habilitacao(String norma, LocalDate validade) {
            this.norma = norma;
            this.validade = validade;
        }

        public String getNorma() { return norma; }
        public LocalDate getValidade() { return validade; }

        public boolean vencida() {
            return validade != null && validade.isBefore(LocalDate.now());
        }
    }

    private int id;
    private String nome;
    private String cpf;
    private String formacao;
    private String registro;
    private String tipo = "INTERNO";
    private String email;
    private String telefone;
    private String assinatura;
    private boolean ativo = true;
    private final List<Habilitacao> habilitacoes = new ArrayList<>();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String v) { this.nome = v; }
    public String getCpf() { return cpf; }
    public void setCpf(String v) { this.cpf = v; }
    public String getFormacao() { return formacao; }
    public void setFormacao(String v) { this.formacao = v; }
    public String getRegistro() { return registro; }
    public void setRegistro(String v) { this.registro = v; }
    public String getTipo() { return tipo; }
    public void setTipo(String v) { this.tipo = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String v) { this.telefone = v; }
    public String getAssinatura() { return assinatura; }
    public void setAssinatura(String v) { this.assinatura = v; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean v) { this.ativo = v; }
    public List<Habilitacao> getHabilitacoes() { return habilitacoes; }

    public boolean habilitadoPara(String norma) {
        for (Habilitacao h : habilitacoes) {
            if (h.getNorma().equalsIgnoreCase(norma) && !h.vencida()) {
                return true;
            }
        }
        return false;
    }
}
