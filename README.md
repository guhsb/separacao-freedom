# Separação Freedom — App Android + Web

Painel de separação de pedidos do Grupo Freedom. O mesmo código roda como **site**
e como **app Android** (empacotado com [Capacitor](https://capacitorjs.com/)).

Os dados ficam no **Supabase** (PostgreSQL), sincronizados em tempo real entre todos
os aparelhos. O app precisa de internet para funcionar.

## Estrutura do projeto

- `www/index.html` — o app inteiro (HTML/CSS/JS). É o único arquivo que você precisa
  editar para mudar o funcionamento.
- `index.html` (raiz) — cópia do arquivo acima, usada pela hospedagem do site.
- `android/` — projeto nativo gerado pelo Capacitor (normalmente não precisa mexer).
- `keystore/separacao-freedom.jks` — chave de assinatura fixa. **Não apague**: é ela que
  permite que uma atualização instale por cima da versão anterior no celular.
- `.github/workflows/build-apk.yml` — compila o APK na nuvem a cada push.
- `supabase-*.sql` — funções que precisam existir no banco (veja abaixo).

## Configuração do Supabase

### Funções SQL

Rode cada arquivo `.sql` uma vez no **SQL Editor** do Supabase:

| Arquivo | Para que serve |
|---|---|
| `supabase-reserva-transacional.sql` | Impede que dois separadores iniciem o mesmo pedido ao mesmo tempo |
| `supabase-horario-servidor.sql` | Sincroniza o relógio do app com o do servidor (horário de Brasília correto) |
| `supabase-compactar-pedidos.sql` | Remove a lista de itens de pedidos finalizados há mais de 2 dias, para não inchar o banco |
| `supabase-publicar-versao-apk.sql` | Usada pelo GitHub Actions para avisar o app que saiu versão nova |

### Tabelas

Cada tabela tem sempre as colunas `id` (text, chave primária), `dados` (jsonb) e
`atualizado_em` (timestamptz):

`usuarios`, `pedidos`, `pedidos_catalogo`, `separadores`, `redes_config`,
`itens_bloqueados`, `clientes_rede`, `produtos_barcode`, `config`

### Realtime

Ative o Realtime nas tabelas que precisam de atualização instantânea entre aparelhos
(principalmente `pedidos`, `pedidos_catalogo` e `usuarios`).

### Segurança (importante)

A chave publicável fica visível no código — isso é normal e esperado. A proteção real
vem das **RLS policies** de cada tabela. Confirme no painel que elas estão configuradas,
senão qualquer pessoa com o endereço do site consegue ler e escrever no banco.

## Secrets do GitHub

Em **Settings → Secrets and variables → Actions**, cadastre:

- `SUPABASE_URL` — endereço do projeto (ex: `https://xxxx.supabase.co`)
- `SUPABASE_SERVICE_ROLE_KEY` — chave *service role* (**nunca** coloque essa no código do app)

Sem esses secrets o APK ainda compila; só o aviso de "nova versão disponível" deixa de funcionar.

## Gerando o APK

O GitHub Actions compila sozinho a cada `git push`. Leva de 3 a 6 minutos.

Para baixar, acesse a aba **Releases** do repositório — a versão mais recente traz o
`app-debug.apk` anexado, pronto para instalar no celular.

Se aparecer aviso de "instalar de fontes desconhecidas", autorize: é normal para apps
que não vêm da Play Store.

### Compilando localmente (opcional)

Precisa de Node.js, JDK 21 e Android SDK:

```bash
npm install
npx cap sync android
cd android && ./gradlew assembleDebug
```

O APK sai em `android/app/build/outputs/apk/debug/app-debug.apk`.

## Fazendo alterações

Edite `www/index.html`, copie para a raiz (`cp www/index.html index.html`) e suba:

```bash
git add .
git commit -m "descrição da mudança"
git push
```

O Actions gera o APK novo e o site é atualizado automaticamente.

## Permissões dos usuários

O acesso é por login único. Cada pessoa recebe permissões marcadas na **Área Master**:

| Permissão | Libera |
|---|---|
| Separação | Separar e bipar pedidos |
| Relatório | Relatório por período, com exportação |
| Busca | Buscar pedido por número |
| Análises | Ranking entre separadores |
| Dashboard | Visão de liderança |
| Área de Pedidos | Importar CSV, bloqueios e prioridades |
| Master | Acesso total, incluindo cadastro de usuários |
