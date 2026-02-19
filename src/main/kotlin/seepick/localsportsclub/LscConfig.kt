package seepick.localsportsclub

import com.github.seepick.uscclient.baseUrl
import com.github.seepick.uscclient.model.UscLang
import seepick.localsportsclub.service.FileResolver
import seepick.localsportsclub.service.FileResolverImpl
import seepick.localsportsclub.sync.SyncMode
import java.io.File
import java.net.URL
import java.time.LocalDate

data class LscConfig(
    val versionCheckEnabled: Boolean = true,
    val databaseMode: DatabaseMode,
    val gcalMode: GcalMode,
    val logbackFileEnabled: Boolean = false,

    val syncMode: SyncMode,
    val syncDaysAhead: Int = 14, // including today

    val currentYear: Int = LocalDate.now().year,
    val appDirectory: File,
    val apiMode: ApiMode,
    val apiLang: UscLang = UscLang.English,
    val baseUrl: URL = apiLang.baseUrl,
    val windowTitleSuffix: String = "",
) {
    val fileResolver: FileResolver = FileResolverImpl(appDirectory)

    init {
        require(syncDaysAhead >= 1) { "sync days ahead must be >= 1 but was: $syncDaysAhead" }
    }

    companion object {
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
