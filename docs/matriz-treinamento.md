# Matriz de Treinamentos NR — TreinaNorma

Documento de domínio para popular a tabela `matriz_treinamento`.
Cada linha = **um treinamento exigível** + **critério que define quem precisa dele** + **carga/validade**.

Legenda de confiança:

| Símbolo | Significado |
|---|---|
| ✔ | Carga horária e/ou periodicidade **fixadas no texto da NR** (conferido) |
| ~ | A NR **não fixa** número: o empregador define (valor de mercado indicado como referência) |
| ⚠ | Número de referência de mercado / versão da NR a conferir antes de faturar ou emitir certificado |

---

## 1. Como o critério funciona

`criterio_tipo` diz **que dimensão** decide a carga horária:

| criterio_tipo | Usado por | `criterio_valor` |
|---|---|---|
| `TODOS` | capacitações gerais (integração, EPI) | vazio |
| `GRAU_RISCO` | **somente NR-05** (CIPA) e dimensionamento de SESMT/CIPA | `1`, `2`, `3`, `4` |
| `CLASSE` | NR-20 (instalação CI/CII/CIII), NR-13 (caldeira A/B/C) | `CI`, `CII`, `CIII`, `A`, `B`, `C` |
| `PAPEL` | NR-33 (vigia, autorizado, supervisor), NR-23 (brigadista), NR-05 (representante nomeado) | nome do papel |
| `FUNCAO` | função/cargo (operador de caldeira, aplicador de agrotóxico) | nome da função |
| `EQUIPAMENTO` | NR-11, NR-12, NR-18 (grua, guindaste, elevador) | tipo do equipamento |
| `ATIVIDADE` | trabalho em altura, espaço confinado, trabalho a quente | nome da atividade |
| `ESTADO` | NR-23 (brigada varia por IT do Corpo de Bombeiros) | `MG`, `SP`, ... |

> **Regra do produto:** o cálculo de "quem está vencido" é sempre
> `colaborador → funções/atribuições → treinamentos exigidos (matriz) → turmas/conclusões → data de validade`.
> A matriz **não** guarda pessoas; guarda a regra.

---

## 2. Matriz

### NR-01 — Disposições gerais e gerenciamento de riscos

| Treinamento | Critério | CH mín. | Presencial | Validade | Base legal | |
|---|---|---|---|---|---|---|
| Capacitação geral em SST (integração) | `TODOS` | definida pelo empregador | — | eventual / a critério | NR-01 item 1.7 | ~ |
| Capacitação por risco do PGR | `FUNCAO` | definida pelo empregador | — | conforme PGR | NR-01 1.7.1 | ~ |

NR-01 **não fixa carga horária**: fixa conteúdo, registro, certificado e critérios de aproveitamento.

### NR-05 — CIPA

| Treinamento | Critério | CH mín. | Presencial | Validade | Base legal | |
|---|---|---|---|---|---|---|
| CIPA — titulares e suplentes | `GRAU_RISCO 1` | 8 h | 0 h (100% EaD) | por mandato (12 meses) | NR-05 Anexo I, item 1.5 | ✔ |
| CIPA — titulares e suplentes | `GRAU_RISCO 2` | 12 h | 4 h | por mandato | idem | ✔ |
| CIPA — titulares e suplentes | `GRAU_RISCO 3` | 16 h | 8 h | por mandato | idem | ✔ |
| CIPA — titulares e suplentes | `GRAU_RISCO 4` | 20 h | 8 h | por mandato | idem | ✔ |
| Representante nomeado da CIPA | `PAPEL nomeado` | 8 h | 0 h | por mandato | NR-05 1.7 | ✔ |
| Membro do SESMT que integra a CIPA | — | dispensado | — | — | NR-05 Anexo I | ✔ |

Aproveitamento de treinamento com menos de **2 anos** na mesma organização: NR-05 5.7.3.
Construção civil tem Anexo I próprio (CIPA tipo 2) — tratar como regra separada quando o cliente for do setor.

### NR-06 — EPI

| Treinamento | Critério | CH mín. | Presencial | Validade | Base legal | |
|---|---|---|---|---|---|---|
| Uso, guarda, conservação e inspeção de EPI | `TODOS` | definida pelo empregador | recomendada prática | eventual (troca de EPI/modelo) | NR-06 6.6.1 + NR-01 1.7 | ~ |

