package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.model.UserProfile
import com.example.data.source.SampleData
import com.example.ui.components.BottomNavBar
import com.example.ui.components.TopNavBar
import com.example.ui.navigation.NavRoutes
import com.example.ui.screens.admin.AdminScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.OnboardingScreen
import com.example.ui.screens.chat.ChatScreen
import com.example.ui.screens.diamonds.DiamondStoreScreen
import com.example.ui.screens.explore.ExploreScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.matches.MatchesScreen
import com.example.ui.screens.notifications.NotificationScreen
import com.example.ui.screens.premium.PremiumScreen
import com.example.ui.screens.profile.EditProfileScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.settings.AboutScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.data.repository.FeedRepository
import com.example.data.repository.NotificationRepository
import com.example.data.repository.UserRepository
import com.example.ui.screens.feed.CreatePostScreen
import com.example.ui.screens.feed.FeedScreen
import com.example.ui.screens.feed.FeedViewModel
import com.example.ui.screens.feed.FeedViewModelFactory
import com.example.ui.theme.LoveLinkTheme
import com.example.ui.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var isDarkMode by remember { mutableStateOf(false) }

            LoveLinkTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoveLinkApp(
                        isDarkMode = isDarkMode,
                        onToggleDarkMode = { isDarkMode = it }
                    )
                }
            }
        }
    }
}

