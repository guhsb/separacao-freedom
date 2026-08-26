#!/usr/bin/env bash
# Configura de qual endereço o APK vai carregar o app.
#
# Depois de rodar isso uma vez e subir para o GitHub, todas as atualizações
# chegam sozinhas nos celulares — não precisa mais instalar APK a cada mudança.
#
# Uso:
#   ./configurar-site.sh https://meu-site.exemplo.com

set -e

URL="$1"

if [ -z "$URL" ]; then
  echo "Faltou o endereço do site."
  echo "Exemplo: ./configurar-site.sh https://separacao-freedom.vercel.app"
  exit 1
fi

# Tira a barra do final, se tiver
URL="${URL%/}"

case "$URL" in
  https://*) ;;
  *)
    echo "O endereço precisa começar com https:// (o Android bloqueia http comum)."
    exit 1
    ;;
esac

python3 - "$URL" <<'PY'
import json, sys

url = sys.argv[1]
with open('capacitor.config.json', encoding='utf-8') as f:
    config = json.load(f)

config.setdefault('server', {})
config['server']['url'] = url
config['server']['androidScheme'] = 'https'
config['server']['cleartext'] = False
config['server']['errorPath'] = 'index.html'

with open('capacitor.config.json', 'w', encoding='utf-8') as f:
    json.dump(config, f, indent=2, ensure_ascii=False)
    f.write('\n')

print(f'APK configurado para carregar de: {url}')
PY

echo
echo "Agora suba para o GitHub para gerar o APK novo:"
echo "  git add ."
echo "  git commit -m \"apk passa a carregar o app do site\""
echo "  git push"
echo
echo "Essa é a ÚLTIMA vez que todo mundo precisa instalar o APK."
echo "Depois disso, é só dar git push que as atualizações chegam sozinhas."
