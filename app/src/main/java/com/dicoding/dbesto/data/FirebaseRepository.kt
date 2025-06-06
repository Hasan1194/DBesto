package com.dicoding.dbesto.data

import android.util.Log
import com.dicoding.dbesto.model.MenuItemListModel
import com.dicoding.dbesto.model.MenuItemModel
import com.dicoding.dbesto.model.OrderMenuItem
import com.dicoding.dbesto.model.OrderModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val orderMenus = mutableListOf<MenuItemModel>()

    suspend fun registerUser(userData: Map<String, Any>, password: String): Result<String> {
        return try {
            val auth = FirebaseAuth.getInstance()
            val email = userData["email"] as String

            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user

            if (user != null) {
                val userDataWithUid = userData + mapOf("uid" to user.uid)
                firestore.collection("users")
                    .document(user.uid)
                    .set(userDataWithUid)
                    .await()

                Log.d("FirebaseRepo", "Register successful for user: ${user.uid}")
                Result.success("Register berhasil")
            } else {
                Result.failure(Exception("Register gagal - user null"))
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Register error", e)
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): Result<Map<String, Any>?> {
        return try {
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser

            if (currentUser != null) {
                val userDoc = firestore.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()

                if (userDoc.exists()) {
                    Result.success(userDoc.data)
                } else {
                    Result.success(null)
                }
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun submitOrder(orderData: Map<String, Any>) {
        firestore.collection("order")
            .add(orderData)
            .addOnSuccessListener {
                Log.d("Cart", "Order berhasil dikirim")
            }
            .addOnFailureListener { e ->
                Log.e("Cart", "Gagal mengirim order", e)
            }
    }

    fun updateOrderMenu(menuId: Long, newCountValue: Int): Boolean {
        return try {
            val index = orderMenus.indexOfFirst { it.menu.menuId.toLong() == menuId }
            if (index >= 0) {
                if (newCountValue <= 0) {
                    orderMenus.removeAt(index)
                } else {
                    val orderMenu = orderMenus[index]
                    orderMenus[index] = orderMenu.copy(count = newCountValue)
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Error updating order menu", e)
            false
        }
    }

    suspend fun addToCart(menuId: Long) {
        try {
            val menu = getMenuByMenuId(menuId)
            val existingIndex = orderMenus.indexOfFirst { it.menu.menuId.toLong() == menuId }

            if (existingIndex >= 0) {
                val existingOrder = orderMenus[existingIndex]
                orderMenus[existingIndex] = existingOrder.copy(count = existingOrder.count + 1)
                Log.d("FirebaseRepo", "Updated cart item: $menuId, new count: ${existingOrder.count + 1}")
            } else {
                val newItem = MenuItemModel(menu = menu, count = 1)
                orderMenus.add(newItem)
                Log.d("FirebaseRepo", "Added new item to cart: $menuId")
            }
            Log.d("FirebaseRepo", "Current cart size: ${orderMenus.size}")
            orderMenus.forEachIndexed { index, item ->
                Log.d("FirebaseRepo", "Cart item $index: ${item.menu.title} x${item.count}")
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Error adding to cart", e)
            throw e
        }
    }

    fun getOrderMenus(): List<MenuItemModel> {
        return orderMenus.toList()
    }

    fun clearCart() {
        orderMenus.clear()
        Log.d("FirebaseRepo", "Cart cleared")
    }

    suspend fun getMenuByMenuId(menuId: Long): MenuItemListModel {
        Log.d("FirebaseRepo", "Mencari menu dengan menuId: $menuId")

        val querySnapshot = firestore.collection("menus")
            .whereEqualTo("menuId", menuId)
            .get()
            .await()

        if (!querySnapshot.isEmpty) {
            val doc = querySnapshot.documents.first()
            val data = doc.data!!
            Log.d("FirebaseRepo", "Found document: ${doc.id} with menuId: $menuId")

            return MenuItemListModel(
                documentId = doc.id,
                menuId = (data["menuId"] as? Long)?.toInt() ?: menuId.toInt(),
                image = data["imageUrl"] as? String ?: "",
                title = data["title"] as? String ?: "",
                description = data["description"] as? String ?: "",
                price = when (val priceField = data["price"]) {
                    is Long -> priceField.toInt()
                    is Int -> priceField
                    else -> 0
                }
            )
        } else {
            throw NoSuchElementException("Menu with menuId $menuId not found")
        }
    }

    suspend fun getMenuById(documentId: String): MenuItemListModel {
        Log.d("FirebaseRepo", "Mencari menu dengan Document ID: $documentId")

        val doc = firestore.collection("menus").document(documentId).get().await()

        if (doc.exists()) {
            val data = doc.data!!
            Log.d("FirebaseRepo", "Found document: ${doc.id} with data: $data")

            return MenuItemListModel(
                documentId = doc.id,
                menuId = (data["menuId"] as? Long)?.toInt() ?: 0,
                image = data["imageUrl"] as? String ?: "",
                title = data["title"] as? String ?: "",
                description = data["description"] as? String ?: "",
                price = when (val priceField = data["price"]) {
                    is Long -> priceField.toInt()
                    is Int -> priceField
                    else -> 0
                }
            )
        } else {
            throw NoSuchElementException("Menu with document ID $documentId not found")
        }
    }

    suspend fun getMenus(): List<MenuItemListModel> {
        val snapshot = firestore.collection("menus").get().await()
        return snapshot.documents.mapNotNull { doc ->
            val data = doc.data
            if (data != null) {
                MenuItemListModel(
                    documentId = doc.id,
                    menuId = (data["menuId"] as? Long)?.toInt() ?: 0,
                    image = data["imageUrl"] as? String ?: "",
                    title = data["title"] as? String ?: "",
                    price = when (val priceField = data["price"]) {
                        is Long -> priceField.toInt()
                        is Int -> priceField
                        else -> 0
                    }
                )
            } else null
        }
    }

    suspend fun saveMenu(item: MenuItemListModel) {
        val data = hashMapOf(
            "menuId" to item.menuId,
            "title" to item.title,
            "price" to item.price,
            "description" to item.description,
            "imageUrl" to item.image
        )

        if (item.documentId.isEmpty()) {
            firestore.collection("menus").add(data).await()
        } else {
            firestore.collection("menus").document(item.documentId).set(data).await()
        }
    }

    suspend fun deleteMenu(documentId: String) {
        firestore.collection("menus").document(documentId).delete().await()
    }

    suspend fun getOrders(): List<OrderModel> {
        return try {
            val snapshot = firestore.collection("order").get().await()

            snapshot.documents.mapNotNull { doc ->
                val data = doc.data
                if (data != null) {
                    // Parse menus array if it exists
                    val menusData = data["menus"] as? List<Map<String, Any>> ?: emptyList()
                    val menuItems = menusData.map { menuData ->
                        OrderMenuItem(
                            menuId = menuData["menuId"] as? String ?: "",
                            title = menuData["title"] as? String ?: "",
                            price = when (val priceField = menuData["price"]) {
                                is Long -> priceField.toInt()
                                is Int -> priceField
                                else -> 0
                            },
                            count = when (val countField = menuData["count"]) {
                                is Long -> countField.toInt()
                                is Int -> countField
                                else -> 0
                            }
                        )
                    }

                    OrderModel(
                        documentId = doc.id,
                        menus = menuItems,
                        totalPrice = when (val totalPriceField = data["totalPrice"]) {
                            is Long -> totalPriceField.toInt()
                            is Int -> totalPriceField
                            else -> 0
                        },
                        totalAllPrice = when (val totalPointField = data["totalAllPrice"]) {
                            is Long -> totalPointField.toInt()
                            is Int -> totalPointField
                            else -> 0
                        },
                        timestamp = data["timestamp"] as? Long ?: 0L,
                        status = data["status"] as? String ?: "pending",
                        customerEmail = data["customerEmail"] as? String ?: "",
                        customerId = data["customerId"] as? String ?: ""
                    )
                } else null
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Error getting orders", e)
            throw e
        }
    }

    suspend fun updateOrderStatus(orderId: String, status: String) {
        try {
            firestore.collection("order")
                .document(orderId)
                .update("status", status)
                .await()

            Log.d("FirebaseRepo", "Order $orderId status updated to $status")
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Error updating order status", e)
            throw e
        }
    }
}