@Composable
fun LoveLinkApp(
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    val navController = rememberNavController()

    // ViewModels
    val authViewModel: AuthViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()
    val exploreViewModel: ExploreViewModel = viewModel()
    val matchesViewModel: MatchesViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()
    val diamondViewModel: DiamondViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    val adminViewModel: AdminViewModel = viewModel()
    val callViewModel: com.example.ui.screens.call.CallViewModel = viewModel()
    val storyViewModel: com.example.ui.screens.story.StoryViewModel = viewModel()
    val liveStreamViewModel: com.example.ui.screens.livestream.LiveStreamViewModel = viewModel()
    val feedViewModel: FeedViewModel = viewModel(
        factory = FeedViewModelFactory(
            feedRepository = FeedRepository(notificationRepository = NotificationRepository()),
            userRepository = UserRepository()
        )
    )

    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val isProfileCompleted by authViewModel.isProfileCompleted.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val diamondBalance by diamondViewModel.balance.collectAsState()
    val isPremium by diamondViewModel.isPremium.collectAsState()
    val matchesList by matchesViewModel.matches.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavRoutes.Splash.route

    val mainTabs = listOf(
        NavRoutes.Home.route,
        NavRoutes.Feed.route,
        NavRoutes.Matches.route,
        NavRoutes.Profile.route
    )

    val showBottomBar = currentRoute in mainTabs
    val showTopBar = currentRoute in mainTabs

    var isHeaderVisible by remember { mutableStateOf(true) }
    LaunchedEffect(currentRoute) {
        isHeaderVisible = true
    }

    val userProfile = currentUser ?: SampleData.CURRENT_USER

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = showTopBar && isHeaderVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                TopNavBar(
                    diamondBalance = diamondBalance,
                    onDiamondClick = { navController.navigate(NavRoutes.DiamondStore.route) },
                    onNotificationClick = { navController.navigate(NavRoutes.Notifications.route) },
                    onAdminClick = { navController.navigate(NavRoutes.AdminPanel.route) },
                    isAdmin = userProfile.isAdmin
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                popUpTo(NavRoutes.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    unreadMatchesCount = matchesList.sumOf { it.unreadCounts[userProfile.id] ?: 0 }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Splash
            composable(NavRoutes.Splash.route) {
                SplashScreen(
                    isLoggedIn = isLoggedIn,
                    isProfileCompleted = isProfileCompleted,
                    onNavigateNext = { route ->
                        navController.navigate(route) {
                            popUpTo(NavRoutes.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // Welcome Screen
            composable(NavRoutes.Welcome.route) {
                com.example.ui.screens.auth.WelcomeScreen(
                    authViewModel = authViewModel,
                    onNavigateToLogin = {
                        navController.navigate(NavRoutes.Login.route)
                    },
                    onNavigateToRegister = {
                        navController.navigate(NavRoutes.Register.route)
                    },
                    onGoogleSuccess = {
                        val targetRoute = if (isProfileCompleted) NavRoutes.Home.route else NavRoutes.Onboarding.route
                        navController.navigate(targetRoute) {
                            popUpTo(NavRoutes.Welcome.route) { inclusive = true }
                        }
                    }
                )
            }

            // Login
            composable(NavRoutes.Login.route) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = {
                        val targetRoute = if (isProfileCompleted) NavRoutes.Home.route else NavRoutes.Onboarding.route
                        navController.navigate(targetRoute) {
                            popUpTo(NavRoutes.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(NavRoutes.Register.route)
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(NavRoutes.ForgotPassword.route)
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            // Register
            composable(NavRoutes.Register.route) {
                com.example.ui.screens.auth.RegisterScreen(
                    authViewModel = authViewModel,
                    onRegisterSuccess = {
                        navController.navigate(NavRoutes.Onboarding.route) {
                            popUpTo(NavRoutes.Register.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(NavRoutes.Login.route)
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            // Forgot Password
            composable(NavRoutes.ForgotPassword.route) {
                com.example.ui.screens.auth.ForgotPasswordScreen(
                    authViewModel = authViewModel,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            // Onboarding Profile Creation
            composable(NavRoutes.Onboarding.route) {
                OnboardingScreen(
                    authViewModel = authViewModel,
                    onOnboardingComplete = {
                        navController.navigate(NavRoutes.Home.route) {
                            popUpTo(NavRoutes.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            // Home / Swiping Discovery
            composable(NavRoutes.Home.route) {
                HomeScreen(
                    homeViewModel = homeViewModel,
                    currentUser = userProfile,
                    onNavigateToChat = { matchId ->
                        navController.navigate(NavRoutes.ChatDetail.createRoute(matchId))
                    },
                    onNavigateToDiamondStore = {
                        navController.navigate(NavRoutes.DiamondStore.route)
                    },
                    onNavigateToProfileInfo = { profile ->
                        navController.navigate(NavRoutes.Profile.route)
                    }
                )
            }

            // Explore / Search Grid & Interest Selection
            composable(NavRoutes.Explore.route) {
                ExploreScreen(
                    currentUser = userProfile,
                    onNavigateToHomeFeed = {
                        navController.navigate(NavRoutes.Home.route) {
                            popUpTo(NavRoutes.Explore.route) { inclusive = true }
                        }
                    },
                    onBackClick = if (navController.previousBackStackEntry != null) {
                        { navController.popBackStack() }
                    } else null
                )
            }

            // Social Feed Screen
            composable(NavRoutes.Feed.route) {
                FeedScreen(
                    feedViewModel = feedViewModel,
                    storyViewModel = storyViewModel,
                    currentUser = userProfile,
                    onCreatePostClick = {
                        navController.navigate(NavRoutes.CreatePost.route)
                    },
                    onAddStoryClick = {
                        navController.navigate(NavRoutes.CreateStory.route)
                    },
                    onStoryClick = { userId ->
                        navController.navigate(NavRoutes.StoryViewer.createRoute(userId))
                    },
                    onStartLiveClick = {
                        navController.navigate(NavRoutes.StartLive.route)
                    },
                    onNavigateToChat = { matchId ->
                        navController.navigate(NavRoutes.ChatDetail.createRoute(matchId))
                    },
                    onUserProfileClick = { userId ->
                        navController.navigate(NavRoutes.Profile.route)
                    },
                    onHeaderVisibilityChange = { visible ->
                        isHeaderVisible = visible
                    }
                )
            }

            // Create Post Screen
            composable(NavRoutes.CreatePost.route) {
                CreatePostScreen(
                    feedViewModel = feedViewModel,
                    currentUser = userProfile,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            // Calling Routes
            composable(
                route = NavRoutes.Call.route,
                arguments = listOf(
                    navArgument("partnerId") { type = NavType.StringType },
                    navArgument("isVideo") { type = NavType.BoolType }
                )
            ) { backStackEntry ->
                val partnerId = backStackEntry.arguments?.getString("partnerId") ?: ""
                val isVideo = backStackEntry.arguments?.getBoolean("isVideo") ?: false
                val partner = SampleData.PROFILES.firstOrNull { it.id == partnerId } ?: SampleData.PROFILES.first()

                com.example.ui.screens.call.CallScreen(
                    callViewModel = callViewModel,
                    currentUser = userProfile,
                    partner = partner,
                    isVideo = isVideo,
                    onEndCallClick = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.CallHistory.route) {
                com.example.ui.screens.call.CallHistoryScreen(
                    callViewModel = callViewModel,
                    currentUser = userProfile,
                    onBackClick = { navController.popBackStack() },
                    onStartCall = { partnerId, isVideo ->
                        navController.navigate(NavRoutes.Call.createRoute(partnerId, isVideo))
                    }
                )
            }

            // Stories Routes
            composable(NavRoutes.CreateStory.route) {
                com.example.ui.screens.story.CreateStoryScreen(
                    storyViewModel = storyViewModel,
                    currentUser = userProfile,
                    onBackClick = { navController.popBackStack() },
                    onStoryCreated = { navController.popBackStack() }
                )
            }

            composable(
                route = NavRoutes.StoryViewer.route,
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                val groups = storyViewModel.storyGroups.collectAsState().value
                val groupIndex = groups.indexOfFirst { it.authorId == userId }.coerceAtLeast(0)

                com.example.ui.screens.story.StoryViewerScreen(
                    storyViewModel = storyViewModel,
                    initialGroupIndex = groupIndex,
                    currentUser = userProfile,
                    onCloseClick = { navController.popBackStack() }
                )
            }

            // Live Streaming Routes
            composable(NavRoutes.StartLive.route) {
                com.example.ui.screens.livestream.StartLiveScreen(
                    liveStreamViewModel = liveStreamViewModel,
                    currentUser = userProfile,
                    onBackClick = { navController.popBackStack() },
                    onLiveStarted = {
                        val currentStream = liveStreamViewModel.currentStream.value
                        if (currentStream != null) {
                            navController.navigate(NavRoutes.LiveStream.createRoute(currentStream.id)) {
                                popUpTo(NavRoutes.StartLive.route) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(
                route = NavRoutes.LiveStream.route,
                arguments = listOf(navArgument("streamId") { type = NavType.StringType })
            ) {
                com.example.ui.screens.livestream.LiveStreamScreen(
                    liveStreamViewModel = liveStreamViewModel,
                    currentUser = userProfile,
                    diamondBalance = diamondBalance,
                    onNavigateToDiamondStore = { navController.navigate(NavRoutes.DiamondStore.route) },
                    onCloseClick = { navController.popBackStack() }
                )
            }

            // Matches & Chat List
            composable(NavRoutes.Matches.route) {
                MatchesScreen(
                    matchesViewModel = matchesViewModel,
                    currentUser = userProfile,
                    isPremium = isPremium,
                    onMatchClick = { match ->
                        navController.navigate(NavRoutes.ChatDetail.createRoute(match.id))
                    },
                    onNavigateToPremium = {
                        navController.navigate(NavRoutes.PremiumPlans.route)
                    }
                )
            }

            // Chat Detail
            composable(
                route = NavRoutes.ChatDetail.route,
                arguments = listOf(navArgument("matchId") { type = NavType.StringType })
            ) { backStackEntry ->
                val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
                val match = matchesList.firstOrNull { it.id == matchId }
                if (match != null) {
                    val otherUser = match.getOtherUser(userProfile.id)
                    ChatScreen(
                        chatViewModel = chatViewModel,
                        match = match,
                        currentUser = userProfile,
                        onStartCallClick = { isVideo ->
                            navController.navigate(NavRoutes.Call.createRoute(otherUser.id, isVideo))
                        },
                        onNavigateToCallHistory = {
                            navController.navigate(NavRoutes.CallHistory.route)
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }

            // Notifications
            composable(NavRoutes.Notifications.route) {
                NotificationScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Profile View
            composable(NavRoutes.Profile.route) {
                ProfileScreen(
                    profileViewModel = profileViewModel,
                    onNavigateToEditProfile = { navController.navigate(NavRoutes.EditProfile.route) },
                    onNavigateToDiamondStore = { navController.navigate(NavRoutes.DiamondStore.route) },
                    onNavigateToPremiumPlans = { navController.navigate(NavRoutes.PremiumPlans.route) },
                    onNavigateToSettings = { navController.navigate(NavRoutes.Settings.route) },
                    onNavigateToAdmin = { navController.navigate(NavRoutes.AdminPanel.route) },
                    onNavigateToOnboarding = { navController.navigate(NavRoutes.Onboarding.route) },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(NavRoutes.Login.route) {
                            popUpTo(0)
                        }
                    }
                )
            }

            // Edit Profile
            composable(NavRoutes.EditProfile.route) {
                EditProfileScreen(
                    profileViewModel = profileViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Diamond Store & Wallet
            composable(NavRoutes.DiamondStore.route) {
                DiamondStoreScreen(
                    diamondViewModel = diamondViewModel,
                    currentUser = userProfile,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Premium VIP Plans
            composable(NavRoutes.PremiumPlans.route) {
                PremiumScreen(
                    diamondViewModel = diamondViewModel,
                    currentUser = userProfile,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Settings & Privacy
            composable(NavRoutes.Settings.route) {
                SettingsScreen(
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = onToggleDarkMode,
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(NavRoutes.Login.route) {
                            popUpTo(0)
                        }
                    },
                    onBackClick = { navController.popBackStack() },
                    onNavigateToAbout = { navController.navigate(NavRoutes.About.route) },
                    onEditInterestsClick = { navController.navigate(NavRoutes.Explore.route) }
                )
            }

            // About LoveLink
            composable(NavRoutes.About.route) {
                AboutScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Admin Panel
            composable(NavRoutes.AdminPanel.route) {
                if (userProfile.isAdmin) {
                    AdminScreen(
                        adminViewModel = adminViewModel,
                        liveStreamViewModel = liveStreamViewModel,
                        storyViewModel = storyViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.navigate(NavRoutes.Home.route) {
                            popUpTo(NavRoutes.Home.route) { inclusive = false }
                        }
                    }
                }
            }
        }
    }
}
