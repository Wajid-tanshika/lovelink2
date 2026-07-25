package com.example.ui.navigation

sealed class NavRoutes(val route: String) {
    object Splash : NavRoutes("splash")
    object Welcome : NavRoutes("welcome")
    object Login : NavRoutes("login")
    object Register : NavRoutes("register")
    object ForgotPassword : NavRoutes("forgot_password")
    object Onboarding : NavRoutes("onboarding")
    
    // Main Tabs
    object Home : NavRoutes("home")
    object Explore : NavRoutes("explore")
    object Feed : NavRoutes("feed")
    object Matches : NavRoutes("matches")
    object Notifications : NavRoutes("notifications")
    object Profile : NavRoutes("profile")

    // Sub screens
    object CreatePost : NavRoutes("create_post")

    // Calling, Stories & Live Streaming
    object Call : NavRoutes("call/{partnerId}/{isVideo}") {
        fun createRoute(partnerId: String, isVideo: Boolean) = "call/$partnerId/$isVideo"
    }
    object CallHistory : NavRoutes("call_history")
    object CreateStory : NavRoutes("create_story")
    object StoryViewer : NavRoutes("story_viewer/{userId}") {
        fun createRoute(userId: String) = "story_viewer/$userId"
    }
    object StartLive : NavRoutes("start_live")
    object LiveStream : NavRoutes("live_stream/{streamId}") {
        fun createRoute(streamId: String) = "live_stream/$streamId"
    }

    // Sub screens
    object ChatDetail : NavRoutes("chat_detail/{matchId}") {
        fun createRoute(matchId: String) = "chat_detail/$matchId"
    }
    object EditProfile : NavRoutes("edit_profile")
    object DiamondStore : NavRoutes("diamond_store")
    object PremiumPlans : NavRoutes("premium_plans")
    object Settings : NavRoutes("settings")
    object About : NavRoutes("about")
    object AdminPanel : NavRoutes("admin_panel")
}
