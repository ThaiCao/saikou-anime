package ani.saikou.others

import ani.saikou.client
import ani.saikou.tryWithSuspend
import kotlinx.serialization.Serializable

object AniSkip {

    suspend fun getResult(malId: Int, episodeNumber: Int, episodeLength: Long): List<Stamp>? {
        println("mal : $malId, num : $episodeNumber, len : $episodeLength")
        val url =
            "https://api.aniskip.com/v2/skip-times/$malId/$episodeNumber?types[]=ed&types[]=mixed-ed&types[]=mixed-op&types[]=op&types[]=recap&episodeLength=$episodeLength"
        return tryWithSuspend {
            val a = client.get(url)
            println("res : $a")
            val res = a.parsed<AniSkipResponse>()
            if (res.found) res.results else null
        }
    }

    @Serializable
    data class AniSkipResponse(
        val found: Boolean,
        val results: List<Stamp>?,
        val message: String?,
        val statusCode: Int
    )

    @Serializable
    data class Stamp(
        val interval: AniSkipInterval,
        val skipType: SkipType,
        val skipId: String,
        val episodeLength: Double
    )

    @Suppress("EnumEntryName")
    enum class SkipType {
        op, ed, recap;

        fun getString(): String {
            return when (this) {
                op    -> "Opening"
                ed    -> "Ending"
                recap -> "Recap"
            }
        }
    }

    @Serializable
    data class AniSkipInterval(
        val startTime: Double,
        val endTime: Double
    )
}