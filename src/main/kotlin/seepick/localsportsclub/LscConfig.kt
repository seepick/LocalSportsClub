package seepick.localsportsclub

import com.github.seepick.uscclient.baseUrl
import com.github.seepick.uscclient.model.UscLang
import java.io.File
import java.net.URL
import java.time.LocalDate

data class LscConfig(
    val database: DatabaseMode,
    val sync: SyncMode,
    val syncDaysAhead: Int = 14, // including today
    val logFileEnabled: Boolean = false,
    val gcal: GcalMode,
    val versionCheckEnabled: Boolean = true,
    val currentYear: Int = LocalDate.now().year,
    val responseLogFolder: File? = null, // FIXME enable File(),
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
            logFileEnabled = false,
        )
        val production = LscConfig(
            database = DatabaseMode.Exposed,
            apiMode = ApiMode.RealHttp,
            gcal = GcalMode.Real,
            sync = SyncMode.Real,
            logFileEnabled = true,
        )

        val downloadImageSize = 400 to 400
    }
}

enum class DatabaseMode {
    Exposed, InMemory
}

enum class SyncMode {
    Noop, Delayed, Dummy, Real
}

enum class GcalMode {
    Noop, Real
}

enum class ApiMode {
    Mock, RealHttp
}
