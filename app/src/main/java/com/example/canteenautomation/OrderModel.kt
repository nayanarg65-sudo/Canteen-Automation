package com.example.canteenautomation

// This matches your existing Firebase structure (items: String, total: Int)
data class OrderModel(
    var orderId: String = "",
    val items: String = "",
    val total: Int = 0,
    val status: String = "New",
    val customerName: String = "Student", // Added for better UI appearance
    var token: Int = 0,
    var userId: String = "",
    var paymentStatus: String = "",
    var timestamp: Any? = null
 )
{
    constructor(items: String, total: Int, token: Int, userId: String, paymentStatus: String, status: String) : this(
    orderId = "",
    items = items,
    total = total,
    status = status,
    token = token,
    userId = userId,
    paymentStatus = paymentStatus,
    timestamp = com.google.firebase.database.ServerValue.TIMESTAMP
    )
}

