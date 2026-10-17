package com.thrifttech.mobile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.thrifttech.mobile.ui.theme.*

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onSwitchToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    Surface(modifier = Modifier.fillMaxSize(), color = ThriftNavy) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // ===== LOGO BADGE =====
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(ThriftTeal),
                contentAlignment = Alignment.Center
            ) {
                Text("TT", color = ThriftNavy,
                    fontSize = 30.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Create Account",
                color = ThriftWhite,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(6.dp))

            Text("Join Thrift Tech in seconds",
                color = ThriftLightGray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(28.dp))

            // ===== FORM CARD =====
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(ThriftLightNavy)
                    .padding(20.dp)
            ) {
                FieldLabel("Full Name")
                Spacer(modifier = Modifier.height(6.dp))
                ThriftField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    placeholder = "e.g. Oratile Morudi",
                    leadingEmoji = "👤",
                    keyboardType = KeyboardType.Text
                )

                Spacer(modifier = Modifier.height(14.dp))

                FieldLabel("Phone Number")
                Spacer(modifier = Modifier.height(6.dp))
                ThriftField(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = "e.g. 0712345678",
                    leadingEmoji = "📱",
                    keyboardType = KeyboardType.Phone
                )

                Spacer(modifier = Modifier.height(14.dp))

                FieldLabel("Email")
                Spacer(modifier = Modifier.height(6.dp))
                ThriftField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "you@example.com",
                    leadingEmoji = "✉",
                    keyboardType = KeyboardType.Email
                )

                Spacer(modifier = Modifier.height(14.dp))

                FieldLabel("Password (min 6 characters)")
                Spacer(modifier = Modifier.height(6.dp))
                ThriftField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Create a password",
                    leadingEmoji = "🔒",
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                    isPasswordVisible = passwordVisible,
                    onToggleVisibility = { passwordVisible = !passwordVisible }
                )

                Spacer(modifier = Modifier.height(14.dp))

                FieldLabel("Confirm Password")
                Spacer(modifier = Modifier.height(6.dp))
                ThriftField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = "Repeat password",
                    leadingEmoji = "🔒",
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                    isPasswordVisible = passwordVisible
                )
            }

            // ===== ERROR MESSAGE =====
            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = ThriftError.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        errorMessage,
                        color = ThriftError,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ===== REGISTER BUTTON =====
            Button(
                onClick = {
                    errorMessage = ""
                    when {
                        fullName.isBlank() || phone.isBlank() || email.isBlank() ||
                                password.isBlank() || confirmPassword.isBlank() -> {
                            errorMessage = "Please fill in all fields"
                        }
                        password != confirmPassword -> {
                            errorMessage = "Passwords do not match"
                        }
                        password.length < 6 -> {
                            errorMessage = "Password must be at least 6 characters"
                        }
                        else -> {
                            isLoading = true
                            auth.createUserWithEmailAndPassword(email.trim(), password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val userId = task.result.user?.uid
                                        if (userId != null) {
                                            val userData = hashMapOf(
                                                "fullName" to fullName.trim(),
                                                "phone" to phone.trim(),
                                                "email" to email.trim()
                                            )
                                            db.collection("users").document(userId)
                                                .set(userData)
                                                .addOnSuccessListener {
                                                    isLoading = false
                                                    onRegisterSuccess()
                                                }
                                                .addOnFailureListener {
                                                    isLoading = false
                                                    errorMessage = "Failed to save profile"
                                                }
                                        } else {
                                            isLoading = false
                                            errorMessage = "User ID not found"
                                        }
                                    } else {
                                        isLoading = false
                                        errorMessage = task.exception?.message ?: "Registration failed"
                                    }
                                }
                        }
                    }
                },
                enabled = !isLoading,
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
                    if (isLoading) "Creating account..." else "Register",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ===== LOGIN LINK =====
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account? ",
                    color = ThriftLightGray, fontSize = 14.sp)
                TextButton(onClick = onSwitchToLogin, enabled = !isLoading) {
                    Text("Login",
                        color = ThriftTeal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("© 2026 Thrift Tech | Braamfontein",
                color = ThriftLightGray.copy(alpha = 0.5f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ===== Small label above fields =====
@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        color = ThriftLightGray,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium
    )
}

// ===== Custom styled text field =====
@Composable
private fun ThriftField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingEmoji: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onToggleVisibility: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = ThriftLightGray, fontSize = 13.sp) },
        leadingIcon = {
            if (leadingEmoji.isNotEmpty()) {
                Text(leadingEmoji, fontSize = 16.sp,
                    modifier = Modifier.padding(start = 4.dp))
            }
        },
        trailingIcon = {
            if (isPassword && onToggleVisibility != null) {
                TextButton(
                    onClick = onToggleVisibility,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(
                        if (isPasswordVisible) "👁" else "👁‍🗨",
                        fontSize = 16.sp
                    )
                }
            }
        },
        visualTransformation = if (isPassword && !isPasswordVisible)
            PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = ThriftWhite,
            unfocusedTextColor = ThriftWhite,
            focusedBorderColor = ThriftTeal,
            unfocusedBorderColor = ThriftNavy,
            cursorColor = ThriftTeal,
            focusedContainerColor = ThriftNavy,
            unfocusedContainerColor = ThriftNavy
        )
    )
}