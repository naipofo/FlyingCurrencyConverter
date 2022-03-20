import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    id("org.jetbrains.compose") version "1.2.0-alpha01-dev620"
}

group = "com.example.flyingcurrency"
version = "1.0"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

@OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
dependencies {
    api(compose.runtime)
    api(compose.foundation)
    api(compose.material3)
    api(compose.materialIconsExtended)

    implementation("org.jetbrains.compose.material:material-icons-extended-desktop:1.2.0-alpha01-dev620")
    implementation("org.jetbrains.compose.material:material-icons-core-desktop:1.2.0-alpha01-dev620")

    // Ktor - api access
    val ktorVersion = "2.0.0-beta-1"
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")

    // dependency injection
    val kodeinVersion = "7.10.0"
    implementation("org.kodein.di:kodein-di:$kodeinVersion")
    implementation("org.kodein.di:kodein-di-framework-compose:$kodeinVersion")

    testImplementation(kotlin("test"))

    // has to be runtime or else there will be material 2 code completion
    runtimeOnly(compose.desktop.currentOs)
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "FlyingCurrencyConverter"
            packageVersion = "1.0.0"
        }
    }
}