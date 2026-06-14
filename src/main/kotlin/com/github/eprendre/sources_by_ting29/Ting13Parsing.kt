package com.github.eprendre.sources_by_ting29

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * Ting13-specific parsing helpers.
 *
 * The compiled jar previously reused the generic PTCMS list parser for Ting13.
 * Ting13 category rows place the title link and statistic spans next to each
 * other, so reading the whole row or indexing every `.list-book-cs span` can
 * swap the book name/category with listen counts. Keep Ting13 parsing isolated
 * and always read the title from the title anchor itself.
 */
internal object Ting13Parsing {
    const val BASE_URL = "https://www.ting13.cc"

    data class Category(val name: String, val path: String)

    data class BookItem(
        val name: String,
        val url: String,
        val cover: String,
        val category: String,
        val meta: String
    )

    val categories = listOf(
        Category("恐怖灵异", "/yousheng/lingyi/lastupdate.html"),
        Category("玄幻武侠", "/yousheng/xuanhuan/lastupdate.html"),
        Category("都市言情", "/yousheng/dushi/lastupdate.html"),
        Category("历史军事", "/yousheng/lishi/lastupdate.html"),
        Category("刑侦推理", "/yousheng/tuili/lastupdate.html"),
        Category("网游竞技", "/yousheng/wangyou/lastupdate.html"),
        Category("官场商战", "/yousheng/guanchang/lastupdate.html"),
        Category("相声评书", "/pingshu/lastupdate.html"),
        Category("儿童读物", "/ertong/lastupdate.html"),
        Category("百家讲坛", "/baijiajiangtan/lastupdate.html")
    )

    fun parseCategoryBooks(document: Document, fallbackCategory: String): List<BookItem> {
        return document.select(".list-works-dl, .bookbox, .book-item, li")
            .mapNotNull { parseBookItem(it, fallbackCategory) }
            .distinctBy { it.url }
    }

    fun parseBookItem(row: Element, fallbackCategory: String): BookItem? {
        val titleLink = row.selectFirst(".list-book-dt a[href], .bookname a[href], h3 a[href], h2 a[href], a[href*=/book/], a[href*=/yousheng/]")
            ?: return null
        val name = titleLink.ownText().ifBlank { titleLink.text() }.cleanTing13Text()
        if (name.isBlank() || name.looksLikeStatistic()) return null

        val url = titleLink.absUrl("href").ifBlank { BASE_URL + titleLink.attr("href") }
        val cover = row.selectFirst("img[src], img[data-src], img[data-original]")?.let {
            it.absUrl("src").ifBlank { it.absUrl("data-src") }.ifBlank { it.absUrl("data-original") }
        }.orEmpty()

        val metaParts = row.select(".list-book-cs span, .book-meta span, p, em")
            .map { it.text().cleanTing13Text() }
            .filter { it.isNotBlank() && it != name }
            .distinct()

        val category = metaParts.firstOrNull { it.contains("分类") || it.contains("类型") }
            ?.substringAfter('：')
            ?.substringAfter(':')
            ?.cleanTing13Text()
            ?.takeIf { it.isNotBlank() && !it.looksLikeStatistic() }
            ?: fallbackCategory

        val meta = metaParts.filterNot { it == category }.joinToString(" / ")
        return BookItem(name = name, url = url, cover = cover, category = category, meta = meta)
    }

    private fun String.cleanTing13Text(): String =
        replace('\u00a0', ' ')
            .replace(Regex("\\s+"), " ")
            .trim()
            .trim('：', ':', '-', ' ')

    private fun String.looksLikeStatistic(): Boolean {
        val text = cleanTing13Text()
        if (text.isBlank()) return true
        return text.contains("收听") || text.contains("播放") || text.contains("人气") ||
            text.matches(Regex("^[0-9,.万亿]+$"))
    }
}
