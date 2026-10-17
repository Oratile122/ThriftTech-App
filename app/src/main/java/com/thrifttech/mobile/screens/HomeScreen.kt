package com.thrifttech.mobile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.thrifttech.mobile.ui.theme.*

@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onGoToProducts: () -> Unit,
    onGoToSell: () -> Unit,
    onGoToProfile: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid ?: ""
    val userEmail = auth.currentUser?.email ?: ""

    var userName by remember { mutableStateOf("") }
    var productCount by remember { mutableStateOf(0) }
    var userCount by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    // Refresh data every time the screen is shown
    LaunchedEffect(Unit) {
        isLoading = true

        // 1. Load current user's full name from Firestore
        if (userId.isNotEmpty()) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { doc ->
                    userName = doc.getString("fullName") ?: userEmail.substringBefore("@")
                }
        }

        // 2. Count products
        db.collection("products").get()
            .addOnSuccessListener { result ->
                productCount = result.size()
                isLoading = false
            }

        // 3. Count users
        db.collection("users").get()
            .addOnSuccessListener { result ->
                userCount = result.size()
            }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = ThriftNavy) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ===== TOP HEADER =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(ThriftTeal),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("TT", color = ThriftNavy, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Thrift Tech", color = ThriftTeal,
                            fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Secondhand clothing — ZAR",
                            color = ThriftLightGray, fontSize = 11.sp)
                    }
                }

                TextButton(
                    onClick = {
                        auth.signOut()
                        onLogout()
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Logout", color = ThriftLightGray, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ===== WELCOME CARD =====
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(ThriftLightNavy)
                    .padding(24.dp)
            ) {
                Text("Welcome back,",
                    color = ThriftLightGray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = userName.ifEmpty { "..." },
                    color = ThriftWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(userEmail, color = ThriftLightGray, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(20.dp))

                // Real stats from Firestore
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatBox(
                        label = "Products",
                        value = if (isLoading) "..." else productCount.toString()
                    )
                    StatBox(
                        label = "Users",
                        value = if (isLoading) "..." else userCount.toString()
                    )
                    StatBox(
                        label = "Status",
                        value = "Active"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "What would you like to do?",
                color = ThriftWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            ActionCard(
                title = "Browse Products",
                subtitle = "See what's for sale right now",
                icon = "🛍",
                onClick = onGoToProducts
            )

            Spacer(modifier = Modifier.height(12.dp))

            ActionCard(
                title = "Sell an Item",
                subtitle = "Post your second-hand clothes",
                icon = "🏷",
                onClick = onGoToSell
            )

            Spacer(modifier = Modifier.height(12.dp))

            ActionCard(
                title = "My Profile",
                subtitle = "Update your name, phone and details",
                icon = "👤",
                onClick = onGoToProfile
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "© 2026 Thrift Tech | Braamfontein",
                color = ThriftLightGray.copy(alpha = 0.5f),
                fontSize = 11.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StatBox(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = ThriftTeal,
            fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, color = ThriftLightGray, fontSize = 11.sp)
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    icon: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        colors = CardDefaults.cardColors(containerColor = ThriftLightNavy),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ThriftTeal),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = ThriftWhite,
                    fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, color = ThriftLightGray, fontSize = 12.sp)
            }

            Text("›", color = ThriftTeal, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
    }
}