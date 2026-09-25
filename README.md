# Vippela Android — 0.4.0

Aplicativo nativo Kotlin + Jetpack Compose com navegação entre as visões de responsável e familiar. Dados de acompanhamento, vínculos, relatórios e atividades são demonstrativos.

## Abrir e executar

Abra esta pasta no Android Studio, use JDK 21 e instale o SDK Android 36.1. Sincronize o Gradle e execute o módulo app. O wrapper Gradle está incluído. O arquivo local.properties é específico do computador e não acompanha o ZIP.

```sh
./gradlew :app:assembleDebug
```

## Contas de demonstração

- Responsável: responsavel@vippela.demo
- Familiar: familiar@vippela.demo
- Senha de ambas: vippela123
- Código de vínculo: 482619

O onboarding abre o acesso universal, com e-mail ou Google. Os cards Responsável/Familiar aparecem somente no cadastro. O tipo escolhido fica salvo neste aparelho e é recuperado nos próximos logins. E-mails locais desconhecidos seguem para o cadastro; contas Google novas escolhem o perfil após a autenticação, e contas conhecidas são identificadas pelo UID.

## Ajustes desta versão

- Onboarding com cinco etapas e página de acesso por perfil.
- Vínculo familiar com entrada de código, validação local e confirmação em tela separada.
- Ícones de marcas proporcionais e percentuais centralizados nos gráficos circulares.
- Seleção de foto pela galeria, edição de nome e contatos e tema claro/escuro.
- Contatos editados aparecem no perfil e nas configurações, sem alterar o e-mail de autenticação.
- Integração Google real preparada com Credential Manager + Firebase Authentication. Consulte GOOGLE-LOGIN.md para ativá-la.

Preferências visuais e dados de acompanhamento ficam em memória durante a sessão. As contas locais (nome, e-mail, perfil e verificador de senha com salt/PBKDF2) e o vínculo UID/perfil do Google ficam nas preferências privadas do aparelho. A permissão de leitura da foto escolhida é mantida pelo Android, mas a escolha do perfil ainda não é persistida após encerrar o processo. A foto é carregada em tamanho reduzido, fora da thread principal.

## Pendências

As duas primeiras ilustrações, a ilustração de vínculo e os nove ícones do onboarding usam os PNGs transparentes fornecidos pelo usuário. O rodapé do onboarding fica ancorado na parte inferior; o conteúdo superior pode rolar em telas menores. O gráfico da quarta etapa foi redesenhado com espessura maior e degradê, e o celular da quinta etapa tem olhos e sorriso.

O pacote contém somente uma ilustração de vínculo (responsável e criança no skate), usada nas duas etapas. A imagem específica da criança nos ombros do adulto não veio no pacote. Os PNGs disponíveis preservam o canal alfa e não carregam mais o fundo claro dos JPGs anteriores. O logotipo e os avatares de escolha continuam sendo as reconstruções vetoriais anteriores.

O login Google exige o arquivo de configuração do projeto do usuário e um teste em dispositivo conectado. Esta entrega não inclui credenciais nem declara esse teste como concluído.

## Organização

- ui/screens: telas
- ui/components: componentes compartilhados
- ui/theme: cores e tipografia
- ui/navigation: rotas
- data: estado demonstrativo
- auth: autenticação Google

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