### NR-10 — Instalações e serviços em eletricidade

| Treinamento | Critério | CH mín. | Presencial | Validade | Base legal | |
|---|---|---|---|---|---|---|
| Básico em segurança em eletricidade | `FUNCAO eletricista / trabalha com eletricidade` | 40 h | — | reciclagem bienal 16 h | NR-10 (redação vigente) | ⚠ |
| Complementar — SEP | `FUNCAO SEP` | 40 h | — | reciclagem bienal | idem | ⚠ |
| Complementar — média e alta tensão (SEC) | `FUNCAO SEC` | 16 h | — | reciclagem bienal | idem | ⚠ |
| Área classificada | `ATIVIDADE área classificada` | 16 h | — | reciclagem bienal | idem | ⚠ |
| Específico e pontual | `ATIVIDADE pontual` | 8 h | — | por evento | idem | ⚠ |
| Compartilhamento de SEP | `ATIVIDADE SEP compartilhada` | 40 h | — | reciclagem bienal | idem | ⚠ |
| Reciclagem periódica | `FUNCAO eletricista` | 16 h | — | 24 meses | idem | ⚠ |

> NR-10 passou por revisão recente. **Conferir a portaria vigente antes de emitir certificado** — este bloco é o de maior risco de defasagem da matriz.

### NR-11 — Transporte, movimentação, armazenagem e manuseio de materiais

| Treinamento | Critério | CH mín. | Presencial | Validade | Base legal | |
|---|---|---|---|---|---|---|
| Operador de equipamento de movimentação de cargas (empilhadeira, ponte rolante, pórtico, guincho) | `EQUIPAMENTO` | 16 h (8 teóricas + 8 práticas) | 8 h | a critério (mercado: 24 meses) | NR-11 11.1.5 | ~ |
| Operador de equipamento de transporte com força motriz própria | `EQUIPAMENTO` | definida pelo empregador | prática obrigatória | a critério | NR-11 11.1.5 | ~ |

NR-11 exige apenas **"treinamento específico dado pela empresa"**. Não há carga horária legal: 16 h é prática de mercado, não exigência normativa. O sistema deve gravar a CH **aplicada**, não fingir que é mínima legal.

### NR-12 — Máquinas e equipamentos

| Treinamento | Critério | CH mín. | Presencial | Validade | Base legal | |
|---|---|---|---|---|---|---|
| Capacitação para operação de máquina/equipamento | `EQUIPAMENTO` | conforme Anexo II (referência 8 h) | ≥ 50% prática | a critério / 24 meses | NR-12 Anexo II | ✔/⚠ |
| Capacitação em dispositivos de segurança e bloqueio | `FUNCAO manutenção` | conforme Anexo II | prática | a critério | NR-12 Anexo II | ✔ |

### NR-13 — Caldeiras, vasos de pressão, tubulações e tanques

| Treinamento | Critério | CH mín. | Presencial | Validade | Base legal | |
|---|---|---|---|---|---|---|
| Treinamento de segurança na operação de caldeiras | `EQUIPAMENTO caldeira` | 40 h + prática supervisionada | prática obrigatória | atualização por evento | NR-13 Anexo I, 1.3/1.5 | ✔ |
| Prática supervisionada — caldeira categoria A | `CLASSE A` | 80 h | 100% | — | NR-13 Anexo I, 1.5 | ✔ |
| Prática supervisionada — caldeira categoria B | `CLASSE B` | 60 h | 100% | — | idem | ✔ |
| Prática supervisionada — caldeira categoria C | `CLASSE C` | 40 h | 100% | — | idem | ✔ |
| Operação de unidade de processo | `FUNCAO operador de processo` | 40 h | prática | atualização por evento | NR-13 Anexo I, 2.10 | ✔ |

A "atualização" da NR-13 é **por evento** (mudança de equipamento/procedimento, acidente, avaliação deficiente), não por calendário.

### NR-18 — Construção civil (Anexo I, Quadro 1)

