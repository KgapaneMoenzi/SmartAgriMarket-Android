package com.smartagrimarket.model

import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Reactive + persistent shopping cart.
 *
 * - Backed by `mutableStateMapOf`, so the UI recomposes when the cart changes.
 * - Saved to SharedPreferences after every mutation.
 * - Keyed per user so different accounts on the same device don't share carts.
 */
class CartState(
    context: Context,
    private val ownerKey: String = "guest"
) {
    private val prefs = context.applicationContext
        .getSharedPreferences("agrimarket_cart", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val cartKey = "cart_json_$ownerKey"

    private val _lines = mutableStateMapOf<String, CartLine>()

    val lines: Map<String, CartLine>
        get() = _lines

    val totalItems: Int
        get() = _lines.values.sumOf { it.quantity }

    val subtotal: Double
        get() = _lines.values.sumOf { it.product.price * it.quantity }

    init {
        loadFromPrefs()
    }

    // ── Mutations ────────────────────────────────────────────────

    fun add(product: Product) {
        val existing = _lines[product.id]
        _lines[product.id] = if (existing == null) {
            CartLine(product = product, quantity = 1)
        } else {
            existing.copy(quantity = existing.quantity + 1)
        }
        saveToPrefs()
    }

    fun increment(productId: String) {
        val existing = _lines[productId] ?: return
        _lines[productId] = existing.copy(quantity = existing.quantity + 1)
        saveToPrefs()
    }

    fun decrement(productId: String) {
        val existing = _lines[productId] ?: return
        if (existing.quantity <= 1) {
            _lines.remove(productId)
        } else {
            _lines[productId] = existing.copy(quantity = existing.quantity - 1)
        }
        saveToPrefs()
    }

    fun remove(productId: String) {
        _lines.remove(productId)
        saveToPrefs()
    }

    fun quantityOf(productId: String): Int {
        return _lines[productId]?.quantity ?: 0
    }

    fun isAtMax(productId: String): Boolean {
        val line = _lines[productId] ?: return false
        val stock = line.product.stock ?: return false
        return line.quantity >= stock
    }

    /**
     * Prune stale items discovered by /cart-validate. Called from the
     * cart screen when it mounts.
     *
     * @param removedIds  productIds whose listing is gone / hidden — removed outright
     * @param clampedTo   map of productId -> new quantity for out-of-stock
     *                    or insufficient-stock items
     * @return a summary of what was changed, so the caller can toast
     */
    fun pruneStale(
        removedIds: Set<String>,
        clampedTo: Map<String, Int>
    ): PruneResult {
        var removed = 0
        var clamped = 0

        for (id in removedIds) {
            if (_lines.remove(id) != null) removed++
        }

        for ((id, newQty) in clampedTo) {
            val existing = _lines[id] ?: continue
            if (newQty <= 0) {
                _lines.remove(id)
                removed++
            } else if (existing.quantity != newQty) {
                _lines[id] = existing.copy(quantity = newQty)
                clamped++
            }
        }

        if (removed > 0 || clamped > 0) {
            saveToPrefs()
        }

        return PruneResult(removedCount = removed, clampedCount = clamped)
    }

    /**
     * Empty the cart. Call this ONLY after a successful checkout.
     * Persisted data is wiped too.
     */
    fun clear() {
        _lines.clear()
        prefs.edit().remove(cartKey).apply()
    }

    // ── Persistence ──────────────────────────────────────────────

    private fun saveToPrefs() {
        try {
            val json = gson.toJson(_lines.values.toList())
            prefs.edit().putString(cartKey, json).apply()
        } catch (_: Exception) { /* ignore serialization errors */ }
    }

    private fun loadFromPrefs() {
        try {
            val json = prefs.getString(cartKey, null) ?: return
            val type = object : TypeToken<List<CartLine>>() {}.type
            val loaded: List<CartLine> = gson.fromJson(json, type)
            _lines.clear()
            loaded.forEach { line -> _lines[line.product.id] = line }
        } catch (_: Exception) {
            // Corrupted or missing data — start with an empty cart
        }
    }

    data class CartLine(
        val product: Product,
        val quantity: Int
    )

    data class PruneResult(
        val removedCount: Int,
        val clampedCount: Int
    ) {
        val hasChanges: Boolean get() = removedCount > 0 || clampedCount > 0
    }
}