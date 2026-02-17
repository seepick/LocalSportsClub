package seepick.localsportsclub.sync

interface SyncProgress {
    fun register(listener: SyncProgressListener)
    fun start()
    fun stop(isError: Boolean)
    fun onProgress(step: String, subStep: String? = null)
}

interface SyncProgressListener {
    fun onSyncStart()
    fun onSyncStep(syncStep: SyncStep)
    fun onSyncFinish(isError: Boolean)
}

data class SyncStep(
    val section: String,
    val detail: String? = null,
)

class DummySyncProgress : SyncProgress {

    private val listeners = mutableListOf<SyncProgressListener>()

    override fun register(listener: SyncProgressListener) {
        listeners += listener
    }

    override fun start() {
        listeners.forEach(SyncProgressListener::onSyncStart)
    }

    override fun stop(isError: Boolean) {
        listeners.forEach {
            it.onSyncFinish(isError)
        }
    }

    override fun onProgress(step: String, subStep: String?) {
        listeners.forEach {
            it.onSyncStep(SyncStep(step, subStep))
        }
    }
}
