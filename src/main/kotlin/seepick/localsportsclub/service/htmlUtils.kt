package seepick.localsportsclub.service

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node

fun jsoupBody(htmlString: String): Element {
    val html = jsoupHtml(htmlString)
    return html.childNodes().single { it.nodeName() == "body" } as Element
}

fun jsoupHeadAndBody(htmlString: String): Pair<Element, Element> {
    val html = jsoupHtml(htmlString)
    return (html.childNodes().single { it.nodeName() == "head" } as Element) to
            (html.childNodes().single { it.nodeName() == "body" } as Element)
}

private fun jsoupHtml(htmlString: String): Node =
    Jsoup.parse(htmlString).childNodes().single { it.nodeName() == "html" }
