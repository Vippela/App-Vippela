# Ativar o login Google

O aplicativo usa Credential Manager e Firebase Authentication. O token Google é validado pelo Firebase antes da abertura da sessão. Não há Firestore, Realtime Database nem sincronização dos dados da família.

## Configuração

1. Abra o [Firebase Console](https://console.firebase.google.com/) com sua conta e crie ou selecione um projeto.
2. Adicione um aplicativo Android com o pacote **br.com.vippela**.
3. Cadastre os certificados de assinatura abaixo para testar o APK gerado neste computador.
4. Em **Authentication → Sign-in method**, habilite **Google** e escolha o e-mail de suporte.
5. Baixe novamente o **google-services.json** após habilitar o Google e coloque o arquivo em **app/google-services.json**.
6. Compile novamente o app e instale o novo APK em um dispositivo com Google Play Services e uma conta Google.

O plugin Google Services é aplicado automaticamente quando o arquivo existe. Ele gera o `default_web_client_id` usado pelo aplicativo. Não coloque um client secret no aplicativo. O arquivo de configuração não está incluído nesta entrega porque o projeto Firebase ainda não foi configurado.

Se o consentimento OAuth estiver em modo de teste, adicione as contas de teste na configuração de público do Google Auth Platform. Para publicar, configure a identidade do aplicativo e os requisitos de consentimento no console.

## Assinatura do APK desta entrega

SHA-1:
```
A6:58:94:4F:3C:AC:72:0C:06:3F:89:21:B6:69:5A:CB:C9:85:7A:3F
```

SHA-256:
```
19:8E:AF:44:00:DE:CC:45:30:25:AB:2F:0C:B4:C7:17:62:8A:7E:76:8E:EC:FD:8B:25:BF:87:6A:AB:5E:28:78
```

Outro computador ou uma assinatura de publicação terá certificados diferentes. Obtenha-os com `./gradlew signingReport` e cadastre-os também.

## Comportamento atual

Sem configuração, o botão explica que o Google ainda não está ativo e mantém a tela de acesso. Ele não simula um login bem-sucedido. Com configuração, o UID autenticado consulta o perfil salvo no aparelho. Se ainda não existir, a escolha de perfil aparece no cadastro e é salva para os próximos acessos; as permissões e os vínculos continuam demonstrativos. O servidor de uma versão futura deverá controlar os papéis e os vínculos, sem confiar nessa escolha local.

A troca de e-mail e telefone nas configurações altera apenas a apresentação em memória, sem chamar `updateEmail` ou modificar o usuário Firebase. Ao sair, o app encerra a sessão Firebase e limpa o estado do Credential Manager.

O login real ainda precisa ser testado após a configuração do projeto.

Referências: [Firebase — autenticação Google no Android](https://firebase.google.com/docs/auth/android/google-signin) e [Android — Credential Manager](https://developer.android.com/identity/sign-in/credential-manager-siwg-implementation).
