package ani.saikou.parsers.anime

import android.util.Base64
import ani.saikou.findBetween
import ani.saikou.httpClient
import ani.saikou.others.logError
import ani.saikou.parsers.*
import ani.saikou.parsers.anime.extractors.FPlayer
import java.net.URI

class HentaiFF : AnimeParser() {
    override val name: String = "HentaiFF"
    override val saveName: String = "hentai_ff"
    override val hostUrl: String = "https://hentaiff.com"

    override val isDubAvailableSeparately = false
    override val isNSFW: Boolean = true

    override suspend fun loadEpisodes(animeLink: String): List<Episode> {
        val map = mutableMapOf<String, Episode>()
        val pageBody = httpClient.get(animeLink).document.body()
        val notRaw = mutableListOf<Episode>()
        val raw = mutableListOf<Episode>()
        pageBody.select("div.eplister>ul>li>a").reversed().forEach { i ->
            i.select(".epl-num").text().split(" ").apply {
                val num = this[0]
                val title = this[1]
                (if (title == "RAW") raw else notRaw).add(
                    Episode(
                        num,
                        i.attr("href"),
                        title
                    )
                )
            }
        }
        raw.map { map[it.number] = it }
        notRaw.map { map[it.number] = it }
        return map.values.toList()
    }

    override suspend fun loadVideoServers(episodeLink: String): List<VideoServer> {
        val list = mutableListOf<VideoServer>()
        httpClient.get(episodeLink).document.select("select.mirror>option").forEach {
            try {
                val base64 = it.attr("value")
                val link = String(Base64.decode(base64, Base64.DEFAULT)).findBetween("src=\"", "\" ")!!
                list.add(VideoServer(it.text(), link))
            } catch (e: Exception) {
                logError(e)
            }
        }
        return list
    }

    override suspend fun getVideoExtractor(server: VideoServer): VideoExtractor? {
        val url = server.embed.url
        val extractor = when {
            "amhentai" in url -> FPlayer(server)
            "cdnview" in url  -> CdnView(server)
            else              -> null
        }
        return extractor
    }

    override suspend fun search(query: String): List<ShowResponse> {
        val list = mutableListOf<ShowResponse>()
        httpClient.get("${hostUrl}/?s=$query").document.body()
            .select(".bs>.bsx>a").forEach {
                val link = it.attr("href").toString()
                val title = it.attr("title")
                val cover = it.select("img").attr("src")
                list.add(ShowResponse(title, link, cover))
            }
        return list
    }

    private class CdnView(override val server: VideoServer) : VideoExtractor() {
        override suspend fun extract(): VideoContainer {
            val host = URI.create(server.embed.url).host
            val link = "https://" + host + httpClient.get(server.embed.url).document.select("source").attr("src")
            return VideoContainer(listOf(Video(null, true, link)))
        }
    }
}