| Treinamento | Critério | CH mín. | Presencial | Validade | Base legal | |
|---|---|---|---|---|---|---|
| Básico em segurança do trabalho | `TODOS construção` | 4 h | obrigatório no inicial | periódico 4 h / 24 meses | NR-18 Anexo I, Q1 | ✔ |
| Operador de grua | `EQUIPAMENTO grua` | 80 h (≥ 40 h prática) | prática | a critério | idem | ✔ |
| Operador de guindaste | `EQUIPAMENTO guindaste` | 120 h (≥ 80 h prática) | prática | a critério | idem | ✔ |
| Operador de equipamentos de guindar | `EQUIPAMENTO guindar` | a critério (≥ 50% prática) | 50% | a critério / 24 meses | idem | ✔ |
| Sinaleiro / amarrador de cargas | `PAPEL sinaleiro` | 16 h | prática | a critério / 24 meses | idem | ✔ |
| Operador de elevador | `EQUIPAMENTO elevador` | 16 h | prática | 4 h / anual | idem | ✔ |
| Instalação, montagem, desmontagem e manutenção de elevadores | `FUNCAO montador` | a critério | prática | a critério / anual | idem | ✔ |
| Operador de PEMT | `EQUIPAMENTO PEMT` | 4 h | prática | 4 h / 24 meses | idem | ✔ |
| Encarregado de ar comprimido | `PAPEL encarregado` | 16 h | — | a critério | idem | ✔ |
| Resgate e remoção em tubulão | `PAPEL resgate` | 8 h | prática | a critério | idem | ✔ |
| Impermeabilização | `ATIVIDADE impermeabilização` | 4 h | — | a critério | idem | ✔ |
| Cadeira suspensa | `EQUIPAMENTO cadeira suspensa` | 16 h (≥ 8 h prática) | prática | 8 h / anual | idem | ✔ |
| Escavação manual de tubulão | `ATIVIDADE tubulão` | 24 h (≥ 8 h prática) | prática | 8 h / anual | idem | ✔ |
| Demais atividades/funções | `FUNCAO outras` | a critério | — | a critério | idem | ✔ |

### NR-19 — Explosivos

| Treinamento | Critério | CH mín. | Presencial | Validade | Base legal | |
|---|---|---|---|---|---|---|
| Capacitação para manuseio, armazenagem e transporte de explosivos | `FUNCAO blaster / aplicador` | definida pelo empregador (mercado: 16 h básico, 40 h avançado) | prática | anual / por evento | NR-19 item 14 | ~ |

NR-19 exige treinamento na admissão, na troca de função, em mudança de processo e **no mínimo anualmente** — mas não fixa carga horária.

### NR-20 — Inflamáveis e combustíveis

| Treinamento | Critério | CH mín. | Presencial | Validade | Base legal | |
|---|---|---|---|---|---|---|
| Básico | `CLASSE CI` | 4 h | — | 36 meses | NR-20, Anexo I | ✔ |
| Básico | `CLASSE CII` | 6 h | — | 36 meses | idem | ✔ |
| Básico | `CLASSE CIII` | 8 h | — | 36 meses | idem | ✔ |
| Intermediário | `CLASSE CI` | 12 h | — | 36 meses | idem | ✔ |
| Intermediário | `CLASSE CII` | 14 h | — | 24 meses | idem | ✔ |
| Intermediário | `CLASSE CIII` | 16 h | — | 24 meses | idem | ✔ |
| Avançado I | `FUNCAO operação` | 24 h | — | 24 meses | idem | ✔ |
| Avançado II | `FUNCAO operação` | 32 h | — | 12 meses | idem | ✔ |
| Iniciação | `TODOS expostos` | 3 h | — | única | idem | ✔ |
| Atualização | `FUNCAO operação` | 4 h | — | conforme nível | idem | ✔ |

### NR-22 — Mineração

| Treinamento | Critério | CH mín. | Presencial | Validade | Base legal | |
|---|---|---|---|---|---|---|
| Introdutório geral — subsolo | `ATIVIDADE subsolo` | 24 h | — | periódico 8 h / 24 meses | NR-22 Anexo II, 2.2 | ✔ |
| Introdutório geral — céu aberto | `ATIVIDADE céu aberto` | 16 h | — | idem | idem | ✔ |
| Introdutório geral — rochas ornamentais, areia, argila, britagem | `ATIVIDADE exceção` | 12 h | — | idem | idem | ✔ |
| Específico na função — superfície | `FUNCAO superfície` | 40 h | prática | periódico 8 h / 24 meses | NR-22 Anexo II | ✔ |
| Específico na função — subsolo | `FUNCAO subsolo` | 48 h | prática | idem | idem | ✔ |
| Operador de máquina (estacionária/autopropelida) | `EQUIPAMENTO` | conforme NR-12 Anexo II, ≥ 50% prática | 50% | idem | NR-22 Anexo II, 2.3.3.2 | ✔ |

