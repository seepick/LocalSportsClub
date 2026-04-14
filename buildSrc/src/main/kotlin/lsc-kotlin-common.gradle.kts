plugins {
    // declaring plugins not possible via version catalog
    // buildSrc/src/main/kotlin NOT available during runtime (misleading as available while writing)
    kotlin("jvm")
    id("java-test-fixtures")
//    id("lsc-versions")
//    id("lsc-detekt")
}

dependencies {
    implementation(Deps.logging.kotlin)
    implementation(Deps.koin.core)

    testImplementation(Deps.testing.kotest.assertionsCore)
    testImplementation(Deps.testing.kotest.property)
    testFixturesApi(Deps.testing.kotest.property)
    // koin-test?
    // mockk?
}


kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xjsr305=strict", // annotations for defect detection
        )
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(Versions.java)
    }
}
