package org.umn.kulkaskis.data.remote

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.umn.kulkaskis.data.FridgeItem
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime

class UnauthorizedException : Exception("Session expired")

class SupabaseDb {
    private val client = OkHttpClient()
    private val jsonType = "application/json; charset=utf-8".toMediaType()
    private val base = SupabaseConfig.URL + "/rest/v1/fridge_items"

    private fun Request.Builder.withAuth(token: String): Request.Builder =
        addHeader("apikey", SupabaseConfig.KEY)
            .addHeader("Authorization", "Bearer " + token)

    fun fetchAll(token: String): List<FridgeItem> {
        val request = Request.Builder()
            .url(base + "?select=*")
            .withAuth(token)
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            check(response.code, text)
            val array = JSONArray(text)
            return (0 until array.length()).map { toItem(array.getJSONObject(it)) }
        }
    }

    fun upsert(token: String, items: List<FridgeItem>) {
        if (items.isEmpty()) return
        val array = JSONArray()
        items.forEach { array.put(toJson(it)) }
        val request = Request.Builder()
            .url(base + "?on_conflict=id")
            .withAuth(token)
            .addHeader("Prefer", "resolution=merge-duplicates,return=minimal")
            .post(array.toString().toRequestBody(jsonType))
            .build()
        client.newCall(request).execute().use { response ->
            check(response.code, response.body?.string().orEmpty())
        }
    }

    private fun check(code: Int, body: String) {
        if (code == 401) throw UnauthorizedException()
        if (code !in 200..299) throw IOException("HTTP " + code + ": " + body)
    }

    private fun toJson(item: FridgeItem): JSONObject = JSONObject()
        .put("id", item.id)
        .put("name", item.name)
        .put("quantity", item.quantity)
        .put("expiry", item.expiry.toString())
        .put("category", item.category)
        .put("purchase_date", item.purchaseDate.toString())
        .put("updated_at", Instant.ofEpochMilli(item.updatedAt).toString())
        .put("deleted", item.deleted)

    private fun toItem(o: JSONObject): FridgeItem = FridgeItem(
        id = o.getString("id"),
        name = o.getString("name"),
        quantity = o.getInt("quantity"),
        expiry = LocalDate.parse(o.getString("expiry")),
        category = o.optString("category", "Other"),
        purchaseDate = LocalDate.parse(o.getString("purchase_date")),
        updatedAt = OffsetDateTime.parse(o.getString("updated_at")).toInstant().toEpochMilli(),
        deleted = o.optBoolean("deleted", false),
        dirty = false
    )
}