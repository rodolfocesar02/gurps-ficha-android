# Atualizacao Remota do APK (sem Play Store)

## 1. Como funciona
1. O app consulta um arquivo JSON remoto (`update.json`).
2. Se `versionCode` remoto for maior que o local, o app mostra que existe atualização.
3. Ao confirmar, o app abre o link do APK da variante atual (Visual ou PraCego).

## 2. Formato do update.json
Use o arquivo [update.example.json](./update.example.json) como base.

Campos obrigatórios:
- `versionCode` (inteiro)
- `versionName` (texto)

Campos recomendados:
- `visualApkUrl`
- `pracegoApkUrl`
- `notes`

## 3. Publicacao de cada nova versao
1. Gere os APKs (`Visual` e `PraCego`).
2. Suba os APKs em um link direto (ex.: GitHub Releases).
3. Atualize o `update.json` com novo `versionCode`, `versionName` e URLs.
4. Publique o `update.json` em URL pública HTTPS.
5. Envie no grupo WhatsApp apenas o aviso com o link de release.

## 4. Configuração no projeto
Defina a URL do `update.json` em `local.properties`:

```properties
UPDATE_METADATA_URL=https://seu-dominio-ou-github/update.json
```

Opcionalmente, pode definir por `-PUPDATE_METADATA_URL=...` no build.

## 5. Checklist operacional (o que voce precisa fazer)
1. Gerar os APKs novos.
2. Publicar os APKs em um release com link direto (GitHub Releases recomendado).
3. Atualizar e publicar o `update.json` com os novos links e `versionCode`.
4. Validar no app: menu `Verificar atualização`.
5. Compartilhar no WhatsApp o link do release (evite reenviar APK antigo manualmente).
