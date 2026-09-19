# 🛡️ Vippela

### Segurança, Bem-Estar e Educação Digital para Famílias

> **Um aplicativo que não quer apenas dizer ao usuário o que é perigoso. Ele quer ensiná-lo a descobrir por que é perigoso.**

Projeto desenvolvido por estudantes da **ETEC da Zona Leste — Centro Estadual de Educação Tecnológica Paula Souza (CEETEPS/SP)** e **semifinalista do Samsung Solve for Tomorrow Brasil 2026**.

---

## 📱 Sobre o projeto

A crescente utilização da internet e de dispositivos móveis tem ampliado a exposição de **crianças, adolescentes e idosos** a riscos digitais, como phishing, malware, golpes, conteúdo impróprio e uso excessivo de telas.

O **Vippela** propõe uma abordagem diferente das ferramentas tradicionais de controle parental: em vez de apenas bloquear uma interação considerada perigosa, o aplicativo busca **explicar o motivo do risco e desenvolver a autonomia digital do usuário**.

O projeto é baseado em três pilares:

| Pilar                        | Objetivo                                                                                        |
| ---------------------------- | ----------------------------------------------------------------------------------------------- |
| 🛡️ **Segurança**            | Identificar riscos digitais, auxiliar no controle de aplicativos e gerenciar o tempo de tela.   |
| 🎓 **Educação Digital**      | Ensinar conceitos relacionados a phishing, privacidade, malware e uso consciente da tecnologia. |
| 💙 **Bem-Estar e Autonomia** | Incentivar uma relação mais equilibrada e consciente com a tecnologia.                          |

### 🌸 Origem do nome

**Vippela** une três elementos:

* **Vip** — referência a *viver pleno*;
* **Peônia** — associada a honra e proteção;
* **Lavanda** — associada à calma e serenidade.

O nome representa a proposta de promover uma vida digital **mais segura, consciente e tranquila para as famílias**.

---

## ⚙️ Funcionalidades

### ⏱️ Gestão de tempo de tela

Permite configurar limites de utilização por aplicativo ou horário, acompanhando o uso em tempo real.

### 📵 Bloqueio de aplicativos

Possibilita restringir aplicativos específicos de acordo com regras definidas pelo responsável.

### 🔗 Análise de links suspeitos

O sistema utiliza características da URL para estimar seu nível de risco:

* 🟢 **Baixo risco**
* 🟡 **Atenção**
* 🔴 **Alto risco**

Entre os fatores considerados estão:

* domínio suspeito;
* utilização de HTTP;
* redirecionamentos;
* encurtadores de URL;
* outras características relacionadas à estrutura do endereço.

> ⚠️ A análise de URL em tempo real por APIs externas ainda está planejada e **não está implementada nesta versão**.

### 🎓 Trilhas educacionais

Conteúdos interativos voltados à conscientização sobre:

* Phishing;
* Malware;
* Ransomware;
* Notícias falsas;
* Privacidade;
* Segurança digital.

### 📊 Relatórios de atividade

Visualização de dados de utilização em períodos:

* Diário;
* Semanal;
* Mensal.

Os relatórios incluem gráficos e comparativos para facilitar a compreensão dos hábitos digitais.

### 👨‍👩‍👧 Vínculo entre familiares

Permite que responsáveis acompanhem familiares por meio de perfis individuais e relatórios personalizados.

No plano gratuito, é possível acompanhar até **2 familiares**, com possibilidade de expansão em planos pagos.

### 🔔 Alertas de conscientização

O aplicativo pode alertar o usuário em situações como:

* uso excessivo de determinado aplicativo;
* tentativa de acesso a aplicativo bloqueado;
* interação com link considerado suspeito.

Sempre que possível, o alerta busca **explicar o motivo da recomendação**, em vez de simplesmente bloquear a ação.

### 🔐 Autenticação

Sistema de autenticação utilizando usuário e senha, com armazenamento protegido das credenciais.

> **Nota:** recursos de controle parental multiplataforma e análise de URLs em tempo real por APIs externas estão planejados para versões futuras.

---

# 🧪 Fundamentação científica

O **Vippela não é apenas um produto tecnológico**. O projeto também consiste em uma pesquisa aplicada que investiga o impacto de **alertas educativos e contextualizados** na capacidade dos usuários de reconhecer riscos digitais.

A pesquisa segue o ciclo:

```text
Problema
   ↓
Hipótese
   ↓
Experimento
   ↓
Coleta de dados
   ↓
Análise
   ↓
Conclusão
```

A proposta é comparar a compreensão dos usuários diante de **alertas educativos** com abordagens baseadas apenas em bloqueios ou notificações simples.

### 📚 Base teórica

O projeto utiliza como referências:

* **Pilares da Cibersegurança**

    * Confidencialidade;
    * Integridade;
    * Disponibilidade;
    * Autenticidade;
    * Legalidade.

* **Zero Trust**

    * Utilizado como referência conceitual para autenticação e controle de acesso.

