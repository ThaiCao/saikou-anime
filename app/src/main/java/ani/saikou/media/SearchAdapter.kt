package ani.saikou.media

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import ani.saikou.R
import ani.saikou.anilist.Anilist
import ani.saikou.databinding.ItemSearchHeaderBinding
import ani.saikou.saveData

class SearchAdapter(private val activity: SearchActivity): RecyclerView.Adapter<SearchAdapter.SearchHeaderViewHolder>() {
    private val itemViewType = 6969
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchHeaderViewHolder {
        val binding = ItemSearchHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchHeaderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchHeaderViewHolder, position: Int) {

        val binding = holder.binding

        val adult = false
        val imm: InputMethodManager = activity.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager

        (if (activity.grid) binding.searchResultGrid else binding.searchResultList).alpha = 1f
        (if (!activity.grid) binding.searchResultGrid else binding.searchResultList).alpha = 0.33f

        binding.searchGenre.setText(activity.intent.getStringExtra("genre")?:"")
        binding.searchGenre.setAdapter(ArrayAdapter(binding.root.context, R.layout.item_dropdown,(Anilist.genres?: mapOf()).keys.toTypedArray()))
        binding.searchSortBy.setText(activity.intent.getStringExtra("sortBy")?:"")
        binding.searchSortBy.setAdapter(ArrayAdapter(binding.root.context, R.layout.item_dropdown, Anilist.sortBy.keys.toTypedArray()))
        binding.searchTag.setAdapter(ArrayAdapter(binding.root.context, R.layout.item_dropdown, Anilist.tags?: arrayListOf()))

        fun searchTitle(){
            val search = if (binding.searchBarText.text.toString()!="") binding.searchBarText.text.toString() else null
            val genre = if (binding.searchGenre.text.toString()!="") arrayListOf(binding.searchGenre.text.toString()) else null
            val sortBy = if (binding.searchSortBy.text.toString()!="") Anilist.sortBy[binding.searchSortBy.text.toString()] else null
            val tag = if (binding.searchTag.text.toString()!="") arrayListOf(binding.searchTag.text.toString()) else null
            activity.search(search,genre,tag,sortBy,adult)

        }

        binding.searchBarText.doOnTextChanged { _, _, _, _ ->
            searchTitle()
        }

        binding.searchBarText.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    searchTitle()
                    binding.searchBarText.clearFocus()
                    imm.hideSoftInputFromWindow(binding.searchBarText.windowToken, 0)
                    true
                }
                else -> false
            }
        }
        binding.searchBar.setEndIconOnClickListener{ searchTitle() }
        binding.searchGenre.setOnItemClickListener { _, _, _, _ -> searchTitle() }
        binding.searchTag.setOnItemClickListener { _, _, _, _ -> searchTitle() }
        binding.searchSortBy.setOnItemClickListener { _, _, _, _ -> searchTitle() }

        binding.searchClear.setOnClickListener {
            binding.searchGenre.setText("")
            binding.searchTag.setText("")
            binding.searchSortBy.setText("")
            searchTitle()
        }

        binding.searchResultGrid.setOnClickListener {
            it.alpha = 1f
            binding.searchResultList.alpha = 0.33f
            activity.grid = true
            saveData("searchGrid",true)
            activity.recycler()
        }
        binding.searchResultList.setOnClickListener {
            it.alpha = 1f
            binding.searchResultGrid.alpha = 0.33f
            activity.grid = false
            saveData("searchGrid",false)
            activity.recycler()
        }

    }

    override fun getItemCount(): Int = 1

    inner class SearchHeaderViewHolder(val binding: ItemSearchHeaderBinding):RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return itemViewType
    }
}