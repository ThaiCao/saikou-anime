package ani.saikou.parsers.anime

import android.net.Uri
import ani.saikou.httpClient
import ani.saikou.others.asyncEach
import ani.saikou.parsers.*
import ani.saikou.parsers.anime.extractors.RapidCloud
import ani.saikou.parsers.anime.extractors.StreamSB
import ani.saikou.parsers.anime.extractors.StreamTape
import org.jsoup.Jsoup
import java.net.URLEncoder

@Suppress("BlockingMethodInNonBlockingContext")
class Zoro : AnimeParser() {

    override val name: String = "Zoro"
    override val saveName: String = "zoro_to"
    override val hostUrl: String = "https://zoro.to"
    override val isDubAvailableSeparately: Boolean = false

    override suspend fun loadEpisodes(animeLink: String): List<Episode> {
        val list = mutableListOf<Episode>()
        val res = httpClient.get("$hostUrl/ajax/v2/episode/list/$animeLink").parsed<HtmlResponse>()
        val element = Jsoup.parse(res.html ?: return list)
        element.select(".detail-infor-content > div > a").forEach {
            val title = it.attr("title")
            val num = it.attr("data-number").replace("\n", "")
            val id = it.attr("data-id")
            val filler = it.attr("class").contains("ssl-item-filler")

            list.add(Episode(number = num, link = id, title = title, isFiller = filler))
        }
        return list
    }

    override suspend fun loadVideoServers(episodeLink: String): List<VideoServer> {
        val list = mutableListOf<VideoServer>()
        val res = httpClient.get("$hostUrl/ajax/v2/episode/servers?episodeId=$episodeLink").parsed<HtmlResponse>()
        val element = Jsoup.parse(res.html?:return list)
        element.select("div.server-item").asyncEach {
            val serverName = "${it.attr("data-type").uppercase()} - ${it.text()}"
            val link = httpClient.get("$hostUrl/ajax/v2/episode/sources?id=${it.attr("data-id")}").parsed<SourceResponse>().link
            list.add(VideoServer(serverName, FileUrl(link)))
        }
        return list
    }

    override suspend fun getVideoExtractor(server: VideoServer): VideoExtractor? {
        val domain = Uri.parse(server.embed.url).host ?: ""
        val extractor: VideoExtractor? = when {
            "rapid" in domain    -> RapidCloud(server)
            "sb" in domain       -> StreamSB(server)
            "streamta" in domain -> StreamTape(server)
            else                 -> null
        }
        return extractor
    }

    override suspend fun search(query: String): List<ShowResponse> {
        val list = mutableListOf<ShowResponse>()

        var url = URLEncoder.encode(query, "utf-8")
        if (query.startsWith("$!")) {
            val a = query.replace("$!", "").split(" | ")
            url = URLEncoder.encode(a[0], "utf-8") + a[1]
        }
        val document = httpClient.get("${hostUrl}/search?keyword=$url").document
        document.select(".film_list-wrap > .flw-item > .film-poster").forEach {
            val link = it.select("a").attr("data-id")
            val title = it.select("a").attr("title")
            val cover = it.select("img").attr("data-src")
            list.add(ShowResponse(link, title, FileUrl(cover)))
        }

        return list
    }

    data class SourceResponse (
        val link: String
    )

    private data class HtmlResponse(
        val status: Boolean,
        val html: String? = null,
    )

}