package it.pietro.calcologiorniedate

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

internal data class CalculationRecord(
    val id: Long,
    val title: String,
    val type: String,
    val firstDate: String,
    val secondDate: String,
    val days: String,
    val result: String,
    val createdAt: Long
)

internal class CalculationStorage(context: Context) {
    private val preferences = context.getSharedPreferences("date_calculator_data", Context.MODE_PRIVATE)

    fun history(): List<CalculationRecord> = readRecords("history")
    fun favorites(): List<CalculationRecord> = readRecords("favorites")

    fun addToHistory(record: CalculationRecord) {
        val updated = (listOf(record) + history()).take(10)
        writeRecords("history", updated)
    }

    fun addFavorite(record: CalculationRecord) {
        writeRecords("favorites", favorites().filterNot { it.id == record.id } + record)
    }

    fun deleteFavorite(id: Long) {
        writeRecords("favorites", favorites().filterNot { it.id == id })
    }

    fun deleteHistoryItem(id: Long) {
        writeRecords("history", history().filterNot { it.id == id })
    }

    fun clearHistory() = writeRecords("history", emptyList())
    fun clearFavorites() = writeRecords("favorites", emptyList())

    fun theme(): String = preferences.getString("theme", "system") ?: "system"
    fun setTheme(value: String) { preferences.edit().putString("theme", value).apply() }

    private fun readRecords(key: String): List<CalculationRecord> {
        val raw = preferences.getString(key, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { index ->
                val item = array.optJSONObject(index) ?: return@mapNotNull null
                CalculationRecord(
                    id = item.optLong("id"),
                    title = item.optString("title"),
                    type = item.optString("type"),
                    firstDate = item.optString("firstDate"),
                    secondDate = item.optString("secondDate"),
                    days = item.optString("days"),
                    result = item.optString("result"),
                    createdAt = item.optLong("createdAt")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun writeRecords(key: String, records: List<CalculationRecord>) {
        val array = JSONArray()
        records.forEach { record ->
            array.put(JSONObject().apply {
                put("id", record.id)
                put("title", record.title)
                put("type", record.type)
                put("firstDate", record.firstDate)
                put("secondDate", record.secondDate)
                put("days", record.days)
                put("result", record.result)
                put("createdAt", record.createdAt)
            })
        }
        preferences.edit().putString(key, array.toString()).apply()
    }
}
