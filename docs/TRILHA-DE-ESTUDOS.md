# Trilha Vippela — educação digital em família

**Documento de design instrucional e de produto · versão 1.1 · outubro/2026**
Para FETEPS e Solve for Tomorrow · alinhado aos pilares **Proteção, Saúde e Bem-estar**

---

## 0. Suposições e decisões antes de começar

### 0.1 Stack: preciso apontar uma divergência

O briefing diz "React Native/Expo no app", mas o repositório `App-Vippela` é **Android nativo em Kotlin + Jetpack Compose** (`minSdk 26`, Compose BOM 2024.09, Retrofit/Gson, sem Room hoje), com telas de aprendizado já prototipadas em `ui/screens/LearningScreens.kt`.

**Assumo que a trilha será construída no app nativo existente.** A especificação de telas, conteúdo e modelo de dados vale para qualquer stack; só as notas de implementação mudam. Recomendo manter o nativo: já existem tema, navegação, autenticação, vínculo e o esqueleto da tela de trilhas. Trocar para React Native agora custaria semanas e não agrega ao edital.

*Prós de manter nativo:* reaproveita 100% do que existe; TTS, acessibilidade e notificações locais do Android são diretas; sem ponte nova. *Contras:* menos reuso futuro em iOS. *Mitigação:* o backend e o conteúdo em JSON/Postgres são agnósticos — uma versão iOS depois relê o mesmo contrato.

### 0.2 Outras suposições

| # | Suposição | Impacto se estiver errada |
|---|---|---|
| 1 | Um **grupo familiar** tem ao menos um responsável com papel de guardião; os demais perfis pertencem a esse grupo | Muda o modelo de permissão (`familia`/`perfil`) |
| 2 | O app já vinculado por dispositivo dará lugar a **perfis** (`perfil_id`) como donos do progresso | Alto: progresso precisa de dono estável |
| 3 | Crianças até 12 anos usam o app em **modo criança** dentro da conta do responsável, com consentimento dele (LGPD art. 14) | Muda cadastro e consentimento |
| 4 | A equipe é pequena (2 devs, 1 designer, 1 conteúdo, apoio de QA) e o prazo do MVP é de ~8 semanas | Muda o escopo do MVP |
| 5 | O conteúdo é autoral e revisado por educador; nada é copiado da IBM, que serve só como modelo de estrutura | Risco jurídico/reputacional |
| 6 | Vídeo é opcional no MVP (custa produção e dados móveis); texto ilustrado + áudio TTS cobre a necessidade | Muda cronograma |
| 7 | O backend continua em Kotlin/Spring Boot com Postgres (Supabase), e o SQLite local será Room no Android | Muda a camada offline |

### 0.3 Cinco princípios que guiam esta trilha

1. **Curto e aplicável.** Nenhuma lição passa de 5 minutos; toda lição termina com uma ação concreta.
2. **Sem medo, sem culpa.** O tom explica o risco e devolve controle — nunca envergonha quem caiu em um golpe.
3. **Família conversa, não vigia.** O progresso compartilhado serve para puxar assunto, não para ranquear filhos.
4. **Acolhimento antes de conteúdo.** Peônia e lavanda aparecem no ritmo, no espaço em branco e na linguagem — não em enfeite excessivo.
5. **Bem-estar vale também para o app.** Nada de sequência punitiva, notificação fora de hora ou rolagem infinita.

### 0.4 Ajustes concretos que o protótipo atual pede

- `LearningScreens.kt` mostrava "Concluído 60%" fixo e "15 min" por lição: **resolvido** — o progresso e a duração agora vêm do conteúdo e do perfil.
- `DemoState.lessonKey()` usava papel + membro da demonstração; **resolvido** — o progresso tem escopo por conta/perfil (`LearningState.bind`) e o modelo novo usa `perfil`.
- A tela de lição revelava o feedback antes de confirmar e não tinha 3 perguntas: **resolvido** — o quiz só revela depois de "Conferir resposta" e cada lição tem 3 perguntas.
- O laranja `#EF883C` sobre branco tem contraste ~2,6:1 — **não passa** para texto pequeno (WCAG AA pede 4,5:1). Usar laranja só em ícones grandes/decoração e adotar um âmbar escuro (`#9C5A00`) para texto.
- O fundo `#D6D6E5` com texto `#160064` está adequado; manter.

### 0.5 O que já está implementado neste repositório (MVP)

O slice vertical do MVP está no app nativo e funciona **offline**, sem dependência nova no Gradle.

| Entrega | Onde está | Estado |
|---|---|---|
| 2 trilhas, 8 módulos, 24 lições, 72 perguntas | `app/src/main/assets/learning/*.json` | Pronto |
| Modelo de conteúdo + progresso | `data/learning/LearningModels.kt` | Pronto |
| Progresso local por conta/perfil | `data/learning/LearningState.kt` | Pronto |
| Catálogo de 12 selos (7 deles "Em breve") | `data/learning/BadgeCatalog.kt` | Pronto |
| Home, detalhe da trilha, lição, quiz e resultado | `ui/screens/LearningScreens.kt` | Pronto |
| Selos, certificado + PDF e progresso da família | `ui/screens/LearningProgressScreens.kt` | Pronto |
| Rotas, título e escopo de sessão | `ui/navigation/VippelaApp.kt` | Pronto |
| Migração Postgres/Supabase | `../backend-Vippela/db/vippela_learning.sql` | Pronto para rodar |

**Regras que o app já aplica:**

- Aprovação da lição: 2 de 3 perguntas. Tentativas ilimitadas, sem punição.
- Checagem de módulo: 1 pergunta por lição (3 no total), 2 acertos para aprovar; o módulo seguinte abre depois disso.
- Módulo bloqueado deixa a primeira lição visível como prévia.
- Selos: primeira lição, 2 trilhas do MVP, 2 lições em família, 4 semanas diferentes. Os outros 7 aparecem como "Em breve".
- Certificado: só depois de todos os módulos aprovados; código `VP-XXXX-XXXX` e PDF compartilhável.
- Privacidade: respostas, erros e tentativas ficam no progresso local do perfil. O responsável vê apenas conclusão, selos e agregados.

**Divergências conscientes em relação ao plano original:**

1. **Room/SQLite** foi planejado para a Fase 2. No MVP o progresso é JSON em `SharedPreferences`, porque o projeto não tem Room e a prioridade era não adicionar dependência. A interface de `LearningState` já isola a persistência para a troca.
2. **Selos e certificado** são emitidos localmente no MVP; o schema do backend já está pronto para o servidor ser a fonte da verdade na integração.
3. **Código do certificado** ainda não tem página pública de verificação (Fase 3).
4. **Variantes criança/idoso** têm o campo `licao_variante` no Postgres, mas o MVP usa o mesmo texto-base para todos os perfis.

**Como rodar e verificar:**

```powershell
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat :app:compileDebugKotlin "-Pvippela.google=false"
.\gradlew.bat :app:testDebugUnitTest --tests "br.com.vippela.NavigationTest" "-Pvippela.google=false"
```

Sem `google-services.json`, use `"-Pvippela.google=false"`; com o arquivo, o build padrão ativa o login Google.

