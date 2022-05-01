package ani.saikou.parsers.anime.extractors

import ani.saikou.client
import ani.saikou.getSize
import ani.saikou.parsers.*

class StreamTape(override val server: VideoServer) : VideoExtractor() {
    private val linkRegex = Regex("""'robotlink'\)\.innerHTML = '(.+?)'\+ \('(.+?)'\)""")

    override suspend fun extract(): VideoContainer {
        val reg = linkRegex.find(client.get(server.embed.url).text)!!
        val extractedUrl = FileUrl("https:${reg.groups[1]!!.value + reg.groups[2]!!.value.substring(3)}")
        return VideoContainer(listOf(Video(0, false, extractedUrl, getSize(extractedUrl))))
    }
}