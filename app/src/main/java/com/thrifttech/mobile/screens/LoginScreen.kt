package com.thrifttech.mobile.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.thrifttech.mobile.R
import com.thrifttech.mobile.ui.theme.*

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onSwitchToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken: String? = account?.idToken
                if (idToken != null) {
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener { authTask ->
                            if (authTask.isSuccessful) onLoginSuccess()
                            else errorMessage = authTask.exception?.message ?: "Google sign-in failed"
                        }
                } else {
                    errorMessage = "No ID token returned"
                }
            } catch (e: ApiException) {
                errorMessage = "Google sign-in error: ${e.message}"
            }
        } else {
            errorMessage = "Google sign-in cancelled"
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = ThriftNavy) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // ===== LOGO BADGE =====
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(ThriftTeal),
                contentAlignment = Alignment.Center
            ) {
                Text("TT", color = ThriftNavy,
                    fontSize = 34.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Welcome Back",
                color = ThriftWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(6.dp))

            Text("Sign in to continue to Thrift Tech",
                color = ThriftLightGray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(36.dp))

            // ===== LOGIN CARD =====
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(ThriftLightNavy)
                    .padding(20.dp)
            ) {
                FieldLabel("Email")
                Spacer(modifier = Modifier.height(6.dp))
                ThriftLoginField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "you@example.com",
                    leadingEmoji = "✉",
                    keyboardType = KeyboardType.Email
                )

                Spacer(modifier = Modifier.height(16.dp))

                FieldLabel("Password")
                Spacer(modifier = Modifier.height(6.dp))
                ThriftLoginField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Enter your password",
                    leadingEmoji = "🔒",
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                    isPasswordVisible = passwordVisible,
                    onToggleVisibility = { passwordVisible = !passwordVisible }
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

            // ===== LOGIN BUTTON =====
            Button(
                onClick = {
                    errorMessage = ""
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Please fill in all fields"
                        return@Button
                    }
                    isLoading = true
                    auth.signInWithEmailAndPassword(email.trim(), password)
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) onLoginSuccess()
                            else errorMessage = task.exception?.message ?: "Login failed"
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
                    if (isLoading) "Logging in..." else "Login",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ===== DIVIDER =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = ThriftLightNavy
                )
                Text("  OR  ", color = ThriftLightGray, fontSize = 12.sp)
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = ThriftLightNavy
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ===== GOOGLE BUTTON =====
            OutlinedButton(
                onClick = {
                    errorMessage = ""
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(context.getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    googleSignInLauncher.launch(googleSignInClient.signInIntent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, ThriftTeal),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent
                )
            ) {
                Text("G   ", color = ThriftTeal, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Sign in with Google",
                    color = ThriftTeal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ===== REGISTER LINK =====
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("New here? ", color = ThriftLightGray, fontSize = 14.sp)
                TextButton(onClick = onSwitchToRegister, enabled = !isLoading) {
                    Text("Create an account",
                        color = ThriftTeal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

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
private fun ThriftLoginField(
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
        placeholder = { Text(placeholder, color = ThriftLightGray, fontSize = 14.sp) },
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