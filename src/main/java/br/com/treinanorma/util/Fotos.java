package br.com.treinanorma.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import javax.imageio.ImageIO;

/** Armazenamento e tratamento das fotos 3x4 dos colaboradores. */
public final class Fotos {

    public static final long LIMITE_BYTES = 10L * 1024 * 1024;
    private static final int LARGURA_FINAL = 600;
    private static final int ALTURA_FINAL = 800;
    private static final int LADO_MAXIMO_ORIGEM = 2000;

    private Fotos() {
    }

    /** Pasta gerenciada pelo aplicativo: ~/TreinaNorma/Fotos. */
    public static File pasta() {
        File pasta = new File(System.getProperty("user.home") + File.separator
                + "TreinaNorma" + File.separator + "Fotos");
        pasta.mkdirs();
        return pasta;
    }

    /**
     * Resolve um caminho salvo no banco. Aceita caminho absoluto (cadastros antigos)
     * ou relativo à pasta ~/TreinaNorma (formato novo: Fotos/arquivo.jpg).
     */
    public static File arquivo(String caminho) {
        if (caminho == null || caminho.isBlank()) {
            return null;
        }
        File informado = new File(caminho);
        if (informado.isAbsolute()) {
            return informado;
        }
        File raiz = new File(System.getProperty("user.home"), "TreinaNorma");
        File relativo = new File(raiz, caminho);
        if (relativo.exists()) {
            return relativo;
        }
        return informado;
    }

    public static BufferedImage carregar(String caminho) {
        File arquivo = arquivo(caminho);
        if (arquivo == null || !arquivo.isFile()) {
            return null;
        }
        try {
            return ler(arquivo);
        } catch (IOException e) {
            return null;
        }
    }

    public static BufferedImage ler(File arquivo) throws IOException {
        if (arquivo == null || !arquivo.isFile()) {
            throw new IOException("Arquivo de imagem não encontrado.");
        }
        if (arquivo.length() > LIMITE_BYTES) {
            throw new IOException("A imagem deve ter no máximo 10 MB.");
        }
        BufferedImage imagem = ImageIO.read(arquivo);
        if (imagem == null) {
            throw new IOException("Formato de imagem não suportado. Use JPG, PNG ou BMP.");
        }
        imagem = aplicarOrientacao(imagem, orientacaoJpeg(arquivo));
        return limitar(imagem);
    }

    /** Importa uma imagem, recorta no formato 3x4 e grava na pasta gerenciada. */
    public static String importar(File origem) throws IOException {
        BufferedImage imagem = ler(origem);
        return salvar(recortar3x4(imagem, LARGURA_FINAL, ALTURA_FINAL));
    }

    /** Grava uma imagem já tratada e devolve o caminho relativo salvo no banco. */
    public static String salvar(BufferedImage imagem) throws IOException {
        if (imagem == null) {
            throw new IOException("Imagem vazia.");
        }
        BufferedImage rgb = new BufferedImage(imagem.getWidth(), imagem.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
        g.drawImage(imagem, 0, 0, null);
        g.dispose();

        File destino = new File(pasta(), UUID.randomUUID().toString() + ".jpg");
        if (!ImageIO.write(rgb, "jpg", destino)) {
            throw new IOException("Não foi possível gravar a foto.");
        }
        return "Fotos/" + destino.getName();
    }

    /** Remove somente arquivos que estejam dentro da pasta gerenciada. */
    public static void remover(String caminho) {
        File arquivo = arquivo(caminho);
        if (arquivo == null || !arquivo.isFile()) {
            return;
        }
        try {
            String raiz = pasta().getCanonicalPath() + File.separator;
            String alvo = arquivo.getCanonicalPath();
            if (alvo.startsWith(raiz)) {
                Files.deleteIfExists(arquivo.toPath());
            }
        } catch (IOException ignored) {
        }
    }

    public static BufferedImage recortar3x4(BufferedImage origem, int largura, int altura) {
        if (origem == null) {
            return null;
        }
        double proporcao = (double) largura / altura;
        int w = origem.getWidth();
        int h = origem.getHeight();
        int recorteW;
        int recorteH;
        if ((double) w / h > proporcao) {
            recorteH = h;
            recorteW = (int) Math.round(h * proporcao);
        } else {
            recorteW = w;
            recorteH = (int) Math.round(w / proporcao);
        }
        int x = (w - recorteW) / 2;
        int y = (h - recorteH) / 2;

        BufferedImage destino = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = destino.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, largura, altura);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(origem, 0, 0, largura, altura,
                x, y, x + recorteW, y + recorteH, null);
        g.dispose();
        return destino;
    }

