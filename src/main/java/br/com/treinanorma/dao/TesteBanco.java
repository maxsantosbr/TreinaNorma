package br.com.treinanorma.dao;

public class TesteBanco {

    public static void main(String[] args) {
        try (java.sql.Connection con = Conexao.abrir()) {
            System.out.println("Arquivo: " + Conexao.caminho());
            System.out.println("Conectado: " + !con.isClosed());
        } catch (Exception e) {
        }
    }
}