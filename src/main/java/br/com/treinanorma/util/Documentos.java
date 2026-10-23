package br.com.treinanorma.util;

import br.com.treinanorma.dao.ColaboradorDAO;
import br.com.treinanorma.dao.ParametroDAO;
import br.com.treinanorma.dao.RegistroDAO;
import br.com.treinanorma.model.Colaborador;
import br.com.treinanorma.model.Empresa;
import br.com.treinanorma.model.Encontro;
import br.com.treinanorma.model.Instrutor;
import br.com.treinanorma.model.Participante;
import br.com.treinanorma.model.RegistroTreinamento;
import br.com.treinanorma.model.Turma;
import com.lowagie.text.Chunk;
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
import com.lowagie.text.pdf.PdfPCellEvent;
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
import java.util.Map;
import java.util.Set;

public final class Documentos {

    private static final Color AZUL = new Color(31, 78, 121);
    private static final Color AZUL_CLARO = new Color(232, 242, 251);
    private static final Color CINZA = new Color(107, 114, 128);
    private static final Color BORDA = new Color(203, 213, 225);
    private static final Color GRAFITE = new Color(31, 41, 55);

    private Documentos() {
    }

    public static File pasta() {
        String configurada = parametro("pasta_documentos", "");
        File pasta;
        if (configurada.isBlank()) {
            pasta = new File(System.getProperty("user.home") + File.separator
                    + "TreinaNorma" + File.separator + "Documentos");
        } else {
            pasta = new File(configurada);
            if (!pasta.isAbsolute()) {
                pasta = new File(System.getProperty("user.home"), configurada);
            }
        }
        pasta.mkdirs();
        if (!pasta.isDirectory()) {
            pasta = new File(System.getProperty("user.home") + File.separator
                    + "TreinaNorma" + File.separator + "Documentos");
            pasta.mkdirs();
        }
        return pasta;
    }

