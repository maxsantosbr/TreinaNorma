package br.com.treinanorma.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Hash {

    private Hash() {
    }

    public static String sha256(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }

    public static String codigoVerificacao(String hash) {
        if (hash == null || hash.length() < 12) {
            return "";
        }
        String h = hash.substring(0, 12).toUpperCase();
        return h.substring(0, 4) + "-" + h.substring(4, 8) + "-" + h.substring(8, 12);
    }
}
