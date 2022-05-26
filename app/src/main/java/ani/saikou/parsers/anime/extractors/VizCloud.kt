package ani.saikou.parsers.anime.extractors

import ani.saikou.FileUrl
import ani.saikou.client
import ani.saikou.parsers.Video
import ani.saikou.parsers.VideoContainer
import ani.saikou.parsers.VideoExtractor
import ani.saikou.parsers.VideoServer
import ani.saikou.parsers.anime.NineAnime.Companion.cipher
import ani.saikou.parsers.anime.NineAnime.Companion.encrypt

class VizCloud(override val server: VideoServer) : VideoExtractor() {

    private data class Sources(val file: String)
    private data class Media(val sources: List<Sources>)
    private data class Data(val media: Media)
    private data class Response(val data: Data)

    //Regex credit to @justfoolingaround
    private val regex = Regex("(.+?/)e(?:mbed)?/([a-zA-Z0-9]+)")

    //key credits to @Modder4869
    private val key = "51wJ0FDq/UVCefLopEcmK3ni4WIQztMjZdSYOsbHr9R2h7PvxBGAuglaN8+kXT6y"
    private val cipherKey = "LCbu3iYC7ln24K7P"

    override suspend fun extract(): VideoContainer {

        val embed = server.embed
        val group = regex.find(embed.url)?.groupValues!!

        val host = group[1]
        val id = encrypt(cipher(cipherKey,encrypt(group[2],key)),key).replace("/", "_")

        val response = client.get(
            "${host}info/$id",
            embed.headers
        ).parsed<Response>()
        return VideoContainer(response.data.media.sources.map {
            val file = FileUrl(it.file, mapOf("referer" to host))
            Video(null, true, file, null)
        })
    }
}