    private static String parametro(String chave, String padrao) {
        try {
            return new ParametroDAO().valor(chave, padrao);
        } catch (RuntimeException e) {
            return padrao;
        }
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

    private static String periodo(RegistroTreinamento registro) {
        if (registro.getData() == null) {
            return "realizado em data a definir";
        }
        if (registro.getDataFim() != null && !registro.getDataFim().equals(registro.getData())) {
            return "realizado de " + data(registro.getData()) + " a " + data(registro.getDataFim());
        }
        return "realizado em " + data(registro.getData());
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

    public static File ataDia(Turma turma, Empresa empresa, Instrutor instrutor, Encontro encontro,
            int indice, int totalDias, List<Participante> presentes)
            throws IOException, DocumentException {
        String sufixo = totalDias > 1 ? String.format("-dia-%02d", indice + 1) : "";
        File destino = new File(pasta(), "ata-" + turma.getCodigo() + sufixo + ".pdf");
        Document doc = new Document(PageSize.A4, 40, 40, 40, 46);
        PdfWriter escritor = PdfWriter.getInstance(doc, new FileOutputStream(destino));
        escritor.setPageEvent(new Rodape("Ata " + turma.getCodigo()
                + (totalDias > 1 ? " - dia " + (indice + 1) + " de " + totalDias : "")
                + " - " + (turma.getHashAta() == null ? "rascunho"
                : "hash " + turma.getHashAta().substring(0, 12))));
        doc.open();

        cabecalho(doc, empresa, "ATA DE PRESENÇA", "Turma " + turma.getCodigo()
                + (totalDias > 1 ? "  |  Dia " + (indice + 1) + " de " + totalDias : ""));

        PdfPTable dados = new PdfPTable(new float[]{1.1f, 2.4f, 1.1f, 2.4f});
        dados.setWidthPercentage(100);
        dados.setSpacingBefore(14);
        dados.setSpacingAfter(8);
        par(dados, "Norma", turma.getNorma(), true);
        par(dados, "Treinamento", turma.getTreinamento(), false);
        par(dados, "Data", data(encontro.getData()), true);
        par(dados, "Horário", texto(encontro.getHoraInicio()) + " às "
                + texto(encontro.getHoraFim()), false);
        par(dados, "CH do dia", numero(encontro.getCh()) + " h", true);
        par(dados, "Modalidade", encontro.modalidade(), false);
        par(dados, "Local", texto(turma.getLocal()), true);
        par(dados, "Instrutor", texto(turma.getInstrutorNome()), false);
        par(dados, "Registro", instrutor == null ? "" : texto(instrutor.getRegistro()), true);
        par(dados, "Presentes", String.valueOf(presentes.size()), false);
        doc.add(dados);

        if (encontro.getConteudo() != null && !encontro.getConteudo().isBlank()) {
            doc.add(tituloSecao("CONTEÚDO MINISTRADO NESTE DIA"));
            PdfPTable caixa = new PdfPTable(1);
            caixa.setWidthPercentage(100);
            PdfPCell c = new PdfPCell(new Phrase(encontro.getConteudo(),
                    fonte(9, Font.NORMAL)));
            c.setPadding(6);
            c.setBorderColor(BORDA);
            c.setMinimumHeight(40);
            caixa.addCell(c);
            doc.add(caixa);
        }

        doc.add(tituloSecao("PARTICIPANTES PRESENTES"));
        if (presentes.isEmpty()) {
            doc.add(new Paragraph("Nenhum participante presente neste dia.",
                    fonte(9, Font.NORMAL, CINZA)));
        } else {
            PdfPTable tabela = new PdfPTable(new float[]{0.45f, 2.7f, 1.35f, 1.6f, 1.2f, 1.9f});
            tabela.setWidthPercentage(100);
            cabecalhoTabela(tabela, "#", "Nome do participante", "CPF", "Função",
                    "Setor", "Assinatura");
            int i = 1;
            for (Participante p : presentes) {
                tabela.addCell(celula(String.valueOf(i++), 8, Element.ALIGN_CENTER, 16));
                tabela.addCell(celula(p.getNome(), 8, Element.ALIGN_LEFT, 16));
                tabela.addCell(celula(Cpf.formatar(p.getCpf()), 8, Element.ALIGN_CENTER, 16));
                tabela.addCell(celula(texto(p.getCargo()), 8, Element.ALIGN_LEFT, 16));
                tabela.addCell(celula(texto(p.getSetor()), 8, Element.ALIGN_LEFT, 16));
                tabela.addCell(celula("", 8, Element.ALIGN_LEFT, 16));
            }
            doc.add(tabela);
        }

        Paragraph declaracao = new Paragraph(
                "Declaramos, para os devidos fins, que os participantes acima relacionados "
                + "compareceram ao encontro de " + data(encontro.getData())
                + ", conforme conteúdo e carga horária registrados nesta ata.",
                fonte(8, Font.NORMAL, CINZA));
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

    public static File ataConsolidada(Turma turma, Empresa empresa, Instrutor instrutor,
            List<Encontro> encontros, List<Participante> participantes,
            Map<Integer, Set<Integer>> presencas) throws IOException, DocumentException {
        File destino = new File(pasta(), "ata-" + turma.getCodigo() + "-consolidada.pdf");
        Document doc = new Document(PageSize.A4, 40, 40, 40, 46);
        PdfWriter escritor = PdfWriter.getInstance(doc, new FileOutputStream(destino));
        escritor.setPageEvent(new Rodape("Ata consolidada " + turma.getCodigo()
                + " - " + (turma.getHashAta() == null ? "rascunho"
                : "hash " + turma.getHashAta().substring(0, 12))));
        doc.open();

        cabecalho(doc, empresa, "ATA CONSOLIDADA", "Turma " + turma.getCodigo());

        double total = 0;
        double pratica = 0;
        for (Encontro e : encontros) {
            total += e.getCh();
            if (e.isPratica()) {
                pratica += e.getCh();
            }
        }
        LocalDate inicio = encontros.isEmpty() ? turma.getData() : encontros.get(0).getData();
        LocalDate fim = encontros.isEmpty() ? turma.getData()
                : encontros.get(encontros.size() - 1).getData();
        Paragraph resumo = new Paragraph(
                "Período: " + data(inicio) + " a " + data(fim)
                + "  |  " + encontros.size() + " encontro(s)"
                + "  |  CH total: " + numero(total) + " h"
                + "  |  Prática: " + numero(pratica) + " h"
                + (total > 0 ? " (" + Math.round(pratica / total * 100) + "%)" : ""),
                fonte(8.5f, Font.NORMAL, CINZA));
        resumo.setSpacingBefore(12);
        resumo.setSpacingAfter(8);
        doc.add(resumo);

        doc.add(tituloSecao("ENCONTROS REALIZADOS"));
        PdfPTable dias = new PdfPTable(new float[]{0.5f, 1.0f, 0.7f, 1.25f, 3.7f});
        dias.setWidthPercentage(100);
        cabecalhoTabela(dias, "#", "Data", "CH", "Tipo", "Conteúdo");
        int i = 1;
        for (Encontro e : encontros) {
            dias.addCell(celula(String.valueOf(i++), 8, Element.ALIGN_CENTER, 14));
            dias.addCell(celula(data(e.getData()), 8, Element.ALIGN_CENTER, 14));
            dias.addCell(celula(numero(e.getCh()) + " h", 8, Element.ALIGN_CENTER, 14));
            dias.addCell(celula(e.isPratica() ? "Prática" : "Teórica", 8,
                    Element.ALIGN_CENTER, 14));
            dias.addCell(celula(texto(e.getConteudo()), 7.5f, Element.ALIGN_LEFT, 14));
        }
        doc.add(dias);

        doc.add(tituloSecao("RESUMO DE PRESENÇA E CARGA HORÁRIA"));
        PdfPTable tabela = new PdfPTable(new float[]{2.5f, 1.2f, 1.25f, 1.0f, 0.9f, 1.0f});
        tabela.setWidthPercentage(100);
        cabecalhoTabela(tabela, "Participante", "CPF", "Função", "Presenças", "CH", "Situação");
        for (Participante p : participantes) {
            int diasPresentes = 0;
            double horas = 0;
            for (Encontro e : encontros) {
                Set<Integer> doDia = presencas.get(e.getId());
                if (doDia != null && doDia.contains(p.getId())) {
                    diasPresentes++;
                    horas += e.getCh();
                }
            }
            boolean apto = encontros.isEmpty()
                    ? p.isPresente() && !Boolean.FALSE.equals(p.getAprovado())
                    : diasPresentes == encontros.size() && !Boolean.FALSE.equals(p.getAprovado());
            tabela.addCell(celula(p.getNome(), 8, Element.ALIGN_LEFT, 16));
            tabela.addCell(celula(Cpf.formatar(p.getCpf()), 8, Element.ALIGN_CENTER, 16));
            tabela.addCell(celula(texto(p.getCargo()), 8, Element.ALIGN_LEFT, 16));
            tabela.addCell(celula(diasPresentes + "/" + encontros.size(), 8,
                    Element.ALIGN_CENTER, 16));
            tabela.addCell(celula(numero(encontros.isEmpty() ? turma.getChTotal() : horas)
                    + " h", 8, Element.ALIGN_CENTER, 16));
            tabela.addCell(celula(apto ? "Apto" : "Repetir", 8, Element.ALIGN_CENTER, 16));
        }
        doc.add(tabela);

        Paragraph declaracao = new Paragraph(
                "A situação \"Repetir\" indica ausência em pelo menos um encontro ou "
                + "reprovação na avaliação; nesse caso o participante deve refazer o treinamento.",
                fonte(7.5f, Font.NORMAL, CINZA));
        declaracao.setSpacingBefore(8);
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
                + periodo(registro)
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

        String cidade = parametro("cidade_emissao", "");
        if (!cidade.isBlank()) {
            LocalDate emissao = registro.getEmitidoEm() == null
                    ? LocalDate.now() : registro.getEmitidoEm().toLocalDate();
            Paragraph local = new Paragraph(cidade + ", " + data(emissao) + ".",
                    fonte(9, Font.NORMAL));
            local.setAlignment(Element.ALIGN_CENTER);
            local.setSpacingAfter(6);
            doc.add(local);
        }

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

        String textoRodape = parametro("texto_rodape_certificado", "");
        if (!textoRodape.isBlank()) {
            Paragraph gerado = new Paragraph(textoRodape, fonte(7, Font.NORMAL, CINZA));
            gerado.setAlignment(Element.ALIGN_CENTER);
            doc.add(gerado);
        }

        doc.close();
        return destino;
    }

    private static final float CRACHA_LARGURA = 54f;
    private static final float CRACHA_ALTURA = 86f;

    public static File crachas(Turma turma, Empresa empresa, List<Participante> participantes)
            throws IOException, DocumentException {
        File destino = new File(pasta(), "cracha-" + turma.getCodigo() + ".pdf");
        Document doc = new Document(new Rectangle(PageSize.A4.getHeight(), PageSize.A4.getWidth()), mm(10), mm(10), mm(10), mm(10));
        PdfWriter.getInstance(doc, new FileOutputStream(destino));
        doc.open();

        Paragraph titulo = new Paragraph("Crachás - turma " + turma.getCodigo()
                + " (" + participantes.size() + " participante(s))", fonte(9, Font.BOLD, CINZA));
        titulo.setSpacingAfter(2);
        doc.add(titulo);
        Paragraph instrucoes = new Paragraph("Imprima em A4 paisagem (4 por folha), sem escala (100%). "
                + "Recorte na borda externa e dobre cada peça ao meio, no tracejado central. "
                + "Frente à esquerda, verso à direita. Peça aberta: 108 x 86 mm; dobrada: 54 x 86 mm.",
                fonte(7, Font.NORMAL, CINZA));
        instrucoes.setSpacingAfter(4);
        doc.add(instrucoes);

        for (int inicio = 0; inicio < participantes.size(); inicio += 4) {
            if (inicio > 0) {
                doc.newPage();
            }
            PdfPTable grade = new PdfPTable(2);
            grade.setTotalWidth(mm(CRACHA_LARGURA * 4));
            grade.setLockedWidth(true);
            grade.setHorizontalAlignment(Element.ALIGN_CENTER);
            grade.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            grade.getDefaultCell().setPadding(0);

            int fim = Math.min(inicio + 4, participantes.size());
            for (int i = inicio; i < fim; i++) {
                Participante p = participantes.get(i);
                Colaborador c = new ColaboradorDAO().porId(p.getColaboradorId());
                List<RegistroTreinamento> registros = new RegistroDAO().porColaborador(p.getColaboradorId());

                PdfPCell celula = new PdfPCell();
                celula.setBorder(Rectangle.NO_BORDER);
                celula.setPadding(0);
                celula.setFixedHeight(mm(CRACHA_ALTURA));
                celula.setVerticalAlignment(Element.ALIGN_TOP);
                celula.setCellEvent(new EventoCorteCracha());
                celula.addElement(cartaoCracha(p, c, empresa, registros));
                grade.addCell(celula);
            }
            if ((fim - inicio) % 2 != 0) {
                PdfPCell vazio = new PdfPCell();
                vazio.setBorder(Rectangle.NO_BORDER);
                vazio.setPadding(0);
                vazio.setFixedHeight(mm(CRACHA_ALTURA));
                grade.addCell(vazio);
            }
            doc.add(grade);
        }
        doc.close();
        return destino;
    }

    private static PdfPTable cartaoCracha(Participante p, Colaborador c, Empresa empresa,
            List<RegistroTreinamento> registros) throws IOException, DocumentException {
        PdfPTable aberto = new PdfPTable(new float[]{1f, 1f});
        aberto.setTotalWidth(mm(CRACHA_LARGURA * 2));
        aberto.setLockedWidth(true);
        aberto.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        aberto.getDefaultCell().setPadding(0);

        PdfPCell frente = new PdfPCell(frenteCracha(p, c, empresa));
        frente.getTable().setExtendLastRow(false);
        frente.setBorder(Rectangle.NO_BORDER);
        frente.setPadding(mm(3));
        frente.setFixedHeight(mm(CRACHA_ALTURA));
        aberto.addCell(frente);

        PdfPCell verso = new PdfPCell(versoCracha(registros));
        verso.getTable().setExtendLastRow(false);
        verso.setBorder(Rectangle.NO_BORDER);
        verso.setPadding(mm(3));
        verso.setFixedHeight(mm(CRACHA_ALTURA));
        aberto.addCell(verso);
        return aberto;
    }

    private static PdfPTable frenteCracha(Participante p, Colaborador c, Empresa empresa)
            throws IOException, DocumentException {
        PdfPTable cartao = new PdfPTable(1);
        cartao.setWidthPercentage(100);
        cartao.setExtendLastRow(false);
        cartao.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        cartao.getDefaultCell().setPadding(0);

        PdfPTable topo = new PdfPTable(1);
        topo.setWidthPercentage(100);
        topo.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        topo.getDefaultCell().setPadding(0);
        topo.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);

        Image logo = empresa == null ? null : imagemAjustada(empresa.getLogo(), 36, 10);
        if (logo != null) {
            PdfPCell celulaLogo = new PdfPCell();
            celulaLogo.setBorder(Rectangle.NO_BORDER);
            celulaLogo.setPadding(0);
            celulaLogo.setHorizontalAlignment(Element.ALIGN_CENTER);
            logo.setAlignment(Element.ALIGN_CENTER);
            celulaLogo.addElement(logo);
            topo.addCell(celulaLogo);
        } else if (empresa != null) {
            Paragraph dados = new Paragraph();
            dados.setAlignment(Element.ALIGN_CENTER);
            dados.setLeading(7.5f);
            dados.add(new Chunk(texto(empresa.nomeExibicao()), fonte(7.5f, Font.BOLD, AZUL)));
            dados.add(Chunk.NEWLINE);
            dados.add(new Chunk("CNPJ " + texto(empresa.getCnpj()),
                    fonte(5.2f, Font.NORMAL, CINZA)));
            topo.addCell(dados);
        } else {
            Paragraph vazio = new Paragraph("Informações/logo da empresa",
                    fonte(5.5f, Font.NORMAL, CINZA));
            vazio.setAlignment(Element.ALIGN_CENTER);
            topo.addCell(vazio);
        }

        PdfPCell celulaTopo = new PdfPCell(topo);
        celulaTopo.getTable().setExtendLastRow(false);
        celulaTopo.setBorder(Rectangle.NO_BORDER);
        celulaTopo.setPadding(0);
        celulaTopo.setFixedHeight(mm(12));
        celulaTopo.setHorizontalAlignment(Element.ALIGN_CENTER);
        celulaTopo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cartao.addCell(celulaTopo);

        PdfPCell espaco = new PdfPCell();
        espaco.setBorder(Rectangle.NO_BORDER);
        espaco.setPadding(0);
        espaco.setFixedHeight(mm(2));
        cartao.addCell(espaco);

        PdfPTable areaFoto = new PdfPTable(1);
        areaFoto.setTotalWidth(mm(30));
        areaFoto.setLockedWidth(true);
        areaFoto.setHorizontalAlignment(Element.ALIGN_CENTER);
        PdfPCell foto = new PdfPCell();
        foto.setFixedHeight(mm(40));
        foto.setPadding(0);
        foto.setHorizontalAlignment(Element.ALIGN_CENTER);
        foto.setVerticalAlignment(Element.ALIGN_MIDDLE);
        Image retrato = c == null ? null : fotoCracha(c.getFoto(), 30, 40);
        if (retrato != null) {
            foto.setBorder(Rectangle.NO_BORDER);
            retrato.setAlignment(Element.ALIGN_CENTER);
            foto.addElement(retrato);
        } else {
            foto.setBorder(Rectangle.BOX);
            foto.setBorderColor(BORDA);
            Paragraph vazio = new Paragraph("Foto 3x4", fonte(5.5f, Font.NORMAL, CINZA));
            vazio.setAlignment(Element.ALIGN_CENTER);
            foto.addElement(vazio);
        }
        areaFoto.addCell(foto);

        PdfPCell celulaFoto = new PdfPCell(areaFoto);
        celulaFoto.getTable().setExtendLastRow(false);
        celulaFoto.setBorder(Rectangle.NO_BORDER);
        celulaFoto.setPadding(0);
        celulaFoto.setFixedHeight(mm(40));
        celulaFoto.setHorizontalAlignment(Element.ALIGN_CENTER);
        celulaFoto.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cartao.addCell(celulaFoto);

        PdfPCell espaco2 = new PdfPCell();
        espaco2.setBorder(Rectangle.NO_BORDER);
        espaco2.setPadding(0);
        espaco2.setFixedHeight(mm(2));
        cartao.addCell(espaco2);

        String nomeColaborador = p.getNome() == null || p.getNome().isBlank()
                ? "Nome do Colaborador" : p.getNome().toUpperCase();
        Paragraph nome = new Paragraph(nomeColaborador, fonte(9.5f, Font.BOLD, GRAFITE));
        nome.setAlignment(Element.ALIGN_CENTER);
        nome.setLeading(10.5f);
        PdfPCell celulaNome = new PdfPCell(nome);
        celulaNome.setBorder(Rectangle.NO_BORDER);
        celulaNome.setPadding(0);
        celulaNome.setFixedHeight(mm(12));
        celulaNome.setHorizontalAlignment(Element.ALIGN_CENTER);
        celulaNome.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cartao.addCell(celulaNome);

        Paragraph cargo = new Paragraph(p.getCargo() == null || p.getCargo().isBlank()
                ? "Função" : p.getCargo(), fonte(7.5f, Font.NORMAL, CINZA));
        cargo.setAlignment(Element.ALIGN_CENTER);
        cargo.setLeading(7f);
        PdfPCell celulaCargo = new PdfPCell(cargo);
        celulaCargo.setBorder(Rectangle.NO_BORDER);
        celulaCargo.setPadding(0);
        celulaCargo.setFixedHeight(mm(8));
        celulaCargo.setHorizontalAlignment(Element.ALIGN_CENTER);
        celulaCargo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cartao.addCell(celulaCargo);
        return cartao;
    }

    private static PdfPTable versoCracha(List<RegistroTreinamento> registros)
            throws DocumentException {
        PdfPTable cartao = new PdfPTable(1);
        cartao.setWidthPercentage(100);
        cartao.setExtendLastRow(false);
        cartao.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        cartao.getDefaultCell().setPadding(0);

        PdfPTable tabela = new PdfPTable(new float[]{2.2f, 1f, 1f});
        tabela.setWidthPercentage(100);
        tabela.setExtendLastRow(false);
        cabecalhoCracha(tabela, "Treinamentos", "Finalizado", "Vencimento");

        int limite = Math.min(12, registros.size());
        for (int i = 0; i < limite; i++) {
            RegistroTreinamento r = registros.get(i);
            PdfPCell nome = new PdfPCell();
            nome.setPadding(2);
            nome.setBorder(Rectangle.BOTTOM);
            nome.setBorderColor(BORDA);
            Paragraph norma = new Paragraph(texto(r.getNorma()), fonte(5.6f, Font.BOLD, GRAFITE));
            norma.setLeading(6f);
            nome.addElement(norma);
            tabela.addCell(nome);
            LocalDate feito = r.getDataFim() != null ? r.getDataFim() : r.getData();
            tabela.addCell(celulaCracha(dataCurta(feito), 5.4f, GRAFITE));
            tabela.addCell(celulaCracha(r.getValidade() == null ? "PGR"
                    : dataCurta(r.getValidade()), 5.4f, GRAFITE));
        }
        if (registros.isEmpty()) {
            PdfPCell vazio = new PdfPCell(new Phrase("Nenhum treinamento registrado.",
                    fonte(5.2f, Font.NORMAL, CINZA)));
            vazio.setColspan(3);
            vazio.setPadding(3);
            vazio.setBorderColor(BORDA);
            tabela.addCell(vazio);
        } else if (registros.size() > limite) {
            PdfPCell resto = new PdfPCell(new Phrase("+ " + (registros.size() - limite)
                    + " outro(s) treinamento(s)", fonte(4.8f, Font.NORMAL, CINZA)));
            resto.setColspan(3);
            resto.setPadding(2);
            resto.setBorderColor(BORDA);
            tabela.addCell(resto);
        }
        cartao.addCell(tabela);
        return cartao;
    }

    private static void cabecalhoCracha(PdfPTable tabela, String... colunas) {
        for (String c : colunas) {
            PdfPCell celula = new PdfPCell(new Phrase(c, fonte(5.2f, Font.BOLD, GRAFITE)));
            celula.setHorizontalAlignment(Element.ALIGN_CENTER);
            celula.setVerticalAlignment(Element.ALIGN_MIDDLE);
            celula.setPadding(2);
            celula.setBorder(Rectangle.BOTTOM);
            celula.setBorderColor(GRAFITE);
            tabela.addCell(celula);
        }
    }

    private static PdfPCell celulaCracha(String valor, float tamanho, Color cor) {
        PdfPCell c = new PdfPCell(new Phrase(valor, fonte(tamanho, Font.NORMAL, cor)));
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPadding(2);
        c.setBorder(Rectangle.BOTTOM);
        c.setBorderColor(BORDA);
        return c;
    }

    private static String dataCurta(LocalDate valor) {
        return valor == null ? ""
                : valor.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy"));
    }

    private static Image fotoCracha(String caminho, float larguraMm, float alturaMm) {
        return imagemAjustada(caminho, larguraMm, alturaMm);
    }

    private static Image imagemAjustada(String caminho, float larguraMm, float alturaMm) {
        if (caminho == null || caminho.isBlank()) {
            return null;
        }
        File f = Fotos.arquivo(caminho);
        if (f == null || !f.exists()) {
            return null;
        }
        try {
            Image img = Image.getInstance(f.getAbsolutePath());
            img.scaleToFit(mm(larguraMm), mm(alturaMm));
            return img;
        } catch (Exception e) {
            return null;
        }
    }

    private static float mm(float valor) {
        return Utilities.millimetersToPoints(valor);
    }

    private static final class EventoCorteCracha implements PdfPCellEvent {

        @Override
        public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
            float largura = position.getWidth();
            float altura = mm(CRACHA_ALTURA);
            float esquerda = position.getLeft();
            float topo = position.getTop();
            PdfContentByte cb = canvases[PdfPTable.LINECANVAS];
            cb.saveState();
            cb.setLineWidth(0.4f);
            cb.setColorStroke(BORDA);
            cb.rectangle(esquerda, topo - altura, largura, altura);
            cb.stroke();
            cb.setLineWidth(0.5f);
            cb.setColorStroke(GRAFITE);
            cb.setLineDash(4f, 2f, 0f);
            cb.moveTo(esquerda + largura / 2f, topo - altura);
            cb.lineTo(esquerda + largura / 2f, topo);
            cb.stroke();
            cb.restoreState();
        }
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
        File f = Fotos.arquivo(caminho);
        if (f == null || !f.exists()) {
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
