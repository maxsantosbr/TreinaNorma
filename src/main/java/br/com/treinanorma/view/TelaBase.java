package br.com.treinanorma.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public abstract class TelaBase extends JPanel {

    private final Runnable voltar;
    private final JPanel acoes = new JPanel(new GridLayout(1, 0, 8, 0));
    private final JPanel corpo = new JPanel(new BorderLayout());
    private final JPanel topo;

    protected TelaBase(String titulo, String subtitulo, Runnable voltar) {
        super(new BorderLayout(0, 14));
        this.voltar = voltar;
        setBackground(Ui.FUNDO);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 20, 20));

        topo = new JPanel(new BorderLayout(12, 0));
        topo.setOpaque(false);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));

        JButton btnVoltar = Ui.link("<- Dashboard");
        btnVoltar.setFont(Ui.fonte(11, Font.PLAIN));
        btnVoltar.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
        btnVoltar.setAlignmentX(LEFT_ALIGNMENT);
        btnVoltar.addActionListener(e -> this.voltar.run());
        textos.add(btnVoltar);
        textos.add(Box.createVerticalStrut(2));

        JLabel t = Ui.titulo(titulo);
        t.setAlignmentX(LEFT_ALIGNMENT);
        textos.add(t);
        textos.add(Box.createVerticalStrut(2));

        JLabel s = Ui.subtitulo(subtitulo);
        s.setAlignmentX(LEFT_ALIGNMENT);
        textos.add(s);

        topo.add(textos, BorderLayout.WEST);

        acoes.setOpaque(false);
        JPanel direita = new JPanel(new BorderLayout());
        direita.setOpaque(false);
        direita.add(acoes, BorderLayout.NORTH);
        topo.add(direita, BorderLayout.EAST);

        add(topo, BorderLayout.NORTH);

        corpo.setOpaque(false);
        add(corpo, BorderLayout.CENTER);
    }

    private boolean construida;

    @Override
    public void addNotify() {
        super.addNotify();
        if (!construida) {
            construida = true;
            construir();
        }
    }

    protected abstract void construir();

    protected <T extends Component> T acao(T componente) {
        acoes.add(componente);
        return componente;
    }

    protected void conteudo(Component componente) {
        corpo.removeAll();
        corpo.add(componente, BorderLayout.CENTER);
        corpo.revalidate();
        corpo.repaint();
    }

    protected JComponent conteudoAtual() {
        return corpo.getComponentCount() == 0 ? null : (JComponent) corpo.getComponent(0);
    }

    protected void irParaDashboard() {
        voltar.run();
    }
}
