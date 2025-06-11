package id.ac.binus.recipeapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.ac.binus.recipeapp.Favorite
import id.ac.binus.recipeapp.R

class FavoriteAdapter (private val items: List<Favorite>, private val onItemClick: (Favorite) -> Unit = {}, private val onFavoriteClick: (Favorite, position: Int) -> Unit) :
    RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>(){

    inner class FavoriteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivRecipe: ImageView = view.findViewById(R.id.ivRecipe)
        val tvTimer: TextView = view.findViewById(R.id.tvTimer)
        val txtRating: TextView = view.findViewById(R.id.txtRating)
        val tvNameRecipe: TextView = view.findViewById(R.id.tvNameRecipe)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvFav: ImageView = view.findViewById(R.id.tvFav)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val item = items[position]
        holder.tvNameRecipe.text = item.name
        holder.tvCategory.text = item.category
        holder.txtRating.text = "${item.rating}"
        holder.tvTimer.text = "${item.duration}"
        holder.tvFav.setBackgroundResource(R.drawable.fav_red)

        Glide.with(holder.itemView.context)
            .load(item.imageResId)
            .into(holder.ivRecipe)

        holder.itemView.setOnClickListener { onItemClick(item) }
        holder.tvFav.setOnClickListener { onFavoriteClick(item, position) }
    }

    override fun getItemCount() = items.size

}