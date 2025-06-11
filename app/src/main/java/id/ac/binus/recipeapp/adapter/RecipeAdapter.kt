package id.ac.binus.recipeapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import id.ac.binus.recipeapp.R
import id.ac.binus.recipeapp.model.Recipe

class RecipeAdapter(private val items: List<Recipe>, private val onItemClick: (Recipe) -> Unit, private val onFavoriteClick: (Recipe) -> Unit) :
    RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>(){

    inner class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivRecipe: ImageView = view.findViewById(R.id.ivRecipe)
        val tvTimer: TextView = view.findViewById(R.id.tvTimer)
        val txtRating: TextView = view.findViewById(R.id.txtRating)
        val tvNameRecipe: TextView = view.findViewById(R.id.tvNameRecipe)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvFav: ImageView = view.findViewById(R.id.tvFav)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val item = items[position]
        holder.tvNameRecipe.text = item.name
        holder.tvCategory.text = item.category
        holder.txtRating.text = "${item.rating}"
        holder.tvTimer.text = "${item.duration}"

        Glide.with(holder.itemView.context)
            .load(item.imageResId)
            .into(holder.ivRecipe)

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }

        if (item.isFavorite) {
            holder.tvFav.setBackgroundResource(R.drawable.fav_red)
        } else {
            holder.tvFav.setBackgroundResource(R.drawable.favorite2)
        }

//        holder.tvFav.setOnClickListener {
//            item.isFavorite = !item.isFavorite
//            notifyItemChanged(position)
//            onFavoriteClick(item)
//
//            val user = FirebaseAuth.getInstance().currentUser
//            val db = FirebaseFirestore.getInstance()
//
//            user?.let {
//                val favRef = db.collection("favorite").document(it.uid).collection("recipes").document(item.name)
//
//                if (item.isFavorite) {
//                    // Save favorite
//                    val favoriteMap = hashMapOf(
//                        "name" to item.name,
//                        "category" to item.category,
//                        "rating" to item.rating,
//                        "duration" to item.duration,
//                        "image" to item.imageResId
//                    )
//                    favRef.set(favoriteMap)
//                } else {
//                    // Remove favorite
//                    favRef.delete()
//                }
//            }
//        }
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }

        holder.tvFav.setOnClickListener {
            onFavoriteClick(item)
        }

    }

    override fun getItemCount() = items.size
}