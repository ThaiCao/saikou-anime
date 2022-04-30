package ani.saikou.parsers.anime.extractors

import android.net.Uri
import ani.saikou.httpClient
import ani.saikou.parsers.*

class VizCloud(override val server: VideoServer) : VideoExtractor() {

    private data class Sources(val file: String, val label: String?)
    private data class Media(val sources: List<Sources>)
    private data class Response(val success: Boolean, val media: Media)

    override suspend fun extract(): VideoContainer {
        val videos = mutableListOf<Video>()

        val url = server.embed.url
        val headers = server.embed.headers

        val embedded = httpClient.get(url, headers).document
        val skey = embedded.selectFirst("script:containsData(window.skey = )")!!.data().substringAfter("window.skey = \'")
            .substringBefore("\'")

        val response = httpClient.get(
            url.replace("/embed/", "/info/").replace("/e/", "/info/"),
            headers,
            params = mapOf("skey" to skey)
        ).parsed<Response>()

        response.media.sources.forEach{
            val file = FileUrl(it.file, mapOf("referer" to "https://${Uri.parse(url).host}/"))
            videos.add(
                Video(null,false,file,null,it.label)
            )
        }
        return VideoContainer(videos)
    }
}