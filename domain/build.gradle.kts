plugins {
    id("lsc-kotlin-common")
}

// TODO delete domain thingy
dependencies {
    implementation(project(":repo"))

    testImplementation(testFixtures(project(":repo")))
}
