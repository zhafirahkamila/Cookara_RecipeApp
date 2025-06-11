package id.ac.binus.recipeapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.ac.binus.recipeapp.R
import id.ac.binus.recipeapp.model.Category

class CategoryAdapter(private val items: MutableList<Category>) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.ivCategory)
        val textView: TextView = itemView.findViewById(R.id.tvCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = items[position]
        holder.textView.text = item.name

        Glide.with(holder.itemView.context)
            .load(item.imageResId)
            .into(holder.imageView)
    }

//    fun updateList(newList: List<Category>) {
//        items.clear()
//        items.addAll(newList)
//        notifyDataSetChanged()
//    }

    override fun getItemCount() = items.size
}