### NR-23 — Proteção contra incêndios

| Treinamento | Critério | CH mín. | Presencial | Validade | Base legal | |
|---|---|---|---|---|---|---|
| Brigada de incêndio | `ESTADO` + `PAPEL brigadista` | definida pela IT do Corpo de Bombeiros do estado | prática obrigatória | conforme IT (12–24 meses) | NR-23 + IT estadual | ⚠ |

A NR-23 **não fixa carga horária**: remete à legislação estadual. O sistema deve ter a matriz por UF (ex.: MG possui níveis básico/intermediário/avançado na IT-12).

### NR-29 — Trabalho portuário

| Treinamento | Critério | CH mín. | Presencial | Validade | Base legal | |
|---|---|---|---|---|---|---|
| Segurança e saúde no trabalho portuário | `FUNCAO trabalhador portuário` | 8 h (prática de mercado / PREPOM) | — | a critério | NR-29 | ~ |
| Operação e armazenagem com cargas perigosas | `ATIVIDADE carga perigosa` | 20 h | — | a critério | NR-29 29.27.6 | ✔ |
| CIPA portuária (CPATP) | `PAPEL membro CPATP` | 20 h (8 h presenciais se EaD) | 8 h | por mandato | NR-29 29.7.16 | ✔ |
| Sinaleiro | `PAPEL sinaleiro` | conforme NR-29 | prática | a critério | NR-29 29.15.9 | ~ |

### NR-31 — Agricultura, pecuária, silvicultura, exploração florestal e aquicultura

| Treinamento | Critério | CH mín. | Presencial | Validade | Base legal | |
|---|---|---|---|---|---|---|
| Agrotóxicos, adjuvantes e produtos afins | `FUNCAO aplicador` | 20 h | prática obrigatória | a critério (mercado 24 meses) | NR-31 31.8.8.1 | ✔ |
| Máquinas autopropelidas e implementos (trator, colhedora, pulverizador) | `EQUIPAMENTO` | 24 h (≥ 12 h prática) | 12 h | reciclagem 4 h por evento | NR-31 31.12.77/78 | ✔ |
| Motosserra, motopoda e similares | `EQUIPAMENTO motosserra` | 8 h | prática | a critério | NR-31 31.12.39 | ✔ |
| Roçadeira costal motorizada / derriçadeira | `EQUIPAMENTO roçadeira` | 4 h | semipresencial ou presencial | a critério | NR-31 31.12.46.1 | ✔ |
| CIPATR — membros | `PAPEL membro CIPATR` | 20 h (máx. 8 h/dia) | — | por mandato | NR-31 31.7.20.3 | ✔ |
| Trabalho em altura (rural) | `ATIVIDADE altura` | 8 h | prática | 24 meses | NR-31 + NR-35 | ⚠ |
| Espaço confinado (rural) | `ATIVIDADE confinado` | conforme NR-33 | prática | conforme NR-33 | NR-31 + NR-33 | ⚠ |

### NR-32 — Serviços de saúde

| Treinamento | Critério | CH mín. | Presencial | Validade | Base legal | |
|---|---|---|---|---|---|---|
| Capacitação em riscos biológicos, químicos e radiológicos | `FUNCAO saúde` | definida pelo empregador / PGR | prática recomendada | eventual + periódica | NR-32 + NR-01 1.7 | ~ |
| Proteção radiológica (radiações ionizantes) | `ATIVIDADE radiação` | definida pelo empregador | prática | anual (recomendado) | NR-32 | ~ |

### NR-33 — Espaços confinados

| Treinamento | Critério | CH mín. | Presencial | Validade | Base legal | |
|---|---|---|---|---|---|---|
| Trabalhador autorizado e vigia | `PAPEL autorizado/vigia` | 16 h | ≥ 50% prática | 8 h / anual | NR-33 | ✔ |
| Supervisor de entrada | `PAPEL supervisor` | 40 h | ≥ 50% prática | 8 h / anual | NR-33 | ✔ |
| Equipe de emergência e salvamento | `PAPEL resgate` | 24 h (não supervisor) / 32 h (supervisor) | ≥ 50% prática | 8 h / 24 meses | NR-33 | ✔ |

