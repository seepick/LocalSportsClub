package seepick.localsportsclub.view.common

import seepick.localsportsclub.service.DummyFileResolver

fun main() {
    val cause = Exception("ganz unten")
    val cause2 = Exception("rums", cause)
    val ex = Exception("bla blu", cause2)
    val message = "This is a longer message explaining what it is."
//    val message = (1..20).fold("") { acc, i -> "$acc #$i: This is my very long text." }
    showErrorDialog(message, ex, DummyFileResolver)
}
