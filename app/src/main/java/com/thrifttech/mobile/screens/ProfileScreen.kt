package com.thrifttech.mobile.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.thrifttech.mobile.ui.theme.*

@Composable
fun ProfileScreen(onBack: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(auth.currentUser?.email ?: "") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (userId.isNotEmpty()) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        fullName = doc.getString("fullName") ?: ""
                        phone = doc.getString("phone") ?: ""
                        email = doc.getString("email") ?: auth.currentUser?.email ?: ""
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                    message = "Failed to load profile"
                }
        } else {
            isLoading = false
        }
    }

    // Initials for the avatar circle
    val initials = remember(fullName) {
        fullName.trim().split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercaseChar() }
            .joinToString("")
            .ifEmpty { "?" }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = ThriftNavy) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ===== TOP BAR =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onBack,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("← Back", color = ThriftTeal,
                        fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text("My Profile", color = ThriftWhite,
                    fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(50.dp))
            }

            // ===== SCROLLABLE CONTENT =====
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                if (isLoading) {
                    Spacer(modifier = Modifier.height(80.dp))
                    CircularProgressIndicator(color = ThriftTeal)
                } else {

                    // ===== AVATAR =====
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(ThriftTeal),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = ThriftNavy,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ===== NAME + EMAIL =====
                    Text(
                        text = fullName.ifBlank { "Your Name" },
                        color = ThriftWhite,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = email,
                        color = ThriftLightGray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // ===== INFO CARD =====
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(ThriftLightNavy)
                            .padding(20.dp)
                    ) {
                        Text("Edit Details",
                            color = ThriftTeal,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Full Name Field
                        ProfileFieldLabel("Full Name")
                        Spacer(modifier = Modifier.height(6.dp))
                        ThriftProfileField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            placeholder = "Enter your name"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Phone Field
                        ProfileFieldLabel("Phone Number")
                        Spacer(modifier = Modifier.height(6.dp))
                        ThriftProfileField(
                            value = phone,
                            onValueChange = { phone = it },
                            placeholder = "Enter your phone"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Email Field (Read-only)
                        ProfileFieldLabel("Email (cannot be changed)")
                        Spacer(modifier = Modifier.height(6.dp))
                        ThriftProfileField(
                            value = email,
                            onValueChange = { },
                            placeholder = "",
                            enabled = false
                        )
                    }

                    // ===== MESSAGE =====
                    if (message.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (message.startsWith("Profile updated"))
                                    ThriftTeal.copy(alpha = 0.2f)
                                else ThriftError.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                message,
                                color = if (message.startsWith("Profile updated")) ThriftTeal else ThriftError,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // ===== STICKY BOTTOM BUTTON =====
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ThriftNavy)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = {
                        message = ""
                        if (fullName.isBlank() || phone.isBlank()) {
                            message = "Please fill in all fields"
                            return@Button
                        }
                        isSaving = true
                        val userData = hashMapOf<String, Any>(
                            "fullName" to fullName.trim(),
                            "phone" to phone.trim(),
                            "email" to email.trim()
                        )
                        db.collection("users").document(userId)
                            .set(userData, com.google.firebase.firestore.SetOptions.merge())
                            .addOnSuccessListener {
                                isSaving = false
                                message = "Profile updated successfully!"
                            }
                            .addOnFailureListener {
                                isSaving = false
                                message = "Failed: ${it.message}"
                            }
                    },
                    enabled = !isSaving && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ThriftTeal,
                        contentColor = ThriftNavy
                    )
                ) {
                    Text(
                        if (isSaving) "Saving..." else "Save Changes",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileFieldLabel(text: String) {
    Text(
        text = text,
        color = ThriftLightGray,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun ThriftProfileField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = ThriftLightGray) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = ThriftWhite,
            unfocusedTextColor = ThriftWhite,
            disabledTextColor = ThriftLightGray,
            focusedBorderColor = ThriftTeal,
            unfocusedBorderColor = ThriftNavy,
            disabledBorderColor = ThriftNavy,
            cursorColor = ThriftTeal,
            focusedContainerColor = ThriftNavy,
            unfocusedContainerColor = ThriftNavy,
            disabledContainerColor = ThriftNavy
        )
    )
}