import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.0.21"
    id("org.jetbrains.compose") version "1.7.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    kotlin("plugin.serialization") version "1.9.0"
    id("com.github.ben-manes.versions") version "0.47.0"
}

group = "com.github.christophpickl"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)

    implementation("io.github.oshai:kotlin-logging:5.1.0")
    implementation("ch.qos.logback:logback-classic:1.4.11")

    fun ktor(artifact: String) = "io.ktor:ktor-$artifact:2.3.3"
    listOf(
        "client-core",
        "client-cio",
        "client-logging",
        "client-content-negotiation",
        "serialization-kotlinx-json"
    ).forEach {
        implementation(ktor(it))
    }
    implementation("org.jsoup:jsoup:1.16.1")

    testImplementation(compose.desktop.uiTestJUnit4)
    testImplementation("io.kotest:kotest-runner-junit5:5.6.2")
    testImplementation("io.kotest:kotest-assertions-core:5.6.2")
    testImplementation("io.kotest:kotest-property:5.6.2")
    testImplementation("io.mockk:mockk:1.13.5")
}

compose.desktop {
    application {
        mainClass = "com.github.christophpickl.localsportsclub.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "LocalSportsClub"
            packageVersion = "1.0.0"
        }
    }
}

tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    val rejectPatterns = listOf(".*-ea.*", ".*RC", ".*[Bb]eta.*", ".*[Aa]lpha.*").map { Regex(it) }
    rejectVersionIf {
        rejectPatterns.any {
            it.matches(candidate.version)
        }
    }
}

configure<ProcessResources>("processResources") {
    from("src/main/resources") {
        include("lsc.properties")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        filter<ReplaceTokens>(
            "tokens" to mapOf(
                "version" to (project.properties["lsc.version"] ?: "0"),
            ),
        )
    }
}

inline fun <reified C> Project.configure(name: String, configuration: C.() -> Unit) {
    (this.tasks.getByName(name) as C).configuration()
}
