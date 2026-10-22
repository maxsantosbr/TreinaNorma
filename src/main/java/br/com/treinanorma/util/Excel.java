package br.com.treinanorma.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class Excel {

    private Excel() {
    }

    public static List<List<String>> ler(File arquivo) throws IOException {
        String nome = arquivo.getName().toLowerCase();
        if (nome.endsWith(".csv")) {
            return lerCsv(arquivo);
        }
        if (nome.endsWith(".xlsx")) {
            return lerXlsx(arquivo);
        }
        throw new IOException("Formato não suportado. Use .xlsx ou .csv");
    }

    private static List<List<String>> lerCsv(File arquivo) throws IOException {
        try (InputStream in = Files.newInputStream(arquivo.toPath())) {
            List<String[]> cru = Csv.ler(in, ';');
            List<List<String>> saida = new ArrayList<>();
            for (String[] linha : cru) {
                List<String> l = new ArrayList<>();
                for (String c : linha) {
                    l.add(c.trim());
                }
                saida.add(l);
            }
            return saida;
        }
    }

    private static List<List<String>> lerXlsx(File arquivo) throws IOException {
        Map<String, byte[]> partes = new HashMap<>();
        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(arquivo.toPath()))) {
            ZipEntry e;
            while ((e = zip.getNextEntry()) != null) {
                if (e.getName().endsWith(".xml")) {
                    partes.put(e.getName(), zip.readAllBytes());
                }
            }
        }
        if (partes.isEmpty()) {
            throw new IOException("Arquivo .xlsx inválido");
        }
        String planilha = "xl/worksheets/sheet1.xml";
        byte[] folha = partes.get(planilha);
        if (folha == null) {
            for (String chave : partes.keySet()) {
                if (chave.startsWith("xl/worksheets/sheet")) {
                    planilha = chave;
                    folha = partes.get(chave);
                    break;
                }
            }
        }
        if (folha == null) {
            throw new IOException("Planilha não encontrada no arquivo");
        }

        List<String> textos = new ArrayList<>();
        byte[] compartilhados = partes.get("xl/sharedStrings.xml");
        if (compartilhados != null) {
            Document doc = xml(compartilhados);
            NodeList itens = doc.getElementsByTagName("si");
            for (int i = 0; i < itens.getLength(); i++) {
                textos.add(textoDe((Element) itens.item(i)));
            }
        }

        List<List<String>> linhas = new ArrayList<>();
        Document doc = xml(folha);
        NodeList rows = doc.getElementsByTagName("row");
        for (int i = 0; i < rows.getLength(); i++) {
            Element linha = (Element) rows.item(i);
            NodeList celulas = linha.getElementsByTagName("c");
            Map<Integer, String> valores = new HashMap<>();
            int maior = -1;
            for (int j = 0; j < celulas.getLength(); j++) {
                Element c = (Element) celulas.item(j);
                int coluna = coluna(c.getAttribute("r"));
                String tipo = c.getAttribute("t");
                String valor;
                if ("s".equals(tipo)) {
                    int indice = inteiro(textoFilho(c, "v"));
                    valor = indice >= 0 && indice < textos.size() ? textos.get(indice) : "";
                } else if ("inlineStr".equals(tipo)) {
                    valor = textoDe(c);
                } else if ("b".equals(tipo)) {
                    valor = "1".equals(textoFilho(c, "v")) ? "SIM" : "NAO";
                } else {
                    valor = textoFilho(c, "v");
                }
                valores.put(coluna, valor.trim());
                maior = Math.max(maior, coluna);
            }
            List<String> linhaTexto = new ArrayList<>();
            for (int c = 0; c <= maior; c++) {
                linhaTexto.add(valores.getOrDefault(c, ""));
            }
            linhas.add(linhaTexto);
        }
        return linhas;
    }

    private static Document xml(byte[] conteudo) throws IOException {
        try {
            DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
            fabrica.setNamespaceAware(false);
            fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document doc = fabrica.newDocumentBuilder()
                    .parse(new java.io.ByteArrayInputStream(conteudo));
            doc.getDocumentElement().normalize();
            return doc;
        } catch (Exception e) {
            throw new IOException("Falha ao ler XML da planilha", e);
        }
    }

    private static String textoDe(Element elemento) {
        StringBuilder sb = new StringBuilder();
        NodeList textos = elemento.getElementsByTagName("t");
        for (int i = 0; i < textos.getLength(); i++) {
            sb.append(textos.item(i).getTextContent());
        }
        return sb.toString();
    }

    private static String textoFilho(Element elemento, String nome) {
        NodeList filhos = elemento.getChildNodes();
        for (int i = 0; i < filhos.getLength(); i++) {
            Node n = filhos.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && nome.equals(n.getNodeName())) {
                return n.getTextContent();
            }
        }
        return "";
    }

    private static int coluna(String referencia) {
        int valor = 0;
        for (int i = 0; i < referencia.length(); i++) {
            char c = referencia.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                valor = valor * 26 + (c - 'A' + 1);
            } else {
                break;
            }
        }
        return Math.max(0, valor - 1);
    }

    private static int inteiro(String valor) {
        try {
            return (int) Double.parseDouble(valor.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static void gravar(File destino, List<String> cabecalho, List<List<String>> linhas)
            throws IOException {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        xml.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>");

        escreverLinha(xml, 1, cabecalho);
        int numeroLinha = 2;
        for (List<String> linha : linhas) {
            escreverLinha(xml, numeroLinha++, linha);
        }
        xml.append("</sheetData></worksheet>");

        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(destino))) {
            zip.putNextEntry(new ZipEntry("[Content_Types].xml"));
            zip.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                    + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                    + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                    + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                    + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                    + "</Types>").getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();

            zip.putNextEntry(new ZipEntry("_rels/.rels"));
            zip.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                    + "</Relationships>").getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();

            zip.putNextEntry(new ZipEntry("xl/workbook.xml"));
            zip.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
                    + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                    + "<sheets><sheet name=\"Dados\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>")
                    .getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();

            zip.putNextEntry(new ZipEntry("xl/_rels/workbook.xml.rels"));
            zip.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                    + "</Relationships>").getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();

            zip.putNextEntry(new ZipEntry("xl/worksheets/sheet1.xml"));
            zip.write(xml.toString().getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
    }

    private static void escreverLinha(StringBuilder xml, int numero, List<String> valores) {
        xml.append("<row r=\"").append(numero).append("\">");
        for (int i = 0; i < valores.size(); i++) {
            String ref = letra(i) + numero;
            String valor = valores.get(i) == null ? "" : valores.get(i);
            xml.append("<c r=\"").append(ref).append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
                    .append(escapar(valor))
                    .append("</t></is></c>");
        }
        xml.append("</row>");
    }

    private static String letra(int indice) {
        StringBuilder sb = new StringBuilder();
        int n = indice;
        do {
            sb.insert(0, (char) ('A' + n % 26));
            n = n / 26 - 1;
        } while (n >= 0);
        return sb.toString();
    }

    private static String escapar(String valor) {
        return valor.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
