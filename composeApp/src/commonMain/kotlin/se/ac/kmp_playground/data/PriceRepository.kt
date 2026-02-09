package se.ac.kmp_playground.data

import io.ktor.client.call.body
import io.ktor.client.request.get

class PriceRepository(
    private val baseUrl: String = "https://thomasalexandre.unison-services.cloud/s/prices-api"
) {
    private val client = createHttpClient()

    /**
     * Get price comparison for a barcode across all stores
     */
    suspend fun comparePrices(barcode: String): Result<List<StorePriceComparison>> {
        return try {
            val comparisons: List<StorePriceComparison> =
                client.get("$baseUrl/prices/barcode/$barcode/compare").body()
            Result.success(comparisons)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get price comparison for multiple barcodes
     * Returns a map of barcode -> list of store prices
     */
    suspend fun comparePricesForBarcodes(barcodes: List<String>): Map<String, List<StorePriceComparison>> {
        val results = mutableMapOf<String, List<StorePriceComparison>>()

        for (barcode in barcodes) {
            comparePrices(barcode)
                .onSuccess { comparisons ->
                    results[barcode] = comparisons
                }
                .onFailure {
                    // If price not found, add empty list
                    results[barcode] = emptyList()
                }
        }

        return results
    }

    /**
     * Calculate total prices for a shopping list across stores
     */
    fun calculateStoreTotals(
        itemComparisons: List<ProductPriceComparison>,
        selectedStoreIds: Set<Int>
    ): Map<Int, Double> {
        val totals = mutableMapOf<Int, Double>()

        for (storeId in selectedStoreIds) {
            var total = 0.0
            for (item in itemComparisons) {
                val storePrice = item.storePrices.find { it.storeId.toIntOrNull() == storeId }
                if (storePrice != null) {
                    total += storePrice.price.price.toDoubleOrNull() ?: 0.0
                }
            }
            totals[storeId] = total
        }

        return totals
    }
}