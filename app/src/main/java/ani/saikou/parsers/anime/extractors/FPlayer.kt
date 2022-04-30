package ani.saikou.parsers.anime.extractors

import ani.saikou.getSize
import ani.saikou.httpClient
import ani.saikou.others.asyncEach
import ani.saikou.parsers.Video
import ani.saikou.parsers.VideoContainer
import ani.saikou.parsers.VideoExtractor
import ani.saikou.parsers.VideoServer
import com.fasterxml.jackson.databind.exc.MismatchedInputException

class FPlayer(override val server: VideoServer) : VideoExtractor() {

    override suspend fun extract(): VideoContainer {
        val videos = mutableListOf<Video>()

        val url = server.embed.url
        val apiLink = url.replace("/v/", "/api/source/")
        try {
            val json = httpClient.post(apiLink, referer = url).parsed<Json>()

            if (json.success) {
                json.data?.asyncEach {
                    videos.add(
                        Video(
                            it.label.replace("p", "").toIntOrNull() ?: 0,
                            false,
                            it.file,
                            getSize(it.file)
                        )
                    )
                }
            }

        } catch (e: MismatchedInputException) {}

        return VideoContainer(videos)
    }


    private data class Data(
        val file: String,
        val label: String
    )

    private data class Json(
        val success: Boolean,
        val data: List<Data>?
    )
}