Prática mínima de 50% da carga total. A reciclagem do supervisor é anual; a da equipe de emergência é bienal.

### NR-34 — Construção, reparação e desmonte naval

| Treinamento | Critério | CH mín. | Presencial | Validade | Base legal | |
|---|---|---|---|---|---|---|
| Admissional / integração | `TODOS naval` | 6 h | presencial | periódico 4 h / anual | NR-34 34.3.4 | ✔ |
| Movimentação de cargas | `FUNCAO movimentação` | 20 h | prática | a critério | NR-34 Anexo I, item 2 | ✔ |
| Complementar para operador de equipamento de guindar | `EQUIPAMENTO guindar` | 20 h | prática | a critério | NR-34 Anexo I, item 3 | ✔ |
| Trabalho a quente — módulo geral | `ATIVIDADE trabalho a quente` | 4 h | prática | a critério | NR-34 Anexo I, item 4.1 | ✔ |
| Trabalho a quente — módulo específico (por modalidade) | `ATIVIDADE modalidade` | 4 h por modalidade | prática | a critério | NR-34 Anexo I, item 4.2 | ✔ |
| Observador de trabalhos a quente | `PAPEL observador` | 8 h | prática | a critério | NR-34 Anexo I, item 1 | ✔ |
| Teste de estanqueidade | `FUNCAO estanqueidade` | 8 h | prática | 8 h / 12 meses | NR-34 34.14.2.2 | ✔ |

### NR-35 — Trabalho em altura

| Treinamento | Critério | CH mín. | Presencial | Validade | Base legal | |
|---|---|---|---|---|---|---|
| Trabalho em altura — inicial | `ATIVIDADE altura` | 8 h (teórico + prático) | prática obrigatória | 24 meses | NR-35 | ✔ |
| Trabalho em altura — periódico | `ATIVIDADE altura` | 8 h | prática | 24 meses | NR-35 | ✔ |
| Supervisão de trabalho em altura | `PAPEL supervisor` | adicional ao inicial | prática | 24 meses | NR-35 | ~ |

### NR-36, NR-37, NR-38 — setoriais (frigorífico, plataformas, limpeza urbana)

| Treinamento | Critério | CH mín. | Presencial | Validade | Base legal | |
|---|---|---|---|---|---|---|
| Capacitação setorial conforme PGR | `FUNCAO` | definida pelo empregador | conforme atividade | eventual + periódica | NR-36/37/38 + NR-01 1.7 | ~ |

Essas NRs **não fixam carga horária própria**: aplicam NR-01 (capacitação) e as NRs especiais (NR-33, NR-35, NR-10, NR-20) quando a atividade disparar.

---

## 3. Regras transversais (valem para todas as linhas)

1. **Aproveitamento**: treinamento anterior pode ser aproveitado quando conteúdo e CH estiverem contidos no novo, dentro do prazo de validade e validado pelo responsável técnico (NR-01 1.7 / NR-31 31.4.10).
2. **Certificado**: nome do trabalhador, CPF, conteúdo programático, CH, data, local, nome e qualificação do instrutor, assinatura do responsável técnico; assinatura do trabalhador na lista de presença.
3. **Registro**: conteúdo, CH e frequência são obrigatórios ao fim de treinamento inicial, periódico e eventual (NR-01 / NR-19 14.1.4).
4. **Presencial mínimo**: quando a NR fixa horas presenciais, o EaD só pode cobrir o restante. O sistema deve armazenar `horas_presenciais_min` separado de `carga_horaria_min`.
5. **Eventual**: mudança de procedimento/equipamento, acidente grave, retorno de afastamento > 90 dias, avaliação insuficiente → novo treinamento independente de vencimento.
6. **Validade**: `validade_meses` vazio = sem periodicidade fixada; nesse caso o alerta é "revisar conforme PGR", não "vencido".

---

## 4. SQL — schema e seed

