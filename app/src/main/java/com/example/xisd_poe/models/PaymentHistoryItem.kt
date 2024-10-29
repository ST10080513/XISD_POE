package com.example.xisd_poe.models
// In models/PaymentHistoryItem.kt
data class PaymentHistoryItem(
    val id: String = "", // Transaction ID
    val amount: Double = 0.0,
    val cardNumber: String = "",
    val cardholderName: String = "",
    val cvv: String = "",
    val expirationDate: String = "",
    val paymentMethod: String = "", // Added Payment Method field
    val transactionDate: String = "",
    val purchaseInfo: Map<String, Any> = mapOf()
)




