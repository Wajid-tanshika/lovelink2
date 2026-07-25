package com.example.data.model

data class VirtualGift(
    val id: String,
    val name: String,
    val emoji: String,
    val diamondCost: Int,
    val description: String,
    val animationType: String = "POP"
) {
    companion object {
        val DEFAULT_GIFTS = listOf(
            VirtualGift("gift_heart", "Heart", "❤️", 10, "Send a sweet warm heart", "FLOAT"),
            VirtualGift("gift_rose", "Rose", "🌹", 50, "A romantic blooming rose", "FLOAT"),
            VirtualGift("gift_box", "Gift Box", "🎁", 100, "A mystery surprise gift box", "POP"),
            VirtualGift("gift_diamond", "Diamond", "💎", 200, "Shine like a precious gem", "SPARKLE"),
            VirtualGift("gift_crown", "Crown", "👑", 500, "Treat them like true royalty", "GLOW"),
            VirtualGift("gift_car", "Sports Car", "🚗", 1000, "Drive into their heart with speed", "ZOOM"),
            VirtualGift("gift_jet", "Private Jet", "✈️", 2500, "Fly them to romantic horizons", "FLY"),
            VirtualGift("gift_castle", "Royal Castle", "🏰", 5000, "Build an empire together", "EXPLODE")
        )
    }
}

