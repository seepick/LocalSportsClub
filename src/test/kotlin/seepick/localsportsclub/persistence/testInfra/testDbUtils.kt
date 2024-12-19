package seepick.localsportsclub.persistence.testInfra

fun buildTestJdbcUrl(namePrefix: String): String =
    "jdbc:h2:mem:$namePrefix-${System.currentTimeMillis()};DB_CLOSE_DELAY=-1"
