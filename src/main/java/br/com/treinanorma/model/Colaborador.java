package br.com.treinanorma.model;

import java.time.LocalDate;

public class Colaborador {

    private int id;
    private Integer empresaId;
    private Integer estabelecimentoId;
    private Integer setorId;
    private String setorNome;
    private String estabelecimentoNome;
    private String nome;
    private String cpf;
    private String matricula;
    private String cargo;
    private LocalDate admissao;
    private LocalDate demissao;
    private String email;
    private String telefone;
    private String foto;
    private boolean ativo = true;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Integer getEmpresaId() { return empresaId; }
    public void setEmpresaId(Integer v) { this.empresaId = v; }
    public Integer getEstabelecimentoId() { return estabelecimentoId; }
    public void setEstabelecimentoId(Integer v) { this.estabelecimentoId = v; }
    public Integer getSetorId() { return setorId; }
    public void setSetorId(Integer v) { this.setorId = v; }
    public String getSetorNome() { return setorNome; }
    public void setSetorNome(String v) { this.setorNome = v; }
    public String getEstabelecimentoNome() { return estabelecimentoNome; }
    public void setEstabelecimentoNome(String v) { this.estabelecimentoNome = v; }
    public String getNome() { return nome; }
    public void setNome(String v) { this.nome = v; }
    public String getCpf() { return cpf; }
    public void setCpf(String v) { this.cpf = v; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String v) { this.matricula = v; }
    public String getCargo() { return cargo; }
    public void setCargo(String v) { this.cargo = v; }
    public LocalDate getAdmissao() { return admissao; }
    public void setAdmissao(LocalDate v) { this.admissao = v; }
    public LocalDate getDemissao() { return demissao; }
    public void setDemissao(LocalDate v) { this.demissao = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String v) { this.telefone = v; }
    public String getFoto() { return foto; }
    public void setFoto(String v) { this.foto = v; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean v) { this.ativo = v; }
}
