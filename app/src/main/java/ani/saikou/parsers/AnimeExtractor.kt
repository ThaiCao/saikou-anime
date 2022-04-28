package ani.saikou.parsers


abstract class VideoExtractor {
    lateinit var name: String
    lateinit var videos: List<Video>
    lateinit var subtitles : List<Subtitle>
    val headers : MutableMap<String,String> = mutableMapOf()

    /**
     * Loads "Video" from a given Url
     * **/
    abstract fun loadVideosFromUrl() : VideoExtractor

    /**
     * Gets called when a Video from this extractor starts playing.
     * Useful for Extractor that require Polling
     * **/
    open fun onVideoPlayed(video: Video){}

    /**
     * Called when a particular video has been stopped playing
     **/
    open fun onVideoStopped(video: Video){}
}

data class Video(
    val name: String,
    val url: String,
    val isM3U8: Boolean,
    val size : Long? = null,
    val extraNote:String? = null,
)

data class Subtitle(
    val language: String,
    val url: String,
)