    private static BufferedImage limitar(BufferedImage imagem) {
        int w = imagem.getWidth();
        int h = imagem.getHeight();
        int maior = Math.max(w, h);
        if (maior <= LADO_MAXIMO_ORIGEM) {
            return imagem;
        }
        double escala = LADO_MAXIMO_ORIGEM / (double) maior;
        int nw = Math.max(1, (int) Math.round(w * escala));
        int nh = Math.max(1, (int) Math.round(h * escala));
        BufferedImage reduzida = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = reduzida.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, nw, nh);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(imagem, 0, 0, nw, nh, null);
        g.dispose();
        return reduzida;
    }

    private static BufferedImage aplicarOrientacao(BufferedImage origem, int orientacao) {
        if (orientacao < 2 || orientacao > 8) {
            return origem;
        }
        int w = origem.getWidth();
        int h = origem.getHeight();
        boolean troca = orientacao >= 5;
        BufferedImage destino = new BufferedImage(troca ? h : w, troca ? w : h,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = destino.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, destino.getWidth(), destino.getHeight());
        AffineTransform t = new AffineTransform();
        switch (orientacao) {
            case 2 -> {
                t.translate(w, 0);
                t.scale(-1, 1);
            }
            case 3 -> {
                t.translate(w, h);
                t.rotate(Math.PI);
            }
            case 4 -> {
                t.translate(0, h);
                t.scale(1, -1);
            }
            case 5 -> {
                t.rotate(-Math.PI / 2);
                t.scale(-1, 1);
            }
            case 6 -> {
                t.translate(h, 0);
                t.rotate(Math.PI / 2);
            }
            case 7 -> {
                t.scale(-1, 1);
                t.translate(-h, 0);
                t.rotate(-Math.PI / 2);
            }
            case 8 -> {
                t.translate(0, w);
                t.rotate(-Math.PI / 2);
            }
            default -> {
            }
        }
        g.setTransform(t);
        g.drawImage(origem, 0, 0, null);
        g.dispose();
        return destino;
    }

    /** Lê o tag EXIF Orientation (0x0112) sem depender de biblioteca externa. */
    private static int orientacaoJpeg(File arquivo) {
        try {
            byte[] dados = Files.readAllBytes(arquivo.toPath());
            int i = 2;
            while (i + 4 < dados.length) {
                if ((dados[i] & 0xFF) != 0xFF) {
                    i++;
                    continue;
                }
                int marcador = dados[i + 1] & 0xFF;
                if (marcador == 0xD8 || marcador == 0xD9) {
                    i += 2;
                    continue;
                }
                if (marcador == 0xDA) {
                    break;
                }
                int tamanho = ((dados[i + 2] & 0xFF) << 8) | (dados[i + 3] & 0xFF);
                if (tamanho < 2) {
                    break;
                }
                if (marcador == 0xE1 && i + 10 < dados.length
                        && dados[i + 4] == 'E' && dados[i + 5] == 'x'
                        && dados[i + 6] == 'i' && dados[i + 7] == 'f'
                        && dados[i + 8] == 0 && dados[i + 9] == 0) {
                    return orientacaoTiff(dados, i + 10);
                }
                i += 2 + tamanho;
            }
        } catch (Exception ignored) {
        }
        return 1;
    }

    private static int orientacaoTiff(byte[] dados, int base) {
        if (base + 8 > dados.length) {
            return 1;
        }
        boolean little = dados[base] == 'I' && dados[base + 1] == 'I';
        boolean big = dados[base] == 'M' && dados[base + 1] == 'M';
        if (!little && !big) {
            return 1;
        }
        int ifd = lerInt(dados, base + 4, little);
        int pos = base + ifd;
        if (pos < 0 || pos + 2 > dados.length) {
            return 1;
        }
        int entradas = lerShort(dados, pos, little);
        pos += 2;
        for (int k = 0; k < entradas && pos + 12 <= dados.length; k++, pos += 12) {
            int tag = lerShort(dados, pos, little);
            if (tag == 0x0112) {
                int tipo = lerShort(dados, pos + 2, little);
                int quantidade = lerInt(dados, pos + 4, little);
                if (tipo == 3 && quantidade >= 1) {
                    return lerShort(dados, pos + 8, little);
                }
            }
        }
        return 1;
    }

    private static int lerShort(byte[] dados, int pos, boolean little) {
        int a = dados[pos] & 0xFF;
        int b = dados[pos + 1] & 0xFF;
        return little ? (b << 8) | a : (a << 8) | b;
    }

    private static int lerInt(byte[] dados, int pos, boolean little) {
        int a = dados[pos] & 0xFF;
        int b = dados[pos + 1] & 0xFF;
        int c = dados[pos + 2] & 0xFF;
        int d = dados[pos + 3] & 0xFF;
        return little ? (d << 24) | (c << 16) | (b << 8) | a
                : (a << 24) | (b << 16) | (c << 8) | d;
    }
}
