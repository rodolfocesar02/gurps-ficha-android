# Smoke Check (manual)

Caso: Desejo

Entrada mínima esperada:
- alvoId = desejo
- conhecidas = {}

Saída esperada (ordem hard-first):
1. ações focadas em liberar Encantar (escolas)
2. após Encantar aprendido, sugerir Pequeno Desejo
3. após Pequeno Desejo, completar escolas para 15
4. sugerir Desejo quando liberado

Checklist:
- não sugerir magia já aprendida
- no máximo 3 ações por rodada
- sem repetir escola enquanto houver escola nova
