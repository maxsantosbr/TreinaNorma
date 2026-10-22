package br.com.treinanorma.util;

import java.text.Normalizer;
import java.util.Locale;

public final class Ufs {

    public static final String VAZIO = "—";

    private static final String[][] DADOS = {
        {"AC", "Acre"},
        {"AL", "Alagoas"},
        {"AP", "Amapá"},
        {"AM", "Amazonas"},
        {"BA", "Bahia"},
        {"CE", "Ceará"},
        {"DF", "Distrito Federal"},
        {"ES", "Espírito Santo"},
        {"GO", "Goiás"},
        {"MA", "Maranhão"},
        {"MT", "Mato Grosso"},
        {"MS", "Mato Grosso do Sul"},
        {"MG", "Minas Gerais"},
        {"PA", "Pará"},
        {"PB", "Paraíba"},
        {"PR", "Paraná"},
        {"PE", "Pernambuco"},
        {"PI", "Piauí"},
        {"RJ", "Rio de Janeiro"},
        {"RN", "Rio Grande do Norte"},
        {"RS", "Rio Grande do Sul"},
        {"RO", "Rondônia"},
        {"RR", "Roraima"},
        {"SC", "Santa Catarina"},
        {"SP", "São Paulo"},
        {"SE", "Sergipe"},
        {"TO", "Tocantins"}
    };

    private Ufs() {
    }

    public static String[] siglas() {
        String[] siglas = new String[DADOS.length];
        for (int i = 0; i < DADOS.length; i++) {
            siglas[i] = DADOS[i][0];
        }
        return siglas;
    }

    public static String[] opcoes() {
        String[] opcoes = new String[DADOS.length + 1];
        opcoes[0] = VAZIO;
        for (int i = 0; i < DADOS.length; i++) {
            opcoes[i + 1] = DADOS[i][0];
        }
        return opcoes;
    }

    public static String sigla(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String chave = normalizar(valor);
        for (String[] uf : DADOS) {
            if (chave.equals(normalizar(uf[0])) || chave.equals(normalizar(uf[1]))) {
                return uf[0];
            }
        }
        return null;
    }

    public static String nome(String sigla) {
        String s = sigla(sigla);
        if (s == null) {
            return null;
        }
        for (String[] uf : DADOS) {
            if (uf[0].equals(s)) {
                return uf[1];
            }
        }
        return null;
    }

    private static String normalizar(String valor) {
        String semAcento = Normalizer.normalize(valor.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.toUpperCase(Locale.ROOT);
    }
}
