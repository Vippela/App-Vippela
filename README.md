# Vippela Android — 0.3.0

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

O onboarding abre a página de escolha de acesso. Os cards de acesso abrem o login do perfil selecionado. O link Cadastro abre uma escolha centralizada de perfis antes do formulário. O cadastro local não cria uma conta em servidor.

## Ajustes desta versão

- Onboarding com cinco etapas e página de acesso por perfil.
- Vínculo familiar com entrada de código, validação local e confirmação em tela separada.
- Ícones de marcas proporcionais e percentuais centralizados nos gráficos circulares.
- Seleção de foto pela galeria, edição de nome e contatos e tema claro/escuro.
- Contatos editados aparecem no perfil e nas configurações, sem alterar o e-mail de autenticação.
- Integração Google real preparada com Credential Manager + Firebase Authentication. Consulte GOOGLE-LOGIN.md para ativá-la.

Preferências, alterações e dados locais ficam em memória durante a sessão do aplicativo. A permissão de leitura da foto escolhida é mantida pelo Android, mas a escolha do perfil ainda não é persistida após encerrar o processo. A foto é carregada em tamanho reduzido, fora da thread principal.

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