**Roteiro de demo (5 minutos):** entrar como familiar → Trilhas → Contas e senhas → primeira lição → ouvir com TTS → quiz de 3 perguntas → resultado com selo "Primeira Pétala" → Meus selos → Família → instalar o modo avião e repetir uma lição para mostrar o offline.


---

## 1. Estrutura da trilha

### 1.1 Visão geral

Seis trilhas, todas com **4 módulos × 3–5 lições** (lições de 3 a 5 min). A carga total é de ~5h30, mas ninguém precisa fazer tudo: cada perfil recebe um **percurso** recomendado.

| # | Trilha | Pilar | Nível | Perfil indicado | Duração | Pré-requisito |
|---|---|---|---|---|---|---|
| T1 | **Contas e senhas: a chave da sua casa digital** | Proteção | Iniciante | Responsável, adolescente, idoso | 55 min | — |
| T2 | **Golpes e fraudes: reconhecer para não cair** | Proteção | Iniciante → Intermediário | Todos | 60 min | T1 recomendado |
| T3 | **Privacidade e dados pessoais** | Proteção | Intermediário | Adolescente, responsável | 50 min | T1 |
| T4 | **Telas com equilíbrio** | Saúde | Iniciante | Todos, especialmente responsável e criança | 45 min | — |
| T5 | **Bem-estar digital e saúde mental** | Bem-estar | Intermediário | Adolescente, responsável | 55 min | T4 recomendado |
| T6 | **Crianças e adolescentes online** | Proteção + Bem-estar | Intermediário | Responsável (versão espelho para adolescente) | 60 min | T1, T3 |

**Objetivo geral da trilha:** ao final do percurso, a família sabe proteger contas, reconhecer golpes, cuidar da privacidade, usar telas com equilíbrio e pedir ajuda — e já praticou isso junto pelo menos uma vez.

### 1.2 Perfis e percursos

Perfil não é uma trilha separada: é uma **ordem recomendada + uma camada de linguagem**. Isso evita conteúdo duplicado e mantém o progresso unificado.

| Perfil | Faixa | Percurso recomendado | Camada de linguagem |
|---|---|---|---|
| **Responsável** | 18+ | T1 → T2 → T6 → T4 → T3 → T5 | Direta, com foco em mediação familiar |
| **Adolescente** | 13–17 | T4 → T5 → T2 → T3 → T1 → T6-teen | "Você", autonomia, exemplos de redes e jogos |
| **Criança** | 7–12 | Percurso curto "Meu mundo seguro": T1-criança (2 lições) → T2-criança (3) → T4-criança (3) | Frases curtas, personagens, sem exames |
| **Idoso** | 60+ | T2 → T1 → T4 → T3 | Modo acessível por padrão: fonte maior, narração ligada, passo a passo |

**Prós e contras das duas formas de organizar**

| Abordagem | Prós | Contras | Veredito |
|---|---|---|---|
| **Percurso + variantes de linguagem** (recomendado) | Progresso único; menos conteúdo para escrever e revisar; badge igual para todos | Exige disciplina editorial nas variantes | ✅ Recomendado |
| Trilhas totalmente separadas por perfil | Linguagem sob medida | 4× o custo de produção; fragmenta a família; criança fica ilhada | ❌ |

### 1.3 As seis trilhas em detalhe

Legenda de tipos: **TXT** texto ilustrado · **VÍD** vídeo curto (≤90 s) · **SIM** simulação de golpe · **CHK** checklist/ação · **QUIZ** quiz curto · **FAM** atividade em família · **AUD** áudio.

#### T1 · Contas e senhas: a chave da sua casa digital
**Objetivo:** criar senhas fortes, ativar verificação em duas etapas e recuperar uma conta invadida sem pânico.
**Perfil:** responsável, adolescente, idoso · **Nível:** iniciante · **Duração:** 55 min · **Pré-requisito:** nenhum

| Módulo | Lições |
|---|---|
| **M1. Por que sua senha vale ouro** (12 min) | Por que contas são roubadas (TXT) · Senha forte sem complicação (TXT + CHK) · A mesma senha em todo lugar (SIM) · Meu primeiro cofre de senhas (CHK) |
| **M2. Verificação em duas etapas** (14 min) | O que é 2FA (VÍD 90 s) · SMS ou aplicativo? (TXT) · Ativando no celular da família (CHK passo a passo) · Quando o código chega sem você pedir (SIM) |
| **M3. Conta invadida: e agora?** (14 min) | Sinais de invasão (TXT) · Recuperar o acesso (CHK) · Avisar a família (TXT) · Limpar o que ficou (CHK) |
| **M4. Senhas na família** (15 min) | Quem sabe o quê (TXT) · Cofre compartilhado: quando faz sentido (TXT) · Desafio: revisar 3 contas juntos (FAM) |

#### T2 · Golpes e fraudes: reconhecer para não cair
**Objetivo:** identificar os sinais de um golpe (pressa, medo, prêmio, autoridade) e saber o que fazer nos primeiros 10 minutos depois de um clique errado.
**Perfil:** todos · **Nível:** iniciante → intermediário · **Duração:** 60 min · **Pré-requisito:** T1 recomendado

| Módulo | Lições |
|---|---|
| **M1. O roteiro do golpe** (16 min) | Pressa, medo e prêmio (TXT) · Quem está do outro lado? (VÍD 90 s) · **Falso suporte técnico (SIM)** · Verificar antes de agir (CHK) |
| **M2. Golpes que chegam por mensagem** (16 min) | Phishing por SMS e e-mail (TXT) · Link encurtado (SIM) · Falso parente no WhatsApp (SIM) · PIX errado e comprovante falso (TXT) |
| **M3. Golpes de confiança** (16 min) | Falso emprego (TXT) · Golpe do amor (TXT) · Falsa central do banco (SIM) · Investimento milagroso (TXT) |
| **M4. Depois do clique** (12 min) | Os primeiros 10 minutos (CHK) · Registrar e pedir ajuda (CHK) · Conversa em família (FAM) |

#### T3 · Privacidade e dados pessoais
**Objetivo:** entender o que se entrega ao instalar um app, revisar permissões e publicar com cuidado.
**Perfil:** adolescente, responsável · **Nível:** intermediário · **Duração:** 50 min · **Pré-requisito:** T1

| Módulo | Lições |
|---|---|
| **M1. O que você entrega quando usa um app** (12 min) | Dados que parecem inofensivos (TXT) · Termos em linguagem simples (TXT) · Permissões: câmera, microfone, localização, contatos (CHK) · O que fazer quando o app pede demais (SIM) |
| **M2. A foto fala por você** (13 min) | O que aparece no fundo (TXT) · Localização em fotos (TXT + CHK) · Apagar não é desaparecer (TXT) · Reconhecimento facial e fotos de crianças (TXT) |
| **M3. Redes sociais com menos exposição** (13 min) | Quem vê o que você publica (CHK) · Perfis falsos e cópia de identidade (SIM) · Dados que nunca vão em bio (QUIZ) |
| **M4. Seus direitos** (12 min) | LGPD em linguagem simples (TXT) · Pedir cópia ou exclusão dos meus dados (CHK) · Dados de crianças: o papel do responsável (TXT) · Atividade: limpeza de privacidade em família (FAM) |

