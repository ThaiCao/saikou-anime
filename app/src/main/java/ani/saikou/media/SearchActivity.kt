package ani.saikou.media

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updatePaddingRelative
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import ani.saikou.*
import ani.saikou.anilist.AnilistSearch
import ani.saikou.anilist.SearchResults
import ani.saikou.databinding.ActivitySearch2Binding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySearch2Binding
    private val scope = lifecycleScope
    val model: AnilistSearch by viewModels()

    var type = "ANIME"
    var grid :Boolean=true
    private var screenWidth:Float = 0f

    private lateinit var mediaAdaptor:MediaAdaptor

    private lateinit var searchResults:SearchResults

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearch2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        initActivity(this)
        screenWidth = resources.displayMetrics.run { widthPixels / density }

        binding.searchRecyclerView.updatePaddingRelative(top= statusBarHeight,bottom = navBarHeight +80f.px)

        type = intent.getStringExtra("type")?:type
        grid = loadData<Boolean>("searchGrid") ?:false

        searchResults = SearchResults(type,false, results = arrayListOf(), hasNextPage = false)
        val headerAdaptor=SearchAdapter(this)
        mediaAdaptor = MediaAdaptor(if(grid) 0 else 1,searchResults.results,this,matchParent = true)

        val gridSize=(screenWidth / 124f).toInt()
        val gridLayoutManager = GridLayoutManager(this,gridSize)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (position) {
                    0 -> gridSize
                    else -> if (grid) 1 else gridSize
                }
            }
        }

        val concatAdapter = ConcatAdapter(headerAdaptor,mediaAdaptor)
        binding.searchRecyclerView.layoutManager = gridLayoutManager
        binding.searchRecyclerView.adapter = concatAdapter

        model.getSearch().observe(this){
            if(it!=null){
                searchResults.apply {
                    isAdult = it.isAdult
                    perPage = it.perPage
                    search = it.search
                    sort = it.sort
                    genres = it.genres
                    tags = it.tags
                    format = it.format
                    page = it.page
                    hasNextPage = it.hasNextPage
                }
                val prev= searchResults.results.size
                searchResults.results.addAll(it.results)
                mediaAdaptor.notifyItemRangeInserted(prev,it.results.size)
            }
        }
    }

    fun search(search:String?=null,genre:ArrayList<String>?=null,tag:ArrayList<String>?=null,sortBy:String?=null,adult:Boolean=false){
        scope.launch(Dispatchers.IO) {
             model.loadSearch(type,search,genre,tag,sortBy,adult)
        }
    }

    fun recycler(){
        mediaAdaptor.type = if (grid) 0 else 1
    }
}