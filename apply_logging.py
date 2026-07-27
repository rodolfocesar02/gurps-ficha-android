import re

filepath = "app/src/main/java/com/gurps/ficha/viewmodel/FichaViewModel.kt"

with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

# Define patterns to replace.
# Examples:
# fun atualizarForca(v: Int) { personagem = attributeDelegate.atualizarForca(personagem, v) }
# -> 
# fun atualizarForca(v: Int) { 
#     val oldVal = personagem.forca
#     personagem = attributeDelegate.atualizarForca(personagem, v) 
#     if (oldVal != v) personagem = historyDelegate.registrarMudanca(personagem, "Alterou Força de $oldVal para $v")
# }

# Actually, it's easier to change the delegates directly if they have access to the old state, but delegates return a new Personagem.
# Another way is to just do a deep comparison if we have the old character!
# FichaViewModel is already doing `personagem = ...` everywhere.
# What if we intercept the setter of `personagem`? 
# In Kotlin, `var personagem by mutableStateOf(Personagem())` makes it a property delegate.
# We can't easily override the setter of a delegated property with custom logic unless we change it to a normal backing field with `mutableStateOf`.
# Let's change `var personagem` to have a custom setter!

# Wait, `mutableStateOf` is used:
# private val _personagem = mutableStateOf(Personagem())
# var personagem: Personagem
#     get() = _personagem.value
#     private set(value) {
#         val old = _personagem.value
#         // diff old and value
#         // apply diff to history
#         _personagem.value = value 
#     }

# This is GENIUS! If we just write a diffing function `diffPersonagens(old: Personagem, new: Personagem): Personagem`
# it will catch EVERY SINGLE MODIFICATION automatically, anywhere in the codebase!
