package com.stack.data.mapper

import com.stack.data.local.db.entity.LyricsEntity
import com.stack.domain.model.Lyrics
import com.stack.domain.model.SyncedLine
import org.json.JSONArray

object LyricsMapper {

    fun toDomain(entity: LyricsEntity): Lyrics {
        val syncedLines = entity.lrcData?.let { parseLrcData(it) }
        return Lyrics(
            id = entity.id,
            trackId = entity.trackId,
            content = entity.content,
            isSynced = entity.isSynced,
            syncedLines = syncedLines,
            source = entity.source,
            updatedAt = entity.updatedAt
        )
    }

    fun toEntity(domain: Lyrics): LyricsEntity {
        val lrcData = domain.syncedLines?.let { serializeLrcData(it) }
        return LyricsEntity(
            id = domain.id,
            trackId = domain.trackId,
            content = domain.content,
            isSynced = domain.isSynced,
            lrcData = lrcData,
            source = domain.source,
            updatedAt = domain.updatedAt
        )
    }

    private fun parseLrcData(json: String): List<SyncedLine> {
        return try {
            val jsonArray = JSONArray(json)
            List(jsonArray.length()) { i ->
                val obj = jsonArray.getJSONObject(i)
                SyncedLine(
                    timestamp = obj.getLong("timestamp"),
                    text = obj.getString("text")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun serializeLrcData(lines: List<SyncedLine>): String {
        val jsonArray = JSONArray()
        lines.forEach { line ->
            val obj = org.json.JSONObject()
            obj.put("timestamp", line.timestamp)
            obj.put("text", line.text)
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }
}
