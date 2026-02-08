package seepick.localsportsclub

import java.util.Properties

data class AppProperties(
    val version: String,
)

private fun Properties.getString(key: String): String = getProperty(key) ?: error("Property [$key] is missing!")

object AppPropertiesProvider {
    private val cached by lazy {
        val props = Properties()
        props.load(openFromClasspath("/lsc.properties"))
        AppProperties(
            version = props.getString("version"),
        )
    }

    fun provide() = cached
}
