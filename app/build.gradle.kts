import org.gradle.api.tasks.PathSensitivity
import java.util.Properties
import org.gradle.api.tasks.Exec

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
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
        applicationId = "com.gurps.ficha"
        minSdk = 24
        targetSdk = 35
        versionCode = 212
        versionName = "12.9.12-ESTADOS-NO-ATAQUE"
        buildConfigField("String", "DISCORD_ROLL_API_BASE_URL", "\"$discordApiBaseUrl\"")
        buildConfigField("String", "DISCORD_ROLL_API_KEY", "\"$discordApiKey\"")
        buildConfigField(
            "Boolean",
            "MODO_ALVO_NEXUS_HABILITADO",
            "Boolean.valueOf(${modoAlvoNexusHabilitado.toString()})"
        )
        buildConfigField("String", "UPDATE_METADATA_URL", "\"$updateMetadataUrl\"")
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
        // no servico de voz, que saiu no lote SEM-IA) como dívida documentada — o build falha só em erro NOVO.
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

    // 🔴 Lote POD-1. Vários testes deste projeto varrem os ASSETS de verdade
    // (armas, poderes, texto do catálogo). O Gradle não sabia disso: mexer num
    // .json e rodar o portão dava VERDE, porque a tarefa de teste era dada como
    // atualizada -- ela só olhava código.
    //
    // Descoberto na sonda do POD-1: estraguei um valor no `poderes.v1.json` de
    // propósito e o teste passou. Com `--rerun-tasks`, reprovava. Ou seja, as
    // varreduras de catálogo estavam dando confiança falsa.
    inputs.dir("src/main/assets")
        .withPropertyName("assetsDoCatalogo")
        .withPathSensitivity(PathSensitivity.RELATIVE)
}
