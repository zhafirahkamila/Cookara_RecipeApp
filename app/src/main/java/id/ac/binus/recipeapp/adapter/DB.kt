package id.ac.binus.recipeapp.adapter

import android.content.ContentValues
import android.content.Context
import id.ac.binus.recipeapp.model.Ingredients
import id.ac.binus.recipeapp.model.Recipe
import kotlin.random.Random

class DB {
    companion object {
//        var userList = mutableListOf<User>()
//        var HAS_SYNC = false
//        var LOGGED_IN_USER: User? = null
//
//        fun syncData(ctx: Context) {
//            if (HAS_SYNC) return
//
//            val helper = Helper(ctx)
//            val db = helper.readableDatabase
//            val cursor = db.rawQuery("SELECT * FROM user", null)
//            userList.clear()
//
//            while (cursor.moveToNext()) {
//                val id = cursor.getInt(0)
//                val email = cursor.getString(1)
//                val username = cursor.getString(2)
//                val password = cursor.getString(3)
//
//                val temp = User(id, email, username, password)
//                userList.add(temp)
//            }
//
//            cursor.close()
//            db.close()
//            HAS_SYNC = true
//        }
//
//        fun insertNewUser(ctx: Context, email: String, username: String, password: String) {
//            var id = 1
//            if (userList.isNotEmpty()) {
//                id = userList.last().id + 1
//            }
//
//            val temp = User(id, email, username, password)
//            userList.add(temp)
//
//            val helper = Helper(ctx)
//            val db = helper.writableDatabase
//
//            val values = ContentValues().apply {
//                put("email", email)
//                put("username", username)
//                put("password", password)
//            }
//
//            db.insert("user", null, values)
//
//            HAS_SYNC = false // Refresh data on next sync
//        }
//
//        fun login(ctx: Context, email: String, username: String, password: String) {
//            // Always refresh data before login
//            HAS_SYNC = false
//            syncData(ctx)
//
//            for (user in userList) {
//                if (user.email == email && user.username == username && user.password == password) {
//                    LOGGED_IN_USER = user
//                    Log.d("LoginCheck", "Logged in as userId: ${user.id}")
//                    return
//                }
//            }
//            LOGGED_IN_USER = null // Explicitly set to null if not found
//        }

        fun getOrCreateRecipe(
            ctx: Context,
            name: String,
            category: String,
            imageResId: String,
            instructions: String,
            videos: String,
            ingredients: List<Ingredients> = listOf()
        ): Recipe {
            val helper = Helper(ctx)
            val db = helper.readableDatabase

            val cursor = db.rawQuery("SELECT * FROM recipe WHERE name = ?", arrayOf(name))
            var recipe: Recipe? = null

            if (cursor.moveToFirst()) {
                // Recipe already exists
                val rating = cursor.getDouble(2)
                val duration = cursor.getString(3)
                recipe = Recipe(name, category, rating, duration, imageResId, instructions, videos, ingredients)
            }

            cursor.close()
            db.close()

            if (recipe != null) return recipe

            // If not exists, generate new rating and duration
            val (minMinutes, maxMinutes) = when (category) {
                "Dessert" -> 15 to 45
                "Beef", "Chicken", "Pork" -> 60 to 120
                "Seafood" -> 30 to 90
                "Vegetarian" -> 20 to 60
                "Pasta" -> 15 to 45
                "Breakfast" -> 10 to 30
                "Goat", "Lamb" -> 90 to 120
                "Side", "Starter" -> 10 to 25
                "Miscellaneous" -> 20 to 60
                else -> 20 to 60
            }

            val totalMinutes = (minMinutes..maxMinutes).random()
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            val duration = when {
                hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
                hours > 0 -> "${hours}h"
                else -> "${minutes}m"
            }

            val rating = (Random.nextDouble(3.0, 5.0) * 10).toInt() / 10.0

            recipe = Recipe(name, category, rating, duration, imageResId, instructions, videos)

            // Save to database
            val writableDb = helper.writableDatabase
            val values = ContentValues().apply {
                put("name", name)
                put("category", category)
                put("rating", rating)
                put("duration", duration)
                put("imageResId", imageResId)
            }
            writableDb.insert("recipe", null, values)
            writableDb.close()

            return recipe
        }
    }
}