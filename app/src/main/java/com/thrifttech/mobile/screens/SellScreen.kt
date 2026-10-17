package com.thrifttech.mobile.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.thrifttech.mobile.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

private const val CLOUD_NAME = "rsbbjorq"
private const val UPLOAD_PRESET = "thrift_tech"
private const val CLOUD_API_KEY = "436357838117245"

private val CATEGORIES = listOf("Tops", "Bottoms", "Outerwear", "Footwear", "Accessories")
private val CONDITIONS = listOf("Excellent", "Good", "Fair", "Poor")

@Composable
fun SellScreen(onSubmitted: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) imageUri = uri
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
                    onClick = onSubmitted,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("← Back", color = ThriftTeal,
                        fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text("Sell an Item", color = ThriftWhite,
                    fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(50.dp))
            }

            // ===== SCROLLABLE CONTENT =====
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // ===== IMAGE CARD =====
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(ThriftLightNavy)
                        .clickable { imagePicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Selected image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.55f))
                                .padding(vertical = 10.dp)
                        ) {
                            Text(
                                "Tap to change photo",
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(ThriftNavy),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📷", fontSize = 32.sp)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Add a photo",
                                color = ThriftWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tap here to pick from gallery",
                                color = ThriftLightGray,
                                fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                SectionLabel("Item name")
                Spacer(modifier = Modifier.height(8.dp))
                ThriftTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "e.g. Vintage Nike Hoodie"
                )

                Spacer(modifier = Modifier.height(20.dp))

                SectionLabel("Price (ZAR)")
                Spacer(modifier = Modifier.height(8.dp))
                ThriftTextField(
                    value = price,
                    onValueChange = { price = it },
                    placeholder = "e.g. 299.99"
                )

                Spacer(modifier = Modifier.height(20.dp))

                SectionLabel("Category")
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalChipRow(
                    options = CATEGORIES,
                    selected = category,
                    onSelect = { category = it }
                )

                Spacer(modifier = Modifier.height(20.dp))

                SectionLabel("Condition")
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalChipRow(
                    options = CONDITIONS,
                    selected = condition,
                    onSelect = { condition = it }
                )

                if (message.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (message.startsWith("Item"))
                                ThriftTeal.copy(alpha = 0.2f)
                            else ThriftError.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            message,
                            color = if (message.startsWith("Item")) ThriftTeal else ThriftError,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
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
                        if (name.isBlank() || price.isBlank() ||
                            category.isBlank() || condition.isBlank()) {
                            message = "Please fill in all fields"
                            return@Button
                        }
                        isLoading = true

                        scope.launch {
                            try {
                                var imageUrl = ""
                                if (imageUri != null) {
                                    imageUrl = uploadToCloudinary(context, imageUri!!) ?: ""
                                    if (imageUrl.isEmpty()) {
                                        isLoading = false
                                        message = "Image upload failed - check Logcat"
                                        return@launch
                                    }
                                }

                                val currentUserId = com.google.firebase.auth.FirebaseAuth
                                    .getInstance().currentUser?.uid ?: ""

                                val product = hashMapOf(
                                    "name" to name.trim(),
                                    "price" to (price.toDoubleOrNull() ?: 0.0),
                                    "category" to category.trim(),
                                    "condition" to condition.trim(),
                                    "imageUrl" to imageUrl,
                                    "sellerId" to currentUserId
                                )

                                db.collection("products").add(product)
                                    .addOnSuccessListener {
                                        isLoading = false
                                        message = "Item posted!"
                                        name = ""; price = ""; category = ""; condition = ""; imageUri = null
                                        onSubmitted()
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        message = "Failed: ${e.message}"
                                    }
                            } catch (e: Exception) {
                                isLoading = false
                                message = "Error: ${e.message}"
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
                        if (isLoading) "Posting..." else "Post Item",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            }
        }
    }
}

// ===== Section Label =====
@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = ThriftWhite,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold
    )
}

// ===== Custom text field =====
@Composable
private fun ThriftTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = ThriftLightGray) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = ThriftWhite,
            unfocusedTextColor = ThriftWhite,
            focusedBorderColor = ThriftTeal,
            unfocusedBorderColor = ThriftLightNavy,
            cursorColor = ThriftTeal,
            focusedContainerColor = ThriftLightNavy,
            unfocusedContainerColor = ThriftLightNavy
        )
    )
}

// ===== Horizontal scrolling chip row (stable, no experimental APIs) =====
@Composable
private fun HorizontalChipRow(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) ThriftTeal else ThriftLightNavy)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) ThriftTeal else ThriftLightNavy,
                        shape = RoundedCornerShape(50)
                    )
                    .clickable { onSelect(option) }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = option,
                    color = if (isSelected) ThriftNavy else ThriftWhite,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

// ===== Cloudinary upload =====
private suspend fun uploadToCloudinary(
    context: android.content.Context,
    uri: Uri
): String? = withContext(Dispatchers.IO) {
    try {
        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        val extension = when {
            mimeType.contains("png") -> ".png"
            mimeType.contains("webp") -> ".webp"
            mimeType.contains("gif") -> ".gif"
            else -> ".jpg"
        }

        val inputStream = context.contentResolver.openInputStream(uri)
            ?: return@withContext null
        val tempFile = File.createTempFile("upload_", extension, context.cacheDir)
        val outputStream = FileOutputStream(tempFile)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()

        val client = OkHttpClient()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("upload_preset", UPLOAD_PRESET)
            .addFormDataPart("api_key", CLOUD_API_KEY)
            .addFormDataPart(
                "file",
                tempFile.name,
                tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        android.util.Log.d("CLOUDINARY_DEBUG", "Status: ${response.code}")
        android.util.Log.d("CLOUDINARY_DEBUG", "Body: $responseBody")

        if (!response.isSuccessful) {
            android.util.Log.e("CLOUDINARY_ERROR", "Status: ${response.code} Body: $responseBody")
            tempFile.delete()
            return@withContext null
        }

        val json = JSONObject(responseBody)
        val url = json.getString("secure_url")
        tempFile.delete()
        url
    } catch (e: Exception) {
        android.util.Log.e("CLOUDINARY_ERROR", "Exception: ${e.message}", e)
        null
    }
}