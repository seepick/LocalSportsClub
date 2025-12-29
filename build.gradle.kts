import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.3.0"
    id("org.jetbrains.compose") version "1.7.1"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    id("com.github.ben-manes.versions") version "0.51.0"
}

val appVersion = project.properties["lsc_version"]?.toString() ?: "1.0.0"
println("Gradle appVersion=[$appVersion]")
version = appVersion

group = "com.github.seepick.localsportsclub"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    val ktorVersion = "3.0.1"

    implementation(compose.desktop.currentOs)
    implementation(compose.components.resources)
    implementation(compose.material3)
    implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.0.0") // enforce version for Exposed NoSuchMethodError
//    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.10.0")
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.0") // when "Module with the Main dispatcher is missing"
//    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")
    implementation("net.coobird:thumbnailator:0.4.20") // resize images
    // NO, as this will break compose (?!). enable, then "inline" the code by copy'n'paste into DuplicateIcons.kt
//    implementation("androidx.compose.material:material-icons-extended:1.7.6")

    // DEPENDENCY INJECTION - https://insert-koin.io/docs/reference/koin-compose/compose
    val versionKoin = "4.0.2"
    listOf("compose", "compose-viewmodel").forEach {
        implementation("io.insert-koin:koin-$it:$versionKoin")
    }

    // PERSISTENCE
    listOf("core", "dao", "jdbc", "java-time").forEach {
        implementation("org.jetbrains.exposed:exposed-$it:0.56.0")
    }
    implementation("org.xerial:sqlite-jdbc:3.48.0.0")
    implementation("org.liquibase:liquibase-core:4.31.1")
    implementation("com.mattbertolini:liquibase-slf4j:5.1.0")

    // WEB
    listOf(
        "client-core",
        // JVM engines: java, apache, jetty, okhttp, cio
        // 'cio' had some issues... 'java' too: SocketException
        "client-apache",
        "client-logging",
        "client-content-negotiation",
        "serialization-kotlinx-json"
    ).forEach {
        implementation("io.ktor:ktor-$it:$ktorVersion")
    }
    implementation("org.jsoup:jsoup:1.18.3")

    // GCAL
    implementation("com.google.api-client:google-api-client:2.0.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.34.1")
    implementation("com.google.apis:google-api-services-calendar:v3-rev20220715-2.0.0")

    // LOGGING
    implementation("io.github.oshai:kotlin-logging:7.0.3")
    implementation("ch.qos.logback:logback-classic:1.5.12")

    // TEST
    testImplementation(compose.desktop.uiTestJUnit4)
    listOf("runner-junit5-jvm", "assertions-core", "property").forEach {
        testImplementation("io.kotest:kotest-$it:5.9.1")
    }
    testImplementation("org.junit.vintage:junit-vintage-engine:5.11.4") // to run JUnit4 with JUnit5
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("io.insert-koin:koin-test:$versionKoin")
    testImplementation("app.cash.turbine:turbine:1.2.0") // testing flows
}

//tasks.withType<KotlinCompile> {
//    kotlinOptions.jvmTarget = "16"
//}

compose.desktop {
    // https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-native-distribution.html
    application {
        mainClass = "seepick.localsportsclub.LocalSportsClub"
        jvmArgs += listOf("-Dlsc.env=PROD", "-Xmx1G", "--add-exports", "java.desktop/com.apple.eawt=ALL-UNNAMED")
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Exe)
            packageName = "LocalSportsClub"
            packageVersion = appVersion
            modules(
                "java.net.http",
                "java.sql",
                "java.naming", // for ktor-client-apache
                "jdk.httpserver", // for com/sun/net/httpserver/HttpHandler for GCal (google jetty OAuth2)
            )
            macOS {
                iconFile.set(project.file("src/main/distribution/icon.icns"))
            }
        }
    }
}

tasks.withType<Test>().configureEach { // to be able to run kotests
//    useJUnit()
    useJUnitPlatform()
}

configure<ProcessResources>("processResources") {
    from("src/main/resources") {
        include("lsc.properties")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        filter<ReplaceTokens>(
            "tokens" to mapOf(
                "version" to appVersion,
                "gcalClientId" to (project.properties["lsc_gcalClientId"] ?: "!MISSING!"),
                "gcalClientSecret" to (project.properties["lsc_gcalClientSecret"] ?: "!MISSING!"),
            ),
        )
    }
}

tasks.withType<DependencyUpdatesTask> {
    val rejectPatterns =
        listOf(".*-ea.*", ".*RC", ".*M1", ".*check", ".*dev.*", ".*[Bb]eta.*", ".*[Aa]lpha.*").map { Regex(it) }
    rejectVersionIf {
        rejectPatterns.any {
            it.matches(candidate.version)
        }
    }
}

inline fun <reified C> Project.configure(name: String, configuration: C.() -> Unit) {
    (this.tasks.getByName(name) as C).configuration()
}