#### T4 · Telas com equilíbrio
**Objetivo:** reconhecer como os apps capturam atenção e construir combinados de uso que caibam na rotina da casa.
**Perfil:** todos · **Nível:** iniciante · **Duração:** 45 min · **Pré-requisito:** nenhum

| Módulo | Lições |
|---|---|
| **M1. Como o app prende atenção** (12 min) | Rolagem infinita (TXT) · Notificações que puxam de volta (TXT) · Autoplay e recompensa variável (VÍD 90 s) · Reconhecendo o piloto automático (CHK) |
| **M2. Sono e telas** (11 min) | Luz e melatonina (TXT) · A última hora do dia (CHK) · Celular fora do quarto: como combinar (FAM) |
| **M3. Combinados que funcionam** (12 min) | Regra que todo mundo cumpre (TXT) · Zonas e horários livres de tela (CHK) · Quando o combinado quebra (TXT) |
| **M4. Meu plano de equilíbrio** (10 min) | Minha meta realista (CHK) · Pausa consciente (AUD 60 s) · Revisão em família (FAM) |

#### T5 · Bem-estar digital e saúde mental
**Objetivo:** cuidar da autoestima, reconhecer violência online e saber pedir ajuda — com aviso de conteúdo sensível e caminhos de apoio.
**Perfil:** adolescente, responsável · **Nível:** intermediário · **Duração:** 55 min · **Pré-requisito:** T4 recomendado

| Módulo | Lições |
|---|---|
| **M1. Comparação e autoestima** (13 min) | A vida editada dos outros (TXT) · Métricas não são valor (TXT) · Um feed que faz bem (CHK) · Quando a comparação machuca (TXT) |
| **M2. Quando a internet machuca** (15 min) | O que é cyberbullying (TXT) · Não responder, guardar prova, bloquear (CHK) · Denunciar no app e na escola (CHK) · Se você viu alguém sendo atacado (TXT) |
| **M3. Ansiedade, sono e notificações** (12 min) | Corpo e tela: sinais de alerta (TXT) · Desligar sem perder o grupo (CHK) · Respiração e pausa (AUD 60 s) |
| **M4. Pedir ajuda é força** (15 min) | Como falar com um adulto (TXT) · CVV 188 e outros caminhos (TXT) · Quando é urgência (CHK) · Conversa em família (FAM) |

> **Cuidado editorial:** T5 não faz diagnóstico nem promete tratamento. Toda lição sensível tem aviso inicial, botão "prefiro pular por hoje" e o CVV 188 sempre visível no rodapé do módulo.

#### T6 · Crianças e adolescentes online
**Objetivo:** dar ao responsável repertório para acompanhar a vida digital de crianças e adolescentes com mediação, não vigilância.
**Perfil:** responsável · **Nível:** intermediário · **Duração:** 60 min · **Pré-requisito:** T1, T3

| Módulo | Lições |
|---|---|
| **M1. O mundo em que eles vivem** (15 min) | Jogos, redes e IA: o que existe hoje (TXT) · Idades mínimas e por que existem (TXT) · O que meu filho faz que eu não conheço (CHK) · Perguntas para abrir conversa (FAM) |
| **M2. Riscos reais, sem pânico** (16 min) | Aliciamento: sinais e prevenção (TXT + VÍD) · Conteúdo impróprio e desafios virais (TXT) · Apostas e jogos com dinheiro (TXT) · Sinais de que algo não vai bem (CHK) |
| **M3. Mediação em vez de vigilância** (15 min) | Combinados por idade (TXT) · Controle parental: quando e como usar (CHK) · Confiança se constrói conversando (TXT) · O que fazer se a criança pedir privacidade (TXT) |
| **M4. Se algo acontecer** (14 min) | Acolher sem culpar (TXT) · Preservar provas (CHK) · Denunciar: Disque 100 e SaferNet (CHK) · Plano de cuidado da família (FAM) |

**Versão espelho para adolescentes — "Minha vida online"** (3 módulos curtos, 30 min): meus direitos, o que fazer se algo der errado, como apoiar um amigo. Mesmo conteúdo de fundo, voz de quem vive a situação.

### 1.4 Custo realista de produção

Uma lição completa (texto, revisão pedagógica, arte, quiz com feedback, revisão de acessibilidade) leva de **3 a 5 horas**. As 6 trilhas somam ~92 lições ≈ **350–450 horas** de conteúdo. Por isso o MVP entrega 2 trilhas completas e o restante entra em ondas (seção 7).

---

## 2. Formato das lições

### 2.1 Anatomia de uma lição (5 minutos, no máximo)

| Bloco | Tempo | O que tem |
|---|---|---|
| 1. Abertura | 20 s | Título, duração real, "o que você vai levar daqui" em 1 frase |
| 2. Situação | 60–90 s | Uma cena concreta (mensagem, print, áudio) — não um conceito abstrato |
| 3. Explicação | 90 s | 2 a 3 blocos de 60–90 palavras; no máximo 1 termo novo, com glossário |
| 4. Prática | 60–90 s | Checklist, simulação ou "faça agora no seu celular" |
| 5. Fechamento | 20 s | Frase-âncora + mini quiz de 3 perguntas |
| 6. Ponte | 15 s | "Quer praticar com alguém?" (atividade em família opcional) |

Regras de escrita: frases de até 15 palavras; voz ativa; zero jargão sem tradução; nenhuma instrução que possa expor a pessoa; sempre um caminho de ajuda quando o tema é sensível.

### 2.2 Tipos de conteúdo e quando usar

| Tipo | Quando usar | Custo | Dados |
|---|---|---|---|
| **TXT** texto ilustrado | Conceito e passo a passo | Baixo | ~150–400 KB |
| **VÍD** vídeo curto (60–90 s) | Demonstração visual (ativar 2FA, revisar permissão) | Alto | 4–8 MB |
| **SIM** simulação de golpe | Treinar decisão sob pressão | Médio | ~300 KB |
| **CHK** checklist/ação | Transformar conteúdo em hábito | Baixo | ~100 KB |
| **QUIZ** quiz curto | Fixar e diagnosticar | Baixo | ~80 KB |
| **FAM** atividade em família | Transferir para a vida real | Baixo | ~80 KB |
| **AUD** áudio (60–90 s) | Pausa, respiração, narração acessível | Médio | ~400 KB |

### 2.3 Exemplo completo de lição

> **T2 · Módulo 1 · Lição 3 — "Falso suporte: quando o 'ajudante' é o golpe"**
> Tipo: texto + simulação · Duração: 4 min · Público: adulto/idoso (variante adolescente com o mesmo roteiro em app de jogo)

**Objetivo:** reconhecer uma abordagem de falso suporte técnico e agir sem instalar aplicativo, sem passar código e sem transferir dinheiro.

**Conteúdo**

*Bloco 1 — A cena (áudio + texto)*
"Boa tarde, aqui é da central de segurança do seu banco. Detectamos um acesso suspeito na sua conta. Para bloqueá-lo agora, preciso que instale este aplicativo de acesso remoto e me informe o código que vai chegar por SMS."

*Bloco 2 — O que está acontecendo*
Três sinais aparecem juntos: **pressa** ("agora"), **autoridade** (nome do banco) e um **pedido estranho** (instalar app e passar código). Banco nenhum pede para instalar programa de acesso remoto nem para ditar o código de verificação. O número exibido pode ser falsificado.

