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
    compileSdk = 34

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
        targetSdk = 34
        versionCode = 2
        versionName = "1.1"
        buildConfigField("String", "DISCORD_ROLL_API_BASE_URL", "\"$discordApiBaseUrl\"")
        buildConfigField("String", "DISCORD_ROLL_API_KEY", "\"$discordApiKey\"")
        buildConfigField(
            "Boolean",
            "MODO_ALVO_NEXUS_HABILITADO",
            "Boolean.valueOf(${modoAlvoNexusHabilitado.toString()})"
        )
        buildConfigField("String", "UPDATE_METADATA_URL", "\"$updateMetadataUrl\"")
        buildConfigField("String", "GURPS_AGENT_API_BASE_URL", "\"$gurpsAgentApiBaseUrl\"")
        buildConfigField("String", "VTT_API_BASE_URL", "\"$vttApiBaseUrl\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    flavorDimensions += "ui"
    productFlavors {
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
    }
    sourceSets {
        getByName("main") {
            kotlin.srcDir("../motor modo alvo/src")
        }
    }
}

val validateActiveJsonAssets by tasks.registering(Exec::class) {
    group = "verification"
    description = "Valida JSONs ativos do app antes do build."
    workingDir = rootProject.projectDir
    commandLine("python", "scripts/audit_active_jsons_v2.py", "--fail-on-issues")
}

tasks.matching { it.name == "preBuild" }.configureEach {
    dependsOn(validateActiveJsonAssets)
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
