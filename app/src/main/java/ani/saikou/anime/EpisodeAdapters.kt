package ani.saikou.anime

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import ani.saikou.databinding.ItemEpisodeCompactBinding
import ani.saikou.databinding.ItemEpisodeGridBinding
import ani.saikou.databinding.ItemEpisodeListBinding
import ani.saikou.loadData
import ani.saikou.media.Media
import ani.saikou.setAnimation
import ani.saikou.updateAnilistProgress
import com.squareup.picasso.Picasso

fun handleProgress(cont:LinearLayout,bar:View,empty:View,mediaId:Int,ep:String){
    val curr = loadData<Long>("${mediaId}_${ep}")
    val max = loadData<Long>("${mediaId}_${ep}_max")
    if(curr!=null && max!=null){
        cont.visibility=View.VISIBLE
        val div = curr.toFloat()/max
        val barParams = bar.layoutParams as LinearLayout.LayoutParams
        barParams.weight = div
        bar.layoutParams = barParams
        val params = empty.layoutParams as LinearLayout.LayoutParams
        params.weight = 1-div
        empty.layoutParams = params
    }else{
        cont.visibility = View.GONE
    }
}

class EpisodeAdapter(
    private var type:Int,
    private val media: Media,
    private val fragment: AnimeWatchFragment,
    var arr: List<Episode> = arrayListOf()
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(type){
            0 -> EpisodeListViewHolder(ItemEpisodeListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            1 -> EpisodeGridViewHolder(ItemEpisodeGridBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            2 -> EpisodeCompactViewHolder(ItemEpisodeCompactBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else->throw IllegalArgumentException()
        }
    }

    override fun getItemViewType(position: Int): Int {
        println("item $position - type $type")
        return type
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        println("$position - ${holder.itemViewType} & $type")
        when (holder.itemViewType) {
            0 -> {
                val binding = (holder as EpisodeListViewHolder).binding
                setAnimation(fragment.requireContext(),holder.binding.root)
                val ep = arr[position]
                Picasso.get().load(ep.thumb?:media.cover).resize(400,0).into(binding.itemEpisodeImage)
                binding.itemEpisodeNumber.text = ep.number
                if(ep.filler){
                    binding.itemEpisodeFiller.visibility = View.VISIBLE
                    binding.itemEpisodeFillerView.visibility = View.VISIBLE
                }else{
                    binding.itemEpisodeFiller.visibility = View.GONE
                    binding.itemEpisodeFillerView.visibility = View.GONE
                }
                binding.itemEpisodeDesc.visibility = if (ep.desc!=null && ep.desc?.trim(' ')!="") View.VISIBLE else View.GONE
                binding.itemEpisodeDesc.text = ep.desc?:""
                binding.itemEpisodeTitle.text = ep.title?:media.userPreferredName
                if (media.userProgress!=null) {
                    if (ep.number.toFloatOrNull()?:9999f<=media.userProgress!!.toFloat()) {
                        binding.root.alpha = 0.1f
                        println("ep: ${ep.number} - watched, ${binding.root.alpha}")
                        binding.itemEpisodeViewed.visibility = View.VISIBLE
                    } else{
                        println("ep: ${ep.number} - not watched, ${binding.root.alpha}")
                        binding.itemEpisodeViewed.visibility = View.GONE
                        binding.root.setOnLongClickListener{
                            updateAnilistProgress(media.id, ep.number)
                            true
                        }
                    }
                }else{
                    binding.itemEpisodeViewed.visibility = View.GONE
                }

                handleProgress(binding.itemEpisodeProgressCont,binding.itemEpisodeProgress,binding.itemEpisodeProgressEmpty,media.id,ep.number)
            }

            1 -> {
                val binding = (holder as EpisodeGridViewHolder).binding
                setAnimation(fragment.requireContext(), binding.root)
                val ep = arr[position]
                Picasso.get().load(ep.thumb ?: media.cover).resize(400, 0)
                    .into(binding.itemEpisodeImage)
                binding.itemEpisodeNumber.text = ep.number
                binding.itemEpisodeTitle.text = ep.title ?: media.name
                if (ep.filler) {
                    binding.itemEpisodeFiller.visibility = View.VISIBLE
                    binding.itemEpisodeFillerView.visibility = View.VISIBLE
                } else {
                    binding.itemEpisodeFiller.visibility = View.GONE
                    binding.itemEpisodeFillerView.visibility = View.GONE
                }
                if (media.userProgress != null) {
                    if (ep.number.toFloatOrNull() ?: 9999f <= media.userProgress!!.toFloat()) {
                        binding.root.alpha = 0.1f
                        binding.itemEpisodeViewed.visibility = View.VISIBLE
                    } else {
                        binding.root.setOnLongClickListener {
                            updateAnilistProgress(media.id, ep.number)
                            true
                        }
                    }
                }
                handleProgress(
                    binding.itemEpisodeProgressCont,
                    binding.itemEpisodeProgress,
                    binding.itemEpisodeProgressEmpty,
                    media.id,
                    ep.number
                )
            }

            2 -> {
                val binding = (holder as EpisodeCompactViewHolder).binding
                setAnimation(fragment.requireContext(),holder.binding.root)
                val ep = arr[position]
                binding.itemEpisodeNumber.text = ep.number
                binding.itemEpisodeFillerView.visibility = if (ep.filler)  View.VISIBLE else View.GONE
                if (media.userProgress!=null) {
                    if (ep.number.toFloatOrNull()?:9999f<=media.userProgress!!.toFloat())
                        binding.root.alpha = 0.1f
                    else{
                        binding.root.setOnLongClickListener{
                            updateAnilistProgress(media.id, ep.number)
                            true
                        }
                    }
                }
                handleProgress(binding.itemEpisodeProgressCont,binding.itemEpisodeProgress,binding.itemEpisodeProgressEmpty,media.id,ep.number)
            }
        }
    }

    override fun getItemCount(): Int = arr.size

    inner class EpisodeCompactViewHolder(val binding: ItemEpisodeCompactBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                fragment.onEpisodeClick(arr[bindingAdapterPosition].number)
            }
        }
    }

    inner class EpisodeGridViewHolder(val binding: ItemEpisodeGridBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                fragment.onEpisodeClick(arr[bindingAdapterPosition].number)
            }
        }
    }

    inner class EpisodeListViewHolder(val binding: ItemEpisodeListBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                fragment.onEpisodeClick(arr[bindingAdapterPosition].number)
            }
            binding.itemEpisodeDesc.setOnClickListener {
                if(binding.itemEpisodeDesc.maxLines == 3)
                    binding.itemEpisodeDesc.maxLines = 100
                else
                    binding.itemEpisodeDesc.maxLines = 3
            }
        }
    }

    fun updateType(t:Int,name:String){

        println("$type updated to $t - $name")
        type = t
    }
}


