package seepick.localsportsclub.gcal

import seepick.localsportsclub.service.FileResolver
import seepick.localsportsclub.service.singles.SinglesService

class PrefsEnabledGcalService(
    private val singlesService: SinglesService,
    private val fileResolver: FileResolver,
) : GcalService {

    private val delegate by lazy {
        val calendarId = singlesService.preferences.gcal.maybeCalendarId
        if (calendarId == null) NoopGcalService
        else RealGcalService(fileResolver)
    }

    override fun create(calendarId: String, entry: GcalEntry) {
        delegate.create(calendarId, entry)
    }

    override fun delete(calendarId: String, deletion: GcalDeletion) {
        delegate.delete(calendarId, deletion)
    }
}
