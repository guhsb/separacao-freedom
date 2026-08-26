#!/usr/bin/env bash
# Volta o APK a rodar a copia embutida (como era antes), em vez de carregar do site.
# Use se o Supabase nao permitir autorizar a origem do GitHub Pages.
#
# Uso:  ./reverter-para-embutido.sh

set -e

python3 - <<'PY'
import json

with open('capacitor.config.json', encoding='utf-8') as f:
    config = json.load(f)

server = config.get('server', {})
# Remove a origem externa; mantem apenas o esquema
config['server'] = {'androidScheme': server.get('androidScheme', 'https')}

with open('capacitor.config.json', 'w', encoding='utf-8') as f:
    json.dump(config, f, indent=2, ensure_ascii=False)
    f.write('\n')

print('APK voltou a rodar a copia embutida.')
PY

echo
echo "Suba para gerar o APK novo:"
echo "  git add . && git commit -m \"apk volta a rodar embutido\" && git push"
echo
echo "Atencao: com isso, cada atualizacao volta a exigir instalar o APK."
echo "O site continua funcionando normalmente pelo navegador."
