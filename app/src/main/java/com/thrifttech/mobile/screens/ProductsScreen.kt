package com.thrifttech.mobile.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.thrifttech.mobile.models.Product
import com.thrifttech.mobile.ui.theme.*

private val ALL_CATEGORIES = listOf("All", "Tops", "Bottoms", "Outerwear", "Footwear", "Accessories")

@Composable
fun ProductsScreen(onBack: () -> Unit) {
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var imageToView by remember { mutableStateOf<String?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    fun loadProducts() {
        isLoading = true
        db.collection("products").get()
            .addOnSuccessListener { result ->
                products = result.documents.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.copy(id = doc.id)
                }
                isLoading = false
            }
            .addOnFailureListener { isLoading = false }
    }

    LaunchedEffect(Unit) { loadProducts() }

    // Filtered products
    val filteredProducts = products.filter { product ->
        val matchesCategory = selectedCategory == "All" ||
                product.category.equals(selectedCategory, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() ||
                product.name.contains(searchQuery, ignoreCase = true) ||
                product.category.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    // ===== FULL-SCREEN IMAGE VIEWER =====
    if (imageToView != null) {
        Dialog(
            onDismissRequest = { imageToView = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .clickable { imageToView = null },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imageToView,
                    contentDescription = "Full image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                )
                Text("✕", color = Color.White, fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(24.dp))
                Text("Tap anywhere to close",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp))
            }
        }
    }

    // ===== DELETE DIALOG =====
    if (showDeleteDialog && productToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                productToDelete = null
            },
            title = { Text("Delete Item?") },
            text = { Text("Are you sure you want to delete \"${productToDelete!!.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    db.collection("products").document(productToDelete!!.id).delete()
                        .addOnSuccessListener {
                            showDeleteDialog = false
                            productToDelete = null
                            loadProducts()
                        }
                }) {
                    Text("Delete", color = ThriftError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    productToDelete = null
                }) {
                    Text("Cancel", color = ThriftTeal)
                }
            }
        )
    }

    Surface(modifier = Modifier.fillMaxSize(), color = ThriftNavy) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ===== TOP BAR =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
                    Text("← Back", color = ThriftTeal,
                        fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text("Marketplace", color = ThriftWhite,
                    fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(50.dp))
            }

            // ===== SEARCH BAR =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ThriftLightNavy)
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔍", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search items...", color = ThriftLightGray, fontSize = 14.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = ThriftWhite,
                        unfocusedTextColor = ThriftWhite,
                        cursorColor = ThriftTeal,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ===== CATEGORY CHIPS =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ALL_CATEGORIES.forEach { cat ->
                    val isSelected = cat == selectedCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isSelected) ThriftTeal else ThriftLightNavy)
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            cat,
                            color = if (isSelected) ThriftNavy else ThriftWhite,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== RESULTS COUNT =====
            Text(
                text = "${filteredProducts.size} item${if (filteredProducts.size != 1) "s" else ""} available",
                color = ThriftLightGray,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ===== PRODUCT LIST =====
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ThriftTeal)
                }
            } else if (filteredProducts.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("🔍", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No items found", color = ThriftWhite,
                        fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Try a different search or category",
                        color = ThriftLightGray, fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredProducts) { product ->
                        ProductCard(
                            product = product,
                            isOwner = product.sellerId == currentUserId && currentUserId.isNotEmpty(),
                            onImageClick = { imageToView = product.imageUrl },
                            onDeleteClick = {
                                productToDelete = product
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
}

// ===== PRODUCT CARD =====
@Composable
private fun ProductCard(
    product: Product,
    isOwner: Boolean,
    onImageClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ThriftLightNavy),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column {
            // ===== IMAGE WITH OVERLAY PRICE =====
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                if (product.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onImageClick() }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(ThriftNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No Image", color = ThriftLightGray, fontSize = 14.sp)
                    }
                }

                // Category tag (top-left)
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .clip(RoundedCornerShape(50))
                        .background(ThriftNavy.copy(alpha = 0.85f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(product.category,
                        color = ThriftTeal,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold)
                }

                // Price tag (bottom-right)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(50))
                        .background(ThriftTeal)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("R${product.price}",
                        color = ThriftNavy,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold)
                }
            }

            // ===== INFO SECTION =====
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(product.name,
                            color = ThriftWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (product.condition.lowercase()) {
                                            "excellent" -> ThriftTeal
                                            "good" -> Color(0xFF4CAF50)
                                            "fair" -> Color(0xFFFFA726)
                                            else -> ThriftError
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Condition: ${product.condition}",
                                color = ThriftLightGray, fontSize = 12.sp)
                        }
                    }

                    if (isOwner) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(ThriftError.copy(alpha = 0.15f))
                                .clickable { onDeleteClick() }
                                .padding(8.dp)
                        ) {
                            Text("🗑", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}