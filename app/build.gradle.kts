import java.util.Properties
import org.gradle.api.tasks.Exec

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("io.objectbox")
}

val localProperties = Properties().apply {
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) {
        localFile.inputStream().use { load(it) }
    }
}

fun firstNonBlank(vararg values: String?): String? {
    return values.firstOrNull { !it.isNullOrBlank() }?.trim()
}

android {
    namespace = "com.gurps.ficha"
    compileSdk = 35

    // FATOR PRIME: Chaves Mascaradas Multi-Flavor (Lidas no topo para visibilidade global)
    val geminiKey = (localProperties.getProperty("mestre.ia.gemini.key") ?: "").replace("\"", "\\\"")
    val gemini1Key = (localProperties.getProperty("mestre.ia.gemini1.key") ?: "").replace("\"", "\\\"")
    val deepseekKey = (localProperties.getProperty("mestre.ia.deepseek.key") ?: "").replace("\"", "\\\"")
    val deepseek2Key = (localProperties.getProperty("mestre.ia.deepseek.2.key") ?: "").replace("\"", "\\\"")
    val openRouter1Key = (localProperties.getProperty("mestre.ia.openrouter.1.key") ?: "").replace("\"", "\\\"")
    val openRouter2Key = (localProperties.getProperty("mestre.ia.openrouter.2.key") ?: "").replace("\"", "\\\"")
    val nvidiaKey = (localProperties.getProperty("mestre.ia.nvidia.key") ?: "").replace("\"", "\\\"")
    val mimoKey = (localProperties.getProperty("mestre.ia.xiaomi.key") ?: "").replace("\"", "\\\"")

    defaultConfig {
        val discordApiBaseUrl = (firstNonBlank(
            project.findProperty("DISCORD_ROLL_API_BASE_URL") as? String,
            localProperties.getProperty("DISCORD_ROLL_API_BASE_URL")
        ) ?: "http://10.0.2.2:8787")
            .trimEnd('/')
            .replace("\"", "\\\"")
        val discordApiKey = (firstNonBlank(
            project.findProperty("DISCORD_ROLL_API_KEY") as? String,
            localProperties.getProperty("DISCORD_ROLL_API_KEY")
        ) ?: "")
            .replace("\"", "\\\"")
        val modoAlvoNexusHabilitado = (firstNonBlank(
            project.findProperty("MODO_ALVO_NEXUS_HABILITADO") as? String,
            localProperties.getProperty("MODO_ALVO_NEXUS_HABILITADO")
        ) ?: "true")
            .lowercase()
            .let { it == "1" || it == "true" || it == "yes" || it == "on" }
        val updateMetadataUrl = (firstNonBlank(
            project.findProperty("UPDATE_METADATA_URL") as? String,
            localProperties.getProperty("UPDATE_METADATA_URL")
        ) ?: "")
            .trim()
            .replace("\"", "\\\"")
        val gurpsAgentApiBaseUrl = (firstNonBlank(
            project.findProperty("GURPS_AGENT_API_BASE_URL") as? String,
            localProperties.getProperty("GURPS_AGENT_API_BASE_URL")
        ) ?: "http://10.0.2.2:8787")
            .trimEnd('/')
            .replace("\"", "\\\"")
        val vttApiBaseUrl = (firstNonBlank(
            project.findProperty("VTT_API_BASE_URL") as? String,
            localProperties.getProperty("VTT_API_BASE_URL")
        ) ?: "http://10.0.2.2:3001")
            .trimEnd('/')
            .replace("\"", "\\\"")


        applicationId = "com.gurps.ficha"
        minSdk = 24
        targetSdk = 35
        versionCode = 145
        versionName = "6.6-MB2"
        buildConfigField("String", "DISCORD_ROLL_API_BASE_URL", "\"$discordApiBaseUrl\"")
        buildConfigField("String", "DISCORD_ROLL_API_KEY", "\"$discordApiKey\"")
        buildConfigField(
            "Boolean",
            "MODO_ALVO_NEXUS_HABILITADO",
            "Boolean.valueOf(${modoAlvoNexusHabilitado.toString()})"
        )
        buildConfigField("String", "UPDATE_METADATA_URL", "\"$updateMetadataUrl\"")
        buildConfigField("String", "GURPS_AGENT_API_BASE_URL", "\"$gurpsAgentApiBaseUrl\"")
        // --- MESTRE IA PRIME (CONFIGURAÇÕES BLOQUEADAS: NÃO ALTERAR SEM TESTE DE CONEXÃO) ---
        // REGRA OPERACIONAL: Estas URLs e IDs foram validados via script. 
        // Proibida a alteração sem validação prévia de conectividade (Success 200).
        
        buildConfigField("String", "VTT_API_BASE_URL", "\"$vttApiBaseUrl\"")
        
        // --- MESTRE IA PRIME (CHAVES GLOBAIS) ---
        buildConfigField("String", "MESTRE_IA_GEMINI_KEY", "\"$geminiKey\"")
        buildConfigField("String", "MESTRE_IA_DEEPSEEK_KEY", "\"$deepseekKey\"")
        buildConfigField("String", "MESTRE_IA_DEEPSEEK_2_KEY", "\"$deepseek2Key\"")
        buildConfigField("String", "MESTRE_IA_OPENROUTER_1_KEY", "\"$openRouter1Key\"")
        buildConfigField("String", "MESTRE_IA_OPENROUTER_2_KEY", "\"$openRouter2Key\"")
        
        buildConfigField("String", "MESTRE_IA_OPENROUTER_URL", "\"https://openrouter.ai/api/v1\"")
        buildConfigField("String", "MESTRE_IA_OPENROUTER_MODEL_1", "\"meta-llama/llama-3.3-70b-instruct\"")

        buildConfigField("String", "MESTRE_IA_NVIDIA_KEY", "\"$nvidiaKey\"")
        buildConfigField("String", "MESTRE_IA_NVIDIA_URL", "\"https://integrate.api.nvidia.com/v1\"")
        buildConfigField("String", "MESTRE_IA_NVIDIA_MODEL", "\"meta/llama-3.3-70b-instruct\"")

        buildConfigField("String", "MESTRE_IA_MIMO_KEY", "\"$mimoKey\"")
        buildConfigField("String", "MESTRE_IA_MIMO_URL", "\"https://api.xiaomimimo.com/v1\"")
        buildConfigField("String", "MESTRE_IA_MIMO_MODEL_PRO", "\"mimo-v2.5-pro\"")
        buildConfigField("String", "MESTRE_IA_MIMO_MODEL_FLASH", "\"mimo-v2-flash\"")


        buildConfigField("String", "MESTRE_IA_GEMINI_IMAGE_KEY", "\"$gemini1Key\"")  // chave paga — imagem
        buildConfigField("String", "MESTRE_IA_GEMINI_IMAGE_MODEL", "\"gemini-3.1-flash-image\"") // $0.067/img 9:16
        buildConfigField("String", "MESTRE_IA_GEMINI_3_1_PRO", "\"gemini-3.1-pro-preview\"")     // confirmado API 05/2026
        buildConfigField("String", "MESTRE_IA_GEMINI_2_5_PRO", "\"gemini-2.5-pro\"")            // Forjador primário — estável com tool calls 50k+
        buildConfigField("String", "MESTRE_IA_GEMINI_3_FLASH", "\"gemini-2.5-flash\"")           // confirmado API 05/2026
        buildConfigField("String", "MESTRE_IA_GEMINI_3_1_FLASH_LITE", "\"gemini-2.5-flash-lite\"") // confirmado API 05/2026

        buildConfigField("String", "MESTRE_IA_LITE_1_KEY", "\"$geminiKey\"") // Legado/Flash
        buildConfigField("String", "MESTRE_IA_LITE_1_URL", "\"https://generativelanguage.googleapis.com/v1beta\"")
        buildConfigField("String", "MESTRE_IA_LITE_1_MODEL", "\"gemini-2.5-flash-lite\"")        // confirmado API 05/2026

        buildConfigField("String", "MESTRE_IA_DEEPSEEK_URL", "\"https://api.deepseek.com/v1\"")
        buildConfigField("String", "MESTRE_IA_DEEPSEEK_MODEL", "\"deepseek-v4-flash\"") // migrado: deepseek-chat deprecado em 24/07/2026
        buildConfigField("String", "MESTRE_IA_DEEPSEEK_MODEL_V3", "\"deepseek-v4-pro\"") // V4 Pro — MoE 1.6T params, mais capaz que Flash

        // Gemini Live (voz bidirecional) — migrado para 2.5 (Lote 3, 2026-05-26): async tools, proactive audio, vozes HD
        buildConfigField("String", "GEMINI_LIVE_MODEL", "\"models/gemini-2.5-flash-native-audio-preview-12-2025\"")
        buildConfigField("String", "GEMINI_LIVE_VOICE", "\"Charon\"")
        buildConfigField("Boolean", "VOZ_BIDIRECIONAL_HABILITADA", "true")

        // Legado / Valores Padrão (DeepSeek por padrão no MESTRE_IA_KEY)
        buildConfigField("String", "MESTRE_IA_KEY", "\"$deepseekKey\"") 
        buildConfigField("String", "MESTRE_IA_URL", "\"https://api.deepseek.com/v1\"")
        buildConfigField("String", "MESTRE_IA_MODEL", "\"deepseek-v4-flash\"") // migrado: deepseek-chat deprecado em 24/07/2026

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    flavorDimensions += listOf("ui")
    productFlavors {
        // --- DIMENSÃO: INTERFACE (UI) ---
        create("visual") {
            dimension = "ui"
            applicationIdSuffix = ".visual"
            versionNameSuffix = "-visual"
            buildConfigField("String", "UI_VARIANT", "\"visual\"")
            resValue("string", "app_name", "GURPS Ficha (Visual)")
        }
        create("pracego") {
            dimension = "ui"
            applicationIdSuffix = ".pracego"
            versionNameSuffix = "-pracego"
            buildConfigField("String", "UI_VARIANT", "\"pracego\"")
            resValue("string", "app_name", "GURPS Ficha (Pra Cego)")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // Gera APK release assinado com a chave debug por padrao.
            // Substituir por keystore de producao quando configurada.
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        // Lote 366 (B8): android.util.Log e afins retornam default em vez de lançar "not mocked"
        // nos testes JVM (permite testar o roteamento do NarradorToolExecutor, que loga).
        unitTests.isReturnDefaultValues = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    lint {
        // Workaround para bug do lint Compose (MutableCollectionMutableStateDetector)
        disable += "MutableCollectionMutableState"
        // Workaround para bug do lint Compose (AutoboxingStateCreationDetector)
        disable += "AutoboxingStateCreation"
        // Lote 384: detectores do compose-runtime lint quebram (IncompatibleClassChangeError —
        // "Found class KaSimpleVariableAccessCall, but interface was expected": incompatibilidade da
        // Kotlin Analysis API com a versão do lint) ao re-executar o lint, derrubando o build.
        // Workaround igual aos de cima — desliga só esses detectores. Sem efeito no código (não usamos Compose neles).
        disable += "NullSafeMutableLiveData"
        disable += "FrequentlyChangingValue"
        disable += "RememberInComposition"
        // Lote 351: baseline congela os 16 erros ANTIGOS de lint (ex.: MissingPermission
        // no GeminiLiveService) como dívida documentada — o build falha só em erro NOVO.
        // Para revisar a dívida: abrir lint-baseline.xml ou rodar gradlew updateLintBaseline.
        baseline = file("lint-baseline.xml")
    }
    sourceSets {
        getByName("main") {
            kotlin.srcDir("../motor modo alvo/src")
        }
    }
}

/*
val validateActiveJsonAssets by tasks.registering(Exec::class) {
    group = "verification"
    description = "Valida JSONs ativos do app antes do build."
    workingDir = rootProject.projectDir
    commandLine("python", "scripts/audit_active_jsons_v2.py", "--fail-on-issues")
}

tasks.matching { it.name == "preBuild" }.configureEach {
    dependsOn(validateActiveJsonAssets)
}
*/

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation(platform("androidx.compose:compose-bom:2024.09.02"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("com.google.mlkit:face-detection:16.1.7")
    implementation("com.google.android.gms:play-services-mlkit-subject-segmentation:16.0.0-beta1")
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    testImplementation("junit:junit:4.13.2")
    // org.json REAL nos testes unitários (o android.jar dos unit tests só tem stubs "not mocked")
    testImplementation("org.json:json:20240303")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.09.02"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Lote 1: Motor 3D Declarativo para Compose
    implementation("io.github.sceneview:sceneview:3.0.0")

    // Lote 2: Física Pura (JBullet portado pelo StephenGold)
    implementation("com.github.stephengold:jbullet:1.0.3")
    implementation("javax.vecmath:vecmath:1.5.2")
}

// Lote BUILD-1 (22/jul): os testes JVM rodavam num unico fork, em fila. Com 12 nucleos, dividir
// as CLASSES de teste entre varios JVMs e de graca. Metade dos nucleos deixa folga para o resto
// do build, que roda em paralelo agora (org.gradle.parallel no gradle.properties).
tasks.withType<Test>().configureEach {
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
}
