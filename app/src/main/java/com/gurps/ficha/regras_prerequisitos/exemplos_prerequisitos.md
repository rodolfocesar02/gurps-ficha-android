# Exemplos de Pré-requisitos de Magias

Este documento lista exemplos reais de textos extraídos das definições de magia
(por exemplo, do arquivo `magias2versao.json`) e como o parser atual irá
interpretá-los.

| Texto original | Resultado esperado | Observações |
|----------------|-------------------|------------|
| `""` ou `—` | nenhum requisito | vazio |
| `IQ 12+` | `AttributeMin("IQ",12)` | atributo mínimo 
| `AM2` | `AttributeMin("AM",2)` | atributo mágico tratado como `AttributeMin`
| `2 mágicas de Fogo` | `MagiasEscola(2, "Fogo")` | comprimento de formulário geral
| `6 mágicas de Ácido` | `MagiasEscola(6, "Ácido")` | caracteres acentuados
| `Persuasão ou a vantagem Empatia com Animais` | duas entradas `MagiaConhecida` | exemplo com `ou`
| `Retardo` | `MagiaConhecida("Retardo")` | nome único sem número
| `AM1, Apressar, IQ 12+` | lista mista dos três tipos | combinação com vírgula
| `Hora Certa` | `MagiaConhecida("Hora Certa")` | texto com espaço
| `4 mágicas sobre Plantas ou Percepção do Perigo` | tratado como dois tokens separados devido a "ou" | ainda gera `MagiaConhecida` para segunda parte |
| `AM2` | `AptidaoMagica(2)` | forma compacta |
| `am 2` | `AptidaoMagica(2)` | espaços e minúsculas |
| `AM+3` | `AptidaoMagica(3)` | sinal + opcional |
| `Aptidão Mágica 1` | `AptidaoMagica(1)` | forma por extenso |
| `aptidao_magica 2` | `AptidaoMagica(2)` | underscore e minúsculas |
| `IQ 12 e AM2` | `AttributeMin("IQ",12)` + `AptidaoMagica(2)` | combinação |
| `2 mágicas de Fogo e AM 1` | `MagiasEscola` + `AptidaoMagica(1)` | conjunção |
| `nenhum` | nenhum requisito | texto genérico |
| `—` | nenhum requisito | traço em campo |
| `AM 2 ou Criar Fogo` | `AptidaoMagica(2)` + `MagiaConhecida("Criar Fogo")` | alternância |

> **Notas:**
> - O parser já distingue a vantagem Aptidão Mágica e converte para o tipo
>   especial `AptidaoMagica`. Variantes de escrita são aceitas.
> - Caso surjam novas formas estranhas, basta estender as regex acima e
>   acrescentar exemplos aqui para documentação.

Este material auxilia no desenvolvimento de novas regras e validações. Quando
outras variações de texto forem detectadas, basta atualizar o parser e
acrescentar novos exemplos aqui para documentação.