*Bloco 3 — Faça agora (checklist)*
1. Desligue. Não é falta de educação: é proteção.
2. Não instale nada e não informe código.
3. Ligue para o banco no número que está no verso do seu cartão.
4. Conte para alguém de confiança o que aconteceu.

*Bloco 4 — Frase-âncora*
**"Quem tem pressa é o golpe. Você pode parar."**

*Simulação (3 escolhas, sem punição)*
A tela mostra a conversa. Em cada escolha, a consequência aparece em 2 linhas e há "refazer".

| Escolha | Consequência mostrada |
|---|---|
| Instalar o app pedido | "A pessoa agora vê sua tela. Golpes assim costumam terminar em transferência. Você pode voltar e escolher de novo." |
| Informar o código do SMS | "Com esse código, ela entra na sua conta. O banco nunca pede esse número por telefone." |
| Desligar e ligar no número do cartão | "O banco confirma: não havia acesso suspeito. Você acabou de evitar um prejuízo." |

*Mini quiz (3 perguntas, feedback imediato)*

**1. Alguém liga dizendo ser do seu banco e pede para você instalar um aplicativo de acesso remoto. O que fazer?**
- a) Instalar rápido, para bloquear a conta. → *Quase.* A pressa é a ferramenta do golpe. O banco não precisa ver sua tela para bloquear nada.
- b) Desligar e ligar para o número que está no cartão. → **Correto.** Você mantém o controle e fala com o canal oficial.
- c) Pedir para a pessoa ligar mais tarde. → *Não resolve.* O contato continua sendo com quem te ligou, não com o banco.

**2. Qual destas informações o banco nunca pede por telefone?**
- a) Seu nome completo. → *Isso ele já tem.* O problema não é confirmar o nome.
- b) Sua senha ou o código de verificação. → **Correto.** Senha e código são pessoais e não se compartilham com quem ligou.
- c) O número do seu cartão. → *Atenção:* o número do cartão também é sensível. Mas a resposta mais crítica é a senha/código.

**3. Você já instalou o aplicativo antes de desconfiar. Qual é o primeiro passo?**
- a) Esperar para ver se acontece algo. → *Risco.* Cada minuto dá mais tempo para o golpe agir.
- b) Desconectar da internet, fechar o acesso remoto e chamar alguém de confiança para ligar ao banco. → **Correto.** Interromper o acesso vem primeiro; depois, registrar e buscar o banco.
- c) Apagar a conversa. → *Isso apaga sua prova.* Guarde o histórico: ele ajuda no registro e na denúncia.

*Atividade em família (5 min, opcional)*
"Ligue para uma pessoa mais velha da família e ensine os três sinais: pressa, autoridade e pedido estranho. Depois pergunte se ela já recebeu algo parecido."

**Critério de conclusão:** ler/ouvir os 4 blocos, passar pela simulação (qualquer caminho) e acertar ao menos 2 das 3 perguntas.

---

## 3. Avaliação e gamificação

### 3.1 Regras de aprovação

| Nível | Composição | Aprovação | Tentativas | O que o aluno vê |
|---|---|---|---|---|
| **Lição** | 3 perguntas | ≥2 acertos | Ilimitadas | Feedback por pergunta, explicação e "tentar de novo" |
| **Módulo** | Quiz de 6–8 perguntas | ≥70% | Ilimitadas, com intervalo de 10 min | Nota, revisão comentada, atalho "revisar lições" |
| **Trilha** | Todos os módulos aprovados + avaliação final de 10 perguntas | ≥70% na final | 3 por dia | Progresso, badge e certificado |
| **Trilha (criança 7–12)** | Todos os módulos + 1 atividade em família | Participação | — | Selo e certificado de participação (sem prova) |

**Princípios**
- Nota não é punição: reprovar abre um caminho de 2 minutos de revisão, não um bloqueio.
- Perguntas e respostas **nunca** são expostas ao responsável — só o estado (concluído, badge, %).
- "Bloqueado" só significa "módulo anterior ainda não aprovado" e sempre mostra o porquê e o atalho.
- Adolescente pode pedir um **teste rápido** para pular um módulo que já domina (nota ≥80%); criança não.
- Zero Trust: o servidor recalcula nota e aprovação a partir das respostas; o app só envia respostas e recebe o resultado.

### 3.2 Badges

Identidade visual: medalhão circular, **pétalas de peônia** ao redor de um miolo lavanda. Nada de troféu, chama, foguete ou pódio — a metáfora é jardim, não competição.

**Paleta oficial**

| Token | Hex | Uso |
|---|---|---|
| Peônia | `#E7A6BE` | Pétalas, conquistas de trilha |
| Peônia clara | `#F6DCE6` | Fundo do medalhão (tema claro) |
| Lavanda | `#B9A7E6` | Miolo, nível intermediário |
| Roxo Vippela | `#160064` | Ícone e texto sobre claros |
| Verde folha | `#7FB89A` | Conquistas de família |
| Âmbar | `#E9B872` | Constância e retorno |
| Violeta ação | `#8047BB` | Botões e links |

**Regras de arte:** 1 pétala = primeiro passo; 3 pétalas = trilha concluída; coroa de 5 pétalas = percurso completo. Arquivos `badge_<codigo>_<nivel>.webp` em 48/96/256 dp, com variante escura. Animação de "florescer" de 400 ms, desligada em "reduzir movimento".

| Código | Nome | Critério | Descrição exibida |
|---|---|---|---|
| `primeira_petala` | **Primeira Pétala** | Concluir a 1ª lição | "Todo jardim começa com uma pétala." |
| `raiz_forte` | **Raiz Forte** | Concluir T1 | "Suas contas têm raízes firmes." |
| `olho_atento` | **Olho Atento** | Concluir T2 | "Você reconhece o roteiro do golpe antes do clique." |
| `jardim_privado` | **Jardim Privado** | Concluir T3 | "Você escolhe o que floresce e o que fica protegido." |
| `respiracao` | **Respiração** | Concluir T4 | "Tela com hora de começar e hora de acabar." |
| `mente_serena` | **Mente Serena** | Concluir T5 | "Cuidar da cabeça também é segurança." |
| `guardiao` | **Guardião da Família** | Concluir T6 (responsável) | "Acompanhar com conversa, não com vigilância." |
| `ponte_confianca` | **Ponte de Confiança** | Concluir 2 lições em família | "O que se aprende junto fica." |
| `constancia_gentil` | **Constância Gentil** | 4 semanas com pelo menos 1 lição (não precisa ser seguida) | "Ritmo vale mais que pressa." |
| `colheita_completa` | **Colheita Completa** | Concluir 4 trilhas | "Um jardim inteiro, no seu tempo." |
| `voz_que_ajuda` | **Voz que Ajuda** | Concluir T5 + 1 atividade de apoio | "Saber pedir e oferecer ajuda é força." |
| `familia_aprendeu` | **Família Aprendeu** | 3 perfis do grupo concluírem 1 trilha cada | "Cada um aprendeu algo — e contou em casa." |

Badges são **concedidos pelo servidor** (fonte da verdade) e replicados no SQLite para exibir offline. Concessão idempotente por `(perfil_id, badge_id, origem)`.

### 3.3 Certificado de conclusão

**Campos**

