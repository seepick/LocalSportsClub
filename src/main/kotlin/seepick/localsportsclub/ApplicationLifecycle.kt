package seepick.localsportsclub

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import java.awt.desktop.QuitHandler

interface ApplicationLifecycleListener {
    fun onStartUp() {}
    fun onExit() {}
}

class ApplicationLifecycle {

    private val log = logger {}
    private val listeners = mutableListOf<ApplicationLifecycleListener>()

    fun registerListener(listener: ApplicationLifecycleListener) {
        listeners += listener
    }

    fun onStartUp() {
        log.info { "onStartUp" }
        listeners.forEach(ApplicationLifecycleListener::onStartUp)
    }

    fun onExit() {
        log.info { "onExit" }
        listeners.forEach(ApplicationLifecycleListener::onExit)
    }

    fun attachQuitHandler() {
        val awtApplication = Class.forName("com.apple.eawt.Application")
        val setQuitHandler = awtApplication.methods.first { it.name == "setQuitHandler" }
        val application = try {
            awtApplication.methods.first { it.name == "getApplication" }.invoke(null)
        } catch (e: IllegalAccessException) {
            throw Exception("Ensure passed the following to the JVM: --add-exports java.desktop/com.apple.eawt=ALL-UNNAMED")
        }
        setQuitHandler.invoke(application, QuitHandler { e, response ->
            onExit()
            response.performQuit()
        })
    }
}
