---
description: Regras sobre testes sistemáticos via comandos para garantir que o Android Build continue funcionando.
---
# Ferramentas e Regras de Teste Obrigatórias

## Por que Testar?
Como a base de código (Android) tem múltiplos arquivos se interligando, quebrar uma dependência entre uma interface numa Aba e a base de rolagens na outra gerará os temidos "Build Errors". 

## Sua Obrigação como Agente
Você está TERMINANTEMENTE PROIBIDO de avançar ou finalizar a conversa de uma alteração considerável sem antes confirmar que o código compila.

## Como Executar Ferramentas de Saúde
Quando concluir ou achar que concluiu uma modificação de código no projeto, chame a ferramenta de execução e use:

```bash
# Navegue para o diretório raiz do aplicativo android
./gradlew build --continue
```
> [!NOTE]
> Se houver muitos arquivos, você pode focar nos que dão mais erros de sintaxe (como testes ou UI) rodando `./gradlew test` (se o projeto tiver JUnit integrado).

Se algum erro de Build surgir, **você é o responsável e deve corrigi-lo IMEDIATAMENTE** antes de avisar o Usuário ou sugerir o fim do trabalho. O emulador depende que o build passe.

### Testando Regras de RPG (Cálculos Matemáticos)
Para garantimos que a interface não interfira nas regras de GURPS, toda vez que trabalhar na separação ou edição das *Calculadoras do Sistema* (Atributos, Cargas e PV), você deve OBRIGATORIAMENTE executar os testes locais (`Testes Unitários de Integração Matemática`).
No Android com Kotlin, ao criar as calculadoras exija uma base de teste rápido na pasta `app/src/test/` e dispare:
```bash
./gradlew testDebugUnitTest
```
Se a *Matemática do Jogo* quebrar (ex: HT12 resultar em Vida errada), aborte o commit! Mantenha as contas 100% blindadas antes de avisar o usuário.

### Outras Ferramentas na Pasta `scripts/`
A versão do projeto para desktop possui arquivos Python de extração de magias ou ajustes (como o `get_schools.py`). Fique atento à `list_dir` nesses locais se precisar de apoio para lidar com dados de *ficha* que vêm dos formatos JSON das tabelas do GURPS.
