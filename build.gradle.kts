import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.3.10"
    id("org.jetbrains.compose") version "1.7.1" // NO! 1.9.3 NoSuchMethodError: SkiaLayer.<init>
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.10"
    kotlin("plugin.serialization") version "2.3.10"
    id("com.github.ben-manes.versions") version "0.53.0"
}

val appVersion = project.properties["lsc_version"]?.toString() ?: "1.0.0"
println("Gradle appVersion=[$appVersion]")
version = appVersion

group = "com.github.seepick.localsportsclub"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.3.10") // enforce version for Exposed NoSuchMethodError

//    val versionUscClient = "2000.0.SNAPSHOT"
    val versionUscClient = "2026.2.4"
    implementation("com.github.seepick:usc-client:$versionUscClient")

    // VIEW
    implementation(compose.desktop.currentOs)
    implementation(compose.components.resources)
    implementation(compose.material3)
    implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4") // NO! 2.9.6 UnsatisfiedLinkError
//    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.10.0")
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.2") // when "Module with the Main dispatcher is missing"
//    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")
    // NO, as this will break compose (?!). enable, then "inline" the code by copy'n'paste into DuplicateIcons.kt
//    implementation("androidx.compose.material:material-icons-extended:1.7.6")
    implementation("net.coobird:thumbnailator:0.4.21") // resize images

    // DEPENDENCY INJECTION - https://insert-koin.io/docs/reference/koin-compose/compose
    val versionKoin = "4.0.2" // NO! 4.1.1 UnsatisfiedLinkError
    listOf("compose", "compose-viewmodel").forEach {
        implementation("io.insert-koin:koin-$it:$versionKoin")
    }

    // PERSISTENCE
    listOf("core", "dao", "jdbc", "java-time").forEach {
        implementation("org.jetbrains.exposed:exposed-$it:1.0.0")
    }
    implementation("org.xerial:sqlite-jdbc:3.51.2.0")
    implementation("org.liquibase:liquibase-core:5.0.1")
    implementation("com.mattbertolini:liquibase-slf4j:5.1.0")

    // WEB
    implementation("org.jsoup:jsoup:1.22.1")
    val ktorVersion = "3.4.0"
    listOf(
        "client-core",
        // 'cio' and 'java' engines had some networking issues...
        "client-apache",
        "client-logging",
        "client-content-negotiation",
        "serialization-kotlinx-json"
    ).forEach {
        implementation("io.ktor:ktor-$it:$ktorVersion")
    }

    // GCAL
    implementation("com.google.api-client:google-api-client:2.8.1")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.39.0")
    implementation("com.google.apis:google-api-services-calendar:v3-rev20251207-2.0.0")

    // LOGGING
    implementation("io.github.oshai:kotlin-logging:8.0.01")
    implementation("ch.qos.logback:logback-classic:1.5.32")

    // TEST
    testImplementation(compose.desktop.uiTestJUnit4)
    listOf("runner-junit5-jvm", "assertions-core", "property").forEach {
        testImplementation("io.kotest:kotest-$it:6.1.3")
    }
    testImplementation("org.junit.vintage:junit-vintage-engine:6.0.3") // to run JUnit4 with JUnit5
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    testImplementation("io.mockk:mockk:1.14.9")
    testImplementation("io.insert-koin:koin-test:$versionKoin")
    testImplementation("app.cash.turbine:turbine:1.2.1") // testing flows
//    testImplementation(testFixtures("com.github.seepick:usc-client:$versionUscClient")) // doesn't work; jitpack?!
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
            ),
        )
    }
}

tasks.withType<DependencyUpdatesTask> {
    val rejectPatterns =
        listOf(
            ".*-ea.*",
            ".*RC",
            ".*rc.*",
            ".*M1",
            ".*check",
            ".*dev.*",
            ".*[Bb]eta.*",
            ".*[Aa]lpha.*",
            ".*SNAPSHOT.*"
        ).map { Regex(it) }
    rejectVersionIf {
        rejectPatterns.any {
            it.matches(candidate.version)
        }
    }
}

inline fun <reified C> Project.configure(name: String, configuration: C.() -> Unit) {
    (this.tasks.getByName(name) as C).configuration()
}
