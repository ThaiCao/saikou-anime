package ani.saikou.manga.source

import ani.saikou.manga.source.parsers.*

object MangaSources {
    private val mangaParsers:MutableMap<Int,MangaParser> = mutableMapOf()

    val names = arrayListOf(
        "MANGABUDDY",
        "MANGASEE",
        "MANGAPILL",
        "MANGADEX",
        "MANGAREADER",
    )

    operator fun get(i:Int):MangaParser?{
        val a = when(i){
            0->mangaParsers.getOrPut(i) { MangaBuddy() }
            1->mangaParsers.getOrPut(i) { MangaSee() }
            2->mangaParsers.getOrPut(i) { MangaPill() }
            3->mangaParsers.getOrPut(i) { MangaDex() }
            4->mangaParsers.getOrPut(i) { MangaReaderTo() }
            else -> null
        }
        return a
    }
    fun flushLive(){
        mangaParsers.forEach{
            it.value.live.value=null
        }
    }
}