package ani.saikou.parsers

import ani.saikou.loadData
import ani.saikou.others.MalSyncBackup
import ani.saikou.others.asyncEach
import ani.saikou.others.logError
import kotlin.properties.Delegates


/**
 * An abstract class for creating a new Source
 *
 * Most of the functions & variables that need to be overridden are abstract
 * **/
abstract class AnimeParser : BaseParser() {

    /**
     * Usually takes a SearchResponse.url as argument & gives a list of total episode present on the server.
     * **/
    abstract suspend fun loadEpisodes(showUrl: String): List<Episode>

    /**
     * Most of the time takes Episode.link as parameter, but you can use anything else if needed
     *
     * This returns a Map of "Video Server's Name" & "Link/Data" of all the Extractor VideoServers, which can be further used by loadVideoServers() & loadSingleVideoServer()
     * **/
    abstract suspend fun loadVideoServers(episodeUrl: String): List<VideoServer>


    /**
     * This function will receive **url of the embed** & **name** of a Video Server present on the site to host the episode.
     *
     *
     * Create a new VideoExtractor for the video server you are trying to scrape, if there's not one already.
     *
     *
     * (Some sites might not have separate video hosts. In that case, just create a new VideoExtractor for that particular site)
     *
     *
     * returns a **VideoExtractor** containing **`server`**, the app will further load the videos using `extract()` function inside it
     *
     * **Example for Site with multiple Video Servers**
     * ```
    val domain = Uri.parse(server.embed.url).host ?: ""
    val extractor: VideoExtractor? = when {
        "fembed" in domain   -> FPlayer(server)
        "sb" in domain       -> StreamSB(server)
        "streamta" in domain -> StreamTape(server)
        else                 -> null
    }
    return extractor
    ```
     * You can use your own way to get the Extractor for reliability.
     * if there's only extractor, you can directly return it.
     * **/
    abstract suspend fun getVideoExtractor(server: VideoServer): VideoExtractor?

    /**
     * This Function used when there "isn't" a default Server set by the user, or when user wants to switch the Server
     *
     * Doesn't need to be overridden, if the parser is following the norm.
     * **/
    open suspend fun loadVideoServers(url: String, callback: (VideoExtractor) -> Unit) {
        loadVideoServers(url).asyncEach {
            try {
                getVideoExtractor(it)?.apply {
                    extract()
                    callback.invoke(this)
                }
            } catch (e: Exception) {
                logError(e)
            }
        }
    }

    /**
     * This Function used when there "is" a default Server set by the user, only loads a Single Server for faster response.
     *
     * Doesn't need to be overridden, if the parser is following the norm.
     * **/
    open suspend fun loadSingleVideoServer(serverName: String, url: String): VideoExtractor? {
        try {
            loadVideoServers(url).apply {
                find { it.name == serverName }?.also {
                    return getVideoExtractor(it)?.apply {
                        extract()
                    }
                }
            }
        } catch (e: Exception) {
            logError(e)
        }
        return null
    }


    /**
     * Many sites have Dub & Sub anime as separate Shows
     *
     * make this `true`, if they are separated else `false`
     *
     * **NOTE : do not forget to override `dubSuffix` if the site does not support only dub search**
     * **/
    open val isDubAvailableSeparately by Delegates.notNull<Boolean>()

    /**
     * The app changes this, depending on user's choice.
     * **/
    open var selectDub = false

    /**
     * Name used to get Shows Directly from MALSyncBackup's github dump
     *
     * Do not override if the site is not present on it.
     * **/
    open val malSyncBackupName = ""


    override suspend fun loadSavedShowResponse(mediaId: Int): ShowResponse? {
        checkIfVariablesAreEmpty()
        var loaded = loadData<ShowResponse>("${saveName}_$mediaId")
        if (loaded == null && malSyncBackupName.isNotEmpty())
            loaded = MalSyncBackup.get(mediaId, malSyncBackupName, selectDub)?.also { saveShowResponse(mediaId, it, false) }
        return loaded
    }

}

/**
 * A class for containing Episode data of a particular parser
 * **/
data class Episode(
    /**
     * Number of the Episode in "String",
     *
     * useful in cases where episode is not a number
     * **/
    val number: String,

    /**
     * Link that links to the episode page containing videos
     * **/
    val link: String,

    //Self-Descriptive
    val title: String? = null,
    val thumbnail: FileUrl? = null,
    val description: String? = null,
    val isFiller: Boolean = false,
) {
    constructor(
        number: String,
        link: String,
        title: String? = null,
        thumbnail: String,
        description: String?,
        isFiller: Boolean = false
    ) : this(number, link, title, FileUrl(thumbnail), description, isFiller)

    constructor(number: String, link: String, title: String? = null, thumbnail: String, description: String?) : this(
        number,
        link,
        title,
        FileUrl(thumbnail),
        description
    )

    constructor(number: String, link: String, title: String? = null, thumbnail: String) : this(
        number,
        link,
        title,
        FileUrl(thumbnail)
    )
}