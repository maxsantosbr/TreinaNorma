package br.com.treinanorma.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class Csv {

    private Csv() {
    }

    public static List<String[]> ler(InputStream entrada, char separador) throws IOException {
        try (Reader leitor = new InputStreamReader(entrada, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(leitor)) {
            return ler(br, separador);
        }
    }

    public static List<String[]> ler(BufferedReader br, char separador) throws IOException {
        List<String[]> linhas = new ArrayList<>();
        String linha;
        StringBuilder acumulado = null;
        while ((linha = br.readLine()) != null) {
            if (acumulado == null) {
                acumulado = new StringBuilder();
            } else {
                acumulado.append('\n');
            }
            acumulado.append(linha);
            if (aspasAbertas(acumulado.toString())) {
                continue;
            }
            linhas.add(quebrar(acumulado.toString(), separador));
            acumulado = null;
        }
        if (acumulado != null && acumulado.length() > 0) {
            linhas.add(quebrar(acumulado.toString(), separador));
        }
        return linhas;
    }

    private static boolean aspasAbertas(String texto) {
        boolean dentro = false;
        for (int i = 0; i < texto.length(); i++) {
            if (texto.charAt(i) == '"') {
                dentro = !dentro;
            }
        }
        return dentro;
    }

    public static String[] quebrar(String linha, char separador) {
        List<String> campos = new ArrayList<>();
        StringBuilder atual = new StringBuilder();
        boolean dentro = false;
        for (int i = 0; i < linha.length(); i++) {
            char c = linha.charAt(i);
            if (c == '"') {
                if (dentro && i + 1 < linha.length() && linha.charAt(i + 1) == '"') {
                    atual.append('"');
                    i++;
                } else {
                    dentro = !dentro;
                }
            } else if (c == separador && !dentro) {
                campos.add(atual.toString());
                atual.setLength(0);
            } else {
                atual.append(c);
            }
        }
        campos.add(atual.toString());
        return campos.toArray(String[]::new);
    }

    public static String campo(String valor) {
        if (valor == null) {
            return "\"\"";
        }
        return "\"" + valor.replace("\"", "\"\"") + "\"";
    }

    public static String linha(String... valores) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < valores.length; i++) {
            if (i > 0) {
                sb.append(';');
            }
            sb.append(campo(valores[i]));
        }
        return sb.toString();
    }
}
