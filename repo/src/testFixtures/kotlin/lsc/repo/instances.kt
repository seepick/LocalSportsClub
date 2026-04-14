package lsc.repo

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string

fun Arb.Companion.dummyDomainDbo() = arbitrary {
    DummyDomainDbo(
        id = int().bind(),
        name = string().bind(),
    )
}

fun Arb.Companion.visitLimitsDbo() = arbitrary {
    VisitLimitsDbo(
        small = int(0..8).orNull().bind(),
        medium = int(0..8).orNull().bind(),
        large = int(0..8).orNull().bind(),
        xlarge = int(0..8).orNull().bind(),
    )
}