| Campo | Exemplo |
|---|---|
| Nome no certificado | "Marina Garcia" |
| Trilha | "Golpes e fraudes: reconhecer para não cair" |
| Carga horária | "1 hora" (soma real das lições concluídas) |
| Data de conclusão | "26 de outubro de 2026" |
| Código de verificação | `VP-7K2M-9QX4` |
| Link de verificação | `vippela.app/c/<codigo>` |
| Conquistas | lista de badges da trilha |
| Texto | "Conclusão de trilha educativa da Vippela. Certificado de participação, sem validade profissional." |
| Assinatura | "Equipe Vippela — Educação Digital" |
| QR Code | aponta para o link de verificação |

**Regras de emissão**
- Emitido no servidor, uma única vez por `(perfil_id, trilha_id)`, com recibo de idempotência.
- Requer 100% dos módulos aprovados + avaliação final ≥70% (ou participação familiar, no perfil criança).
- Nome = nome do perfil; alteração posterior gera reemissão com novo código (o antigo é revogado).
- Criança: certificado traz "Acompanhado por: [responsável]" e só é emitido com consentimento registrado.
- Download em PDF (≤500 KB) e PNG (story). Reemissão permitida; revogação registrada em `certificado.revogado_em`.
- Nunca usar a palavra "diploma", "certificação profissional" ou qualquer referência à IBM.

### 3.4 Engajamento saudável

| Mecânica | Como funciona | Por que é saudável |
|---|---|---|
| **Ritmo semanal** (em vez de sequência diária) | Conta semanas com ≥1 lição. Não zera por falha; pausa sem culpa | Evita ansiedade e uso forçado |
| **Meta da família** | 1 desafio por semana, até 20 min, opt-in | Cria assunto em casa, não competição |
| **Pausa consciente** | Após 20 min na trilha, tela de respiro com "continuar depois" | O app respeita o tempo |
| **Fim explícito** | Toda sessão termina em "Por hoje é isso" com o que foi aprendido | Nada de rolagem infinita |
| **Conquista sem comparação** | Badges são pessoais; não há ranking, feed de amigos nem "X passou você" | Zero pressão social |
| **Lembrete gentil** | Máx. 2 por semana, 9h–20h, texto convidativo, fácil desligar | Não usa culpa nem medo |
| **Sem ofensiva punitiva** | Não existe "você perdeu sua sequência" | Coerente com o pilar de bem-estar |

**Métrica de sucesso:** "famílias que concluem 1 trilha em 60 dias" — não DAU, não tempo de tela.

---

## 4. Fluxo de telas do app

### 4.1 Mapa de navegação

```
[Tab Trilhas]
   └─ Home da trilha (percursos, continuar, desafio da semana)
        ├─ Detalhe da trilha
        │    ├─ Módulo (lista de lições)
        │    │    └─ Lição (blocos)
        │    │         ├─ Simulação
        │    │         ├─ Quiz da lição → Resultado
        │    │         └─ Atividade em família
        │    └─ Avaliação final → Resultado da trilha → Certificado
        ├─ Meus badges
        ├─ Progresso da família
        ├─ Desafio da semana
        └─ Downloads e acessibilidade
```

Rotas sugeridas (seguindo o padrão atual de `VippelaApp.kt`): `learn`, `track/{slug}`, `lesson/{id}`, `quiz/{id}`, `result/{attempt}`, `badges`, `certificate/{id}`, `family-progress`, `challenge/{id}`, `downloads`, `learning-settings`.

### 4.2 Telas necessárias

| # | Tela | Conteúdo principal | Ação primária |
|---|---|---|---|
| 1 | **Home da trilha** | Saudação, "continuar de onde parei", percurso do meu perfil, 6 trilhas em cards com % e badge, desafio da semana | Retomar lição |
| 2 | **Detalhe da trilha** | Objetivo, nível, duração, pré-requisito, módulos com estado, badges que ganha, "baixar para offline" | Começar/continuar |
| 3 | **Módulo (bottom sheet ou tela)** | Lições com duração e tipo; quiz do módulo; critério de aprovação | Abrir lição |
| 4 | **Lição** | Barra de progresso dos blocos, texto/áudio/vídeo, "faça agora", narração e fonte | Avançar / quiz |
| 5 | **Simulação** | Conversa ou tela falsa, 3 escolhas, consequência, refazer | Escolher |
| 6 | **Quiz** | Pergunta, alternativas grandes, progresso, sem cronômetro | Responder |
| 7 | **Resultado** | Acertos, feedback por questão, explicação, próximo passo | Revisar / continuar |
| 8 | **Resultado da trilha** | % final, badges ganhos, atividade em família sugerida | Ver certificado |
| 9 | **Meus badges** | Medalhões por trilha, data, critério, bloqueados em silhueta com "como ganhar" | Ver detalhe |
| 10 | **Certificado** | Prévia, código, QR, compartilhar, baixar | Baixar/compartilhar |
| 11 | **Progresso da família** | Cards por perfil com nível de visibilidade, % e último badge; sem notas | Convidar para desafio |
| 12 | **Desafio da semana** | Tema, duração, quem participou, "marcar como feito" | Participar |
| 13 | **Downloads** | Trilhas baixadas, tamanho, atualizar, remover | Baixar/remover |
| 14 | **Acessibilidade da trilha** | Fonte, narração, legendas, reduzir movimento, alto contraste | Ajustar |
| 15 | **Onboarding da trilha** (1x) | "Quem está aprendendo?" e objetivo ("proteger contas", "ajudar meus pais", "entender meus filhos") | Personalizar percurso |
| 16 | **Glossário** | Termos em linguagem simples, busca | Consultar |

### 4.3 Estados de tela

| Estado | Como aparece | O que o usuário pode fazer |
|---|---|---|
| **Em andamento** | Anel de progresso parcial + "Continuar: lição 3" | Retomar, ver módulos |
| **Bloqueado** | Cartão em tom lavanda claro, cadeado discreto, texto "Conclua o módulo 1 para abrir" | Ver o que falta, prévia de 30 s da 1ª lição |
| **Concluído** | Selo de pétala + data + "Revisar" | Rever, compartilhar badge |
| **Offline** | Faixa fina "Você está offline. Conteúdo baixado disponível" | Estudar o que está baixado; progresso vai para a fila |
| **Sincronizando** | Ícone discreto no topo, sem bloquear | Continuar |
| **Conteúdo desatualizado** | Aviso no detalhe da trilha com "Atualizar (2 MB)" | Atualizar ou seguir |
| **Erro de sincronização** | Cartão com "Tentar de novo", sem perder dados locais | Tentar de novo |
| **Indisponível** (trilha ainda não publicada para o perfil) | Card cinza com "Em breve" | Ver outras trilhas |

Detalhes de UX: nada de modal bloqueante para offline; o botão "Continuar" é sempre o elemento mais forte da tela; o quiz nunca tem cronômetro; toda tela de erro preserva o progresso local.

### 4.4 Progresso dos familiares: privacidade por perfil

Três níveis de visibilidade, escolhidos pelo **dono do progresso** (ou pelo responsável, no caso de criança):

| Nível | O que o outro vê |
|---|---|
| **Resumo** (padrão) | Trilhas concluídas, badges, última atividade em data aproximada ("esta semana") |
| **Detalhado** (opt-in) | Módulo atual, % da trilha, atividades em família feitas |
| **Privado** | Nada além de "participa da trilha" |

