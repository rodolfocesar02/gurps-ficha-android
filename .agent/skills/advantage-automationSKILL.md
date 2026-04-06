---
name: advantage-automation
description: Guia técnico para automatizar vantagens de forma modular no GURPS Ficha Android.
---

# 🤖 Automação Modular de Vantagens (Trait Rules)

Para manter o código limpo e evitar que arquivos de regras globais cresçam demais, usamos o padrão **Modular Trait Rules**. Toda vantagem com lógica complexa deve ter seu próprio arquivo em `domain/rules/traits/`.

## 🚀 Como Criar uma Nova Automação

### 1. Implementar a Interface `TraitRule`
Crie `[NomeDaVantagem]Rule.kt`. Abaixo estão os métodos disponíveis na interface atual para atingir 100% de automação:

```kotlin
class MinhaVantagemRule : TraitRule {
    override val traitId: String = "id_no_json" // Ex: "mestre_de_armas"

    // 💰 Custo customizado (se não for fixo ou por nível padrão)
    override fun calculateCost(selection: VantagemSelecionada, modifiers: List<ModificadorSelecao>): Int? = null

    // ⚔️ Adicionar novo Ataque (NH) na Aba de Rolagem (Ex: Ataque Inato, Mordida)
    override fun getAttackOptions(personagem: Personagem, selection: VantagemSelecionada): List<RollMappedOption> = emptyList()

    // 🛡️ Adicionar nova Defesa (Apara/Bloqueio) (Ex: Garras Longas permitindo Apara)
    override fun getDefenseOptions(personagem: Personagem, selection: VantagemSelecionada): List<ActiveDefense> = emptyList()

    // 💥 Adicionar nova Fonte de Dano (Ex: Dano de Mordida, Sopro)
    override fun getDamageOptions(personagem: Personagem, selection: VantagemSelecionada): List<DamageSourceOption> = emptyList()

    // 🏃 Modificadores de Defesa (Esquiva, Bloqueio, Apara)
    override fun getDodgeModifier(p: Personagem, s: VantagemSelecionada): Int = 0
    override fun getBlockModifier(p: Personagem, s: VantagemSelecionada): Int = 0
    override fun getParryModifier(p: Personagem, s: VantagemSelecionada, periciaId: String?): Int = 0

    // 🎓 Bônus em Perícias Existentes (Ex: +1 em Karatê por ter Golpeadores)
    override fun getSkillModifiers(p: Personagem, s: VantagemSelecionada): Map<String, Int> = emptyMap()

    // 🔨 Bônus de Dano por Dado (Ex: Mestre de Armas)
    // periciaId é a perícia selecionada no botão de Ataque da UI.
    override fun getDamageBonusPerDie(p: Personagem, s: VantagemSelecionada, periciaId: String?, weaponName: String?, armaGrupo: String?): Int = 0
}
```

### 2. Registro Obrigatório
Adicione a nova instância no `TraitRuleRegistry.kt`. Sem isso, o motor de regras não encontrará sua automação.

### 3. Sincronização com o JSON
A automação só funciona se o `definicaoId` da vantagem no `vantagens.json` for **exatamente igual** ao `traitId` definido na classe Kotlin.

---

## ⚠️ Regras de Ouro
1. **Contexto de Perícia:** Sempre use o `periciaId` fornecido nos métodos de bônus para garantir que o bônus só se aplique à arma/estilo selecionado pelo usuário na UI.
2. **Modularização:** Nunca coloque lógica de uma vantagem específica dentro da `TabRolagem.kt`. Use `getAttackOptions` para injetar a UI dinamicamente.
3. **Escudo/BD:** Se a vantagem interage com o Bônus de Defesa do escudo, certifique-se de usar os métodos do `CombatRules` ou `Personagem` para obter o valor atualizado.
