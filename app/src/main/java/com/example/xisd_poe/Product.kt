package com.example.xisd_poe

// Product data class to map Firebase data
data class Product(
    val name: String = "",
    val sellPrice: Int = 0,       // Integer to match Firebase
    val description: String = "",
    val stock: Int = 0,
    val shortDesc: String = "",
    val use: String = "",
    val actualPrice: Int = 0,     // Integer to match Firebase
    val discount: Int = 0,        // Integer to match Firebase
    val exDate: String = "",
    val manuDate: String = "",
    val prescription: String = "",
    val images: List<String> = emptyList()
)


