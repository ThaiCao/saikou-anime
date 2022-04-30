package ani.saikou.parsers.anime

import ani.saikou.httpClient
import ani.saikou.media.Media
import ani.saikou.parsers.*
import ani.saikou.parsers.anime.extractors.VizCloud
import ani.saikou.sortByTitle

class AnimeKisa : AnimeParser() {

    override val name = "AnimeKisa"
    override val saveName = "anime_kisa_in"
    override val hostUrl = "https://animekisa.in"
    override val isDubAvailableSeparately = true

    override suspend fun loadEpisodes(showUrl: String): List<Episode> {
        val list = mutableListOf<Episode>()
        val pageBody = httpClient.get(showUrl).document
        pageBody.select(".tab-pane > ul.nav").forEach {
            it.select("li>a").forEach { i ->
                val num = i.text().trim()
                list.add(Episode(num, i.attr("href").trim()))
            }
        }
        return list
    }

    override suspend fun loadVideoServers(episodeUrl: String): List<VideoServer> {
        return httpClient.get(episodeUrl).document.select("#servers-list ul.nav li a").map { servers ->
            VideoServer(servers.select("span").text(), servers.attr("data-embed"))
        }
    }

    override suspend fun getVideoExtractor(server: VideoServer): VideoExtractor = VizCloud(server)

    override suspend fun search(query: String): List<ShowResponse> {

        var url = encode(query)
        if (query.startsWith("$!")) {
            val a = query.replace("$!", "").split(" | ")
            url = encode(a[0]) + a[1]
        }

        val list = mutableListOf<ShowResponse>()

        httpClient.get("$hostUrl/filter?keyword=$url").document
            .select("#main-wrapper .film_list-wrap > .flw-item .film-poster").forEach {
                val link = it.select("a").attr("href")
                val title = it.select("img").attr("title")
                val cover = it.select("img").attr("data-src")
                list.add(ShowResponse(title, link, cover))
            }
        return list
    }

    override suspend fun autoSearch(mediaObj: Media): ShowResponse? {
        var response = loadSavedShowResponse(mediaObj.id)
        if (response != null) {
            setUserText("Selected : ${response.name}")
        } else {
            setUserText("Searching : ${mediaObj.mainName}")
            val query = "$! | &language%5B%5D=${if (selectDub) "dubbed" else "subbed"}&year%5B%5D=${mediaObj.anime?.seasonYear?:""}&sort=default&season%5B%5D=${mediaObj.anime?.season?.lowercase()?:""}&type%5B%5D=${mediaObj.typeMAL?.lowercase()?:""}"
            val responses = search(query).toMutableList()
            responses.sortByTitle(mediaObj.mainName)
            response =  if (responses.isNotEmpty()) responses[0] else null
        }
        saveShowResponse(mediaObj.id, response)
        return response
    }
}