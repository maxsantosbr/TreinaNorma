package br.com.treinanorma.util;

import br.com.treinanorma.model.Perfil;
import br.com.treinanorma.model.Usuario;

public final class Permissoes {

    private Permissoes() {
    }

    public static Perfil perfil() {
        Usuario usuario = Sessao.getUsuario();
        return usuario == null ? Perfil.CONSULTA : usuario.getPerfil();
    }

    public static boolean admin() {
        return perfil() == Perfil.ADMIN;
    }

    public static boolean operador() {
        return perfil() != Perfil.CONSULTA;
    }

    public static boolean somenteLeitura() {
        return perfil() == Perfil.CONSULTA;
    }
}
