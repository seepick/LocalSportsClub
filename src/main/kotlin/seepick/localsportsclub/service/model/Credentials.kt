package seepick.localsportsclub.service.model

@Deprecated("use usc-client")
data class Credentials(
    val username: String,
    val password: String,
) {
    override fun toString() = "Credentials[username=$username, password=***]"
}

