# Separação Freedom — App Android

Painel de separação de pedidos do Grupo Freedom, empacotado como app Android usando [Capacitor](https://capacitorjs.com/).

O app em si (HTML/CSS/JS) fica em `www/index.html`. Ele funciona 100% offline (usa `localStorage` do dispositivo para salvar tudo), exceto o botão "Baixar Excel" no relatório, que precisa de internet no momento do clique.

## Como gerar o APK (sem instalar nada no computador)

Este projeto já vem com um workflow do GitHub Actions (`.github/workflows/build-apk.yml`) que compila o APK automaticamente na nuvem. Você só precisa:

1. **Criar um repositório novo no GitHub** (pode ser privado ou público):
   - Acesse https://github.com/new
   - Dê um nome, por exemplo `separacao-freedom`
   - Não marque nenhuma opção de "adicionar README" (para não conflitar)
   - Clique em "Create repository"

2. **Subir esta pasta para o repositório.** Se você tem o Git instalado no seu computador, dentro desta pasta rode:
   ```bash
   git init
   git add .
   git commit -m "Primeira versão do app"
   git branch -M main
   git remote add origin https://github.com/SEU-USUARIO/separacao-freedom.git
   git push -u origin main
   ```
   Troque `SEU-USUARIO` pelo seu usuário do GitHub.

   Se você não usa o Git no terminal, também dá para subir os arquivos direto pela interface do GitHub (arrastando a pasta na página do repositório em "Add file" → "Upload files").

3. **Aguardar o build.** Assim que o código chegar no GitHub, o Actions começa a compilar automaticamente. Isso leva de 3 a 6 minutos. Você acompanha em:
   ```
   https://github.com/SEU-USUARIO/separacao-freedom/actions
   ```

4. **Baixar o APK.** Quando o build terminar (ícone verde ✔), tem duas formas de pegar o arquivo:
   - Na aba **"Actions"**, clique no build mais recente → na seção **"Artifacts"**, baixe `separacao-freedom-apk`.
   - Ou na aba **"Releases"** do repositório (lado direito da página principal), vai aparecer uma release nova com o `app-debug.apk` já anexado para baixar direto no celular.

5. **Instalar no celular.** Baixe o `.apk` no seu Android e abra o arquivo. Se aparecer aviso de "instalar de fontes desconhecidas", autorize — é normal para apps que não vêm da Play Store.

## Rodando o build você mesmo, sem o GitHub Actions (opcional)

Se preferir compilar localmente, é necessário ter instalado: Node.js, JDK 17, e o Android SDK. Com tudo instalado:

```bash
npm install
npx cap sync android
cd android
./gradlew assembleDebug
```

O APK fica em `android/app/build/outputs/apk/debug/app-debug.apk`.

## Estrutura do projeto

- `www/index.html` — o app (HTML/CSS/JS), é a única coisa que você precisa editar para mudar o funcionamento do app.
- `android/` — projeto nativo Android gerado pelo Capacitor (gerado automaticamente, normalmente não precisa mexer aqui).
- `.github/workflows/build-apk.yml` — automação que compila o APK na nuvem a cada push.
- `capacitor.config.json` — configurações do app (nome, id do pacote, etc.).

## Fazendo alterações no app depois

Sempre que quiser mudar alguma coisa no app, edite `www/index.html` e suba de novo para o GitHub (`git add . && git commit -m "..." && git push`). O Actions gera um novo APK automaticamente a cada push.
