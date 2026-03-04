# Lote 04 - Smoke Test no Emulador (NEXUS ARCANO)

## Pré-condição
- Emulador Android iniciado.
- `adb` disponível no PATH do terminal.

## 1) Build/Install com modo alvo novo ligado
```powershell
./gradlew :app:installVisualDebug -PMODO_ALVO_NEXUS_HABILITADO=true
```

## 2) Abrir app no emulador
- Pacote esperado (flavor visual): `com.gurps.ficha.visual`

```powershell
adb shell monkey -p com.gurps.ficha.visual -c android.intent.category.LAUNCHER 1
```

## 3) Roteiro rápido de validação
1. Abrir aba Magias.
2. Ativar `Modo Alvo`.
3. Buscar `desejo` e definir como alvo.
4. Confirmar exibição de:
- `3 próximas ações`;
- `chaves faltantes`.
5. Adicionar 1 magia recomendada.
6. Validar recálculo sem congelamento da tela.

## 4) Critérios de aceite (Lote 4)
- Não travar UI ao abrir diálogo de magias.
- Atualização da recomendação em tempo curto após adicionar magia.
- Estado coerente após fechar/abrir o diálogo novamente.
- Sem blocos pretos ou ANR ao navegar entre abas.

## 5) Comando de rollback rápido
```powershell
./gradlew :app:installVisualDebug -PMODO_ALVO_NEXUS_HABILITADO=false
```
