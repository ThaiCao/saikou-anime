package ani.saikou.parsers.manga

import ani.saikou.findBetween
import ani.saikou.httpClient
import ani.saikou.parsers.MangaChapter
import ani.saikou.parsers.MangaImage
import ani.saikou.parsers.MangaParser
import ani.saikou.parsers.ShowResponse
import ani.saikou.sortByTitle
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.readValue
import com.lagradost.nicehttp.Requests

class MangaSee : MangaParser() {

    override val name = "MangaSee"
    override val saveName = "manga_see"
    override val hostUrl = host

    override suspend fun loadChapters(mangaLink: String): List<MangaChapter> {

        val list = mutableListOf<MangaChapter>()

        val json = httpClient.get("$hostUrl/manga/$mangaLink").document.select("script")
            .lastOrNull()?.toString()?.findBetween("vm.Chapters = ", ";")?: return list

        Requests.mapper.readValue<List<MangaResponse>>(json).forEach {
            val chap = it.chapter
            val num = chapChop(chap, 3)
            val link = hostUrl + "/read-online/$mangaLink-chapter-" + chapChop(chap, 1) + chapChop(chap, 2) + chapChop(chap, 0) + ".html"
            list.add(
                MangaChapter(
                    num,
                    link,
                    it.chapterName
                )
            )
        }

        return list
    }

    override suspend fun loadImages(chapterLink: String): List<MangaImage> {

        val list = mutableListOf<MangaImage>()

        val a = httpClient.get(chapterLink).document.select("script").lastOrNull()
        val str = (a ?: return list).toString()
        val server = (str.findBetween("vm.CurPathName = ", ";") ?: return list).trim('"')
        val slug = (str.findBetween("vm.IndexName = ", ";") ?: return list).trim('"')
        val json = Requests.mapper
            .readValue<ChapterResponse>(str.findBetween("vm.CurChapter = ", ";") ?: return list)
        val id = json.chapter
        val chap = chapChop(id, 1) + chapChop(id, 2) + chapChop(id, 0)
        val pages = json.page.toInt()

        for (i in 1..pages) {
            val link = "https://$server/manga/$slug/$chap-${"000$i".takeLast(3)}.png"
            list.add(MangaImage(link))
        }

        return list
    }

    override suspend fun search(query: String): List<ShowResponse> {

        val list = mutableListOf<ShowResponse>()

        list.addAll(getSearchData())
        list.sortByTitle(query)

        return list
    }

    companion object {
        const val host = "https://mangasee123.com"
        private var response: List<ShowResponse>? = null
        suspend fun getSearchData(): List<ShowResponse> {
            response = if (response != null) response ?: listOf()
            else {
                val json = httpClient.get("$host/search/").document.select("script").last().toString()
                    .findBetween("vm.Directory = ", "\n")!!.replace(";", "")
                Requests.mapper.readValue<List<SearchResponse>>(json).map {
                    ShowResponse(
                        it.s,
                        it.i,
                        "https://cover.nep.li/cover/${it.i}.jpg"
                    )
                }
            }
            return response ?: listOf()
        }
    }

    private fun chapChop(id: String, type: Int): String = when (type) {
        0    -> if (id.startsWith("1")) "" else ("-index-${id[0]}")
        1    -> (id.substring(1, 5).replace("[^0-9]".toRegex(), ""))
        2    -> if (id.endsWith("0")) "" else (".${id[id.length - 1]}")
        3    -> "${id.drop(1).dropLast(1).toInt()}${chapChop(id, 2)}"
        else -> ""
    }

    private data class MangaResponse(
        @JsonProperty("Chapter") val chapter: String,
        @JsonProperty("ChapterName") val chapterName: String?
    )

    private data class ChapterResponse(
        @JsonProperty("Chapter") val chapter: String,
        @JsonProperty("Page") val page: String
    )

    private data class SearchResponse(
        val s: String,
        val i: String
    )
}