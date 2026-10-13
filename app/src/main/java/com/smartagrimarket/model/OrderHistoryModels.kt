package com.smartagrimarket.model

enum class OrderStatus(val label: String) {
    DELIVERED("Delivered"),
    OUT_FOR_DELIVERY("Out for delivery"),
    PROCESSING("Processing"),
    CANCELLED("Cancelled")
}

data class OrderLineItem(
    val product: Product,
    val quantity: Int
)

data class BuyerOrder(
    val id: String,
    val placedOn: String,
    val status: OrderStatus,
    val items: List<OrderLineItem>
) {
    val total: Double
        get() = items.sumOf { it.product.price * it.quantity }
}

/**
 * Mock order history for the frontend-only pass. Reuses products from
 * SampleData.marketplace so reorder totals line up with real prices.
 */
object SampleOrderHistory {

    val orders: List<BuyerOrder> by lazy {
        val products = SampleData.marketplace
        if (products.size < 3) return@lazy emptyList()

        listOf(
            BuyerOrder(
                id = "ORD-1042",
                placedOn = "3 days ago",
                status = OrderStatus.DELIVERED,
                items = listOf(
                    OrderLineItem(products[0], 2),
                    OrderLineItem(products[1], 1)
                )
            ),
            BuyerOrder(
                id = "ORD-1039",
                placedOn = "1 week ago",
                status = OrderStatus.OUT_FOR_DELIVERY,
                items = listOf(
                    OrderLineItem(products[2], 3)
                )
            ),
            BuyerOrder(
                id = "ORD-1021",
                placedOn = "3 weeks ago",
                status = OrderStatus.CANCELLED,
                items = listOf(
                    OrderLineItem(products[0], 1)
                )
            )
        )
    }
}