---
name: android-jetpack-compose-expert
description: "Expert guidance for building modern Android UIs with Jetpack Compose, covering state management, navigation, performance, and Material Design 3."
risk: safe
source: community
date_added: "2026-02-27"
---

# Android Jetpack Compose Expert

Android Jetpack Compose Expert
Overview
A comprehensive guide for building production-quality Android applications using Jetpack Compose. This skill covers architectural patterns, state management with ViewModels, navigation type-safety, and performance optimization techniques.

When to Use This Skill
Use when starting a new Android project with Jetpack Compose.
Use when migrating legacy XML layouts to Compose.
Use when implementing complex UI state management and side effects.
Use when optimizing Compose performance (recomposition counts, stability).
Use when setting up Navigation with type safety.
Step-by-Step Guide
1. Project Setup & Dependencies
As dependencias do Jetpack Compose estão configuradas diretamente no seu `app/build.gradle.kts`. Certifique-se de usar o BOM (Bill of Materials) para garantir compatibilidade entre versões.

```kotlin
dependencies {
    // Importa o Compose BOM
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))

    // Adiciona as bibliotecas sem especificar versões (serão herdadas do BOM)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.8.1")
}
```
2. State Management Pattern (MVI/MVVM)
Use ViewModel with StateFlow to expose UI state. Avoid exposing MutableStateFlow.

// UI State Definition
data class UserUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null
)

// ViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    fun loadUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val user = userRepository.getUser()
                _uiState.update { it.copy(user = user, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}
3. Creating the Screen Composable
Consume the state in a "Screen" composable and pass data down to stateless components.

@Composable
fun UserScreen(
    viewModel: UserViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    UserContent(
        uiState = uiState,
        onRetry = viewModel::loadUser
    )
}

@Composable
fun UserContent(
    uiState: UserUiState,
    onRetry: () -> Unit
) {
    Scaffold { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                uiState.isLoading -> CircularProgressIndicator()
                uiState.error != null -> ErrorView(uiState.error, onRetry)
                uiState.user != null -> UserProfile(uiState.user)
            }
        }
    }
}
Examples
Example 1: Type-Safe Navigation
Using the new Navigation Compose Type Safety (available in recent versions).

// Define Destinations
@Serializable
object Home

@Serializable
data class Profile(val userId: String)

// Setup NavHost
@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = Home) {
        composable<Home> {
            HomeScreen(onNavigateToProfile = { id ->
                navController.navigate(Profile(userId = id))
            })
        }
        composable<Profile> { backStackEntry ->
            val profile: Profile = backStackEntry.toRoute()
            ProfileScreen(userId = profile.userId)
        }
    }
}
Best Practices
✅ Do: Use remember and derivedStateOf to minimize unnecessary calculations during recomposition.
✅ Do: Mark data classes used in UI state as @Immutable or @Stable if they contain List or other unstable types to enable smart recomposition skipping.
✅ Do: Use LaunchedEffect for one-off side effects (like showing a Snackbar) triggered by state changes.
❌ Don't: Perform expensive operations (like sorting a list) directly inside the Composable function body without remember.
❌ Don't: Pass ViewModel instances down to child components. Pass only the data (state) and lambda callbacks (events).
Troubleshooting
Problem: Infinite Recomposition loop. Solution: Check if you are creating new object instances (like List or Modifier) inside the composition without remember, or if you are updating state inside the composition phase instead of a side-effect or callback. Use Layout Inspector to debug recomposition counts.