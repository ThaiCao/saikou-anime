package ani.saikou.parsers.manga

import ani.saikou.findBetween
import ani.saikou.httpClient
import ani.saikou.parsers.*

class MangaBuddy : MangaParser() {

    override val name = "MangaBuddy"
    override val saveName = "manga_buddy"
    override val hostUrl = "https://mangabuddy.com"

    val headers = mapOf("referer" to hostUrl)

    override suspend fun loadChapters(mangaLink: String): List<MangaChapter> {

        val list = mutableListOf<MangaChapter>()

        val res = httpClient.get("$hostUrl/api/manga${mangaLink}/chapters?source=detail")
            .document.select("#chapter-list>li").reversed()

        res.forEach {
            if (it.select("strong").text().contains("Chapter")) {
                val chap = Regex("(Chapter ([A-Za-z0-9.]+))( ?: ?)?( ?(.+))?").find(it.select("strong").text())?.destructured
                val number: String
                var title: String? = null
                val link = hostUrl + it.select("a").attr("href")
                if (chap != null) {
                    number = chap.component2()
                    title = chap.component5()
                } else {
                    number = it.select("strong").text()
                }
                list.add(MangaChapter(number, link, title))
            }
        }

        return list
    }

    override suspend fun loadImages(chapterLink: String): List<MangaImage> {

        val list = mutableListOf<MangaImage>()

        val res = httpClient.get(chapterLink).text
        val cdn = res.findBetween("var mainServer = \"", "\";")
        val arr = res.findBetween("var chapImages = ", "\n")?.trim('\'')?.split(",")
        arr?.forEach {
            val link = FileUrl("https:$cdn$it", headers)
            list.add(MangaImage(link))
        }

        return list
    }

    override suspend fun search(query: String): List<ShowResponse> {

        val list = mutableListOf<ShowResponse>()

        val doc = httpClient.get("$hostUrl/search?status=all&sort=views&q=$query").document
        doc.select(".list > .book-item > .book-detailed-item > .thumb > a").forEach {
            if (it.attr("title") != "") {
                list.add(
                    ShowResponse(
                        it.attr("title"),
                        it.attr("href"),
                        FileUrl(it.select("img").attr("data-src"), headers)
                    )
                )
            }
        }

        return list
    }

}