| Quem vê → / perfil | Responsável | Adolescente (13–17) | Criança (≤12) | Idoso | Outro adulto |
|---|---|---|---|---|---|
| **Responsável** | — | Resumo (detalhado só se o adolescente liberar) | Detalhado (padrão) | Resumo (só se liberar) | Nada |
| **Adolescente** | Seu próprio progresso | — | — | — | — |
| **Criança** | — | — | Seu próprio progresso | — | — |
| **Idoso** | Seu próprio progresso | — | — | — | — |

**O que nunca é compartilhado, em nenhum nível:** respostas do quiz, erros, tempo por lição, tentativas reprovadas, histórico de navegação. O responsável vê **conclusão e conquista**, não desempenho.

**Zero Trust na prática**
- O vínculo de dispositivo **não** concede acesso a progresso. A autorização é por `perfil` + `consentimento`, verificada a cada requisição.
- Endpoints devolvem apenas os campos permitidos (view `vw_progresso_familia`), nunca a linha inteira.
- Consentimento é revogável a qualquer momento e a revogação propaga em até 1 sincronização.
- Toda leitura de progresso de outro perfil gera registro em `evento_auditoria` (ator, alvo, escopo, horário).
- Sessão de responsável que acessa dados de criança pede reautenticação após 30 dias.

---

## 5. Modelo de dados

### 5.1 Entidades e relações

```
usuario (conta) 1──1 perfil (pessoa) ──* matricula ──1 trilha ──* modulo ──* licao ──1 quiz
     │                    │                                   │
     │                    ├──* progresso_licao               └──* licao_variante
     │                    ├──* tentativa (quiz)  [privado]
     │                    ├──* perfil_badge
     │                    ├──1 certificado
     │                    └──* consentimento
     └──* dispositivo ──1 vinculo ──1 familia ──* perfil
                                     └──* desafio_familia ──* participacao
```

Pontos de atenção:
- **Progresso pertence ao perfil, não à conta nem ao dispositivo.** Hoje o app usa `FamilyMember` de demonstração; será preciso migrar para `perfil` com id estável.
- `licao_variante` guarda as versões por público **sem duplicar a lição**: o progresso continua único, mesmo que a pessoa troque de variante.
- `tentativa` é privada por definição. Nenhum endpoint familiar a expõe.

### 5.2 Esquema PostgreSQL sugerido

```sql
-- Já existentes (nomes aproximados): usuario, dispositivo, vinculo
create table familia (
  id            uuid primary key default gen_random_uuid(),
  nome          text not null,
  criada_em     timestamptz not null default now()
);

create table perfil (
  id             uuid primary key default gen_random_uuid(),
  familia_id     uuid not null references familia(id) on delete cascade,
  usuario_id     uuid references usuario(id) on delete set null,
  nome_exibicao  text not null,
  papel          text not null check (papel in ('GUARDIAO','ADULTO','ADOLESCENTE','CRIANCA','IDOSO')),
  ano_nascimento int,
  avatar_key     text,
  criado_em      timestamptz not null default now()
);
create index perfil_familia_idx on perfil(familia_id);

-- vínculo existente passa a apontar para um perfil
alter table vinculo add column perfil_id uuid references perfil(id);

create table consentimento (
  id           uuid primary key default gen_random_uuid(),
  perfil_id    uuid not null references perfil(id) on delete cascade,
  escopo       text not null,          -- 'progresso_resumo' | 'progresso_detalhado'
  visibilidade text not null check (visibilidade in ('RESUMO','DETALHADO','PRIVADO')),
  concedido_em timestamptz not null default now(),
  revogado_em  timestamptz
);
create unique index consentimento_ativo_idx
  on consentimento(perfil_id, escopo) where revogado_em is null;

create table trilha (
  id             uuid primary key default gen_random_uuid(),
  slug           text not null unique,
  titulo         text not null,
  pilar          text not null check (pilar in ('PROTECAO','SAUDE','BEM_ESTAR')),
  nivel          text not null check (nivel in ('INICIANTE','INTERMEDIARIO','AVANCADO')),
  publico        text[] not null default '{}',
  duracao_min    int not null,
  objetivo       text not null,
  prerequisitos  uuid[] not null default '{}',
  capa_key       text,
  versao         int not null default 1,
  status         text not null default 'RASCUNHO',
  publicado_em   timestamptz
);

create table modulo (
  id                uuid primary key default gen_random_uuid(),
  trilha_id         uuid not null references trilha(id) on delete cascade,
  posicao           int not null,
  titulo            text not null,
  objetivo          text,
  duracao_min       int not null,
  aprovacao_minima  int not null default 70,
  unique (trilha_id, posicao)
);

create table licao (
  id            uuid primary key default gen_random_uuid(),
  modulo_id     uuid not null references modulo(id) on delete cascade,
  posicao       int not null,
  titulo        text not null,
  tipo          text not null check (tipo in ('TXT','VID','SIM','CHK','QUIZ','FAM','AUD')),
  duracao_seg   int not null check (duracao_seg <= 300),
  conteudo      jsonb not null,        -- blocos, mídia, texto alternativo
  midia         jsonb not null default '{}',
  versao        int not null default 1,
  unique (modulo_id, posicao)
);

create table licao_variante (
  licao_id    uuid not null references licao(id) on delete cascade,
  publico     text not null check (publico in ('ADULTO','ADOLESCENTE','CRIANCA','IDOSO')),
  titulo      text,
  conteudo    jsonb not null,
  midia       jsonb not null default '{}',
  duracao_seg int,
  primary key (licao_id, publico)
);

create table quiz (
  id              uuid primary key default gen_random_uuid(),
  escopo          text not null check (escopo in ('LICAO','MODULO','TRILHA')),
  licao_id        uuid references licao(id) on delete cascade,
  modulo_id       uuid references modulo(id) on delete cascade,
  trilha_id       uuid references trilha(id) on delete cascade,
  nota_minima     int not null default 70,
  check (
    (escopo = 'LICAO'  and licao_id  is not null) or
    (escopo = 'MODULO' and modulo_id is not null) or
    (escopo = 'TRILHA' and trilha_id is not null)
  )
);

create table questao (
  id             uuid primary key default gen_random_uuid(),
  quiz_id        uuid not null references quiz(id) on delete cascade,
  posicao        int not null,
  tipo           text not null default 'ESCOLHA_UNICA',
  publico        text,                 -- null = todos
  enunciado      text not null,
  alternativas   jsonb not null,       -- [{chave, texto, correta}]
  feedback_ok    text not null,
  feedback_erro  text not null,
  explicacao     text,
  unique (quiz_id, posicao)
);

create table tentativa (
  id           uuid primary key default gen_random_uuid(),
  perfil_id    uuid not null references perfil(id) on delete cascade,
  quiz_id      uuid not null references quiz(id) on delete cascade,
  iniciada_em  timestamptz not null default now(),
  concluida_em timestamptz,
  nota         int,
  respostas    jsonb not null default '[]',
  aprovado     boolean not null default false,
  idempotency  text unique
);
create index tentativa_perfil_idx on tentativa(perfil_id, quiz_id, iniciada_em desc);

create table matricula (
  id              uuid primary key default gen_random_uuid(),
  perfil_id       uuid not null references perfil(id) on delete cascade,
  trilha_id       uuid not null references trilha(id) on delete cascade,
  status          text not null default 'EM_ANDAMENTO',
  iniciada_em     timestamptz not null default now(),
  ultima_licao_id uuid references licao(id),
  concluida_em    timestamptz,
  unique (perfil_id, trilha_id)
);

create table progresso_licao (
  perfil_id      uuid not null references perfil(id) on delete cascade,
  licao_id       uuid not null references licao(id) on delete cascade,
  estado         text not null default 'NAO_INICIADA',
  segundos       int not null default 0,
  primeira_vez_em timestamptz,
  concluida_em   timestamptz,
  atualizado_em  timestamptz not null default now(),
  primary key (perfil_id, licao_id)
);

create table badge (
  id          uuid primary key default gen_random_uuid(),
  codigo      text not null unique,
  nome        text not null,
  descricao   text not null,
  criterio    jsonb not null,
  arte_key    text not null,
  nivel       int not null default 1,
  publico     text[] not null default '{}'
);

create table perfil_badge (
  perfil_id      uuid not null references perfil(id) on delete cascade,
  badge_id       uuid not null references badge(id) on delete cascade,
  conquistado_em timestamptz not null default now(),
  origem         text not null,        -- 'TRILHA:T2' | 'CONSTANCIA:2026-W43'
  visto_em       timestamptz,
  primary key (perfil_id, badge_id)
);

create table certificado (
  id                  uuid primary key default gen_random_uuid(),
  codigo_verificacao  text not null unique,
  perfil_id           uuid not null references perfil(id) on delete cascade,
  trilha_id           uuid not null references trilha(id) on delete restrict,
  nome_certificado    text not null,
  carga_horaria_min   int not null,
  emitido_em          timestamptz not null default now(),
  revogado_em         timestamptz,
  hash                text not null,
  unique (perfil_id, trilha_id)
);

create table desafio_familia (
  id           uuid primary key default gen_random_uuid(),
  familia_id   uuid not null references familia(id) on delete cascade,
  semana       date not null,
  titulo       text not null,
  descricao    text,
  meta_min     int not null default 20,
  status       text not null default 'ABERTO',
  unique (familia_id, semana)
);

create table desafio_participacao (
  desafio_id   uuid not null references desafio_familia(id) on delete cascade,
  perfil_id    uuid not null references perfil(id) on delete cascade,
  concluido_em timestamptz,
  primary key (desafio_id, perfil_id)
);

create table evento_auditoria (
  id            bigserial primary key,
  ator_perfil_id uuid references perfil(id),
  alvo_perfil_id uuid references perfil(id),
  acao          text not null,
  recurso       text not null,
  criado_em     timestamptz not null default now()
);
```

