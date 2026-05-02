# 🛠️ Arsenal do Mestre IA - GURPS Ficha Android

Este documento define a transição da **Mestre IA** de um sistema de RAG Passivo para um **Agente de Investigação Ativo**. A IA agora terá ferramentas para "folhear" o manual e pesquisar dados técnicos sob demanda.

## 🎯 Objetivo
Dar autonomia à IA para decidir quais partes do Códex (assets) ela precisa ler para responder com 100% de precisão, reduzindo alucinações e otimizando o uso do Context Token.

---

## 🛠️ Catálogo de Ferramentas (Arsenal do Agente)

### 1. Pesquisa Ativa (Busca no Códex)

#### `consultar_grafo_regras(query: String, pagina: Int)`
- **Função:** Busca híbrida (Grafo + Chunks). É o ponto de entrada para dúvidas genéricas.
- **Exemplo:** `consultar_grafo_regras("Como funciona o recuo?")`

#### `buscar_por_id(entity_id: String)`
- **Função:** Recupera o conteúdo integral de um nó específico do Grafo usando seu ID único.
- **Uso:** Quando a IA vê uma referência como `[Veja: regra_colisao]` e precisa do detalhe exato.
- **Exemplo:** `buscar_por_id("regra_dano_por_colisao")`

#### `ler_pagina_manual(livro: String, pagina: Int)`
- **Função:** Recupera o texto integral de uma página específica do banco de Chunks.
- **Uso:** Investigação documental profunda quando o resumo do grafo é insuficiente.

---

### 2. Inteligência Contextual e Formatação

#### `analisar_ficha_atual()`
- **Função:** Injeta o JSON completo do personagem atual no prompt da iteração.
- **Uso:** Responder perguntas específicas sobre os atributos ou perícias do usuário.

#### `renderizar_tabela(dados_brutos: String)`
- **Função:** Transforma dados técnicos brutos em tabelas Markdown limpas.
- **Uso:** Exibir tabelas de armas, dano ou modificadores de forma legível.

---

### 3. Motores Especializados

#### `consultar_nexus_arcano(magia_alvo: String)`
- **Função:** Cálculo determinístico de pré-requisitos de magias.
- **Uso:** Sempre que o assunto for planejamento de feitiços.

---

## 💻 Implementação Técnica (Kotlin)

Para que o Agente funcione, o código Android deve seguir este padrão de "Dispatcher":

### 1. Definição do Schema (MestreIATools.kt)
```kotlin
// Schema para a ferramenta buscar_por_id
JSONObject().apply {
    put("name", "buscar_por_id")
    put("description", "Busca uma regra específica pelo seu ID único. Use quando citarem [Veja: ID].")
    put("parameters", JSONObject().apply {
        put("type", "OBJECT")
        put("properties", JSONObject().apply {
            put("entity_id", JSONObject().put("type", "STRING").put("description", "Ex: regra_colisao"))
        })
        put("required", JSONArray().put("entity_id"))
    })
}
```

### 2. Lógica de Execução (MestreIAUseCase.kt)
```kotlin
if (resposta.toolCalls.isNotEmpty()) {
    val call = resposta.toolCalls[0]
    when (call.name) {
        "buscar_por_id" -> {
            val id = call.args.getString("entity_id")
            val node = repository.buscarResumoNode(id)
            
            // Injeta o resultado no contexto para a PRÓXIMA iteração da IA
            catalogoDinamico = catalogoDinamico.copy(
                ponteDeFerro = catalogoDinamico.ponteDeFerro + "\n\n[INVESTIGAÇÃO]: O nó '$id' diz: ${node?.summary}"
            )
        }
        "analisar_ficha_atual" -> {
            val fichaJson = viewModel.personagem.toJson()
            catalogoDinamico = catalogoDinamico.copy(
                ponteDeFerro = catalogoDinamico.ponteDeFerro + "\n\n[CONTEXTO PERSONAGEM]: $fichaJson"
            )
        }
    }
}
```

---

## 📋 Tabela de Referência para Comandos

| Ferramenta | Comando (IA) | Ação (App) |
| :--- | :--- | :--- |
| **Pesquisa** | `consultar_grafo_regras` | `graphEngine.buscarNoGrafo()` |
| **ID Direto** | `buscar_por_id` | `repository.buscarResumoNode()` |
| **Página** | `ler_pagina_manual` | `repository.buscarPorPagina()` |
| **Nexus** | `consultar_nexus_arcano` | `nexusEngine.formatarGabarito()` |
| **Ficha** | `analisar_ficha_atual` | Injeta `personagem.toJson()` |

---
> [!NOTE]
> Estas definições garantem que a IA saiba exatamente quais "botões" ela pode apertar durante a investigação das regras, eliminando a necessidade de alucinar conteúdos que não estão no contexto imediato.
