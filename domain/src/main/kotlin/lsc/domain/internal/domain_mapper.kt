package lsc.domain.internal

import lsc.domain.DummyDomain
import lsc.repo.DummyDomainDbo

internal fun DummyDomainDbo.toDomain() = DummyDomain(
    id = id,
    name = name,
)

internal fun DummyDomain.toDomainDbo() = DummyDomainDbo(
    id = id,
    name = name,
)
