plugins {
    id("lsc-kotlin-common")
}

dependencies {
    api(project(":domain-model"))
    implementation(project(":domain-repo"))

//    testImplementation(testFixtures(project(":repo")))
}
