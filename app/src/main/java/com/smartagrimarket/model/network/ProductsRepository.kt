package com.smartagrimarket.model.network

import android.util.Log
import com.google.gson.JsonParser
import com.smartagrimarket.model.Product
import com.smartagrimarket.model.ProductCategory

object ProductsRepository {
    private val api = RetrofitClient.api

    suspend fun fetchProducts(): ApiResult<List<Product>> {
        return try {
            val response = api.getProducts()
            if (response.isSuccessful && response.body() != null) {
                val mapped = response.body()!!.products.map { dto -> mapToProduct(dto) }
                ApiResult.Success(mapped)
            } else {
                ApiResult.Error("Could not load products (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun createProduct(
        name: String,
        price: Double,
        stock: Int?,
        imageUrl: String? = null
    ): ApiResult<Product> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.createProduct(
                "Bearer $token",
                CreateProductRequest(
                    name = name,
                    price = price,
                    stock = stock,
                    imageUrl = imageUrl
                )
            )
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(mapToProduct(response.body()!!.product))
            } else {
                ApiResult.Error("Could not upload product (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun fetchMyProducts(): ApiResult<List<Product>> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.getMyProducts("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!.products.map { mapToProduct(it) })
            } else {
                ApiResult.Error("Could not load your products (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun fetchMyProductsRaw(): ApiResult<List<ProductDto>> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.getMyProducts("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!.products)
            } else {
                ApiResult.Error("Could not load your products (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun deleteProduct(id: String): ApiResult<Unit> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.deleteProduct("Bearer $token", id)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                val raw = response.errorBody()?.string()
                Log.e("ProductsRepo", "deleteProduct failed ${response.code()}: $raw")
                ApiResult.Error(
                    extractErrorMessage(raw) ?: "Could not delete product (${response.code()})"
                )
            }
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun updateStock(productId: String, newStock: Int): ApiResult<Unit> {
        return setStock(productId, newStock)
    }

    suspend fun addStock(
        productId: String,
        currentStock: Int,
        amountToAdd: Int
    ): ApiResult<Unit> {
        if (amountToAdd <= 0) {
            return ApiResult.Error("Enter a number greater than 0.")
        }
        val newTotal = (currentStock.coerceAtLeast(0)) + amountToAdd
        return setStock(productId, newTotal)
    }

    private suspend fun setStock(productId: String, newStock: Int): ApiResult<Unit> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.updateProduct(
                "Bearer $token",
                productId,
                UpdateProductRequest(stock = newStock)
            )
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                val raw = response.errorBody()?.string()
                Log.e("ProductsRepo", "setStock failed ${response.code()}: $raw")
                ApiResult.Error(
                    extractErrorMessage(raw) ?: "Could not update stock (${response.code()})"
                )
            }
        } catch (e: Exception) {
            Log.e("ProductsRepo", "setStock crashed", e)
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    suspend fun logView(productId: String): ApiResult<Unit> {
        val token = TokenStore.token ?: return ApiResult.Error("Please sign in again.")
        return try {
            val response = api.logProductView("Bearer $token", productId)
            if (response.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error("Could not log view (${response.code()})")
        } catch (e: Exception) {
            ApiResult.Error("Couldn't reach the server.")
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Cart validation
    //
    // Called when the buyer opens the cart screen. Returns a list of
    // items that are no longer valid — product deleted, hidden by
    // admin, out of stock, or insufficient stock — so the client can
    // prune them from the local cart before the buyer reaches checkout.
    //
    // No auth token needed: guest carts must be able to validate too.
    // ─────────────────────────────────────────────────────────────
    suspend fun validateCart(items: List<CartValidateItem>): ApiResult<CartValidateResponse> {
        return try {
            val response = api.validateCart(CartValidateRequest(items = items))
            val body = response.body()
            if (response.isSuccessful && body != null) {
                ApiResult.Success(body)
            } else {
                val raw = response.errorBody()?.string()
                Log.e("ProductsRepo", "validateCart failed ${response.code()}: $raw")
                ApiResult.Error(
                    extractErrorMessage(raw) ?: "Could not validate cart (${response.code()})"
                )
            }
        } catch (e: Exception) {
            Log.e("ProductsRepo", "validateCart crashed", e)
            ApiResult.Error("Couldn't reach the server. Check your connection.")
        }
    }

    private fun extractErrorMessage(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return try {
            val json = JsonParser.parseString(raw).asJsonObject
            json.get("error")?.asString?.takeIf { it.isNotBlank() }
                ?: json.get("message")?.asString?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            raw.take(200)
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Map the network DTO into the local Product model.
    // ─────────────────────────────────────────────────────────────
    private fun mapToProduct(dto: ProductDto): Product {
        return Product(
            id = dto.id,
            name = dto.name,
            farmName = dto.location ?: "Local Farm",
            price = dto.price,
            category = guessCategory(dto.name),
            fromFarmerBadge = true,
            lowStock = dto.stock != null && dto.stock in 1..2,
            stock = dto.stock,
            imageUrl = dto.imageUrl,
            farmerEmail = dto.farmerEmail,
            averageRating = dto.averageRating ?: 0.0,
            reviewCount = dto.reviewCount ?: 0,
        )
    }

    private fun guessCategory(name: String): ProductCategory {
        val n = name.lowercase()
        val fruitWords = listOf(
            "apple", "banana", "orange", "mango", "grape", "watermelon",
            "peach", "pear", "plum", "avocado", "fruit", "berry"
        )
        val dairyWords = listOf(
            "milk", "cheese", "egg", "yogurt", "yoghurt", "butter", "cream"
        )
        return when {
            fruitWords.any { n.contains(it) } -> ProductCategory.FRUITS
            dairyWords.any { n.contains(it) } -> ProductCategory.DAIRY
            else -> ProductCategory.VEGETABLES
        }
    }
}