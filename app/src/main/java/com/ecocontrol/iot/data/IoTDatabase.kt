package com.ecocontrol.iot.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class IoTDatabase(context: Context) : SQLiteOpenHelper(context, "ecocontrol.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE measurements(id INTEGER PRIMARY KEY AUTOINCREMENT, temperature REAL, humidity REAL, relay INTEGER, created_at TEXT)")
        db.execSQL("CREATE TABLE events(id INTEGER PRIMARY KEY AUTOINCREMENT, type TEXT, detail TEXT, created_at TEXT)")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS measurements")
        db.execSQL("DROP TABLE IF EXISTS events")
        onCreate(db)
    }
    fun addMeasurement(t: Double, h: Double, relay: Boolean) { writableDatabase.insert("measurements", null, ContentValues().apply { put("temperature", t); put("humidity", h); put("relay", if (relay) 1 else 0); put("created_at", System.currentTimeMillis().toString()) }) }
    fun addEvent(type: String, detail: String) { writableDatabase.insert("events", null, ContentValues().apply { put("type", type); put("detail", detail); put("created_at", System.currentTimeMillis().toString()) }) }
    fun latest(limit: Int = 8): List<String> { val out = mutableListOf<String>(); readableDatabase.rawQuery("SELECT temperature, humidity, relay, created_at FROM measurements ORDER BY id DESC LIMIT ?", arrayOf(limit.toString())).use { c -> while (c.moveToNext()) out.add("T=${c.getDouble(0).format()}°C  H=${c.getDouble(1).format()}%  Relé=${if (c.getInt(2)==1) "ON" else "OFF"}") }; return out }
    private fun Double.format() = String.format(java.util.Locale.US, "%.1f", this)
}
