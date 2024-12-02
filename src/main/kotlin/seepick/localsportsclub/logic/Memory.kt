package seepick.localsportsclub.logic

interface Memory {
    val sessionId: String
}
class MemoryReadWrite : Memory {
    override var sessionId: String = ""
}