```sql
-- Normas
CREATE TABLE IF NOT EXISTS normas (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo        TEXT NOT NULL UNIQUE,
    titulo        TEXT NOT NULL,
    setorial      INTEGER NOT NULL DEFAULT 0,
    observacao    TEXT
);

-- Matriz de treinamento
CREATE TABLE IF NOT EXISTS matriz_treinamento (
    id                    INTEGER PRIMARY KEY AUTOINCREMENT,
    norma_id              INTEGER NOT NULL REFERENCES normas(id),
    tipo                  TEXT NOT NULL,
    criterio_tipo         TEXT NOT NULL CHECK (criterio_tipo IN
                              ('TODOS','GRAU_RISCO','CLASSE','PAPEL','FUNCAO','EQUIPAMENTO','ATIVIDADE','ESTADO')),
    criterio_valor        TEXT,
    carga_horaria_min     INTEGER NOT NULL DEFAULT 0,
    horas_presenciais_min INTEGER NOT NULL DEFAULT 0,
    validade_meses        INTEGER,
    modalidade            TEXT CHECK (modalidade IN ('PRESENCIAL','EAD','SEMIPRESENCIAL','A_DEFINIR')),
    carga_fixada_norma    INTEGER NOT NULL DEFAULT 1,
    conteudo_minimo       TEXT,
    base_legal            TEXT,
    ativo                 INTEGER NOT NULL DEFAULT 1,
    UNIQUE (norma_id, tipo, criterio_tipo, criterio_valor)
);

CREATE INDEX IF NOT EXISTS idx_matriz_norma ON matriz_treinamento(norma_id);
CREATE INDEX IF NOT EXISTS idx_matriz_criterio ON matriz_treinamento(criterio_tipo, criterio_valor);
```

> `carga_fixada_norma = 0` marca as linhas `~`: o número é referência de mercado, não piso legal. A tela de turma deve exibir essa diferença.

Seed (normas):

```sql
INSERT OR IGNORE INTO normas (codigo, titulo, setorial) VALUES
 ('NR-01','Disposições Gerais e Gerenciamento de Riscos Ocupacionais',0),
 ('NR-05','Comissão Interna de Prevenção de Acidentes e de Assédio',0),
 ('NR-06','Equipamentos de Proteção Individual',0),
 ('NR-10','Instalações e Serviços em Eletricidade',1),
 ('NR-11','Transporte, Movimentação, Armazenagem e Manuseio de Materiais',1),
 ('NR-12','Segurança no Trabalho em Máquinas e Equipamentos',1),
 ('NR-13','Caldeiras, Vasos de Pressão, Tubulações e Tanques Metálicos',1),
 ('NR-18','Segurança e Saúde no Trabalho na Indústria da Construção',1),
 ('NR-19','Explosivos',1),
 ('NR-20','Inflamáveis e Combustíveis',1),
 ('NR-22','Segurança e Saúde Ocupacional na Mineração',1),
 ('NR-23','Proteção Contra Incêndios',0),
 ('NR-29','Segurança e Saúde no Trabalho Portuário',1),
 ('NR-31','Segurança e Saúde no Trabalho na Agricultura, Pecuária e Silvicultura',1),
 ('NR-32','Segurança e Saúde no Trabalho em Serviços de Saúde',1),
 ('NR-33','Segurança e Saúde no Trabalho em Espaços Confinados',0),
 ('NR-34','Construção, Reparação e Desmonte Naval',1),
 ('NR-35','Trabalho em Altura',0),
 ('NR-36','Segurança e Saúde no Trabalho em Empresas de Abate e Processamento de Carnes',1),
 ('NR-37','Segurança e Saúde em Plataformas de Petróleo',1),
 ('NR-38','Segurança e Saúde no Trabalho nas Atividades de Limpeza Urbana',1);
```

Seed (matriz — trechos; completar as demais linhas do mesmo jeito):

