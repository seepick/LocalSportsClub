plugins {
    id("lsc-kotlin-common")
}

dependencies {
    implementation(project(":repo"))

    testImplementation(testFixtures(project(":repo")))
}
