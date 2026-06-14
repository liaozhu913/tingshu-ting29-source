package com.github.eprendre.sources_by_ting29

import org.jsoup.nodes.Element

/** Shared defensive parsing helpers for every listening source in this package. */
internal object ParsingGuards {
    private val statisticPattern = Regex(".*(收听|播放|人气|点击|热度|次|万|亿).*|^[0-9,.]+$")

    fun Element.firstOwnText(vararg selectors: String): String = selectors
        .asSequence()
        .mapNotNull { selectFirst(it) }
        .map { it.ownText().ifBlank { it.text() }.cleanText() }
        .firstOrNull { it.isStableName() }
        .orEmpty()

    fun Element.firstAbsoluteUrl(vararg selectors: String): String = selectors
        .asSequence()
        .mapNotNull { selectFirst(it) }
        .map { it.absUrl("href").ifBlank { it.attr("href") }.cleanText() }
        .firstOrNull { it.isNotBlank() }
        .orEmpty()

    fun Element.firstImageUrl(): String = selectFirst("img[src], img[data-src], img[data-original], img[data-lazy-src]")
        ?.let { image ->
            sequenceOf("src", "data-src", "data-original", "data-lazy-src")
                .map { image.absUrl(it).ifBlank { image.attr(it) }.cleanText() }
                .firstOrNull { it.isNotBlank() }
        }
        .orEmpty()

    fun List<String>.safeMeta(excludedName: String = ""): String = asSequence()
        .map { it.cleanText() }
        .filter { it.isNotBlank() && it != excludedName }
        .distinct()
        .joinToString(" / ")

    fun String.cleanText(): String = replace('\u00a0', ' ')
        .replace(Regex("\\s+"), " ")
        .trim()
        .trim('：', ':', '-', ' ')

    fun String.isStableName(): Boolean {
        val text = cleanText()
        return text.isNotBlank() && !statisticPattern.matches(text)
    }
}
