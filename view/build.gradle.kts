plugins {
    id("lsc-kotlin-common")
}

dependencies {
    implementation(project(":domain"))

    testImplementation(testFixtures(project(":domain")))
}
