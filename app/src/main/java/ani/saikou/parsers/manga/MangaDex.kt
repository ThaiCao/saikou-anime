package ani.saikou.parsers.manga

import ani.saikou.httpClient
import ani.saikou.others.asyncEach
import ani.saikou.parsers.MangaChapter
import ani.saikou.parsers.MangaImage
import ani.saikou.parsers.MangaParser
import ani.saikou.parsers.ShowResponse

class MangaDex : MangaParser() {

    override val name = "MangaDex"
    override val saveName = "manga_dex"
    override val hostUrl = "https://mangadex.org"

    val host = "https://api.mangadex.org"

    override suspend fun loadChapters(mangaLink: String): List<MangaChapter> {
        setUserText("Getting Chapters...")
        val list = mutableListOf<MangaChapter>()
        val totalChapters =
            httpClient.get("$host/manga/$mangaLink/feed?limit=0").parsed<MangaResponse>().total ?: return list
        setUserText("Parsing Chapters...")
        (0..totalChapters step 200).reversed().toList().asyncEach { index ->
            val data = httpClient
                .get("$host/manga/$mangaLink/feed?limit=200&order[volume]=desc&order[chapter]=desc&offset=$index")
                .parsed<MangaResponse>().data?.reversed()
            data?.forEach {
                if (it.attributes!!.translatedLanguage == "en") {
                    val chapter = (it.attributes.chapter ?: return@forEach).toString()
                    val title = it.attributes.title ?: return@forEach
                    val id = it.id ?: return@forEach
                    list.add(MangaChapter(chapter, id, title))
                }
            }
        }
        list.sortBy { it.number }
        return list
    }

    override suspend fun loadImages(chapterLink: String): List<MangaImage> {
        val list = mutableListOf<MangaImage>()
        httpClient.get("$host/at-home/server/${chapterLink}").parsed<ChapterResponse>().chapter?.apply {
            for (page in data ?: return@apply) {
                list.add(MangaImage("https://uploads.mangadex.org/data/${hash ?: return@apply}/${page}"))
            }
        }
        return list
    }

    override suspend fun search(query: String): List<ShowResponse> {
        val list = mutableListOf<ShowResponse>()
        val json = httpClient.get("$host/manga?limit=15&title=$query&order[relevance]=desc&includes[]=cover_art")
            .parsed<SearchResponse>()
        json.data?.forEach {
            val id = it.id ?: return@forEach
            val title = it.attributes?.title?.en ?: return@forEach
            val coverName = it.relationships?.find { i -> i.type == "cover_art" }?.attributes?.fileName
            val coverURL = if (coverName != null) "https://uploads.mangadex.org/covers/$id/$coverName.256.jpg" else ""
            list.add(ShowResponse(title, id, coverURL))
        }
        return list
    }

    private data class SearchResponse(
        val result: String? = null,
        val data: List<Datum>? = null,
        val total: Long? = null
    ) {
        data class Datum(
            val id: String? = null,
            val attributes: DatumAttributes? = null,
            val relationships: List<Relationship>? = null
        )

        data class DatumAttributes(
            val title: Title? = null
        )

        data class Title(
            val en: String? = null
        )

        data class Relationship(
            val id: String? = null,
            val type: String? = null,
            val attributes: RelationshipAttributes? = null
        )

        data class RelationshipAttributes(
            val fileName: String? = null
        )
    }

    private data class MangaResponse(
        val result: String? = null,
        val data: List<Datum>? = null,
        val total: Long? = null
    ) {
        data class Datum(
            val id: String? = null,
            val attributes: Attributes? = null
        )

        data class Attributes(
            val volume: Any? = null,
            val chapter: Any? = null,
            val title: String? = null,
            val translatedLanguage: String? = null
        )
    }

    private data class ChapterResponse(
        val result: String? = null,
        val baseURL: String? = null,
        val chapter: Chapter? = null
    ) {
        data class Chapter(
            val hash: String? = null,
            val data: List<String>? = null,
            val dataSaver: List<String>? = null
        )
    }
}