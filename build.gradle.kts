import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") // version "2.3.10"
    id("org.jetbrains.compose") version "1.7.1" // NO! 1.9.3 NoSuchMethodError: SkiaLayer.<init>
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.10"
    kotlin("plugin.serialization") // version "2.3.10"
    id("com.github.ben-manes.versions") // version "0.53.0"
}

val appVersion = project.properties["lsc_version"]?.toString() ?: "1.0.0"
logger.info("lsc_version=[$appVersion]")
version = appVersion

group = "com.github.seepick.localsportsclub"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.3.10") // enforce version for Exposed NoSuchMethodError

    implementation(project(":repo"))
    implementation(project(":domain"))
    implementation(project(":view"))
    implementation(Deps.uscClient)

    // VIEW
    implementation(compose.desktop.currentOs)
    implementation(compose.components.resources)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4") // NO! 2.9.6 UnsatisfiedLinkError
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.2") // when "Module with the Main dispatcher is missing"
    implementation("net.coobird:thumbnailator:0.4.21") // resize images

    implementation(Deps.koin.compose)
    implementation(Deps.koin.composeViewmodel)

    // PERSISTENCE
    implementation(Deps.database.exposed.core)
    implementation(Deps.database.exposed.dao) // TODO delete this?
    implementation(Deps.database.exposed.jdbc)
    implementation(Deps.database.exposed.javaTime)
    implementation(Deps.database.sqliteJdbc)
    implementation(Deps.database.liquibase.core)
    implementation(Deps.database.liquibase.slf4j)

    // WEB
    implementation("org.jsoup:jsoup:1.22.1")
    val ktorVersion = "3.4.0"
    listOf(
        "client-core",
        "client-apache5", // 'cio' and 'java' engines had some networking issues...
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

    implementation(Deps.logging.kotlin)
    implementation(Deps.logging.logback)

    // TEST
    testImplementation(compose.desktop.uiTestJUnit4)
    testImplementation(Deps.testing.kotest.runnerJunit5)
    testImplementation(Deps.testing.kotest.assertionsCore)
    testImplementation(Deps.testing.kotest.property)
    testImplementation("org.junit.vintage:junit-vintage-engine:6.0.3") // to run JUnit4 with JUnit5
    testImplementation(Deps.ktor.client.mock)
    testImplementation(Deps.testing.mockk)
    testImplementation(Deps.koin.test)
    testImplementation("app.cash.turbine:turbine:1.2.1") // testing flows
//    testImplementation(testFixtures("com.github.seepick:usc-client:$versionUscClient")) // doesn't work; jitpack?!
}

//subprojects {
//    apply(plugin = "org.jetbrains.kotlin.jvm")
//
//}


kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

compose.desktop {
    // https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-native-distribution.html
    application {
        mainClass = Constants.Fqn.mainClass
        jvmArgs += listOf("-Xmx1G", "--add-exports", "java.desktop/com.apple.eawt=ALL-UNNAMED")
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Exe)
            packageName = "LocalSportsClub"
            packageVersion = appVersion
//            appResourcesRootDir.set(project.layout.projectDirectory.dir("src/main/distribution"))
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
