package br.com.treinanorma.util;

import java.awt.Image;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;

public final class Icones {

    private Icones() {
    }

    public static ImageIcon carregar(String caminho) {
        URL url = Icones.class.getResource(caminho);
        if (url == null) {
            throw new IllegalArgumentException("Imagem não encontrada: " + caminho);
        }
        return new ImageIcon(url);
    }

    public static void aplicarIcone(java.awt.Window janela) {
        Image base = carregar("/icones/converterICO.png").getImage();
        List<Image> lista = new ArrayList<>();
        for (int lado : new int[]{16, 20, 24, 32, 40, 48, 64}) {
            lista.add(base.getScaledInstance(lado, lado, Image.SCALE_SMOOTH));
        }
        janela.setIconImages(lista);
    }
}