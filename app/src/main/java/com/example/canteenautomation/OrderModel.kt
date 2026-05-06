package com.example.canteenautomation

import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.ServerValue

@IgnoreExtraProperties
data class OrderModel(
    var orderId: String = "",
    var userId: String = "",
    var customerName: String = "", // Default empty
    var items: String = "",
    var total: Int = 0,
    var status: String = "Pending", // Changed "New" to "Pending" to match your logic
    var token: Int = 0,
    var instructions: String = "",
    var timestamp: Any? = null
) {
    /**
     * Secondary constructor - REMOVED paymentStatus
     */
    constructor(
        orderId: String,
        userId: String,
        customerName: String,
        items: String,
        total: Int,
        token: Int,
        instructions: String
    ) : this(
        orderId = orderId,
        userId = userId,
        customerName = customerName,
        items = items,
        total = total,
        status = "Pending",
        token = token,
        instructions = instructions,
        timestamp = ServerValue.TIMESTAMP
    )
}