package br.com.treinanorma.view;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class PainelFundo extends JPanel {

    private Image imagem;

    public PainelFundo() {
        URL url = getClass().getResource("/images/login.png");
        if (url == null) {
            url = Thread.currentThread().getContextClassLoader()
                    .getResource("images/login.png");
        }
        if (url != null) {
            imagem = new javax.swing.ImageIcon(url).getImage();
        }
    }

    public void setImagem(Image imagem) {
        this.imagem = imagem;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (imagem == null) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2.drawImage(imagem, 0, 0, getWidth(), getHeight(), this);
        g2.dispose();
    }
}
