package seepick.localsportsclub

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.view.common.showErrorDialog
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
        try {
            listeners.forEach(ApplicationLifecycleListener::onExit)
        } catch (e: Exception) {
            log.error(e) { "Error during exiting." }
            showErrorDialog("Failed to exit the application.", e)
        }
    }

    fun attachMacosQuitHandler() {
        if (System.getProperty("os.name") != "Mac OS X") {
            log.debug { "Not attaching quit handler (only supported under MacOS but running '${System.getProperty("os.name")}')" }
            return
        }
        log.debug { "Attaching MacOS quit handler reflectively." }
        val awtApplication = Class.forName("com.apple.eawt.Application")
        val setQuitHandler = awtApplication.methods.first { it.name == "setQuitHandler" }
        val application = try {
            awtApplication.methods.first { it.name == "getApplication" }.invoke(null)
        } catch (e: IllegalAccessException) {
            throw Exception("Ensure passed the following to the JVM: --add-exports java.desktop/com.apple.eawt=ALL-UNNAMED")
        }
        setQuitHandler.invoke(application, QuitHandler { _, response ->
            log.info { "Quit invoked." }
            onExit()
            response.performQuit()
        })
    }
}
