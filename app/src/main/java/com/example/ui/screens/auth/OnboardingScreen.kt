package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.RosePrimary
import com.example.ui.viewmodel.AuthUiState
import com.example.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    authViewModel: AuthViewModel,
    onOnboardingComplete: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val uiState by authViewModel.uiState.collectAsState()

    var name by remember { mutableStateOf(currentUser?.name ?: "Alex Morgan") }
    var username by remember { mutableStateOf(currentUser?.username ?: "alex_morgan") }
    var age by remember { mutableIntStateOf(currentUser?.age ?: 24) }
    var dob by remember { mutableStateOf(currentUser?.dateOfBirth ?: "2002-05-15") }
    var gender by remember { mutableStateOf(currentUser?.gender ?: "Woman") }
    var lookingFor by remember { mutableStateOf(currentUser?.lookingFor ?: "Long-term Relationship") }
    var bio by remember { mutableStateOf(currentUser?.bio ?: "Software designer & coffee enthusiast ☕. Always down for spontaneous weekend road trips!") }
    var country by remember { mutableStateOf(currentUser?.country ?: "United States") }
    var state by remember { mutableStateOf(currentUser?.state ?: "New York") }
    var city by remember { mutableStateOf(currentUser?.city ?: "New York") }

    val availableInterests = listOf(
        "Coffee", "Hiking", "Photography", "Art", "Music", "Yoga",
        "Foodie", "Travel", "Fitness", "Movies", "Baking", "Surfing"
    )
    val selectedInterests = remember { mutableStateListOf<String>().apply { addAll(currentUser?.interests.orEmpty().ifEmpty { listOf("Coffee", "Hiking", "Photography", "Travel") }) } }

    val defaultPhotos = remember {
        mutableStateListOf(
            "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=800&q=80"
        )
    }

    LaunchedEffect(Unit) {
        authViewModel.resetUiState()
    }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onOnboardingComplete()
            authViewModel.resetUiState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Complete Your Profile",
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Be authentic. Show your true personality to attract great matches.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }

            // 18+ Safety Notice Banner
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = RosePrimary.copy(alpha = 0.12f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, RosePrimary.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = "Safety", tint = RosePrimary)
                    Text(
                        text = "LoveLink is strictly for adults 18+. Members under 18 will be blocked immediately.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Basic Info
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Display Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it.lowercase().replace(" ", "") },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Age Picker (18+) & DOB
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Age (Must be 18+): $age",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { if (age > 18) age-- },
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) { Text("-", style = MaterialTheme.typography.titleLarge) }

                    Button(
                        onClick = { age++ },
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
                    ) { Text("+", style = MaterialTheme.typography.titleLarge) }
                }
            }

            OutlinedTextField(
                value = dob,
                onValueChange = { dob = it },
                label = { Text("Date of Birth (YYYY-MM-DD)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Location
            Text("Your Location:", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("Country") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Gender Selector
            Text("I identify as:", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Woman", "Man", "Non-Binary").forEach { item ->
                    val isSelected = gender == item
                    FilterChip(
                        selected = isSelected,
                        onClick = { gender = item },
                        label = { Text(item) },
                        leadingIcon = if (isSelected) { { Icon(Icons.Default.Check, contentDescription = null) } } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RosePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Looking For Selector
            Text("Relationship Preference:", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Long-term Relationship", "Dating & Romance", "Friendship & Social", "Casual Fun").forEach { target ->
                    val isSelected = lookingFor == target
                    FilterChip(
                        selected = isSelected,
                        onClick = { lookingFor = target },
                        label = { Text(target) },
                        leadingIcon = if (isSelected) { { Icon(Icons.Default.Check, contentDescription = null) } } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RosePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Bio
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("About You (Bio)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(12.dp)
            )

            // Interests Selection
            Text("Select Interests (at least 2):", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableInterests.forEach { interest ->
                    val isSelected = selectedInterests.contains(interest)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) selectedInterests.remove(interest) else selectedInterests.add(interest)
                        },
                        label = { Text(interest) },
                        leadingIcon = if (isSelected) { { Icon(Icons.Default.Check, contentDescription = null) } } else null
                    )
                }
            }

            // Photos Preview
            Text("Profile Photos (Minimum 2 required):", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(defaultPhotos) { photoUrl ->
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Profile Photo",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(2.dp, RosePrimary, RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            if (uiState is AuthUiState.Error) {
                Text(
                    text = (uiState as AuthUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Complete Button
            Button(
                onClick = {
                    authViewModel.completeProfile(
                        displayName = name,
                        username = username,
                        gender = gender,
                        dob = dob,
                        age = age,
                        bio = bio,
                        photos = defaultPhotos.toList(),
                        interests = selectedInterests.toList(),
                        country = country,
                        state = state,
                        city = city,
                        lookingFor = lookingFor
                    )
                },
                enabled = uiState !is AuthUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Save & Start Swiping ❤️", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                }
            }
        }
    }
}

