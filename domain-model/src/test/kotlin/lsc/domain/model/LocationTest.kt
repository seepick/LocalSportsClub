package lsc.domain.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec

class LocationTest : StringSpec({
    val validLatitude = 0.0
    val validLongitude = 0.0

    "should create valid Location" {
        Location(0.0, 0.0)
        Location(90.0, 180.0)
        Location(-90.0, -180.0)
    }

    "should throw for invalid latitude too big" {
        shouldThrow<IllegalArgumentException> {
            Location(91.0, validLongitude)
        }
    }
    "should throw for invalid latitude too little" {
        shouldThrow<IllegalArgumentException> {
            Location(-91.0, validLongitude)
        }
    }

    "should throw for invalid longitude for too big" {
        shouldThrow<IllegalArgumentException> {
            Location(validLatitude, 181.0)
        }
    }
    "should throw for invalid longitude for too little" {
        shouldThrow<IllegalArgumentException> {
            Location(validLatitude, -181.0)
        }
    }
})
