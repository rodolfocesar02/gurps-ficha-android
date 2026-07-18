# GURPS Ficha - Android App

Aplicativo de ficha editavel para GURPS 4a Edicao em portugues brasileiro.

## Mapa da documentacao

| Onde | O que tem |
|---|---|
| **[docs/pendencias/PENDENCIAS.md](docs/pendencias/PENDENCIAS.md)** | ⭐ **Comece aqui.** O que falta, o que e narrado por projeto e o que ja esta pronto mas nao validado. |
| [docs/pendencias/](docs/pendencias/) | Fronteiras honestas da magia e a auditoria de schema. |
| [docs/planos/](docs/planos/) | Planos por pilar: Magia, Combate Tatico, Tokens VTT, Automacao de Vantagens. |
| [docs/fonte-regras/](docs/fonte-regras/) | Transcricao do livro (Combate, indice do Modulo Basico) — a **fonte** contra a qual as regras sao implementadas. |
| [PROGRESS.md](PROGRESS.md) | Diario de bordo: um registro por lote, do mais novo ao mais antigo. |
| `.agent/skills/` | Planos e regras operacionais do agente de IA. |

> **Antes de reportar um bug de magia**, confira o PENDENCIAS.md: **9 em cada 10 magias sao narradas
> por projeto**, nao por falta de implementacao. O logcat (`tag:Saga_Combate`) diz qual e o caso.

## Funcionalidades

### Atributos Primarios
- **ST (Forca)**: ±10 pontos/nivel
- **DX (Destreza)**: ±20 pontos/nivel
- **IQ (Inteligencia)**: ±20 pontos/nivel
- **HT (Vitalidade)**: ±10 pontos/nivel

### Atributos Secundarios (calculados automaticamente)
- **PV (Pontos de Vida)**: Base ST, ±2 pts/nivel
- **Vontade**: Base IQ, ±5 pts/nivel
- **Percepcao**: Base IQ, ±5 pts/nivel
- **PF (Pontos de Fadiga)**: Base HT, ±3 pts/nivel
- **Velocidade Basica**: (HT + DX) / 4
- **Deslocamento Basico**: Velocidade Basica (sem fracoes)
- **Esquiva**: Velocidade Basica + 3
- **Base de Carga**: (ST x ST) / 10 kg
- **Dano GdP/GeB**: Calculado pela tabela oficial

### Tracos
- Vantagens com custo em pontos
- Desvantagens com custo negativo e autocontrole
- Peculiaridades (-1 pt cada)

### Pericias
- Atributo base configuravel (ST, DX, IQ, HT, Per, Von)
- Dificuldade (Facil, Media, Dificil, Muito Dificil)
- Calculo automatico do nivel

### Equipamentos
- Controle de peso e custo
- Calculo de carga (Sem Carga ate Extra Pesada)
- Ajuste automatico de Deslocamento e Esquiva por carga

### Persistencia
- Salvar/Carregar multiplas fichas
- Dados salvos no dispositivo

## Como Compilar

1. Abra o projeto no Android Studio
2. Sincronize o Gradle
3. Execute no emulador ou dispositivo fisico

## Requisitos
- Android SDK 24+ (Android 7.0)
- Kotlin 1.9+
- Jetpack Compose
