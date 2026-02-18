package seepick.localsportsclub

import com.github.seepick.uscclient.baseUrl
import com.github.seepick.uscclient.model.UscLang
import seepick.localsportsclub.service.DirectoryEntry
import seepick.localsportsclub.service.FileResolver
import seepick.localsportsclub.sync.SyncMode
import java.io.File
import java.net.URL
import java.time.LocalDate

data class LscConfig(
    val versionCheckEnabled: Boolean = true,
    val database: DatabaseMode,
    val gcal: GcalMode,
    val logbackFileEnabled: Boolean = false,

    val sync: SyncMode,
    val syncDaysAhead: Int = 14, // including today

    val currentYear: Int = LocalDate.now().year,
    val responseLogFolder: File?,
    val apiMode: ApiMode,
    val apiLang: UscLang = UscLang.English,
    val baseUrl: URL = apiLang.baseUrl,
) {

    init {
        require(syncDaysAhead >= 1) { "sync days ahead must be >= 1 but was: $syncDaysAhead" }
    }

    companion object {
        val development = LscConfig(
//            api = ApiMode.Mock,
            apiMode = ApiMode.RealHttp,

//            sync = SyncMode.Dummy,
            sync = SyncMode.Real,
//            sync = SyncMode.Noop,
//            sync = SyncMode.Delayed,

//            gcal = GcalMode.Real,
            gcal = GcalMode.Noop,

            versionCheckEnabled = false,
            database = DatabaseMode.Exposed,
//            database = DatabaseMode.InMemory,
            logbackFileEnabled = false,
            responseLogFolder = File("build/api-logs-development"),
        )
        val production = LscConfig(
            database = DatabaseMode.Exposed,
            apiMode = ApiMode.RealHttp,
            gcal = GcalMode.Real,
            sync = SyncMode.Real,
            logbackFileEnabled = true,
            responseLogFolder = FileResolver.resolve(DirectoryEntry.ApiLogs),
        )

        val downloadImageSize = 400 to 400
    }
}

enum class DatabaseMode {
    Exposed, InMemory
}


enum class GcalMode {
    Noop, Real
}

enum class ApiMode {
    Mock, RealHttp
}
