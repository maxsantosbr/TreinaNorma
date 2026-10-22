package br.com.treinanorma.model;

public class Usuario {

    private int id;
    private String nome;
    private String usuario;
    private Perfil perfil;
    private boolean deveTrocarSenha;
    private boolean ativo = true;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }
    public boolean isDeveTrocarSenha() { return deveTrocarSenha; }
    public void setDeveTrocarSenha(boolean v) { this.deveTrocarSenha = v; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean v) { this.ativo = v; }
}
