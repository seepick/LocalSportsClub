package seepick.localsportsclub.gcal

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import java.io.File
import java.io.Reader
import java.io.StringReader
import java.util.Properties

object GcalCredentialsLoader {

    private val log = logger {}

    fun buildReader(
        credentialsJsonFile: File, // = fileResolver.resolve(FileEntry.GoogleCredentials)
        gcalCredsPropFile: File, //  = fileResolver.resolve(FileEntry.GoogleCalendarCredsProperties)
    ): Reader = if (credentialsJsonFile.exists()) {
        log.debug { "Reading GCal credentials from local file: ${credentialsJsonFile.absolutePath}" }
        credentialsJsonFile.inputStream().reader()
    } else {
        val gcalApiConfig = RuntimeGcalConfigLoader.load(gcalCredsPropFile)
        StringReader(buildJson(clientId = gcalApiConfig.clientId, clientSecret = gcalApiConfig.clientSecret))
    }

    private fun buildJson(clientId: String, clientSecret: String) = """
        {
          "installed": {
            "client_id": "$clientId",
            "project_id": "localsportsclub",
            "auth_uri": "https://accounts.google.com/o/oauth2/auth",
            "token_uri": "https://oauth2.googleapis.com/token",
            "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
            "client_secret": "$clientSecret",
            "redirect_uris": [
              "http://localhost"
            ]
          }
        }
    """.trimIndent()
}

object RuntimeGcalConfigLoader {
    private val log = logger {}
    fun load(gcalCredsPropFile: File): RuntimeGcalConfig {
        require(gcalCredsPropFile.exists()) {
            "GCal credentials file not found at: ${gcalCredsPropFile.absolutePath}! Please create it."
        }
        log.debug { "Loading GCal credentials file: ${gcalCredsPropFile.absolutePath}" }
        val props = Properties()
        props.load(gcalCredsPropFile.inputStream())
        return RuntimeGcalConfig(
            clientId = props.getProperty("clientId"),
            clientSecret = props.getProperty("clientSecret"),
        )
    }
}

data class RuntimeGcalConfig(
    val clientId: String,
    val clientSecret: String,
)
