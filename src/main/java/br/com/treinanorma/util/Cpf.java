package br.com.treinanorma.util;

public final class Cpf {

    private Cpf() {
    }

    public static String limpar(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replaceAll("\\D", "");
    }

    public static String formatar(String valor) {
        String d = limpar(valor);
        if (d.length() != 11) {
            return valor == null ? "" : valor.trim();
        }
        return d.substring(0, 3) + "." + d.substring(3, 6) + "."
                + d.substring(6, 9) + "-" + d.substring(9);
    }

    public static String mascarar(String valor) {
        String d = limpar(valor);
        if (d.length() != 11) {
            return formatar(valor);
        }
        return "***." + d.substring(3, 6) + "." + d.substring(6, 9) + "-**";
    }

    public static boolean valido(String valor) {
        String d = limpar(valor);
        if (d.length() != 11 || d.chars().distinct().count() == 1) {
            return false;
        }
        try {
            int soma = 0;
            for (int i = 0; i < 9; i++) {
                soma += (d.charAt(i) - '0') * (10 - i);
            }
            int resto = (soma * 10) % 11;
            int dv1 = resto == 10 ? 0 : resto;
            if (dv1 != d.charAt(9) - '0') {
                return false;
            }
            soma = 0;
            for (int i = 0; i < 10; i++) {
                soma += (d.charAt(i) - '0') * (11 - i);
            }
            resto = (soma * 10) % 11;
            int dv2 = resto == 10 ? 0 : resto;
            return dv2 == d.charAt(10) - '0';
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
