package id.ac.binus.recipeapp.model
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Ingredients(
    val name: String,
    val gram: String,
//    val imageResId: String
) : Parcelable
