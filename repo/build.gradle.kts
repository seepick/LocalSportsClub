plugins {
    id("lsc-kotlin-common")
}

dependencies {
    implementation(Deps.database.exposed.core)
    implementation(Deps.database.exposed.jdbc)
    implementation(Deps.database.exposed.javaTime)
    implementation(Deps.database.sqliteJdbc)
    implementation(Deps.database.liquibase.core)
    implementation(Deps.database.liquibase.slf4j)

    testFixturesImplementation(Deps.database.exposed.jdbc)
}
