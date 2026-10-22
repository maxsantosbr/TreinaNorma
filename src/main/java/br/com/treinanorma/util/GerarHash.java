package br.com.treinanorma.util;

import org.mindrot.jbcrypt.BCrypt;

public class GerarHash {

    public static void main(String[] args) {
        System.out.println(BCrypt.hashpw("senha123", BCrypt.gensalt(12)));
    }
}