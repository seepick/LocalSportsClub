package lsc.view

import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import lsc.domain.dummyDomain

class DummyViewTest : StringSpec({
    "test" {
        val model = Arb.dummyDomain().next()
    }
})
