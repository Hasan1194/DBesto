package com.dicoding.dbesto.model

data class OrderModel(
    val documentId: String = "",
    val menus: List<OrderMenuItem> = emptyList(),
    val totalPrice: Int = 0,
    val totalAllPrice: Int = 0,
    val timestamp: Long = 0L,
    val status: String = "pending",
    val customerEmail: String = "",
    val customerId: String = ""
)

data class OrderMenuItem(
    val menuId: String = "",
    val title: String = "",
    val price: Int = 0,
    val count: Int = 0
)