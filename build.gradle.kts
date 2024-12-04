import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.1.0"
    id("org.jetbrains.compose") version "1.7.1"
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
    val ktorVersion = "3.0.1"

    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.0.0") // enforce version for Exposed NoSuchMethodError

    // DEPENDENCY INJECTION - https://insert-koin.io/docs/reference/koin-compose/compose
    listOf("compose", "compose-viewmodel", "compose-viewmodel-navigation").forEach {
        implementation("io.insert-koin:koin-$it:4.0.0")
    }

    // PERSISTENCE
    listOf("core", "dao", "jdbc", "java-time").forEach {
        implementation("org.jetbrains.exposed:exposed-$it:0.56.0")
    }
    implementation("com.h2database:h2:2.3.232")
    implementation("org.liquibase:liquibase-core:4.30.0")
    implementation("com.mattbertolini:liquibase-slf4j:5.1.0")

    // WEB
    listOf("client-core", "client-cio", "client-logging", "client-content-negotiation", "serialization-kotlinx-json").forEach {
        implementation("io.ktor:ktor-$it:$ktorVersion")
    }
    implementation("org.jsoup:jsoup:1.18.3")

    // LOGGING
    implementation("io.github.oshai:kotlin-logging:7.0.3")
    implementation("ch.qos.logback:logback-classic:1.5.12")

    // TEST
    testImplementation(compose.desktop.uiTestJUnit4)
    listOf("runner-junit5", "assertions-core", "property").forEach {
        testImplementation("io.kotest:kotest-$it:5.9.1")
    }
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    testImplementation("io.mockk:mockk:1.13.13")
}

compose.desktop {
    application {
        mainClass = "seepick.localsportsclub.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "LocalSportsClub"
            packageVersion = "1.0.0"
        }
    }
}

tasks.withType<Test>().configureEach { // to be able to run kotests
    useJUnitPlatform()
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

tasks.withType<DependencyUpdatesTask> {
    val rejectPatterns = listOf(".*-ea.*", ".*RC", ".*M1", ".*check", ".*dev.*", ".*[Bb]eta.*", ".*[Aa]lpha.*").map { Regex(it) }
    rejectVersionIf {
        rejectPatterns.any {
            it.matches(candidate.version)
        }
    }
}

inline fun <reified C> Project.configure(name: String, configuration: C.() -> Unit) {
    (this.tasks.getByName(name) as C).configuration()
}
