package ani.saikou.parsers.manga

import ani.saikou.httpClient
import ani.saikou.parsers.*

class MangaKakalot : MangaParser() {

    override val name = "MangaKakalot"
    override val saveName = "manga_kakalot"
    override val hostUrl = "https://mangakakalot.com"

    val headers = mapOf("referer" to hostUrl)

    override suspend fun loadChapters(mangaLink: String): List<MangaChapter> {

        val list = mutableListOf<MangaChapter>()

        val res = httpClient.get(mangaLink).document
            .select(
                if (mangaLink.contains("readmanganato.com")) ".row-content-chapter > .a-h"
                else ".chapter-list > .row > span"
            ).reversed()
        
        res.forEach {
            val chap = Regex("((?<=Chapter )[0-9.]+)([\\s:]+)?(.+)?").find(it.select("a").text())?.destructured
            if (chap != null) {
                val link = it.select("a").attr("href")
                list.add(MangaChapter(link, chap.component1(), chap.component3()))
            }
        }

        return list
    }

    override suspend fun loadImages(chapterLink: String): List<MangaImage> {

        val list = mutableListOf<MangaImage>()

        httpClient.get(chapterLink).document.select(".container-chapter-reader > img").forEach {
            val file = FileUrl(it.attr("src"), headers)
            list.add(MangaImage(file))
        }

        return list
    }

    override suspend fun search(query: String): List<ShowResponse> {

        val list = mutableListOf<ShowResponse>()

        val res = httpClient
            .get("$hostUrl/search/story/${query.replace(" ", "_").replace(Regex("\\W"), "")}")
            .document.select(".story_item")
        res.forEach {
            if (it.select(".story_name > a").text() != "") {
                list.add(
                    ShowResponse(
                        it.select(".story_name > a").text(),
                        it.select("a").attr("href"),
                        FileUrl(it.select("img").attr("src"), headers)
                    )
                )
            }
        }

        return list
    }
}