```sql
-- NR-05 CIPA por grau de risco
INSERT OR IGNORE INTO matriz_treinamento
 (norma_id, tipo, criterio_tipo, criterio_valor, carga_horaria_min, horas_presenciais_min, validade_meses, modalidade, carga_fixada_norma, base_legal)
SELECT id,'CIPA - titulares e suplentes','GRAU_RISCO','1', 8, 0,12,'EAD',1,'NR-05 Anexo I item 1.5' FROM normas WHERE codigo='NR-05';
INSERT OR IGNORE INTO matriz_treinamento
 (norma_id, tipo, criterio_tipo, criterio_valor, carga_horaria_min, horas_presenciais_min, validade_meses, modalidade, carga_fixada_norma, base_legal)
SELECT id,'CIPA - titulares e suplentes','GRAU_RISCO','2',12, 4,12,'SEMIPRESENCIAL',1,'NR-05 Anexo I item 1.5' FROM normas WHERE codigo='NR-05';
INSERT OR IGNORE INTO matriz_treinamento
 (norma_id, tipo, criterio_tipo, criterio_valor, carga_horaria_min, horas_presenciais_min, validade_meses, modalidade, carga_fixada_norma, base_legal)
SELECT id,'CIPA - titulares e suplentes','GRAU_RISCO','3',16, 8,12,'SEMIPRESENCIAL',1,'NR-05 Anexo I item 1.5' FROM normas WHERE codigo='NR-05';
INSERT OR IGNORE INTO matriz_treinamento
 (norma_id, tipo, criterio_tipo, criterio_valor, carga_horaria_min, horas_presenciais_min, validade_meses, modalidade, carga_fixada_norma, base_legal)
SELECT id,'CIPA - titulares e suplentes','GRAU_RISCO','4',20, 8,12,'SEMIPRESENCIAL',1,'NR-05 Anexo I item 1.5' FROM normas WHERE codigo='NR-05';

-- NR-33 espaços confinados
INSERT OR IGNORE INTO matriz_treinamento
 (norma_id, tipo, criterio_tipo, criterio_valor, carga_horaria_min, horas_presenciais_min, validade_meses, modalidade, carga_fixada_norma, base_legal)
SELECT id,'Trabalhador autorizado e vigia','PAPEL','AUTORIZADO/VIGIA',16,8,12,'SEMIPRESENCIAL',1,'NR-33' FROM normas WHERE codigo='NR-33';
INSERT OR IGNORE INTO matriz_treinamento
 (norma_id, tipo, criterio_tipo, criterio_valor, carga_horaria_min, horas_presenciais_min, validade_meses, modalidade, carga_fixada_norma, base_legal)
SELECT id,'Supervisor de entrada','PAPEL','SUPERVISOR',40,20,12,'SEMIPRESENCIAL',1,'NR-33' FROM normas WHERE codigo='NR-33';
INSERT OR IGNORE INTO matriz_treinamento
 (norma_id, tipo, criterio_tipo, criterio_valor, carga_horaria_min, horas_presenciais_min, validade_meses, modalidade, carga_fixada_norma, base_legal)
SELECT id,'Equipe de emergência e salvamento','PAPEL','RESGATE',24,12,24,'SEMIPRESENCIAL',1,'NR-33' FROM normas WHERE codigo='NR-33';

-- NR-35 trabalho em altura
INSERT OR IGNORE INTO matriz_treinamento
 (norma_id, tipo, criterio_tipo, criterio_valor, carga_horaria_min, horas_presenciais_min, validade_meses, modalidade, carga_fixada_norma, base_legal)
SELECT id,'Trabalho em altura','ATIVIDADE','ALTURA',8,8,24,'PRESENCIAL',1,'NR-35' FROM normas WHERE codigo='NR-35';

-- NR-11 operador de movimentação de cargas (CH de mercado)
INSERT OR IGNORE INTO matriz_treinamento
 (norma_id, tipo, criterio_tipo, criterio_valor, carga_horaria_min, horas_presenciais_min, validade_meses, modalidade, carga_fixada_norma, base_legal)
SELECT id,'Operador de movimentação de cargas','EQUIPAMENTO','EMPILHADEIRA',16,8,24,'SEMIPRESENCIAL',0,'NR-11 11.1.5 (CH nao fixada)' FROM normas WHERE codigo='NR-11';
```

---

## 5. Pendências desta matriz

- [ ] Confirmar a redação vigente da **NR-10** e ajustar as 7 linhas (bloco `⚠`).
- [ ] Confirmar **NR-23** por UF — começar por MG (IT-12) e SP.
- [ ] Preencher `conteudo_minimo` das linhas NR-05, NR-18 e NR-33 (as mais usadas em licitação).
- [ ] Definir se `validade_meses` de CIPA fica 12 (mandato) ou nulo com alerta por mandato.
- [ ] Criar a tabela `cnae` + `cnae_grau_risco` (NR-04 Anexo I) para derivar o GR do cliente e assim escolher a linha de NR-05 automaticamente.
