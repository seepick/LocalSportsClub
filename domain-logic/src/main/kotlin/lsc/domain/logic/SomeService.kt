package lsc.domain.logic

import lsc.domain.repo.VenueDomainRepo

class SomeService(
    private val repo: VenueDomainRepo,
) {
    fun foo() {
        repo.selectAllAnywhere().forEach { venue ->
            println(venue)
        }
    }
}
