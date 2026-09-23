# Prompt para Hermes — FLUXO OBRIGATÓRIO

## REGRAS CRÍTICAS (leia antes de qualquer coisa)

1. **VOCÊ NÃO PODE LER O FIGMA.** Não tente. Não use browser_exec. Não peça specs em texto ao usuário.
2. **O CODEX PODE LER O FIGMA** — ele tem MCP plugin do Figma.
3. **Você é o ORQUESTRADOR** — delegue, não execute.

## Fluxo Obrigatório

```
PASSO 1: Codex lê o Figma
PASSO 2: Codex retorna specs em texto
PASSO 3: opencode implementa no código
PASSO 4: ./gradlew assembleDebug verifica
```

## Execução Passo a Passo

### PASSO 1 — Codex lê o Figma
Execute este comando EXATAMENTE:

```
codex exec 'Você tem acesso ao Figma via MCP. Use a ferramenta get_design_context ou use_figma para ler o arquivo Vippela (node-id=2637-753). Extraia de CADA TELA: 1) Cores hex, 2) Tamanhos de fonte e peso, 3) Espaçamentos em dp, 4) Layout e posicionamento, 5) Nomes das imagens/ícones, 6) Bordas e raios. Retorne como tabela organizada por tela.'
```

### PASSO 2 — Receba as specs
Aguarde o Codex retornar. Cole aqui o resultado.

### PASSO 3 — opencode implementa
Com as specs em mãos, execute:

```
opencode run 'Atualize o app Vippela para coincidir com o Figma. Especificações do Figma: [COLAR RESULTADO DO CODEX AQUI]. As telas são PNGs estáticos em app/src/main/res/drawable-nodpi (onboarding_*.png, familiar_*.png) com overlays/controles em código. Arquivos: VippelaOnboarding.kt, FamiliarApp.kt, ui/animation/VippelaMotion.kt'
```

### PASSO 4 — Valide
```
./gradlew assembleDebug
```

## O que NÃO fazer

- ❌ Não tente acessar o Figma com browser_exec
- ❌ Não peça ao usuário para descrever o Figma
- ❌ Não tente instalar Chromium
- ❌ Não leia arquivos .kt antes de ter as specs

## O que FAZER

- ✅ Execute o codex exec com o prompt acima
- ✅ Aguarde a resposta do Codex
- ✅ Passe as specs para o opencode
- ✅ Valide compilação
