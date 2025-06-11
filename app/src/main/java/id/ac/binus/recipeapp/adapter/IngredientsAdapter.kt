package id.ac.binus.recipeapp.adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.ac.binus.recipeapp.R
import id.ac.binus.recipeapp.model.Ingredients

class IngredientsAdapter (private val items: List<Ingredients>) :
    RecyclerView.Adapter<IngredientsAdapter.IngredientsViewHolder>() {

    inner class IngredientsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIngredients: ImageView = view.findViewById(R.id.ivIngredients)
        val nameIngredient: TextView = view.findViewById(R.id.nameIngredient)
        val tvGram: TextView = view.findViewById(R.id.tvGram)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ingredients, parent, false)
        return IngredientsViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientsViewHolder, position: Int) {
        val item = items[position]
        holder.nameIngredient.text = item.name
        holder.tvGram.text = item.gram.toString()

        val imageResId = "https://www.themealdb.com/images/ingredients/${item.name}-Small.png"
        Glide.with(holder.itemView.context)
            .load(imageResId)
            .into(holder.ivIngredients)
    }

    override fun getItemCount() = items.size
}