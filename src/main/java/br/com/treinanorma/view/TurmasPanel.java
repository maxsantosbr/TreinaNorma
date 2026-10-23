package br.com.treinanorma.view;

import br.com.treinanorma.dao.ColaboradorDAO;
import br.com.treinanorma.dao.EmpresaDAO;
import br.com.treinanorma.dao.InstrutorDAO;
import br.com.treinanorma.dao.MatrizDAO;
import br.com.treinanorma.dao.TurmaDAO;
import br.com.treinanorma.model.Colaborador;
import br.com.treinanorma.model.Encontro;
import br.com.treinanorma.model.Instrutor;
import br.com.treinanorma.model.MatrizItem;
import br.com.treinanorma.model.Participante;
import br.com.treinanorma.model.Turma;
import br.com.treinanorma.util.Cpf;
import br.com.treinanorma.util.Datas;
import br.com.treinanorma.util.Documentos;
import br.com.treinanorma.util.Fotos;
import br.com.treinanorma.util.Sessao;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class TurmasPanel extends TelaBase {
   private final TurmaDAO dao = new TurmaDAO();
   private final MatrizDAO matriz = new MatrizDAO();
   private final InstrutorDAO instrutores = new InstrutorDAO();
   private final ColaboradorDAO colaboradores = new ColaboradorDAO();
   private final EmpresaDAO empresas = new EmpresaDAO();
   private final JTextField busca = Ui.campo("Buscar por código, NR, treinamento, local ou instrutor...");
   private final JTable tabela = Ui.tabela("Código", "Data", "Norma", "Treinamento", "Instrutor", "Participantes", "Status");
   private final TableRowSorter<DefaultTableModel> ordenador;
   private final List<Turma> lista = new ArrayList<>();

   public TurmasPanel(Runnable voltar) {
      super("Turmas", "Execução coletiva de treinamento, chamada e geração da ata.", voltar);
      this.ordenador = new TableRowSorter<>((DefaultTableModel)this.tabela.getModel());
      this.tabela.setRowSorter(this.ordenador);
      this.tabela.getColumnModel().getColumn(1).setPreferredWidth(165);
      this.tabela.getColumnModel().getColumn(3).setPreferredWidth(260);
      this.acao(Ui.primario("+ Nova turma")).addActionListener(e -> this.editar(null));
   }

   @Override
   protected void construir() {
      JPanel cartao = Ui.cartao(new BorderLayout(0, 10));
      JPanel topo = new JPanel(new BorderLayout());
      topo.setOpaque(false);
      topo.add(this.busca, "West");
      this.busca.setPreferredSize(new Dimension(360, 30));
      this.busca.getDocument().addDocumentListener(new DocumentListener() {
         @Override
         public void insertUpdate(DocumentEvent e) {
            TurmasPanel.this.filtrar();
         }

         @Override
         public void removeUpdate(DocumentEvent e) {
            TurmasPanel.this.filtrar();
         }

         @Override
         public void changedUpdate(DocumentEvent e) {
            TurmasPanel.this.filtrar();
         }
      });
      topo.add(Ui.subtitulo("Fluxo: participantes > chamada > concluir > certificados."), "East");
      cartao.add(topo, "North");
      cartao.add(Ui.rolagem(this.tabela), "Center");
      JPanel rodape = new JPanel(new FlowLayout(0, 8, 0));
      rodape.setOpaque(false);
      JButton editar = Ui.secundario("Editar");
      editar.addActionListener(e -> this.editar(this.selecionado()));
      JButton participantes = Ui.secundario("Participantes");
      participantes.addActionListener(e -> this.participantes(this.selecionado()));
      JButton chamada = Ui.secundario("Chamada");
      chamada.addActionListener(e -> this.chamada(this.selecionado()));
      JButton concluir = Ui.secundario("Concluir turma");
      concluir.addActionListener(e -> this.concluir(this.selecionado()));
      JButton documentos = Ui.secundario("Documentos");
      documentos.addActionListener(e -> this.documentos(this.selecionado()));
      JButton excluir = Ui.perigo("Excluir");
      excluir.addActionListener(e -> this.excluir(this.selecionado()));
      rodape.add(editar);
      rodape.add(participantes);
      rodape.add(chamada);
      rodape.add(concluir);
      rodape.add(documentos);
      rodape.add(excluir);
      cartao.add(rodape, "South");
      this.tabela.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
               TurmasPanel.this.editar(TurmasPanel.this.selecionado());
            }
         }
      });
      this.conteudo(cartao);
      this.carregar();
   }

   private void carregar() {
      try {
         this.lista.clear();
         this.lista.addAll(this.dao.listar(null));
         DefaultTableModel modelo = (DefaultTableModel)this.tabela.getModel();
         modelo.setRowCount(0);

         for (Turma t : this.lista) {
            String periodo = Datas.br(t.getData());
            if (t.getDataFim() != null && !t.getDataFim().equals(t.getData())) {
               periodo = periodo + " a " + Datas.br(t.getDataFim());
            }

            modelo.addRow(
               new Object[]{
                  t.getCodigo(), periodo, t.getNorma(), t.getTreinamento(), this.nz(t.getInstrutorNome()), t.getParticipantes(), this.status(t.getStatus())
               }
            );
         }
      } catch (RuntimeException var5) {
         Ui.erro(this, var5);
      }
   }

   private String status(String valor) {
      if ("CONCLUIDA".equals(valor)) {
         return "Concluída";
      } else {
         return "CANCELADA".equals(valor) ? "Cancelada" : "Agendada";
      }
   }

   private void filtrar() {
      String termo = Ui.texto(this.busca);
      this.ordenador.setRowFilter(termo.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(termo)));
   }

   private Turma selecionado() {
      int linha = this.tabela.getSelectedRow();
      if (linha < 0) {
         Ui.aviso(this, "Selecione uma turma.");
         return null;
      } else {
         return this.lista.get(this.tabela.convertRowIndexToModel(linha));
      }
   }

   private void editar(Turma turma) {
      Turma alvo = turma == null ? new Turma() : turma;
      if (turma != null && "CONCLUIDA".equals(turma.getStatus())) {
         Ui.aviso(this, "Turma concluída não pode ser alterada.\n\nCrie uma nova turma de reciclagem para um novo treinamento.");
      } else {
         JDialog d = Ui.dialogo(this, turma == null ? "Nova turma" : "Editar turma", 880, 720);
         List<MatrizItem> itens = this.matriz.itens();
         Map<String, MatrizItem> porRotulo = new LinkedHashMap<>();

         for (MatrizItem i : itens) {
            porRotulo.put(i.toString(), i);
         }

         JComboBox<String> comboMatriz = Ui.combo(porRotulo.keySet().toArray(new String[0]));
         JTextField local = Ui.campo();
         JComboBox<String> modalidade = Ui.combo("Presencial", "Semipresencial", "EAD");
         JComboBox<String> comboInstrutor = Ui.combo();
         JTextArea conteudoGeral = new JTextArea(3, 20);
         conteudoGeral.setFont(Ui.fonte(12, 0));
         conteudoGeral.setLineWrap(true);
         conteudoGeral.setWrapStyleWord(true);
         JLabel baseLegal = Ui.subtitulo("Selecione o treinamento da matriz.");
         JLabel validade = Ui.subtitulo("");
         JLabel resumo = Ui.subtitulo("");
         JLabel ajudaConteudo = Ui.ajuda("Selecione um dia na grade para editar o conteúdo ministrado.");
         List<Encontro> encontros = new ArrayList<>();
         if (turma != null) {
            encontros.addAll(this.dao.encontros(turma.getId()));
            if (encontros.isEmpty() && turma.getData() != null) {
               Encontro unico = new Encontro();
               unico.setData(turma.getData());
               unico.setHoraInicio(turma.getHoraInicio());
               unico.setHoraFim(turma.getHoraFim());
               unico.setCh(turma.getChTotal());
               unico.setPratica(turma.getChTotal() > 0.0 && turma.getChPresencial() >= turma.getChTotal());
               unico.setConteudo(turma.getConteudo());
               encontros.add(unico);
            }
         }

         boolean[] carregando = new boolean[]{false};
         int[] linhaConteudo = new int[]{-1};
         DefaultTableModel modeloEnc = new DefaultTableModel(new Object[]{"Dia", "Data", "Início", "Fim", "CH", "Prática"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
               return column != 0;
            }

            @Override
            public Class<?> getColumnClass(int column) {
               return column == 5 ? Boolean.class : String.class;
            }
         };
         JTable tabelaEnc = Ui.tabela("Dia", "Data", "Início", "Fim", "CH", "Prática");
         tabelaEnc.setModel(modeloEnc);
         tabelaEnc.setRowHeight(28);
         tabelaEnc.getColumnModel().getColumn(0).setMaxWidth(44);
         tabelaEnc.getColumnModel().getColumn(1).setPreferredWidth(100);
         tabelaEnc.getColumnModel().getColumn(2).setPreferredWidth(70);
         tabelaEnc.getColumnModel().getColumn(3).setPreferredWidth(70);
         tabelaEnc.getColumnModel().getColumn(4).setPreferredWidth(60);
         tabelaEnc.getColumnModel().getColumn(5).setPreferredWidth(70);
         JScrollPane rolagemEnc = Ui.rolagem(tabelaEnc);
         rolagemEnc.setPreferredSize(new Dimension(780, 178));
         JTextArea conteudoDia = new JTextArea(3, 20);
         conteudoDia.setFont(Ui.fonte(12, 0));
         conteudoDia.setLineWrap(true);
         conteudoDia.setWrapStyleWord(true);
         conteudoDia.setEnabled(false);
         JScrollPane rolagemConteudoDia = new JScrollPane(conteudoDia);
         rolagemConteudoDia.setPreferredSize(new Dimension(780, 78));
         final Runnable sincronizarConteudo = () -> {
            if (linhaConteudo[0] >= 0 && linhaConteudo[0] < encontros.size()) {
               encontros.get(linhaConteudo[0]).setConteudo(conteudoDia.getText());
            }
         };
         Runnable carregarConteudo = () -> {
            int linha = tabelaEnc.getSelectedRow();
            carregando[0] = true;
            if (linha >= 0 && linha < encontros.size()) {
               linhaConteudo[0] = linha;
               String texto = encontros.get(linha).getConteudo();
               conteudoDia.setText(texto == null ? "" : texto);
               conteudoDia.setEnabled(true);
            } else {
               linhaConteudo[0] = -1;
               conteudoDia.setText("");
               conteudoDia.setEnabled(false);
            }

            carregando[0] = false;
         };
         Runnable atualizarResumo = () -> {
            double total = 0.0;
            double pratica = 0.0;

            for (Encontro e : encontros) {
               total += e.getCh();
               if (e.isPratica()) {
                  pratica += e.getCh();
               }
            }

            MatrizItem item = porRotulo.get((String)comboMatriz.getSelectedItem());
            String texto = encontros.size()
               + " encontro(s)  -  "
               + this.numero(total)
               + " h  -  prática "
               + this.numero(pratica)
               + " h"
               + (total > 0.0 ? " (" + Math.round(pratica / total * 100.0) + "%)" : "");
            Color cor = Ui.SUAVE;
            if (item != null && item.getChMinima() != null && total + 0.001 < item.getChMinima()) {
               texto = "CH abaixo da matriz: " + this.numero(total) + " h de " + this.numero(item.getChMinima()) + " h";
               cor = Ui.VERMELHO;
            } else if (item != null && item.getPresencialMinimo() != null && pratica + 0.001 < item.getPresencialMinimo()) {
               texto = texto + "  -  prática abaixo do mínimo (" + this.numero(item.getPresencialMinimo()) + " h)";
               cor = Ui.AMBAR;
            }

            resumo.setText(texto);
            resumo.setForeground(cor);
         };
         Runnable carregarGrade = () -> {
            carregando[0] = true;
            modeloEnc.setRowCount(0);

            for (int i = 0; i < encontros.size(); i++) {
               Encontro e = encontros.get(i);
               modeloEnc.addRow(
                  new Object[]{i + 1, Datas.br(e.getData()), this.nz(e.getHoraInicio()), this.nz(e.getHoraFim()), this.numero(e.getCh()), e.isPratica()}
               );
            }

            carregando[0] = false;
            atualizarResumo.run();
            if (!encontros.isEmpty()) {
               tabelaEnc.setRowSelectionInterval(0, 0);
            }

            carregarConteudo.run();
         };
         modeloEnc.addTableModelListener(ev -> {
            if (!carregando[0] && ev.getType() == 0) {
               int linha = ev.getFirstRow();
               if (linha >= 0 && linha < encontros.size()) {
                  Encontro e = encontros.get(linha);
                  e.setData(Datas.ler(String.valueOf(modeloEnc.getValueAt(linha, 1))));
                  e.setHoraInicio(String.valueOf(modeloEnc.getValueAt(linha, 2)));
                  e.setHoraFim(String.valueOf(modeloEnc.getValueAt(linha, 3)));
                  e.setCh(this.numero(String.valueOf(modeloEnc.getValueAt(linha, 4))));
                  e.setPratica(Boolean.TRUE.equals(modeloEnc.getValueAt(linha, 5)));
                  atualizarResumo.run();
               }
            }
         });
         tabelaEnc.getSelectionModel().addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting() && !carregando[0]) {
               sincronizarConteudo.run();
               carregarConteudo.run();
            }
         });
         conteudoDia.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
               sincronizarConteudo.run();
            }
         });
         JButton adicionarDia = Ui.secundario("+ Adicionar dia");
         JButton removerDia = Ui.secundario("Remover dia");
         adicionarDia.addActionListener(e -> {
            sincronizarConteudo.run();
            Encontro ultimo = encontros.isEmpty() ? null : encontros.get(encontros.size() - 1);
            Encontro novo = new Encontro();
            LocalDate base = ultimo != null && ultimo.getData() != null ? ultimo.getData().plusDays(1L) : LocalDate.now();
            novo.setData(base);
            novo.setHoraInicio(ultimo != null ? ultimo.getHoraInicio() : "08:00");
            novo.setHoraFim(ultimo != null ? ultimo.getHoraFim() : "17:00");
            novo.setCh(ultimo != null ? ultimo.getCh() : 8.0);
            novo.setPratica(ultimo != null && ultimo.isPratica());
            encontros.add(novo);
            carregarGrade.run();
            tabelaEnc.setRowSelectionInterval(encontros.size() - 1, encontros.size() - 1);
         });
         removerDia.addActionListener(e -> {
            int linha = tabelaEnc.getSelectedRow();
            if (linha < 0) {
               Ui.aviso(d, "Selecione o dia que deseja remover.");
            } else if (encontros.size() == 1) {
               Ui.aviso(d, "A turma precisa de pelo menos um dia de treinamento.");
            } else {
               sincronizarConteudo.run();
               linhaConteudo[0] = -1;
               encontros.remove(linha);
               carregarGrade.run();
               int alvoLinha = Math.min(linha, encontros.size() - 1);
               tabelaEnc.setRowSelectionInterval(alvoLinha, alvoLinha);
            }
         });
         Runnable atualizarInstrutores = () -> {
            MatrizItem item = porRotulo.get((String)comboMatriz.getSelectedItem());
            comboInstrutor.removeAllItems();
            comboInstrutor.addItem("(sem instrutor)");
            if (item != null) {
               for (Instrutor i : this.instrutores.habilitadosPara(item.getNorma())) {
                  comboInstrutor.addItem(i.getNome());
               }
            }

            if (alvo.getInstrutorId() != null) {
               Instrutor i = this.instrutores.porId(alvo.getInstrutorId());
               if (i != null) {
                  comboInstrutor.setSelectedItem(i.getNome());
               }
            }
         };
         Consumer<MatrizItem> aplicar = item -> {
            if (item != null) {
               baseLegal.setText("Base legal: " + this.nz(item.getBaseLegal()));
               validade.setText(
                  item.getValidadeMeses() == null
                     ? "Validade definida pelo PGR (a NR não fixa prazo)."
                     : "Validade: " + item.getValidadeMeses() + " meses a partir do início da turma."
               );
               atualizarInstrutores.run();
               sincronizarConteudo.run();
               boolean auto = turma == null && encontros.size() == 1 && encontros.get(0).getId() == 0;
               if (encontros.isEmpty() || auto) {
                  if (encontros.isEmpty()) {
                     Encontro novo = new Encontro();
                     novo.setData(LocalDate.now());
                     novo.setHoraInicio("08:00");
                     novo.setHoraFim("17:00");
                     encontros.add(novo);
                  }

                  encontros.get(0).setCh(item.getChMinima() == null ? 8.0 : item.getChMinima());
                  carregarGrade.run();
               }

               if (item.getModalidade() != null && !item.getModalidade().isBlank()) {
                  modalidade.setSelectedItem(item.getModalidade());
               }

               atualizarResumo.run();
            }
         };
         if (turma != null) {
            for (MatrizItem i : itens) {
               if (i.getId() == (alvo.getMatrizId() == null ? -1 : alvo.getMatrizId())) {
                  comboMatriz.setSelectedItem(i.toString());
                  break;
               }
            }

            local.setText(this.nz(alvo.getLocal()));
            modalidade.setSelectedItem(this.nz(alvo.getModalidade()).isEmpty() ? "Presencial" : alvo.getModalidade());
            conteudoGeral.setText(this.nz(alvo.getConteudo()));
         }

         comboMatriz.addActionListener(e -> aplicar.accept(porRotulo.get((String)comboMatriz.getSelectedItem())));
         aplicar.accept(porRotulo.get((String)comboMatriz.getSelectedItem()));
         carregarGrade.run();
         JPanel form = Ui.formulario();
         GridBagConstraints gbc = Ui.gbc();
         Ui.campo(form, gbc, 0, 0, "Treinamento da matriz *", comboMatriz, 2);
         Ui.campo(form, gbc, 2, 0, "Modalidade", modalidade, 1);
         Ui.campo(form, gbc, 0, 1, "Local", local, 2);
         Ui.campo(form, gbc, 2, 1, "Instrutor", comboInstrutor, 2);
         JPanel tituloEnc = new JPanel(new BorderLayout());
         tituloEnc.setOpaque(false);
         tituloEnc.add(Ui.rotulo("Encontros (dias) *"), "West");
         tituloEnc.add(Ui.ajuda("Cada dia gera uma ata; o certificado cobre o período."), "East");
         JPanel botoesEnc = new JPanel(new FlowLayout(0, 6, 2));
         botoesEnc.setOpaque(false);
         botoesEnc.add(adicionarDia);
         botoesEnc.add(removerDia);
         botoesEnc.add(resumo);
         JPanel blocoConteudoDia = new JPanel(new BorderLayout(0, 3));
         blocoConteudoDia.setOpaque(false);
         blocoConteudoDia.add(Ui.linhaEsquerda(Ui.rotulo("Conteúdo do dia"), ajudaConteudo), "North");
         blocoConteudoDia.add(rolagemConteudoDia, "Center");
         JPanel blocoGeral = new JPanel(new BorderLayout(0, 3));
         blocoGeral.setOpaque(false);
         blocoGeral.add(Ui.rotulo("Conteúdo programático (geral)"), "North");
         JScrollPane rolagemGeral = new JScrollPane(conteudoGeral);
         rolagemGeral.setPreferredSize(new Dimension(780, 64));
         blocoGeral.add(rolagemGeral, "Center");
         JPanel corpo = new JPanel();
         corpo.setLayout(new BoxLayout(corpo, 1));
         corpo.setBorder(BorderFactory.createEmptyBorder(10, 14, 6, 14));
         form.setAlignmentX(0.0F);
         tituloEnc.setAlignmentX(0.0F);
         rolagemEnc.setAlignmentX(0.0F);
         botoesEnc.setAlignmentX(0.0F);
         blocoConteudoDia.setAlignmentX(0.0F);
         blocoGeral.setAlignmentX(0.0F);
         baseLegal.setAlignmentX(0.0F);
         validade.setAlignmentX(0.0F);
         corpo.add(form);
         corpo.add(Box.createVerticalStrut(4));
         corpo.add(tituloEnc);
         corpo.add(rolagemEnc);
         corpo.add(botoesEnc);
         corpo.add(blocoConteudoDia);
         corpo.add(Box.createVerticalStrut(6));
         corpo.add(blocoGeral);
         corpo.add(Box.createVerticalStrut(4));
         corpo.add(baseLegal);
         corpo.add(validade);
         JScrollPane rolagemCorpo = new JScrollPane(corpo);
         rolagemCorpo.setBorder(BorderFactory.createEmptyBorder());
         JButton salvar = Ui.primario(turma == null ? "Criar turma" : "Salvar alterações");
         JButton cancelar = Ui.secundario("Cancelar");
         JLabel dicaRodape = Ui.ajuda(
            turma == null ? "A turma será criada com os dias informados." : "As alterações substituem os dados desta turma; nenhum novo registro é criado."
         );
         dicaRodape.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));
         JPanel botoesRodape = new JPanel(new FlowLayout(2, 8, 8));
         botoesRodape.add(cancelar);
         botoesRodape.add(salvar);
         JPanel rodape = new JPanel(new BorderLayout());
         rodape.add(dicaRodape, "Center");
         rodape.add(botoesRodape, "East");
         salvar.addActionListener(
            e -> {
               try {
                  sincronizarConteudo.run();
                  MatrizItem item = porRotulo.get((String)comboMatriz.getSelectedItem());
                  if (item == null) {
                     Ui.aviso(d, "Selecione o treinamento da matriz.");
                     return;
                  }

                  if (encontros.isEmpty()) {
                     Ui.aviso(d, "Adicione pelo menos um dia de treinamento.");
                     return;
                  }

                  Set<LocalDate> datas = new HashSet<>();
                  double total = 0.0;
                  double pratica = 0.0;

                  for (int ix = 0; ix < encontros.size(); ix++) {
                     Encontro en = encontros.get(ix);
                     if (en.getData() == null) {
                        Ui.aviso(d, "Dia " + (ix + 1) + ": data inválida. Use dd/mm/aaaa.");
                        return;
                     }

                     if (!datas.add(en.getData())) {
                        Ui.aviso(d, "Dia " + (ix + 1) + ": data repetida (" + Datas.br(en.getData()) + "). Cada dia deve ter uma data distinta.");
                        return;
                     }

                     if (en.getCh() <= 0.0) {
                        Ui.aviso(d, "Dia " + (ix + 1) + ": informe a carga horária do dia.");
                        return;
                     }

                     total += en.getCh();
                     if (en.isPratica()) {
                        pratica += en.getCh();
                     }
                  }

                  if (item.getChMinima() != null && total + 0.001 < item.getChMinima()) {
                     Ui.aviso(
                        d, "A soma dos dias (" + this.numero(total) + " h) é menor que a CH mínima da matriz (" + this.numero(item.getChMinima()) + " h)."
                     );
                     return;
                  }

                  if (item.getPresencialMinimo() != null
                     && pratica + 0.001 < item.getPresencialMinimo()
                     && !Ui.confirma(
                        d,
                        "A parte prática ("
                           + this.numero(pratica)
                           + " h) está abaixo do mínimo da matriz ("
                           + this.numero(item.getPresencialMinimo())
                           + " h).\n\nSalvar mesmo assim?"
                     )) {
                     return;
                  }

                  if (turma != null && "CONCLUIDA".equals(alvo.getStatus())) {
                     Ui.aviso(d, "Turma concluída não pode ser alterada. Crie uma nova turma de reciclagem.");
                     return;
                  }

                  encontros.sort((a, b) -> a.getData().compareTo((ChronoLocalDate)b.getData()));
                  Encontro primeiro = encontros.get(0);
                  alvo.setMatrizId(item.getId());
                  alvo.setEmpresaId(1);
                  alvo.setNorma(item.getNorma());
                  alvo.setTreinamento(item.getTreinamento());
                  alvo.setData(primeiro.getData());
                  alvo.setHoraInicio(primeiro.getHoraInicio());
                  alvo.setHoraFim(primeiro.getHoraFim());
                  alvo.setLocal(Ui.texto(local));
                  alvo.setModalidade((String)modalidade.getSelectedItem());
                  alvo.setChTotal(total);
                  alvo.setChPresencial(pratica);
                  String nomeInstrutor = (String)comboInstrutor.getSelectedItem();
                  alvo.setInstrutorId(null);
                  if (nomeInstrutor != null && !nomeInstrutor.startsWith("(")) {
                     for (Instrutor ix : this.instrutores.listar(nomeInstrutor)) {
                        if (ix.getNome().equals(nomeInstrutor)) {
                           alvo.setInstrutorId(ix.getId());
                           break;
                        }
                     }
                  }

                  alvo.setConteudo(conteudoGeral.getText());
                  if (turma == null) {
                     alvo.setCriadoPor(Sessao.nome());
                  }

                  this.dao.salvar(alvo);
                  this.dao.salvarEncontros(alvo.getId(), encontros);
                  d.dispose();
                  this.carregar();
               } catch (RuntimeException var23x) {
                  Ui.erro(d, var23x);
               }
            }
         );
         cancelar.addActionListener(e -> d.dispose());
         d.add(rolagemCorpo, "Center");
         d.add(rodape, "South");
         d.setVisible(true);
      }
   }

   private void participantes(Turma turma) {
      if (turma != null) {
         try {
            List<Colaborador> todos = this.colaboradores.ativos();
            List<Participante> atuais = this.dao.participantes(turma.getId());
            DefaultListModel<String> modelo = new DefaultListModel<>();
            List<Colaborador> ordem = new ArrayList<>();

            for (Colaborador c : todos) {
               if (turma.getInstrutorId() != null) {
                  Instrutor i = this.instrutores.porId(turma.getInstrutorId());
                  if (i != null && i.getCpf() != null && i.getCpf().equals(c.getCpf())) {
                     continue;
                  }
               }

               ordem.add(c);
               modelo.addElement(c.getNome() + "  -  " + c.getCpf() + (c.getCargo() == null ? "" : "  -  " + c.getCargo()));
            }

            JList<String> listaColab = new JList<>(modelo);
            listaColab.setFont(Ui.fonte(12, 0));
            listaColab.setSelectionMode(2);
            List<Integer> selecionados = new ArrayList<>();

            for (Participante p : atuais) {
               for (int i = 0; i < ordem.size(); i++) {
                  if (ordem.get(i).getId() == p.getColaboradorId()) {
                     selecionados.add(i);
                  }
               }
            }

            int[] indices = selecionados.stream().mapToInt(Integer::intValue).toArray();
            listaColab.setSelectedIndices(indices);
            JDialog d = Ui.dialogo(this, "Participantes - " + turma.getCodigo(), 620, 520);
            JPanel corpo = new JPanel(new BorderLayout(0, 8));
            corpo.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
            corpo.add(Ui.subtitulo("Selecione os colaboradores (Ctrl para vários). Somente presentes e aprovados geram certificado."), "North");
            JScrollPane rolagem = new JScrollPane(listaColab);
            corpo.add(rolagem, "Center");
            JButton salvar = Ui.primario("Salvar seleção");
            JButton cancelar = Ui.secundario("Cancelar");
            JPanel rodape = new JPanel(new FlowLayout(2, 8, 8));
            rodape.add(cancelar);
            rodape.add(salvar);
            salvar.addActionListener(ex -> {
               try {
                  List<Integer> escolhidos = new ArrayList<>();

                  for (int ix : listaColab.getSelectedIndices()) {
                     escolhidos.add(ordem.get(ix).getId());
                  }

                  List<Integer> atuaisIds = new ArrayList<>();

                  for (Participante p : atuais) {
                     atuaisIds.add(p.getColaboradorId());
                  }

                  List<Integer> adicionar = new ArrayList<>(escolhidos);
                  adicionar.removeAll(atuaisIds);
                  List<Integer> remover = new ArrayList<>(atuaisIds);
                  remover.removeAll(escolhidos);
                  if (!adicionar.isEmpty()) {
                     this.dao.adicionarParticipantes(turma.getId(), adicionar);
                  }

                  for (Integer id : remover) {
                     this.dao.removerParticipante(turma.getId(), id);
                  }

                  d.dispose();
                  this.carregar();
               } catch (RuntimeException var13x) {
                  Ui.erro(d, var13x);
               }
            });
            cancelar.addActionListener(ex -> d.dispose());
            d.add(corpo, "Center");
            d.add(rodape, "South");
            d.setVisible(true);
         } catch (RuntimeException var15) {
            Ui.erro(this, var15);
         }
      }
   }

   private void chamada(Turma turma) {
      if (turma != null) {
         if ("CONCLUIDA".equals(turma.getStatus())) {
            Ui.aviso(this, "Turma concluída: a chamada não pode mais ser alterada.\n\nOs registros e documentos já foram gerados.");
         } else {
            try {
               List<Participante> participantes = this.dao.participantes(turma.getId());
               if (participantes.isEmpty()) {
                  Ui.aviso(this, "Adicione participantes antes de lançar a chamada.");
                  return;
               }

               List<Encontro> encontros = this.dao.encontros(turma.getId());
               Map<Integer, Set<Integer>> presencas = this.dao.presencas(turma.getId());
               DefaultTableModel modelo = new DefaultTableModel(new Object[]{"Nome", "CPF", "Presente", "Aprovado"}, 0) {
                  @Override
                  public boolean isCellEditable(int row, int column) {
                     return column >= 2;
                  }

                  @Override
                  public Class<?> getColumnClass(int column) {
                     return column >= 2 ? Boolean.class : String.class;
                  }
               };

               for (Participante p : participantes) {
                  modelo.addRow(new Object[]{p.getNome(), Cpf.formatar(p.getCpf()), p.isPresente(), p.getAprovado() == null ? Boolean.TRUE : p.getAprovado()});
               }

               JTable t = Ui.tabela("Nome", "CPF", "Presente", "Aprovado");
               t.setModel(modelo);
               t.getColumnModel().getColumn(0).setPreferredWidth(280);
               t.getColumnModel().getColumn(2).setPreferredWidth(70);
               t.getColumnModel().getColumn(3).setPreferredWidth(70);
               JComboBox<String> comboDia = Ui.combo();

               for (int i = 0; i < encontros.size(); i++) {
                  Encontro e = encontros.get(i);
                  comboDia.addItem(
                     "Dia " + (i + 1) + " - " + Datas.br(e.getData()) + " (" + this.numero(e.getCh()) + " h" + (e.isPratica() ? ", prática" : "") + ")"
                  );
               }

               boolean[] carregando = new boolean[]{false};
               Runnable carregarPresencas = () -> {
                  int indice = comboDia.getSelectedIndex();
                  if (indice >= 0 && indice < encontros.size()) {
                     Set<Integer> doDia = presencas.getOrDefault(encontros.get(indice).getId(), Set.of());

                     for (int i = 0; i < participantes.size(); i++) {
                        modelo.setValueAt(doDia.contains(participantes.get(i).getId()), i, 2);
                     }
                  } else {
                     for (int i = 0; i < participantes.size(); i++) {
                        modelo.setValueAt(participantes.get(i).isPresente(), i, 2);
                     }
                  }
               };
               Runnable coletar = () -> {
                  int indice = comboDia.getSelectedIndex();
                  if (indice >= 0 && indice < encontros.size()) {
                     Set<Integer> marcados = new HashSet<>();

                     for (int i = 0; i < participantes.size(); i++) {
                        if (Boolean.TRUE.equals(modelo.getValueAt(i, 2))) {
                           marcados.add(participantes.get(i).getId());
                        }
                     }

                     presencas.put(encontros.get(indice).getId(), marcados);
                  }
               };
               comboDia.addActionListener(ex -> {
                  if (!carregando[0]) {
                     coletar.run();
                     carregarPresencas.run();
                  }
               });
               if (!encontros.isEmpty()) {
                  carregando[0] = true;
                  comboDia.setSelectedIndex(0);
                  carregando[0] = false;
                  carregarPresencas.run();
               }

               JButton marcarTodos = Ui.link("Marcar todos presentes");
               marcarTodos.addActionListener(ex -> {
                  for (int i = 0; i < participantes.size(); i++) {
                     modelo.setValueAt(Boolean.TRUE, i, 2);
                  }
               });
               JButton limpar = Ui.link("Limpar dia");
               limpar.addActionListener(ex -> {
                  for (int i = 0; i < participantes.size(); i++) {
                     modelo.setValueAt(Boolean.FALSE, i, 2);
                  }
               });
               JPanel topo = new JPanel(new BorderLayout(0, 6));
               topo.setOpaque(false);
               JPanel linhaDia = new JPanel(new FlowLayout(0, 8, 0));
               linhaDia.setOpaque(false);
               if (!encontros.isEmpty()) {
                  linhaDia.add(Ui.rotulo("Dia:"));
                  comboDia.setPreferredSize(new Dimension(300, 28));
                  linhaDia.add(comboDia);
                  linhaDia.add(marcarTodos);
                  linhaDia.add(limpar);
               }

               topo.add(linhaDia, "North");
               topo.add(
                  Ui.subtitulo(
                     encontros.isEmpty()
                        ? "Marque presença e aprovação. A preventiva pode ser lançada depois."
                        : "Marque a presença do dia selecionado. O certificado exige presença em todos os " + encontros.size() + " encontros."
                  ),
                  "South"
               );
               JDialog d = Ui.dialogo(this, "Chamada - " + turma.getCodigo(), 720, 500);
               JPanel corpo = new JPanel(new BorderLayout(0, 8));
               corpo.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
               corpo.add(topo, "North");
               corpo.add(Ui.rolagem(t), "Center");
               JButton salvar = Ui.primario("Salvar chamada");
               JButton cancelar = Ui.secundario("Cancelar");
               JPanel rodape = new JPanel(new FlowLayout(2, 8, 8));
               rodape.add(cancelar);
               rodape.add(salvar);
               salvar.addActionListener(ex -> {
                  try {
                     for (int i = 0; i < participantes.size(); i++) {
                        Participante p = participantes.get(i);
                        Object aprovado = modelo.getValueAt(i, 3);
                        p.setAprovado(aprovado == null ? null : Boolean.TRUE.equals(aprovado));
                        p.setEletronica(true);
                     }

                     if (encontros.isEmpty()) {
                        for (int i = 0; i < participantes.size(); i++) {
                           participantes.get(i).setPresente(Boolean.TRUE.equals(modelo.getValueAt(i, 2)));
                        }

                        this.dao.salvarChamada(participantes);
                     } else {
                        coletar.run();

                        for (Encontro en : encontros) {
                           Set<Integer> doDia = presencas.getOrDefault(en.getId(), Set.of());
                           Map<Integer, Boolean> porParticipante = new LinkedHashMap<>();

                           for (Participante p : participantes) {
                              porParticipante.put(p.getId(), doDia.contains(p.getId()));
                           }

                           this.dao.salvarPresencas(en.getId(), porParticipante);
                        }

                        this.dao.sincronizarPresenca(turma.getId());
                        this.dao.salvarAprovacao(participantes);
                     }

                     d.dispose();
                     this.carregar();
                  } catch (RuntimeException var15x) {
                     Ui.erro(d, var15x);
                  }
               });
               cancelar.addActionListener(ex -> d.dispose());
               d.add(corpo, "Center");
               d.add(rodape, "South");
               d.setVisible(true);
            } catch (RuntimeException var20) {
               Ui.erro(this, var20);
            }
         }
      }
   }

   private void concluir(Turma turma) {
      if (turma != null) {
         if ("CONCLUIDA".equals(turma.getStatus())) {
            Ui.info(this, "Turma já concluída em " + turma.getConcluidaEm() + ".");
         } else {
            try {
               List<Participante> participantes = this.dao.participantes(turma.getId());
               List<Encontro> encontros = this.dao.encontros(turma.getId());
               int presentes = 0;

               for (Participante p : participantes) {
                  if (p.isPresente()) {
                     presentes++;
                  }
               }

               if (presentes == 0) {
                  Ui.aviso(this, "Nenhum participante presente. Lance a chamada antes de concluir.");
                  return;
               }

               if (!Ui.confirma(
                  this,
                  "Concluir a turma "
                     + turma.getCodigo()
                     + "?\n\n"
                     + presentes
                     + " participante(s) com presença em "
                     + (encontros.isEmpty() ? "todos os dias" : "todos os " + encontros.size() + " encontros")
                     + " gerarão registro de treinamento com validade calculada pela matriz.\nQuem faltou a algum dia não recebe registro (deve refazer o treinamento).\nA ata será selada com hash de integridade."
               )) {
                  return;
               }

               int gerados = this.dao.concluir(turma.getId(), Sessao.nome());
               this.carregar();
               if (Ui.confirma(this, gerados + " registro(s) gerado(s).\n\nDeseja gerar as atas em PDF agora?")) {
                  Turma cheia = this.dao.porId(turma.getId());
                  List<File> atas = this.gerarAtas(cheia);
                  Ui.perguntaAbrirLocal(
                     this, atas.size() + " ata(s) gerada(s) em " + Documentos.pasta().getAbsolutePath(), atas.size() == 1 ? atas.get(0) : Documentos.pasta()
                  );
               }
            } catch (Exception var8) {
               Ui.erro(this, "Falha ao gerar documento: " + var8.getMessage());
            }
         }
      }
   }

   private void documentos(Turma turma) {
      if (turma != null) {
         JDialog d = Ui.dialogo(this, "Documentos - " + turma.getCodigo(), 500, 340);
         JPanel corpo = new JPanel();
         corpo.setLayout(new BoxLayout(corpo, 1));
         corpo.setBorder(BorderFactory.createEmptyBorder(16, 18, 12, 18));
         JLabel pastaInfo = Ui.subtitulo("Arquivos gerados na pasta Documentos do sistema.");
         pastaInfo.setToolTipText(Documentos.pasta().getAbsolutePath());
         corpo.add(pastaInfo);
         corpo.add(Box.createVerticalStrut(10));
         JButton atas = Ui.primario("Atas de presença - uma por dia (A4)");
         JButton consolidada = Ui.secundario("Ata consolidada do período (A4)");
         JButton crachas = Ui.secundario("Crachás - frente e verso (A4 paisagem)");
         JButton pasta = Ui.link("Abrir pasta de documentos");

         for (JButton b : new JButton[]{atas, consolidada, crachas, pasta}) {
            b.setAlignmentX(0.0F);
            corpo.add(b);
            corpo.add(Box.createVerticalStrut(6));
         }

         atas.addActionListener(
            e -> {
               try {
                  Turma cheia = this.dao.porId(turma.getId());
                  List<File> arquivos = this.gerarAtas(cheia);
                  Ui.perguntaAbrirLocal(
                     d,
                     arquivos.size() + " ata(s) gerada(s) em " + Documentos.pasta().getAbsolutePath(),
                     arquivos.size() == 1 ? arquivos.get(0) : Documentos.pasta()
                  );
               } catch (Exception var6x) {
                  Ui.erro(d, "Falha ao gerar a ata: " + var6x.getMessage());
               }
            }
         );
         consolidada.addActionListener(
            e -> {
               try {
                  Turma cheia = this.dao.porId(turma.getId());
                  List<Encontro> encontros = this.dao.encontros(cheia.getId());
                  if (encontros.isEmpty()) {
                     Ui.aviso(d, "Esta turma não possui encontros cadastrados.");
                     return;
                  }

                  File f = Documentos.ataConsolidada(
                     cheia, this.empresas.obter(), this.instrutor(cheia), encontros, this.dao.participantes(cheia.getId()), this.dao.presencas(cheia.getId())
                  );
                  Ui.perguntaAbrirLocal(d, "Ata consolidada gerada em " + Documentos.pasta().getAbsolutePath(), f);
               } catch (Exception var7x) {
                  Ui.erro(d, "Falha ao gerar a ata consolidada: " + var7x.getMessage());
               }
            }
         );
         crachas.addActionListener(e -> {
            try {
               List<Participante> ps = this.dao.participantes(turma.getId());
                if (ps.isEmpty()) {
                   Ui.aviso(d, "Sem participantes.");
                   return;
                }

                int semFoto = 0;
                for (Participante p : ps) {
                   Colaborador c = this.colaboradores.porId(p.getColaboradorId());
                   File f = c == null ? null : Fotos.arquivo(c.getFoto());
                   if (f == null || !f.isFile()) {
                      semFoto++;
                   }
                }
                if (semFoto > 0
                      && !Ui.confirma(d, semFoto + " participante(s) sem foto. Gerar os crachás assim mesmo?")) {
                   return;
                }

                this.abrir(Documentos.crachas(turma, this.empresas.obter(), ps));
            } catch (Exception var5x) {
               Ui.erro(d, "Falha ao gerar crachás: " + var5x.getMessage());
            }
         });
         pasta.addActionListener(e -> {
            try {
               Desktop.getDesktop().open(Documentos.pasta());
            } catch (Exception var3x) {
               Ui.erro(d, var3x);
            }
         });
         d.add(corpo, "Center");
         d.setVisible(true);
      }
   }

   private Instrutor instrutor(Turma t) {
      return t.getInstrutorId() == null ? null : this.instrutores.porId(t.getInstrutorId());
   }

   private List<File> gerarAtas(Turma t) throws Exception {
      List<Encontro> encontros = this.dao.encontros(t.getId());
      List<Participante> participantes = this.dao.participantes(t.getId());
      List<File> arquivos = new ArrayList<>();
      if (encontros.isEmpty()) {
         arquivos.add(Documentos.ata(t, this.empresas.obter(), this.instrutor(t), participantes));
         return arquivos;
      } else {
         Map<Integer, Set<Integer>> presencas = this.dao.presencas(t.getId());

         for (int i = 0; i < encontros.size(); i++) {
            Encontro e = encontros.get(i);
            Set<Integer> doDia = presencas.getOrDefault(e.getId(), Set.of());
            List<Participante> presentes = new ArrayList<>();

            for (Participante p : participantes) {
               if (doDia.contains(p.getId())) {
                  presentes.add(p);
               }
            }

            arquivos.add(Documentos.ataDia(t, this.empresas.obter(), this.instrutor(t), e, i, encontros.size(), presentes));
         }

         return arquivos;
      }
   }

   private void abrir(File arquivo) {
      try {
         Desktop.getDesktop().open(arquivo);
      } catch (Exception var3) {
         Ui.info(this, "Arquivo gerado em:\n" + arquivo.getAbsolutePath());
      }
   }

   private void excluir(Turma t) {
      if (t != null) {
         if ("CONCLUIDA".equals(t.getStatus())) {
            Ui.aviso(this, "Turma concluída não pode ser excluída (rastreabilidade).");
         } else if (Ui.confirma(this, "Excluir a turma " + t.getCodigo() + "?")) {
            try {
               this.dao.excluir(t.getId());
               this.carregar();
            } catch (RuntimeException var3) {
               Ui.erro(this, var3);
            }
         }
      }
   }

   private double numero(String valor) {
      if (valor != null && !valor.isBlank()) {
         try {
            return Double.parseDouble(valor.trim().replace(',', '.'));
         } catch (NumberFormatException var3) {
            return 0.0;
         }
      } else {
         return 0.0;
      }
   }

   private String numero(double valor) {
      return valor == Math.rint(valor) ? String.valueOf((int)valor) : String.valueOf(valor);
   }

   private String nz(String valor) {
      return valor == null ? "" : valor;
   }
}
