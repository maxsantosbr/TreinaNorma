package br.com.treinanorma.model;

public class Empresa {

    private int id;
    private String razaoSocial;
    private String nomeFantasia;
    private String cnpj;
    private String cnae;
    private Integer grauRisco;
    private String endereco;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String uf;
    private String cep;
    private String telefone;
    private String email;
    private String logo;
    private String assinatura;
    private String responsavelNome;
    private String responsavelRegistro;
    private String responsavelProfissao;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String v) { this.razaoSocial = v; }
    public String getNomeFantasia() { return nomeFantasia; }
    public void setNomeFantasia(String v) { this.nomeFantasia = v; }
    public String getCnpj() { return cnpj; }
    public void setCnpj(String v) { this.cnpj = v; }
    public String getCnae() { return cnae; }
    public void setCnae(String v) { this.cnae = v; }
    public Integer getGrauRisco() { return grauRisco; }
    public void setGrauRisco(Integer v) { this.grauRisco = v; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String v) { this.endereco = v; }
    public String getNumero() { return numero; }
    public void setNumero(String v) { this.numero = v; }
    public String getComplemento() { return complemento; }
    public void setComplemento(String v) { this.complemento = v; }
    public String getBairro() { return bairro; }
    public void setBairro(String v) { this.bairro = v; }
    public String getCidade() { return cidade; }
    public void setCidade(String v) { this.cidade = v; }
    public String getUf() { return uf; }
    public void setUf(String v) { this.uf = v; }
    public String getCep() { return cep; }
    public void setCep(String v) { this.cep = v; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String v) { this.telefone = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getLogo() { return logo; }
    public void setLogo(String v) { this.logo = v; }
    public String getAssinatura() { return assinatura; }
    public void setAssinatura(String v) { this.assinatura = v; }
    public String getResponsavelNome() { return responsavelNome; }
    public void setResponsavelNome(String v) { this.responsavelNome = v; }
    public String getResponsavelRegistro() { return responsavelRegistro; }
    public void setResponsavelRegistro(String v) { this.responsavelRegistro = v; }
    public String getResponsavelProfissao() { return responsavelProfissao; }
    public void setResponsavelProfissao(String v) { this.responsavelProfissao = v; }

    public String enderecoCompleto() {
        StringBuilder sb = new StringBuilder();
        if (endereco != null) {
            sb.append(endereco);
        }
        if (numero != null) {
            sb.append(", ").append(numero);
        }
        if (complemento != null && !complemento.isBlank()) {
            sb.append(" - ").append(complemento);
        }
        if (bairro != null) {
            sb.append(" - ").append(bairro);
        }
        if (cidade != null) {
            sb.append(" - ").append(cidade);
        }
        if (uf != null) {
            sb.append("/").append(uf);
        }
        if (cep != null) {
            sb.append(" - CEP ").append(cep);
        }
        return sb.toString();
    }

    public String nomeExibicao() {
        return nomeFantasia == null || nomeFantasia.isBlank() ? razaoSocial : nomeFantasia;
    }
}
