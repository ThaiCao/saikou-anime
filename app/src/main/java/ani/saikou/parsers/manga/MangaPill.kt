package ani.saikou.parsers.manga

import ani.saikou.httpClient
import ani.saikou.parsers.MangaChapter
import ani.saikou.parsers.MangaImage
import ani.saikou.parsers.MangaParser
import ani.saikou.parsers.ShowResponse

class MangaPill : MangaParser() {

    override val name = "MangaPill"
    override val saveName = "manga_pill"
    override val hostUrl = "https://mangapill.com"

    override suspend fun loadChapters(mangaLink: String): List<MangaChapter> {

        val list = mutableListOf<MangaChapter>()

        httpClient.get(mangaLink).document.select("#chapters > div > a").reversed().forEach {
            val chap = it.text().replace("Chapter ", "")
            list.add(MangaChapter(chap, hostUrl + it.attr("href")))
        }

        return list
    }

    override suspend fun loadImages(chapterLink: String): List<MangaImage> {

        val list = mutableListOf<MangaImage>()

        httpClient.get(chapterLink).document.select("img.js-page").forEach {
            list.add(MangaImage(it.attr("data-src")))
        }

        return list
    }

    override suspend fun search(query: String): List<ShowResponse> {

        val list = mutableListOf<ShowResponse>()

        val link = "$hostUrl/quick-search?q=${encode(query)}"
        httpClient.get(link).document.select(".bg-card").forEach {
            list.add(
                ShowResponse(
                    it.select(".font-black").text(),
                    hostUrl + it.attr("href"),
                    it.select("img").attr("src")
                )
            )
        }

        return list
    }
}