**View de privacidade** (única porta para o responsável ler progresso alheio):

```sql
create view vw_progresso_familia as
select p.familia_id, p.id as perfil_id, p.nome_exibicao, p.papel,
       count(distinct m.trilha_id) filter (where m.status = 'CONCLUIDA') as trilhas_concluidas,
       max(pl.atualizado_em) as ultima_atividade,
       array_agg(distinct b.codigo) as badges
from perfil p
left join matricula m on m.perfil_id = p.id
left join progresso_licao pl on pl.perfil_id = p.id
left join perfil_badge pb on pb.perfil_id = p.id
left join badge b on b.id = pb.badge_id
join consentimento c on c.perfil_id = p.id and c.revogado_em is null
where c.visibilidade <> 'PRIVADO'
group by p.familia_id, p.id, p.nome_exibicao, p.papel;
```

> Em Supabase, aplicar RLS em todas as tabelas e expor apenas `vw_progresso_familia` para o cliente do responsável. O backend Spring continua sendo a autoridade para emissão de badge e certificado.

### 5.3 O que espelhar no SQLite (offline)

| Tabela | Espelhar? | Observação |
|---|---|---|
| `trilha`, `modulo`, `licao`, `licao_variante`, `quiz`, `questao` | ✅ | Conteúdo baixado, com `versao` e `atualizado_em` |
| `matricula`, `progresso_licao` | ✅ | Progresso do próprio perfil |
| `tentativa` | ✅ (fila) | Guardar respostas localmente até sincronizar |
| `badge`, `perfil_badge` | ✅ (cache) | Concessão local otimista, servidor confirma |
| `certificado` | ✅ (metadados) | PDF só com conexão; prévia offline |
| `consentimento` | ✅ (próprio) | Cache do que **eu** autorizei |
| Progresso de outros perfis | ⚠️ cache com TTL | Somente leitura, nunca editável |
| `usuario`, `dispositivo`, `vinculo`, `familia` | ✅ (referência) | Só ids e nomes |
| `evento_auditoria`, `tentativa` de terceiros | ❌ | Nunca saem do servidor |

Room no Android, com `sync_outbox` para progresso e tentativas.

### 5.4 Sincronização

- **Conteúdo:** `GET /learning/content?since=<versao>` devolve delta + ETag. Checagem no máximo 1×/dia ou sob demanda.
- **Progresso:** fila local `(idempotency_key, tipo, payload)`; envio em lote de até 50 ao abrir o app, ao concluir lição e a cada 5 min em foreground. Backoff exponencial; nunca descarta dados.
- **Conflito:** por lição vale o maior `concluida_em`; tentativas são append-only e o servidor recalcula a nota; badge nunca é removido.
- **Certificado:** exige conexão (validação no servidor).
- **Revogação de consentimento:** limpa o cache de progresso de terceiros na próxima sincronização.

---

## 6. Requisitos específicos para mobile

### 6.1 Offline e download

| Requisito | Meta |
|---|---|
| Trilha completa sem vídeo | ≤ 4 MB |
| Trilha com vídeos (2–3) | ≤ 20 MB |
| Áudio das lições (TTS ou gravado) | ≤ 1,5 MB por 10 min |
| Cache total | 200 MB, LRU, com tela de gerenciamento |
| Download | Wi-Fi por padrão, com opção "usar dados móveis" explícita |
| Escrita offline | 100% das lições e quizzes concluíveis offline |
| Sincronização | automática, silenciosa, com indicador discreto |

### 6.2 Baixo consumo de dados

- Imagens em WebP (ou AVIF quando disponível), largura máx. 1080 px, ~60 KB por ilustração.
- Vídeo 720p, ≤90 s, sem autoplay, com pôster.
- Áudio Opus 32 kbps mono.
- Nenhum polling de conteúdo em background; sem prefetch agressivo.
- Payload de quiz ~2 KB; progresso em lote.

### 6.3 Acessibilidade

