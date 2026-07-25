package com.example.data.source

import android.content.Context
import android.util.Log
import com.example.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class InterestCard(
    val id: String,
    val name: String,
    val icon: String,
    val category: String,
    val colorHex: String
)

object UserInterestManager {
    private const val TAG = "UserInterestManager"
    private const val PREFS_NAME = "lovelink_user_prefs"
    private const val KEY_COMPLETED = "has_completed_interest_selection"
    private const val KEY_INTERESTS = "user_selected_interests"

    val ALL_INTERESTS = listOf(
        InterestCard("travel", "Travel", "✈️", "Lifestyle", "#FF6B6B"),
        InterestCard("fitness", "Fitness", "🏋️", "Health", "#4ECDC4"),
        InterestCard("movies", "Movies", "🎬", "Entertainment", "#FFD166"),
        InterestCard("music", "Music", "🎵", "Entertainment", "#06D6A0"),
        InterestCard("photography", "Photography", "📸", "Art", "#118AB2"),
        InterestCard("food", "Food", "🍕", "Lifestyle", "#EF476F"),
        InterestCard("fashion", "Fashion", "👗", "Lifestyle", "#F72585"),
        InterestCard("gaming", "Gaming", "🎮", "Entertainment", "#7209B7"),
        InterestCard("sports", "Sports", "⚽", "Health", "#3A0CA3"),
        InterestCard("business", "Business", "💼", "Career", "#4361EE"),
        InterestCard("technology", "Technology", "💻", "Tech", "#4CC9F0"),
        InterestCard("nature", "Nature", "🌿", "Outdoors", "#2A9D8F"),
        InterestCard("pets", "Pets", "🐶", "Lifestyle", "#E9C46A"),
        InterestCard("books", "Books", "📚", "Education", "#F4A261"),
        InterestCard("dance", "Dance", "💃", "Arts", "#E76F51"),
        InterestCard("art", "Art", "🎨", "Arts", "#9D4EDD"),
        InterestCard("adventure", "Adventure", "🧗", "Outdoors", "#FF9F1C"),
        InterestCard("cars", "Cars", "🚗", "Hobbies", "#CB99C9"),
        InterestCard("bikes", "Bikes", "🏍️", "Hobbies", "#7F96FF"),
        InterestCard("cricket", "Cricket", "🏏", "Sports", "#00B4D8"),
        InterestCard("football", "Football", "⚽", "Sports", "#52B788"),
        InterestCard("cooking", "Cooking", "👨‍🍳", "Lifestyle", "#FF9E00"),
        InterestCard("gym", "Gym", "🏋️‍♂️", "Health", "#D00000"),
        InterestCard("anime", "Anime", "⛩️", "Entertainment", "#8338EC"),
        InterestCard("coffee", "Coffee", "☕", "Food", "#B5E48C"),
        InterestCard("shopping", "Shopping", "🛍️", "Lifestyle", "#FFC6FF"),
        InterestCard("hiking", "Hiking", "🥾", "Outdoors", "#70E000"),
        InterestCard("swimming", "Swimming", "🏊", "Sports", "#0077B6")
    )

    fun hasCompletedInterestSelection(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_COMPLETED, false)
    }

    fun getLocalInterests(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_INTERESTS, emptySet()) ?: emptySet()
    }

    fun saveLocalInterests(context: Context, interests: Set<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_COMPLETED, true)
            .putStringSet(KEY_INTERESTS, interests)
            .apply()
    }

    suspend fun saveInterestsToFirestore(
        context: Context,
        userId: String,
        interests: List<String>
    ): Boolean {
        // First save locally
        saveLocalInterests(context, interests.toSet())

        // Save to Firebase Firestore
        return try {
            val db = FirebaseFirestore.getInstance()
            val updates = mapOf(
                "interests" to interests,
                "hasCompletedInterestSelection" to true,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("users").document(userId).update(updates).await()
            Log.d(TAG, "Successfully updated interests in Firestore for user: $userId")
            true
        } catch (e: Throwable) {
            Log.w(TAG, "Firestore update warning (using local fallback): ${e.message}")
            true // fallback success locally
        }
    }
}
