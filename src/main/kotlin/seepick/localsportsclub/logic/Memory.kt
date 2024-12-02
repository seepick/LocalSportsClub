package seepick.localsportsclub.logic

interface Service {
    fun say(): String
}
class ServiceImpl : Service {
    override fun say() = "Hello Service Impl."
}

interface Memory {
    val sessionId: String
}
class MemoryReadWrite : Memory {
    override var sessionId: String = ""
}