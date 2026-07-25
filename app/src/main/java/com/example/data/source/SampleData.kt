package com.example.data.source

import com.example.data.model.UserProfile

object SampleData {
    val CURRENT_USER = UserProfile(
        id = "user_me",
        name = "Alex Morgan",
        email = "gwajji2212@gmail.com",
        age = 24,
        gender = "Man",
        lookingFor = "Long-term Relationship",
        bio = "Software designer & coffee enthusiast ☕. Always down for spontaneous weekend road trips and sunset hikes 🌅",
        interests = listOf("Coffee", "Design", "Hiking", "Photography", "Travel", "Music"),
        profession = "UX Designer at TechCorp",
        heightCm = 178,
        city = "San Francisco",
        distanceKm = 0.0,
        photoUrls = listOf(
            "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=800&q=80"
        ),
        isVerified = true,
        isOnline = true,
        isPremium = true,
        diamondBalance = 350,
        profileCompleted = true,
        isAdmin = true
    )

    val PROFILES = listOf(
        UserProfile(
            id = "user_1",
            name = "Sophia Chen",
            age = 23,
            gender = "Woman",
            lookingFor = "Long-term Relationship",
            bio = "Architectural designer dreaming of sustainable homes. Plant mom to 14 succulents 🌱. Let's explore local art galleries and hidden matcha spots!",
            interests = listOf("Art Galleries", "Plants", "Architecture", "Matcha", "Photography", "Yoga"),
            profession = "Architectural Designer",
            heightCm = 168,
            city = "San Francisco",
            distanceKm = 2.4,
            photoUrls = listOf(
                "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=800&q=80"
            ),
            isVerified = true,
            isOnline = true
        ),
        UserProfile(
            id = "user_2",
            name = "Elena Rostova",
            age = 25,
            gender = "Woman",
            lookingFor = "Dating & Romance",
            bio = "Classical violinist & indie music festival lover 🎻. Looking for someone who can match my energy and appreciates good acoustic sessions and cozy vinyl evenings.",
            interests = listOf("Music", "Violin", "Indie Rock", "Vinyl Records", "Wine Tasting"),
            profession = "Violin Instructor & Composer",
            heightCm = 172,
            city = "Oakland",
            distanceKm = 5.1,
            photoUrls = listOf(
                "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?auto=format&fit=crop&w=800&q=80"
            ),
            isVerified = true,
            isOnline = false
        ),
        UserProfile(
            id = "user_3",
            name = "Maya Lin",
            age = 22,
            gender = "Woman",
            lookingFor = "Casual & Fun",
            bio = "Foodie, baker, and aspiring pastry chef 🥐. Warning: I will test my latest matcha croissant recipes on you!",
            interests = listOf("Baking", "Pastry", "Foodie", "Desserts", "Cafe Hopping"),
            profession = "Pastry Chef",
            heightCm = 162,
            city = "San Jose",
            distanceKm = 8.7,
            photoUrls = listOf(
                "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=800&q=80"
            ),
            isVerified = true,
            isOnline = true
        ),
        UserProfile(
            id = "user_4",
            name = "Liam Vance",
            age = 26,
            gender = "Man",
            lookingFor = "Long-term Relationship",
            bio = "Fitness trainer & adventure seeker 🏋️‍♂️. Love bouldering, trail running, and late night taco runs.",
            interests = listOf("Bouldering", "Fitness", "Trail Running", "Tacos", "Camping"),
            profession = "Fitness Coach",
            heightCm = 185,
            city = "San Francisco",
            distanceKm = 3.1,
            photoUrls = listOf(
                "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=800&q=80"
            ),
            isVerified = false,
            isOnline = true
        ),
        UserProfile(
            id = "user_5",
            name = "Chloe Dubois",
            age = 24,
            gender = "Woman",
            lookingFor = "Dating & Romance",
            bio = "Fashion photographer living between SF & Paris 📸. obsessed with vintage film cameras and rooftop sunsets.",
            interests = listOf("Fashion", "Photography", "Film Cameras", "French", "Travel"),
            profession = "Fashion Photographer",
            heightCm = 175,
            city = "San Francisco",
            distanceKm = 1.8,
            photoUrls = listOf(
                "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80"
            ),
            isVerified = true,
            isOnline = true
        ),
        UserProfile(
            id = "user_6",
            name = "Zoe Rivera",
            age = 21,
            gender = "Woman",
            lookingFor = "Friendship & Dating",
            bio = "Marine biology student who spends weekends surfing 🏄‍♀️. Let me tell you cool facts about ocean whales!",
            interests = listOf("Surfing", "Marine Life", "Scuba Diving", "Environmentalism", "Beach"),
            profession = "Marine Biology Researcher",
            heightCm = 165,
            city = "Santa Cruz",
            distanceKm = 12.0,
            photoUrls = listOf(
                "https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?auto=format&fit=crop&w=800&q=80"
            ),
            isVerified = true,
            isOnline = true
        )
    )
}