| Item | Especificação |
|---|---|
| Fonte | Corpo da lição **≥16 sp** (hoje o app usa 13 sp em `bodyMedium` — a lição precisa de estilo próprio), escala do sistema até 200% sem quebrar layout |
| Contraste | ≥4,5:1 para texto; ≥3:1 para ícones; nunca usar cor como único sinal |
| Toque | Alvos ≥48 dp; alternativas do quiz em cartões de largura total |
| Narração | Android `TextToSpeech` pt-BR (offline quando a voz estiver instalada); áudio gravado nas lições críticas |
| Legendas/transcrição | Todo vídeo e áudio tem texto equivalente |
| Linguagem | Frases ≤15 palavras, voz ativa, glossário, sem inglês desnecessário |
| Movimento | Respeitar "remover animações"; transições ≤300 ms |
| Leitor de tela | Ordem de foco correta, `contentDescription` em toda imagem, quiz anuncia "pergunta 1 de 3" |
| Modo idoso | Ativado por perfil: fonte 18 sp, narração ligada, um passo por tela |
| Modo criança | Ícones grandes, sem texto corrido longo, feedback por personagem |

### 6.4 Notificações

- Máximo **2 por semana**, entre 9h e 20h, opt-in por perfil.
- Textos convidativos, sem culpa: "Sobrou um minutinho? A lição de hoje é curta."
- Criança: lembretes vão para o responsável, não para a criança.
- Sem notificação de "sequência perdida", sem "fulano avançou".
- Implementação com `WorkManager` local (sem push obrigatório) + canal silencioso.

### 6.5 Privacidade e Zero Trust

- Autorização por perfil + consentimento, checada no servidor a cada chamada; nada de confiar no `device link` para leitura de progresso.
- Tokens de curta duração e escopo mínimo (`learning:read`, `learning:write`, `family:progress:read`).
- Respostas de quiz **nunca** saem do escopo do próprio perfil.
- Logs sem PII e sem conteúdo de resposta.
- Dados de criança: consentimento do responsável registrado, retenção mínima, exclusão sob solicitação.
- SQLCipher para o banco local na Fase 2 (dados de criança primeiro).
- Exportação e exclusão de dados disponíveis em Configurações (LGPD).

---

## 7. Plano de implementação em fases

### 7.1 MVP — 8 semanas

**Escopo:** 2 trilhas completas (T1 e T2), 8 módulos, **24 lições**, 1 simulação por trilha, 1 desafio de família, 4 badges, 1 modelo de certificado. Perfis adulto e adolescente. Sem vídeo.

*Na implementação, os 5 selos alcançáveis com T1/T2 já estão ativos; os outros 7 aparecem como "Em breve".*

| Semana | Entrega |
|---|---|
| 1 | Fechar modelo de dados, migração v1 no backend, content JSON schema, decisões de privacidade |
| 2 | Seed das 2 trilhas + telas Home, Detalhe e Módulo |
| 3 | Player de lição (blocos, TTS, fonte ajustável), navegação completa |
| 4 | Quiz de lição e de módulo, resultado, regras de aprovação no servidor |
| 5 | Room + download das trilhas + fila de sincronização |
| 6 | Badges, certificado em PDF, progresso da família com consentimento |
| 7 | Acessibilidade (TalkBack, 200%, contraste), testes com 3 famílias |
| 8 | Correções, instrumentação ética, roteiro de demo para a FETEPS |

**Critérios de saída:** 24 lições concluíveis offline; badge e certificado emitidos pelo servidor; responsável vê apenas o permitido; TalkBack navega a lição inteira; teste com 5 pessoas de 3 gerações.

**Status em outubro/2026:** o esqueleto do MVP está implementado no app (seção 0.5): 24 lições offline, quiz, selos, certificado em PDF e progresso da família. Ainda faltam a sincronização com o backend, a troca do armazenamento local para Room, a revisão pedagógica externa e o teste com famílias reais.

**Fora do MVP:** vídeo, narração gravada, trilhas T3–T6, modos criança/idoso completos, CMS, ranking (nunca), push remoto.

### 7.2 Fase 2 — +10 semanas

- T3, T4 e T5 completas; T6 com versão espelho adolescente.
- Variantes criança e idoso com revisão pedagógica.
- Áudio gravado nas lições críticas, legendas e transcrições.
- Downloads com atualização incremental e tela de armazenamento.
- Desafios semanais recorrentes e tela de progresso da família completa.
- CMS-lite no backend (CRUD de trilha/módulo/lição/quiz) para o time de conteúdo.
- SQLCipher + auditoria de acesso + exportação/exclusão de dados.

### 7.3 Fase 3 — +12 semanas

- Percursos adaptativos (recomendação simples por perfil, histórico e objetivo).
- Verificação pública de certificado (`vippela.app/c/<codigo>`) e página para escolas/ONGs.
- Parceria com escolas/FETEPS: turmas, relatório agregado e anônimo.
- Acessibilidade auditada por terceiros; modo libras nos vídeos.
- iOS reaproveitando o mesmo contrato de conteúdo e progresso.
- Métricas de impacto (não de engajamento): lições aplicadas, conversas em família, confiança relatada.

### 7.4 Riscos e mitigação

| Risco | Prob. | Impacto | Mitigação |
|---|---|---|---|
| Conteúdo sensível mal escrito para crianças | Média | Alto | Revisão por educador + checklist de segurança + linguagem sem detalhes gráficos |
| Escopo grande demais para o prazo | Alta | Alto | MVP com 2 trilhas; T3–T6 em ondas |
| Sincronização offline com bugs | Média | Alto | Fila idempotente, testes de conflito, nunca descartar dados |
| Certificado interpretado como certificação oficial | Média | Médio | Texto explícito de "participação", sem marcas de terceiros |
| Dados de criança expostos ao responsável | Baixa | Alto | Consentimento, view restrita, auditoria, testes de autorização |
| Equipe trocar de stack no meio | Baixa | Alto | Congelar decisão de stack no fim da semana 1 |

---

## 8. Próximos passos práticos

1. **Decidir a stack** (recomendo manter Kotlin/Compose) e registrar a decisão no README. — **Feito:** o MVP foi implementado em Compose.
2. **Validar o modelo de perfil** com o backend: `perfil_id` como dono do progresso, migrando os `FamilyMember` de demonstração. — **Feito no schema** (`vippela_learning.sql`); falta a API.
3. **Fechar o MVP em 2 trilhas** e escrever as 24 lições — começar por T2, que é a mais próxima do que a banca espera ver. — **Feito:** T1 e T2 têm 24 lições e 72 perguntas.
4. **Prototipar 5 telas** (Home, Detalhe, Lição, Quiz, Resultado) em alta fidelidade com a paleta peônia/lavanda e testar contraste. — **Feito** em telas reais, não protótipo.
5. **Spike técnico de 3 dias:** Room + JSON de conteúdo + `TextToSpeech` + fila de sincronização. — **Parcial:** JSON + TTS prontos; Room e fila ficaram para a Fase 2.
6. **Criar o seed de conteúdo** no Postgres e o endpoint `GET /learning/content?since=`. — **Pendente:** o schema existe; o seed e o endpoint entram na integração.
7. **Testar com 3 famílias** (uma com criança, uma com adolescente, uma com idoso) e ajustar linguagem e tamanho de fonte. — **Pendente:** o teste de navegação automatizado cobre o fluxo, mas não substitui a validação com pessoas.
8. **Preparar a demo da FETEPS:** uma família fictícia percorrendo T2, ganhando o badge "Olho Atento" e emitindo o certificado — com o modo offline visível. — **Roteiro pronto** na seção 0.5.

---

*Documento vivo. Toda alteração de regra de aprovação, badge ou visibilidade deve ser registrada aqui e refletida no backend antes de chegar ao app.*
