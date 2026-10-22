package br.com.treinanorma.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class Datas {

    public static final DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;
    public static final DateTimeFormatter BR_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    private Datas() {
    }

    public static String iso(LocalDate data) {
        return data == null ? null : data.format(ISO);
    }

    public static String br(LocalDate data) {
        return data == null ? "" : data.format(BR);
    }

    public static String brHora(LocalDateTime data) {
        return data == null ? "" : data.format(BR_HORA);
    }

    /** Lê o timestamp UTC do SQLite (yyyy-MM-dd HH:mm:ss) e converte para o fuso local. */
    public static LocalDateTime lerDataHora(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            LocalDateTime utc = LocalDateTime.parse(valor.trim().replace(' ', 'T'));
            return utc.atOffset(ZoneOffset.UTC).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static LocalDate ler(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String t = valor.trim();
        try {
            if (t.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
                return LocalDate.parse(t.substring(0, 10));
            }
            if (t.matches("\\d{2}/\\d{2}/\\d{4}")) {
                return LocalDate.parse(t, BR);
            }
            if (t.matches("\\d{2}-\\d{2}-\\d{4}")) {
                return LocalDate.parse(t, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            }
        } catch (DateTimeParseException e) {
            return null;
        }
        return null;
    }

    public static LocalDate validade(LocalDate conclusao, Integer meses) {
        if (conclusao == null || meses == null || meses <= 0) {
            return null;
        }
        return conclusao.plusMonths(meses);
    }

    public static String hojeIso() {
        return iso(LocalDate.now());
    }
}