* **Proteção digital de crianças e adolescentes**

    * Considerando legislações e diretrizes brasileiras relacionadas à proteção de menores no ambiente digital, incluindo a **Lei nº 15.211/2025**.

---

# 🌍 Objetivos de Desenvolvimento Sustentável

| ODS                                               | Relação com o Vippela                                                                      |
| ------------------------------------------------- | ------------------------------------------------------------------------------------------ |
| **ODS 3 — Saúde e Bem-Estar**                     | Promoção de hábitos digitais mais saudáveis e equilibrados.                                |
| **ODS 4 — Educação de Qualidade**                 | Educação e conscientização sobre riscos e segurança digital.                               |
| **ODS 9 — Indústria, Inovação e Infraestrutura**  | Desenvolvimento de uma solução tecnológica voltada à segurança e ao bem-estar digital.     |
| **ODS 16 — Paz, Justiça e Instituições Eficazes** | Contribuição para a proteção de crianças e adolescentes contra riscos no ambiente digital. |

---

# 🛠️ Tecnologias

| Camada                             | Tecnologia      |
| ---------------------------------- | --------------- |
| **Linguagem**                      | Kotlin          |
| **Plataforma**                     | Android         |
| **IDE**                            | Android Studio  |
| **Interface**                      | Jetpack Compose |
| **Banco de dados local**           | Room / SQLite   |
| **Backend / Nuvem**                | Supabase        |
| **Banco de dados**                 | PostgreSQL      |
| **Autenticação / APIs / Realtime** | Supabase        |
| **Design e prototipação**          | Figma           |
| **Gestão de projeto**              | Notion / Padlet |
| **Versionamento**                  | Git / GitHub    |

> **Nota sobre a arquitetura:** documentos anteriores do projeto mencionaram uma arquitetura híbrida baseada em **React Native/Expo + Spring Boot**. Esta versão do README considera como referência a stack nativa Android descrita no TCC mais recente. Caso a arquitetura definitiva tenha sido alterada, esta seção deve ser atualizada.

---

# 🏗️ Arquitetura*

A arquitetura atual do projeto é baseada em uma aplicação Android nativa (adapto a mudanças):

```text
┌──────────────────────────────┐
│        Aplicativo Android    │
│                              │
│       Kotlin + Compose       │
└──────────────┬───────────────┘
               │
       ┌───────┴────────┐
       │                │
       ▼                ▼
┌─────────────┐   ┌─────────────┐
│ Room/SQLite │   │  Supabase   │
│   Offline   │   │ Cloud/Sync  │
└─────────────┘   └──────┬──────┘
                         │
                         ▼
                  ┌─────────────┐
                  │ PostgreSQL  │
                  └─────────────┘
```

---

# 👥 Equipe

* **Derik Soares Batinga**
* **Guilherme Izídio Nogueira**
* **Inaê Victória Maria de Freitas**
* **Rafael Nunes Cardoso**

### Instituição

**ETEC da Zona Leste**
Centro Estadual de Educação Tecnológica Paula Souza — **CEETEPS/SP**

### Competição

**Samsung Solve for Tomorrow Brasil 2026**

---

# 🚀 Como executar

## Pré-requisitos

* Android Studio;
* JDK compatível com a versão do projeto;
* Android SDK;
* Dispositivo físico ou emulador Android 8.0 (API 26) ou superior.

## Instalação

Clone o repositório:

```bash
git clone https://github.com/<seu-usuario>/vippela.git
cd vippela
```

Abra o projeto no **Android Studio** e aguarde a sincronização das dependências do Gradle.

Em seguida, execute o projeto em:

* um emulador Android; ou
* um dispositivo físico compatível.

---

## 📱 Requisitos mínimos

### Usuário final

* **Android:** 8.0 (Oreo) ou superior;
* **RAM:** 2 GB;
* **Internet:** necessária para funcionalidades que dependem de sincronização com a nuvem.

> Algumas funcionalidades podem permanecer disponíveis localmente mesmo sem conexão, dependendo da implementação atual da aplicação.

---

# 🔬 Pesquisa e desenvolvimento

O Vippela está sendo desenvolvido como um projeto de **pesquisa científica aplicada**, buscando integrar tecnologia, educação digital e segurança.

A proposta central é investigar como a apresentação de informações contextualizadas pode contribuir para que usuários desenvolvam maior capacidade de **identificar, compreender e evitar riscos digitais**.

---

# 📄 Licença

Este projeto foi desenvolvido no contexto do **Samsung Solve for Tomorrow Brasil 2026**.

A definição da licença de software deverá considerar as regras da competição e os direitos da equipe sobre o código e demais materiais desenvolvidos.

> **Licença ainda não definida.**

---

# 📬 Contato

O Vippela é um projeto desenvolvido por estudantes da **ETEC da Zona Leste** para o **Samsung Solve for Tomorrow Brasil 2026**.

Para dúvidas, sugestões ou contribuições, abra uma **Issue** neste repositório.

---

<p align="center">
  <strong>Vippela</strong><br>
  Segurança, bem-estar e educação para uma vida digital mais consciente.
</p>
