package com.dicoding.dbesto.model

data class MenuItemListModel(
    val documentId: String = "",
    val menuId: Int = 0,
    val image: String = "",
    val title: String = "",
    val description: String = "",
    val price: Int = 0
)