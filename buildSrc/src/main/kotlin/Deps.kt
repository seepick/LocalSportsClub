@Suppress("MayBeConstant", "unused", "ClassName", "ClassOrdering")
object Deps {

    val uscClient = "com.github.seepick:usc-client:${Versions.uscClient}"
    /*
        implementation("org.jetbrains.kotlin:kotlin-reflect:2.3.10") // enforce version for Exposed NoSuchMethodError


        // VIEW
        implementation(compose.desktop.currentOs)
        implementation(compose.components.resources)
        implementation(compose.material3)
        implementation(compose.materialIconsExtended)
        implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4") // NO! 2.9.6 UnsatisfiedLinkError
        runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.2") // when "Module with the Main dispatcher is missing"
        implementation("net.coobird:thumbnailator:0.4.21") // resize images

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
     */
    val serializationx = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0"

    object database {
        val sqliteJdbc = "org.xerial:sqlite-jdbc:3.51.2.0"

        object liquibase {
            val core = "org.liquibase:liquibase-core:5.0.1"
            val slf4j = "com.mattbertolini:liquibase-slf4j:5.1.0"
        }

        object exposed {
            private fun make(artifact: String) = "org.jetbrains.exposed:exposed-$artifact:${Versions.exposed}"

            val core = make("core")
            val dao = make("dao")
            val jdbc = make("jdbc")
            val javaTime = make("java-time")
        }
    }

    object logging {
        val kotlin = "io.github.oshai:kotlin-logging-jvm:${Versions.logging.kotlin}"
        val logback = "ch.qos.logback:logback-classic:${Versions.logging.logback}"
    }

    object hoplite {
        @Suppress("SameParameterValue")
        private fun make(artifact: String) = "com.sksamuel.hoplite:hoplite-$artifact:${Versions.hoplite}"

        val core = make("core")
//        val yaml = make("yaml") NO!
    }

    object ktor {
        private fun make(artifact: String) = "io.ktor:ktor-$artifact:${Versions.ktor}"

        val serialization = make("serialization-kotlinx-json")
        val io = make("io-jvm")

        object server {
            val testHost = make("server-test-host")
            val core = make("server-core")
            val netty = make("server-netty")
            val contentNegotiation = make("server-content-negotiation")
            val hostCommon = make("server-host-common")
            val statusPages = make("server-status-pages")
        }

        object client {
            val core = make("client-core")
            val cio = make("client-cio")
            val logging = make("client-logging")
            val mock = make("client-mock")
            val contentNegotiation = make("client-content-negotiation")
        }
    }

    /** https://insert-koin.io/docs/reference/koin-compose/compose */
    object koin {
        private fun make(artifact: String) = "io.insert-koin:koin-$artifact:${Versions.koin}"

        val core = make("core")
        val compose = make("compose")
        val composeViewmodel = make("compose-viewmodel")
        val ktor = make("ktor")
        val test = make("test")
        val logger = make("logger-slf4j")
    }

    object testing {
        val mockk = "io.mockk:mockk:${Versions.mockk}"
        val wiremock = "org.wiremock:wiremock:3.13.2"
        val jsonPath = "com.jayway.jsonpath:json-path:2.10.0"
        val jsonPathHamcrestAssert = "com.jayway.jsonpath:json-path-assert:2.10.0" // hamcrest matchers?!
        val hamcrest = "org.hamcrest:hamcrest:3.0"
        val jsonAssert = "org.skyscreamer:jsonassert:1.5.3"

        object junit {
            val platformSuite = "org.junit.platform:junit-platform-suite:${Versions.testing.junit}"
            val jupiter = "org.junit.jupiter:junit-jupiter:${Versions.testing.junit}"
            val jupiterApi = "org.junit.jupiter:junit-jupiter-api:${Versions.testing.junit}"
        }

        object kotest {
            private fun make(artifact: String) = "io.kotest:kotest-$artifact:${Versions.testing.kotest}"

            val frameworkEngine = make("framework-engine")
            val runnerJunit5 = make("runner-junit5-jvm")
            val assertionsCore = make("assertions-core")
            val property = make("property")
        }

        object testcontainers {
            private fun make(suffix: String) = "org.testcontainers:testcontainers$suffix:${Versions.testcontainers}"

            val main = make("")
        }
    }

    object pluginIds {
        val manesVersion = "com.github.ben-manes.versions"
    }
}
