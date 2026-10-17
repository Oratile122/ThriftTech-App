package com.thrifttech.mobile.models

data class Product(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val condition: String = "",
    val imageUrl: String = "",
    val sellerId: String = ""
)