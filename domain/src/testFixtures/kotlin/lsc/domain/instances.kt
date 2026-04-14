package lsc.domain

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string

fun Arb.Companion.dummyDomain() = arbitrary {
    DummyDomain(
        id = int().bind(),
        name = string().bind(),
    )
}
