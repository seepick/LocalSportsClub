package lsc.repo

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string

fun Arb.Companion.dummyDomainDbo() = arbitrary {
    DummyDomainDbo(
        id = int().bind(),
        name = string().bind(),
    )
}
