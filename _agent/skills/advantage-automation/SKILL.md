---
name: advantage-automation
description: Guia para automatizar novas vantagens de forma modular no GURPS Ficha Android.
---

# 🤖 Automação Modular de Vantagens

Para evitar que o arquivo `CharacterRules.kt` ou `TabRolagem.kt` cresçam indefinidamente, usamos o padrão **Modular Trait Rules**.

## 🚀 Como Adicionar uma Nova Automação

Se você for automatizar uma vantagem (ex: *Amplitute de Combate* ou *Visão Noturna*), siga estes passos:

### 1. Criar o Arquivo de Regra
Crie um novo arquivo em `app/src/main/java/com/gurps/ficha/domain/rules/traits/` com o nome `[NomeDaVantagem]Rule.kt`.

Exemplo:
```kotlin
class MinhaVantagemRule : TraitRule {
    override val traitId: String = "id_no_json"

    // Opcional: Se o custo depender de lógica complexa (Metadados)
    override fun calculateCost(selection: VantagemSelecionada, modifiers: List<ModificadorSelecao>): Int? {
        return null // ou o calculo
    }

    // Opcional: Se adicionar uma linha de NH na Aba de Rolagem
    override fun getAttackOptions(...) : List<RollMappedOption> { ... }

    // Opcional: Se adicionar Aparar/Bloqueio
    override fun getDefenseOptions(...) : List<ActiveDefense> { ... }
    
    // Opcional: Se adicionar uma opção de Dano
    override fun getDamageOptions(...) : List<DamageSourceOption> { ... }
}
```

### 2. Registrar a Regra
Abra o arquivo `TraitRuleRegistry.kt` e adicione sua regra no bloco `init`:

```diff
init {
    register(AtaqueInatoRule())
    register(GolpeadoresRule())
+   register(MinhaVantagemRule())
}
```

### 3. Verificar o JSON
Certifique-se de que no `vantagens.json` o campo `tipoCusto` está como `variavel` se você for usar diálogos de configuração customizados.

---

## ⚠️ Regras de Ouro
- **Não altere `TabRolagem.kt`** para adicionar ataques de vantagens. Use o método `getAttackOptions` na sua regra.
- **Não altere o `CharacterRules.kt`** para custos específicos. Use `calculateCost`.
- **Mantenha o fallback:** Se a vantagem não precisa de lógica (é apenas custo fixo), **NÃO** crie um arquivo de regra. Deixe o sistema usar o padrão.
