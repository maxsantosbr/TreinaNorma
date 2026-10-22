package br.com.treinanorma.util;

import br.com.treinanorma.dao.ColaboradorDAO;
import br.com.treinanorma.dao.RegistroDAO;
import br.com.treinanorma.model.Colaborador;
import br.com.treinanorma.model.Empresa;
import br.com.treinanorma.model.Instrutor;
import br.com.treinanorma.model.Participante;
import br.com.treinanorma.model.RegistroTreinamento;
import br.com.treinanorma.model.Turma;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Utilities;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class Documentos {

    private static final Color AZUL = new Color(31, 78, 121);
    private static final Color AZUL_CLARO = new Color(232, 242, 251);
    private static final Color CINZA = new Color(107, 114, 128);
    private static final Color BORDA = new Color(203, 213, 225);
    private static final Color GRAFITE = new Color(31, 41, 55);

    private Documentos() {
    }

    public static File pasta() {
        File pasta = new File(System.getProperty("user.home") + File.separator
                + "TreinaNorma" + File.separator + "Documentos");
        pasta.mkdirs();
        return pasta;
    }

    private static Font fonte(float tamanho, int estilo) {
        return fonte(tamanho, estilo, GRAFITE);
    }

    private static Font fonte(float tamanho, int estilo, Color cor) {
        try {
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            return new Font(bf, tamanho, estilo, cor);
        } catch (Exception e) {
            return new Font(Font.HELVETICA, tamanho, estilo, cor);
        }
    }

    private static String texto(String valor) {
        return valor == null ? "" : valor;
    }

    private static String numero(double valor) {
        return valor == Math.rint(valor) ? String.valueOf((int) valor)
                : String.valueOf(valor).replace('.', ',');
    }

    private static String data(LocalDate valor) {
        return valor == null ? "" : valor.format(Datas.BR);
    }

    public static File ata(Turma turma, Empresa empresa, Instrutor instrutor,
            List<Participante> participantes) throws IOException, DocumentException {
        File destino = new File(pasta(), "ata-" + turma.getCodigo() + ".pdf");
        Document doc = new Document(PageSize.A4, 40, 40, 40, 46);
        PdfWriter escritor = PdfWriter.getInstance(doc, new FileOutputStream(destino));
        escritor.setPageEvent(new Rodape("Ata " + turma.getCodigo()
                + " - " + (turma.getHashAta() == null ? "rascunho"
                : "hash " + turma.getHashAta().substring(0, 12))));
        doc.open();

        cabecalho(doc, empresa, "ATA DE PRESENÇA", "Turma " + turma.getCodigo());

        PdfPTable dados = new PdfPTable(new float[]{1.1f, 2.4f, 1.1f, 2.4f});
        dados.setWidthPercentage(100);
        dados.setSpacingBefore(14);
        dados.setSpacingAfter(10);
        par(dados, "Norma", turma.getNorma(), true);
        par(dados, "Treinamento", turma.getTreinamento(), false);
        par(dados, "Data", data(turma.getData()), true);
        par(dados, "Horário", texto(turma.getHoraInicio()) + " às " + texto(turma.getHoraFim()), false);
        par(dados, "Local", texto(turma.getLocal()), true);
        par(dados, "Modalidade", texto(turma.getModalidade()), false);
        par(dados, "Carga horária", numero(turma.getChTotal()) + " h", true);
        par(dados, "Presencial", numero(turma.getChPresencial()) + " h", false);
        par(dados, "Instrutor", texto(turma.getInstrutorNome()), true);
        par(dados, "Registro", instrutor == null ? "" : texto(instrutor.getRegistro()), false);
        doc.add(dados);

        if (turma.getConteudo() != null && !turma.getConteudo().isBlank()) {
            doc.add(tituloSecao("CONTEÚDO PROGRAMÁTICO"));
            Paragraph conteudo = new Paragraph(turma.getConteudo(), fonte(9, Font.NORMAL));
            conteudo.setLeading(13);
            conteudo.setSpacingAfter(10);
            doc.add(conteudo);
        }

        doc.add(tituloSecao("RELAÇÃO DE PARTICIPANTES"));
        PdfPTable tabela = new PdfPTable(new float[]{0.45f, 2.9f, 1.35f, 1.6f, 1.3f, 1.9f});
        tabela.setWidthPercentage(100);
        cabecalhoTabela(tabela, "#", "Nome do participante", "CPF", "Função", "Setor", "Assinatura");
        int i = 1;
        for (Participante p : participantes) {
            tabela.addCell(celula(String.valueOf(i++), 8, Element.ALIGN_CENTER, 16));
            tabela.addCell(celula(p.getNome(), 8, Element.ALIGN_LEFT, 16));
            tabela.addCell(celula(Cpf.formatar(p.getCpf()), 8, Element.ALIGN_CENTER, 16));
            tabela.addCell(celula(texto(p.getCargo()), 8, Element.ALIGN_LEFT, 16));
            tabela.addCell(celula(texto(p.getSetor()), 8, Element.ALIGN_LEFT, 16));
            tabela.addCell(celula("", 8, Element.ALIGN_LEFT, 16));
        }
        doc.add(tabela);

        Paragraph declaracao = new Paragraph(
                "Declaramos, para os devidos fins, que os participantes acima relacionados "
                + "compareceram e concluíram o treinamento nesta data, conforme carga horária "
                + "e conteúdo programático registrados.", fonte(8, Font.NORMAL, CINZA));
        declaracao.setSpacingBefore(10);
        declaracao.setSpacingAfter(4);
        doc.add(declaracao);

        PdfPTable assinaturas = new PdfPTable(2);
        assinaturas.setWidthPercentage(100);
        assinaturas.setSpacingBefore(22);
        assinaturas.addCell(linhaAssinatura(
                instrutor == null ? turma.getInstrutorNome() : instrutor.getNome(),
                instrutor == null ? "Instrutor" : texto(instrutor.getFormacao())
                + (instrutor.getRegistro() == null ? "" : " - Registro " + instrutor.getRegistro())));
        assinaturas.addCell(linhaAssinatura(
                empresa == null ? "" : texto(empresa.getResponsavelNome()),
                empresa == null ? "Responsável técnico"
                : texto(empresa.getResponsavelProfissao())
                + (empresa.getResponsavelRegistro() == null ? ""
                : " - Registro " + empresa.getResponsavelRegistro())));
        doc.add(assinaturas);

        doc.close();
        return destino;
    }

    public static File certificado(RegistroTreinamento registro, Turma turma, Empresa empresa,
            Instrutor instrutor) throws IOException, DocumentException {
        return certificado(registro, turma, empresa, instrutor, null);
    }

    public static File certificado(RegistroTreinamento registro, Turma turma, Empresa empresa,
            Instrutor instrutor, LocalDateTime reimpressao) throws IOException, DocumentException {
        File destino = new File(pasta(), "certificado-" + registro.getNumeroCertificado() + ".pdf");
        Document doc = new Document(PageSize.A4.rotate(), 36, 36, 34, 34);
        PdfWriter escritor = PdfWriter.getInstance(doc, new FileOutputStream(destino));
        doc.open();

        PdfContentByte cb = escritor.getDirectContent();
        cb.setColorStroke(AZUL);
        cb.setLineWidth(2f);
        cb.rectangle(24, 24, PageSize.A4.rotate().getWidth() - 48,
                PageSize.A4.rotate().getHeight() - 48);
        cb.stroke();
        cb.setColorStroke(new Color(10, 167, 232));
        cb.setLineWidth(0.8f);
        cb.rectangle(30, 30, PageSize.A4.rotate().getWidth() - 60,
                PageSize.A4.rotate().getHeight() - 60);
        cb.stroke();

        logotipo(doc, empresa, Element.ALIGN_LEFT);
        Paragraph empresaNome = new Paragraph(empresa == null ? "Empresa" : texto(empresa.getRazaoSocial()),
                fonte(13, Font.BOLD, AZUL));
        empresaNome.setAlignment(Element.ALIGN_LEFT);
        empresaNome.setSpacingBefore(2);
        doc.add(empresaNome);

        Paragraph empresaInfo = new Paragraph(
                "CNPJ " + (empresa == null ? "" : texto(empresa.getCnpj()))
                + (empresa == null || empresa.getEndereco() == null ? ""
                : "  |  " + empresa.enderecoCompleto()), fonte(8, Font.NORMAL, CINZA));
        empresaInfo.setSpacingAfter(18);
        doc.add(empresaInfo);

        Paragraph titulo = new Paragraph("CERTIFICADO", fonte(30, Font.BOLD, AZUL));
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(4);
        doc.add(titulo);

        Paragraph subtitulo = new Paragraph("DE TREINAMENTO - NORMAS REGULAMENTADORAS",
                fonte(9, Font.BOLD, CINZA));
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        subtitulo.setSpacingAfter(20);
        doc.add(subtitulo);

        Paragraph nome = new Paragraph(texto(registro.getColaboradorNome()).toUpperCase(),
                fonte(22, Font.BOLD, GRAFITE));
        nome.setAlignment(Element.ALIGN_CENTER);
        nome.setSpacingAfter(2);
        doc.add(nome);

        Paragraph cpf = new Paragraph("CPF " + Cpf.formatar(registro.getColaboradorCpf())
                + (registro.getColaboradorCargo() == null ? ""
                : "  |  " + registro.getColaboradorCargo()), fonte(10, Font.NORMAL, CINZA));
        cpf.setAlignment(Element.ALIGN_CENTER);
        cpf.setSpacingAfter(16);
        doc.add(cpf);

        Paragraph corpo = new Paragraph(
                "concluiu com aproveitamento o treinamento de "
                + registro.getNorma() + " - " + registro.getTreinamento()
                + ", com carga horária de " + numero(registro.getCh()) + " hora(s), "
                + "realizado em " + data(registro.getData())
                + (turma == null || turma.getLocal() == null ? "" : ", em " + turma.getLocal())
                + ", atendendo aos requisitos da " + registro.getNorma()
                + " e da NR-01 item 1.7.",
                fonte(11, Font.NORMAL));
        corpo.setAlignment(Element.ALIGN_CENTER);
        corpo.setLeading(17);
        corpo.setSpacingAfter(10);
        doc.add(corpo);

        if (turma != null && turma.getConteudo() != null && !turma.getConteudo().isBlank()) {
            Paragraph rotulo = new Paragraph("CONTEÚDO PROGRAMÁTICO", fonte(8, Font.BOLD, AZUL));
            rotulo.setAlignment(Element.ALIGN_CENTER);
            rotulo.setSpacingAfter(2);
            doc.add(rotulo);
            Paragraph conteudo = new Paragraph(turma.getConteudo(), fonte(8.5f, Font.NORMAL, CINZA));
            conteudo.setAlignment(Element.ALIGN_CENTER);
            conteudo.setLeading(12);
            conteudo.setSpacingAfter(12);
            doc.add(conteudo);
        }

        Paragraph validade = new Paragraph(registro.getValidade() == null
                ? "Validade: revisar conforme PGR (a NR não fixa periodicidade)."
                : "Validade: " + data(registro.getValidade()) + ".",
                fonte(9, Font.BOLD));
        validade.setAlignment(Element.ALIGN_CENTER);
        validade.setSpacingAfter(26);
        doc.add(validade);

        PdfPTable assinaturas = new PdfPTable(2);
        assinaturas.setWidthPercentage(72);
        assinaturas.addCell(linhaAssinatura(
                instrutor == null ? (turma == null ? "" : turma.getInstrutorNome()) : instrutor.getNome(),
                instrutor == null ? "Instrutor"
                : texto(instrutor.getFormacao())
                + (instrutor.getRegistro() == null ? "" : " - Registro " + instrutor.getRegistro())));
        assinaturas.addCell(linhaAssinatura(
                empresa == null ? "" : texto(empresa.getResponsavelNome()),
                empresa == null ? "Responsável técnico"
                : texto(empresa.getResponsavelProfissao())
                + (empresa.getResponsavelRegistro() == null ? ""
                : " - Registro " + empresa.getResponsavelRegistro())));
        doc.add(assinaturas);

        Paragraph rodape = new Paragraph(
                "Certificado nº " + registro.getNumeroCertificado()
                + "  |  Código de verificação: " + registro.getCodigoVerificacao()
                + "  |  Emitido em " + (registro.getEmitidoEm() == null
                        ? data(LocalDate.now()) : Datas.brHora(registro.getEmitidoEm()))
                + (reimpressao == null ? "" : "  |  2ª via em " + Datas.brHora(reimpressao))
                + "  |  Valide em TreinaNorma > Validar certificado",
                fonte(7, Font.NORMAL, CINZA));
        rodape.setAlignment(Element.ALIGN_CENTER);
        rodape.setSpacingBefore(16);
        doc.add(rodape);

        doc.close();
        return destino;
    }

    public static File crachasFrentes(Turma turma, Empresa empresa, List<Participante> participantes)
            throws IOException, DocumentException {
        File destino = new File(pasta(), "cracha-" + turma.getCodigo() + "-frentes.pdf");
        Document doc = new Document(PageSize.A4, 12, 12, 14, 14);
        PdfWriter.getInstance(doc, new FileOutputStream(destino));
        doc.open();
        doc.add(new Paragraph("Crachás - frentes (" + participantes.size() + ") - turma "
                + turma.getCodigo(), fonte(9, Font.BOLD, CINZA)));
        doc.add(new Paragraph("Imprima em papel A4 (sem escala) e recorte nas linhas. "
                + "Tamanho 54 x 86 mm, compatível com porta-crachá de papelaria.",
                fonte(7, Font.NORMAL, CINZA)));
        PdfPTable grade = gradeCrachas();
        int coluna = 0;
        for (Participante p : participantes) {
            Colaborador c = new ColaboradorDAO().porId(p.getColaboradorId());
            grade.addCell(celulaCracha(frenteCracha(p, c, empresa)));
            if (++coluna % 2 == 0) {
                grade.completeRow();
            }
        }
        doc.add(grade);
        doc.close();
        return destino;
    }

    public static File crachasVersos(Turma turma, Empresa empresa, List<Participante> participantes)
            throws IOException, DocumentException {
        File destino = new File(pasta(), "cracha-" + turma.getCodigo() + "-versos.pdf");
        Document doc = new Document(PageSize.A4, 12, 12, 14, 14);
        PdfWriter.getInstance(doc, new FileOutputStream(destino));
        doc.open();
        doc.add(new Paragraph("Crachás - versos (" + participantes.size() + ") - turma "
                + turma.getCodigo(), fonte(9, Font.BOLD, CINZA)));
        doc.add(new Paragraph("Imprima este arquivo no verso da folha das frentes.",
                fonte(7, Font.NORMAL, CINZA)));
        PdfPTable grade = gradeCrachas();
        int coluna = 0;
        for (Participante p : participantes) {
            List<RegistroTreinamento> registros = new RegistroDAO().porColaborador(p.getColaboradorId());
            StringBuilder sb = new StringBuilder();
            int limite = Math.min(5, registros.size());
            for (int i = 0; i < limite; i++) {
                RegistroTreinamento r = registros.get(i);
                sb.append(r.getNorma()).append(" - ").append(numero(r.getCh())).append("h");
                if (r.getValidade() != null) {
                    sb.append(" até ").append(r.getValidade()
                            .format(java.time.format.DateTimeFormatter.ofPattern("MM/yyyy")));
                }
                sb.append('\n');
            }
            if (registros.size() > limite) {
                sb.append("+ ").append(registros.size() - limite).append(" outros treinamentos");
            }
            grade.addCell(celulaCracha(versoCracha(p, empresa, sb.toString())));
            if (++coluna % 2 == 0) {
                grade.completeRow();
            }
        }
        doc.add(grade);
        doc.close();
        return destino;
    }

    private static PdfPTable gradeCrachas() throws DocumentException {
        PdfPTable grade = new PdfPTable(2);
        grade.setWidthPercentage(100);
        grade.setWidths(new float[]{1, 1});
        grade.setHorizontalAlignment(Element.ALIGN_CENTER);
        grade.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        grade.getDefaultCell().setFixedHeight(Utilities.millimetersToPoints(86) + 8);
        grade.getDefaultCell().setPadding(4);
        return grade;
    }

    private static PdfPTable frenteCracha(Participante p, Colaborador c, Empresa empresa) {
        PdfPTable cartao = new PdfPTable(1);
        cartao.setTotalWidth(Utilities.millimetersToPoints(54));
        cartao.setLockedWidth(true);
        PdfPCell dentro = new PdfPCell();
        dentro.setBorder(Rectangle.BOX);
        dentro.setBorderColor(BORDA);
        dentro.setPadding(6);
        dentro.setFixedHeight(Utilities.millimetersToPoints(86));

        Paragraph topo = new Paragraph(empresa == null ? "TreinaNorma" : texto(empresa.nomeExibicao()),
                fonte(6.5f, Font.BOLD, AZUL));
        topo.setAlignment(Element.ALIGN_CENTER);
        dentro.addElement(topo);

        Paragraph nome = new Paragraph(p.getNome(), fonte(9, Font.BOLD, GRAFITE));
        nome.setAlignment(Element.ALIGN_CENTER);
        nome.setSpacingBefore(6);
        dentro.addElement(nome);

        Paragraph cargo = new Paragraph(texto(p.getCargo()), fonte(7, Font.NORMAL, CINZA));
        cargo.setAlignment(Element.ALIGN_CENTER);
        dentro.addElement(cargo);

        Paragraph dados = new Paragraph("CPF " + Cpf.mascarar(p.getCpf())
                + (c == null || c.getMatricula() == null ? "" : "\nMat. " + c.getMatricula()),
                fonte(6.5f, Font.NORMAL, CINZA));
        dados.setAlignment(Element.ALIGN_CENTER);
        dados.setSpacingBefore(8);
        dentro.addElement(dados);

        Paragraph rodape = new Paragraph("IDENTIFICAÇÃO INTERNA", fonte(5.5f, Font.NORMAL, CINZA));
        rodape.setAlignment(Element.ALIGN_CENTER);
        rodape.setSpacingBefore(8);
        dentro.addElement(rodape);

        cartao.addCell(dentro);
        return cartao;
    }

    private static PdfPTable versoCracha(Participante p, Empresa empresa, String registros) {
        PdfPTable cartao = new PdfPTable(1);
        cartao.setTotalWidth(Utilities.millimetersToPoints(54));
        cartao.setLockedWidth(true);
        PdfPCell dentro = new PdfPCell();
        dentro.setBorder(Rectangle.BOX);
        dentro.setBorderColor(BORDA);
        dentro.setPadding(6);
        dentro.setFixedHeight(Utilities.millimetersToPoints(86));

        Paragraph titulo = new Paragraph("TREINAMENTOS", fonte(6.5f, Font.BOLD, AZUL));
        titulo.setAlignment(Element.ALIGN_CENTER);
        dentro.addElement(titulo);

        Paragraph nome = new Paragraph(p.getNome(), fonte(7.5f, Font.BOLD, GRAFITE));
        nome.setAlignment(Element.ALIGN_CENTER);
        nome.setSpacingAfter(4);
        dentro.addElement(nome);

        Paragraph lista = new Paragraph(registros, fonte(6, Font.NORMAL, new Color(71, 85, 105)));
        lista.setLeading(9);
        dentro.addElement(lista);

        Paragraph rodape = new Paragraph(empresa == null ? "" : texto(empresa.getTelefone())
                + (empresa.getEmail() == null ? "" : "\n" + empresa.getEmail()),
                fonte(5.5f, Font.NORMAL, CINZA));
        rodape.setAlignment(Element.ALIGN_CENTER);
        rodape.setSpacingBefore(6);
        dentro.addElement(rodape);

        cartao.addCell(dentro);
        return cartao;
    }

    private static void cabecalho(Document doc, Empresa empresa, String titulo, String direita)
            throws DocumentException, IOException {
        PdfPTable cab = new PdfPTable(new float[]{3.2f, 1.8f});
        cab.setWidthPercentage(100);
        PdfPCell esquerda = new PdfPCell();
        esquerda.setBorder(Rectangle.NO_BORDER);
        logotipoRelatorio(esquerda, empresa);
        Paragraph nome = new Paragraph(empresa == null ? "Empresa" : texto(empresa.getRazaoSocial()),
                fonte(11, Font.BOLD, AZUL));
        esquerda.addElement(nome);
        Paragraph info = new Paragraph("CNPJ " + (empresa == null ? "" : texto(empresa.getCnpj()))
                + (empresa == null || empresa.getEndereco() == null ? "" : "\n" + empresa.enderecoCompleto()),
                fonte(7.5f, Font.NORMAL, CINZA));
        esquerda.addElement(info);
        cab.addCell(esquerda);

        PdfPCell direitaCel = new PdfPCell();
        direitaCel.setBorder(Rectangle.NO_BORDER);
        direitaCel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph t = new Paragraph(titulo, fonte(12, Font.BOLD, AZUL));
        t.setAlignment(Element.ALIGN_RIGHT);
        direitaCel.addElement(t);
        Paragraph d = new Paragraph(direita + "\nEmitido em " + data(LocalDate.now()),
                fonte(7.5f, Font.NORMAL, CINZA));
        d.setAlignment(Element.ALIGN_RIGHT);
        direitaCel.addElement(d);
        cab.addCell(direitaCel);
        doc.add(cab);

        PdfPTable linha = new PdfPTable(1);
        linha.setWidthPercentage(100);
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.BOTTOM);
        c.setBorderColor(AZUL);
        c.setFixedHeight(6);
        linha.addCell(c);
        doc.add(linha);
    }

    private static void logotipo(Document doc, Empresa empresa, int alinhamento)
            throws DocumentException, IOException {
        Image imagem = imagem(empresa == null ? null : empresa.getLogo(), 54);
        if (imagem != null) {
            imagem.setAlignment(alinhamento);
            doc.add(imagem);
        }
    }

    private static void logotipoRelatorio(PdfPCell celula, Empresa empresa) throws IOException {
        Image imagem = imagem(empresa == null ? null : empresa.getLogo(), 38);
        if (imagem != null) {
            imagem.setAlignment(Element.ALIGN_LEFT);
            celula.addElement(imagem);
        }
    }

    private static Image imagem(String caminho, float altura) throws IOException {
        if (caminho == null || caminho.isBlank()) {
            return null;
        }
        File f = new File(caminho);
        if (!f.exists()) {
            return null;
        }
        try {
            Image img = Image.getInstance(f.getAbsolutePath());
            img.scaleToFit(altura * 2, altura);
            return img;
        } catch (DocumentException e) {
            return null;
        }
    }

    private static Paragraph tituloSecao(String texto) {
        Paragraph p = new Paragraph(texto, fonte(8.5f, Font.BOLD, AZUL));
        p.setSpacingBefore(8);
        p.setSpacingAfter(4);
        return p;
    }

    private static void par(PdfPTable tabela, String rotulo, String valor, boolean destacado) {
        PdfPCell r = new PdfPCell(new Phrase(rotulo, fonte(7.5f, Font.BOLD, CINZA)));
        r.setBackgroundColor(AZUL_CLARO);
        r.setPadding(4);
        r.setBorderColor(BORDA);
        tabela.addCell(r);
        PdfPCell v = new PdfPCell(new Phrase(texto(valor), fonte(8f, destacado ? Font.BOLD : Font.NORMAL)));
        v.setPadding(4);
        v.setBorderColor(BORDA);
        tabela.addCell(v);
    }

    private static void cabecalhoTabela(PdfPTable tabela, String... colunas) {
        for (String c : colunas) {
            PdfPCell celula = new PdfPCell(new Phrase(c, fonte(7.5f, Font.BOLD, Color.WHITE)));
            celula.setBackgroundColor(AZUL);
            celula.setPadding(4);
            celula.setBorderColor(AZUL);
            celula.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabela.addCell(celula);
        }
        tabela.setHeaderRows(1);
    }

    private static PdfPCell celula(String texto, float tamanho, int alinhamento, float altura) {
        PdfPCell c = new PdfPCell(new Phrase(texto, fonte(tamanho, Font.NORMAL)));
        c.setPadding(4);
        c.setBorderColor(BORDA);
        c.setHorizontalAlignment(alinhamento);
        c.setMinimumHeight(altura);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return c;
    }

    private static PdfPCell celulaCracha(PdfPTable conteudo) {
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(0);
        c.addElement(conteudo);
        return c;
    }

    private static PdfPCell linhaAssinatura(String nome, String detalhe) {
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.NO_BORDER);
        c.setPaddingTop(18);
        c.setPaddingLeft(12);
        c.setPaddingRight(12);
        Paragraph linha = new Paragraph("_______________________________________",
                fonte(9, Font.NORMAL, CINZA));
        linha.setAlignment(Element.ALIGN_CENTER);
        c.addElement(linha);
        Paragraph n = new Paragraph(texto(nome), fonte(9, Font.BOLD));
        n.setAlignment(Element.ALIGN_CENTER);
        c.addElement(n);
        Paragraph d = new Paragraph(texto(detalhe), fonte(7.5f, Font.NORMAL, CINZA));
        d.setAlignment(Element.ALIGN_CENTER);
        c.addElement(d);
        return c;
    }

    private static class Rodape extends PdfPageEventHelper {

        private final String texto;

        Rodape(String texto) {
            this.texto = texto;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            cb.saveState();
            cb.setColorFill(CINZA);
            cb.setFontAndSize(fonte(7, Font.NORMAL).getBaseFont(), 7);
            String rodape = texto + "  |  página " + writer.getPageNumber();
            cb.beginText();
            cb.showTextAligned(Element.ALIGN_CENTER, rodape,
                    (document.left() + document.right()) / 2, document.bottom() - 14, 0);
            cb.endText();
            cb.restoreState();
        }
    }
}
