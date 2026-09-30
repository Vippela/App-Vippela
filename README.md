# Integração de bloqueio por vínculo — 0.5.0

O app agora consulta os aplicativos elegíveis do familiar vinculado e envia permissões reais pelo backend. As instruções de servidor, pareamento, acessibilidade, protocolo e limitações estão no README do projeto `backend-Vippela`.

## Cadastro e login no servidor — 0.6.0

Não existem mais contas de demonstração: o login, o cadastro e o botão Google são atendidos pelo backend (`POST /auth/register`, `/auth/login`, `/auth/google`). O endereço do servidor agora é pedido **antes** do acesso, em **Perfil → Vínculo familiar → Configurar servidor** e também nas telas de acesso, cadastro e perfil.

O login Google está **desligado por padrão**: o botão só aparece com a propriedade `vippela.google=true` (`./gradlew -Pvippela.google=true assembleDebug`) e o `app/google-services.json` presente. Sem os dois, o aplicativo funciona só com e-mail e senha e avisa isso na tela de acesso. Do lado do servidor, `VIPPELA_GOOGLE_CREDENTIALS` é opcional e `POST /auth/google` responde 503 quando não está definido.

O aplicativo guarda apenas a sessão (token opaco, id, nome, e-mail, tipo e validade) nas preferências privadas. Ao abrir, ele confirma o token com `GET /auth/me`; token recusado é apagado e a tela de login aparece de novo. Senha nunca é guardada no aparelho. As telas de acesso, cadastro e perfil mostram o erro do servidor em português e o botão fica ocupado durante a chamada.

O login Google envia o idToken do Firebase; quem valida a assinatura é o backend. Se o e-mail da conta Google já existir com senha no servidor, o aplicativo não faz vínculo automático e orienta entrar com a senha. O tipo de conta (Responsável ou Familiar) é pedido no primeiro acesso e ignorado pelo servidor quando a conta já existe.

A recuperação de senha por e-mail ainda não está disponível.

Configure a mesma URL em **Perfil → Vínculo familiar → Configurar servidor** nos dois celulares. Gere o código no responsável, confirme no familiar e ative **Proteção familiar Vippela** em Acessibilidade. Em **Aplicativos**, o responsável altera as permissões e acompanha a confirmação do aparelho. A última regra recebida fica salva para uso offline; novas regras precisam de conexão.

A autorização é por chaves de vínculo separadas por conta/servidor nesta instalação. Dados de acompanhamento e pedidos antigos continuam demonstrativos. Nenhum perfil sem pareamento recebe um bloqueio real. A proteção depende do serviço de acessibilidade ativo e retorna à tela inicial ao detectar o app bloqueado; não é suspensão de pacotes pelo sistema.

Para rodar o backend, configure `VIPPELA_DB_PASSWORD`. HTTP é permitido somente no APK debug para teste em rede local; release exige HTTPS. O código fixo `482619` foi removido do fluxo real. O JDK dos testes Robolectric é fixado em 21.

---

## Histórico anterior (UI de demonstração)

# Vippela Android — 0.4.0

Aplicativo nativo Kotlin + Jetpack Compose com navegação entre as visões de responsável e familiar. Dados de acompanhamento, vínculos, relatórios e atividades são demonstrativos.

## Abrir e executar

Abra esta pasta no Android Studio, use JDK 21 e instale o SDK Android 36.1. Sincronize o Gradle e execute o módulo app. O wrapper Gradle está incluído. O arquivo local.properties é específico do computador e não acompanha o ZIP.

```sh
./gradlew :app:assembleDebug
```

## Contas

Não há mais conta pronta no aplicativo. O usuário se cadastra na tela "Criar conta" (nome, e-mail, senha de 8 caracteres ou mais e tipo) e o registro fica no PostgreSQL do Supabase, acessado pelo backend. O código de vínculo `482619` continua removido do fluxo real.

O onboarding abre o acesso universal, com e-mail ou Google. Os cards Responsável/Familiar aparecem somente no cadastro e no primeiro acesso pelo Google; o servidor guarda o tipo escolhido e o devolve nos próximos logins.

## Ajustes desta versão

- Onboarding com cinco etapas e página de acesso por perfil.
- Vínculo familiar com entrada de código, validação local e confirmação em tela separada.
- Ícones de marcas proporcionais e percentuais centralizados nos gráficos circulares.
- Seleção de foto pela galeria, edição de nome e contatos e tema claro/escuro.
- Contatos editados aparecem no perfil e nas configurações, sem alterar o e-mail de autenticação.
- Integração Google real preparada com Credential Manager + Firebase Authentication. Consulte GOOGLE-LOGIN.md para ativá-la.

Preferências visuais e dados de acompanhamento ficam em memória durante a sessão. A sessão do servidor (token opaco, id, nome, e-mail, tipo e validade) fica nas preferências privadas do aparelho; senha não é guardada. A permissão de leitura da foto escolhida é mantida pelo Android, mas a escolha do perfil ainda não é persistida após encerrar o processo. A foto é carregada em tamanho reduzido, fora da thread principal.

## Pendências

As duas primeiras ilustrações, a ilustração de vínculo e os nove ícones do onboarding usam os PNGs transparentes fornecidos pelo usuário. O rodapé do onboarding fica ancorado na parte inferior; o conteúdo superior pode rolar em telas menores. O gráfico da quarta etapa foi redesenhado com espessura maior e degradê, e o celular da quinta etapa tem olhos e sorriso.

O pacote contém somente uma ilustração de vínculo (responsável e criança no skate), usada nas duas etapas. A imagem específica da criança nos ombros do adulto não veio no pacote. Os PNGs disponíveis preservam o canal alfa e não carregam mais o fundo claro dos JPGs anteriores. O logotipo e os avatares de escolha continuam sendo as reconstruções vetoriais anteriores.

O login Google exige o arquivo de configuração do projeto do usuário e um teste em dispositivo conectado. Esta entrega não inclui credenciais nem declara esse teste como concluído.

## Organização

- ui/screens: telas
- ui/components: componentes compartilhados
- ui/theme: cores e tipografia
- ui/navigation: rotas
- data: estado da tela, sessão do servidor e chaves de vínculo
- data/auth: cadastro, login, Google e sessão guardada
- data/linking: pareamento e regras por conta
- auth: autenticação Google (Credential Manager + Firebase)

Não há monitoramento real de aplicativos, restrições do dispositivo, banco de dados ou notificações do Android nesta etapa.

## Revisão do acesso

- Transições discretas de fade + slide, de 260–320 ms, incluindo navegação de volta.
- Login único, sem seleção prévia do tipo de usuário.
- Opção Facebook removida; nenhuma dependência foi adicionada.
- `ic_google_logo.xml`: vetor em quatro cores, sem fundo, correspondente à versão clássica enviada. A imagem anexada tinha quadriculado opaco incorporado e não foi usada no botão.

O cadastro por e-mail continua sendo uma simulação local, sem conta de servidor. A associação local de perfil não é autorização de backend e não sincroniza entre dispositivos. O login Google continua exigindo a configuração descrita em GOOGLE-LOGIN.md.

## Animações — 0.4.0

A abelha repete o percurso de cada etapa e permanece dentro da tela. O onboarding usa AnimatedContent com fade e slide; avanço e volta usam sentidos opostos. Os destinos do Navigation Compose usam transições de 320 ms.

Os testes de movimento controlam o relógio para comparar quadros da abelha e verificar posições intermediárias das páginas, inclusive ao voltar.
