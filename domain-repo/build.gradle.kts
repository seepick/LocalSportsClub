plugins {
    id("lsc-kotlin-common")
}

dependencies {
    implementation(project(":repo")) // hide it!
    api(project(":domain-model"))

//    testImplementation(testFixtures(project(":repo")))
}
