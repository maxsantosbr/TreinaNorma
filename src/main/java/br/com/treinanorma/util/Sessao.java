package br.com.treinanorma.util;

import br.com.treinanorma.model.Usuario;

public final class Sessao {

    private static Usuario usuario;

    private Sessao() {
    }

    public static void iniciar(Usuario u) {
        usuario = u;
    }

    public static Usuario getUsuario() {
        return usuario;
    }

    public static void encerrar() {
        usuario = null;
    }

    public static String nome() {
        return usuario == null ? "sistema" : usuario.getNome();
    }
}