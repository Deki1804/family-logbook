package com.familylogbook.app.data.shopping

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder

/**
 * Service for searching shopping deals using Google Custom Search API.
 * 
 * This service searches for deals on specific products in Croatian stores.
 * Uses Google Custom Search Engine configured to search only store websites.
 * 
 * Setup required:
 * 1. Create Google Custom Search Engine at https://programmablesearchengine.google.com/
 * 2. Configure it to search only: kaufland.hr, konzum.hr, spar.hr, lidl.hr, plodine.hr
 * 3. Get API key from Google Cloud Console
 * 4. Add API key and Engine ID to gradle.properties:
 *    GOOGLE_CSE_API_KEY=your_api_key
 *    GOOGLE_CSE_ENGINE_ID=your_engine_id
 */
class GoogleCustomSearchService(
    private val apiKey: String?,
    private val engineId: String?
) {
    
    private val client = OkHttpClient()
    
    // Croatian store domains to search
    private val storeDomains = listOf(
        "kaufland.hr",
        "konzum.hr",
        "spar.hr",
        "lidl.hr",
        "plodine.hr"
    )
    
    /**
     * Represents a shopping deal found via search.
     */
    data class ShoppingDeal(
        val productName: String,
        val storeName: String,
        val title: String,
        val snippet: String,
        val url: String,
        val price: String? = null,
        val discount: String? = null
    )
    
    /**
     * Searches for deals on a specific product.
     * 
     * @param product Product name to search for (e.g., "jaja", "mlijeko")
     * @param location Optional location for better results (e.g., "Umag")
     * @return List of ShoppingDeal objects, or empty list if no deals found
     */
    suspend fun searchDeals(
        product: String,
        @Suppress("UNUSED_PARAMETER") location: String = "Hrvatska"
    ): List<ShoppingDeal> = withContext(Dispatchers.IO) {
        if (apiKey.isNullOrBlank() || engineId.isNullOrBlank()) {
            android.util.Log.w("GoogleCustomSearchService", "API key or Engine ID not configured")
            return@withContext emptyList()
        }
        
        try {
            // Build search query: "product akcija popust site:store1.hr OR site:store2.hr ..."
            // Add multiple discount keywords to ensure we only get actual deals
            val storeSiteFilter = storeDomains.joinToString(" OR ") { "site:$it" }
            // Search for actual deals: must contain "akcija" AND ("popust" OR discount indicators)
            val query = "$product (akcija popust OR akcija -% OR akcija sniženje) ($storeSiteFilter)"
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            
            // Build API URL
            val url = "https://www.googleapis.com/customsearch/v1?" +
                    "key=$apiKey&" +
                    "cx=$engineId&" +
                    "q=$encodedQuery&" +
                    "num=5" // Limit to 5 results per product
            
            android.util.Log.d("GoogleCustomSearchService", "Searching for: $query")
            
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                android.util.Log.e("GoogleCustomSearchService", "API call failed: ${response.code} ${response.message}")
                return@withContext emptyList()
            }
            
            val responseBody = response.body?.string()
            if (responseBody == null) {
                android.util.Log.e("GoogleCustomSearchService", "Empty response body")
                return@withContext emptyList()
            }
            
            val json = JSONObject(responseBody)
            val items = json.optJSONArray("items") ?: return@withContext emptyList()
            
            val deals = mutableListOf<ShoppingDeal>()
            
            for (i in 0 until items.length()) {
                val item = items.getJSONObject(i)
                val title = item.optString("title", "")
                val snippet = item.optString("snippet", "")
                val link = item.optString("link", "")
                
                // Extract store name from URL
                val storeName = extractStoreName(link)
                
                // Extract price/discount from snippet if available
                val priceInfo = extractPriceInfo(snippet)
                
                deals.add(
                    ShoppingDeal(
                        productName = product,
                        storeName = storeName,
                        title = title,
                        snippet = snippet,
                        url = link,
                        price = priceInfo.first,
                        discount = priceInfo.second
                    )
                )
            }
            
            android.util.Log.d("GoogleCustomSearchService", "Found ${deals.size} deals for $product")
            return@withContext deals
            
        } catch (e: Exception) {
            android.util.Log.e("GoogleCustomSearchService", "Error searching deals: ${e.message}", e)
            return@withContext emptyList()
        }
    }
    
    /**
     * Extracts store name from URL.
     */
    private fun extractStoreName(url: String): String {
        return when {
            url.contains("kaufland.hr") -> "Kaufland"
            url.contains("konzum.hr") -> "Konzum"
            url.contains("spar.hr") -> "Spar"
            url.contains("lidl.hr") -> "Lidl"
            url.contains("plodine.hr") -> "Plodine"
            else -> "Trgovina"
        }
    }
    
    /**
     * Extracts price and discount information from snippet text.
     * Returns Pair(price, discount) or null if not found.
     */
    private fun extractPriceInfo(snippet: String): Pair<String?, String?> {
        var price: String? = null
        var discount: String? = null
        
        // Try to find price patterns: "15.99 kn", "1.99€", "15,99 kn", etc.
        val pricePattern = Regex("""(\d+[.,]\d+)\s*(kn|€|EUR|HRK)""", RegexOption.IGNORE_CASE)
        val priceMatch = pricePattern.find(snippet)
        if (priceMatch != null) {
            price = priceMatch.value
        }
        
        // Try to find discount patterns: "-30%", "30% popust", "akcija", etc.
        val discountPattern = Regex("""(-?\d+%)|(\d+%\s*popust)|(akcija)""", RegexOption.IGNORE_CASE)
        val discountMatch = discountPattern.find(snippet)
        if (discountMatch != null) {
            discount = discountMatch.value
        }
        
        return Pair(price, discount)
    }
}
