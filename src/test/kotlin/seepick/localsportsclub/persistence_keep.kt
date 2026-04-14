package seepick.localsportsclub

import com.github.seepick.uscclient.model.City
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string
import seepick.localsportsclub.service.Location
import seepick.localsportsclub.service.WindowPref
import seepick.localsportsclub.service.model.Gcal
import seepick.localsportsclub.service.model.Preferences
import testfixtUsc.credentials

@Suppress("unused")
fun Arb.Companion.windowPref() = arbitrary {
    WindowPref(
        width = int(min = 0).bind(),
        height = int(min = 0).bind(),
        posX = int(min = 0).bind(),
        posY = int(min = 0).bind(),
    )
}

fun Arb.Companion.preferences() = arbitrary {
    Preferences(
        uscCredentials = credentials().orNull().bind(),
        city = City.all.random(),
        home = location().orNull().bind(),
        gcal = gcal().bind(),
        periodFirstDay = int(min = 1, max = 28).orNull().bind(),
    )
}

fun Arb.Companion.gcal() = arbitrary {
    if (boolean().bind()) {
        Gcal.GcalDisabled
    } else {
        Gcal.GcalEnabled(calendarId = string(maxSize = 64).bind())
    }
}

fun Arb.Companion.location() = arbitrary {
    Location(
        latitude = double(min = -90.0, max = 90.0, false).bind(),
        longitude = double(min = -180.0, max = 180.0, false).bind(),
    )
}

