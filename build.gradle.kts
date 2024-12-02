import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.1.0"
    id("org.jetbrains.compose") version "1.7.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    id("com.github.ben-manes.versions") version "0.51.0"
}

group = "com.github.seepick.localsportsclub"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)

    implementation("io.github.oshai:kotlin-logging:7.0.3")
    implementation("ch.qos.logback:logback-classic:1.5.12")

    // https://insert-koin.io/docs/reference/koin-compose/compose
    implementation("io.insert-koin:koin-compose:4.0.0")
    implementation("io.insert-koin:koin-compose-viewmodel:4.0.0")
    implementation("io.insert-koin:koin-compose-viewmodel-navigation:4.0.0")

    listOf(
        "client-core",
        "client-cio",
        "client-logging",
        "client-content-negotiation",
        "serialization-kotlinx-json"
    ).forEach {
        implementation("io.ktor:ktor-$it:3.0.1")
    }
    implementation("org.jsoup:jsoup:1.18.3")

    testImplementation(compose.desktop.uiTestJUnit4)
    listOf("runner-junit5", "assertions-core", "property").forEach {
        testImplementation("io.kotest:kotest-$it:5.9.1")
    }
    testImplementation("io.mockk:mockk:1.13.13")
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
    val rejectPatterns = listOf(".*-ea.*", ".*RC", ".*M1", ".*[Bb]eta.*", ".*[Aa]lpha.*").map { Regex(it) }
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
