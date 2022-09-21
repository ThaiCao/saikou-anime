package ani.saikou.parsers.anime.extractors

import android.net.Uri.encode
import ani.saikou.FileUrl
import ani.saikou.client
import ani.saikou.parsers.*
import ani.saikou.parsers.anime.NineAnime.Companion.cipher
import ani.saikou.parsers.anime.NineAnime.Companion.encrypt
import kotlinx.serialization.Serializable

class VizCloud(override val server: VideoServer) : VideoExtractor() {

    @Serializable
    private data class Sources(val file: String)

    @Serializable
    private data class Media(val sources: List<Sources>)

    @Serializable
    private data class Data(val media: Media)

    @Serializable
    private data class Response(val data: Data)

    private val regex = Regex("(.+?/)e(?:mbed)?/([a-zA-Z0-9]+)")

    override suspend fun extract(): VideoContainer {

        val embed = server.embed
        val group = regex.find(embed.url)?.groupValues!!

        val host = group[1]
        val id = encodeVrf(group[2], getKey())

        val link = "${host}/${group[2]}/$id"
        val response = client.get(link, embed.headers)
        println(link)
        if (!response.text.startsWith("{")) throw Exception("Seems like 9Anime kiddies changed stuff again, Go touch some grass for bout an hour Or use a different Server")
        return VideoContainer(response.parsed<Response>().data.media.sources.map {
            val file = FileUrl(it.file, mapOf("referer" to host))
            Video(null, VideoType.M3U8, file)
        })
    }

    companion object {
        private var lastChecked = 0L
        private const val jsonLink = "https://raw.githubusercontent.com/AnimeJeff/Brohflow/main/keys.json"
        private var cipherKey: CipherKey? = null
        suspend fun getKey(): CipherKey {
            cipherKey = if (cipherKey != null && (lastChecked - System.currentTimeMillis()) < 1000 * 60 * 30) cipherKey!!
            else {
                lastChecked = System.currentTimeMillis()
                client.get(jsonLink).parsed()
            }
            return cipherKey!!
        }

        fun encodeVrf(input: String, key: CipherKey): String {
            val ciphered = cipher(key.cipher, input)
            val encrypted = encrypt(ciphered, baseTable)
            val mapped = mapKeys(encrypted, key.keyMap)
            val encryptedAgain = encrypt(mapped, baseTable)
            return encode(encryptedAgain)
        }

        private const val baseTable = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=_"
        private const val baseTable1 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+=/_"

        private fun mapKeys(encrypted: String, keyMap: String): String {
            val table = keyMap.split("")
            return encrypted.mapIndexedNotNull { i, c ->
                table.getOrNull((baseTable1.indexOf(c) * 16) + 1 + (i % 16))
            }.joinToString("")
        }

        @Serializable
        data class CipherKey(
            val cipher: String,
            val decipher: String,
            val keyMap: String
        )
    }
}

