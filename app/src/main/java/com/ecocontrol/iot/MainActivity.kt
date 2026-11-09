package com.ecocontrol.iot

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ecocontrol.iot.data.IoTDatabase
import com.ecocontrol.iot.net.WifiPeer
import com.ecocontrol.iot.security.AuthStore
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Locale
import kotlin.concurrent.thread
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var db: IoTDatabase
    private lateinit var peer: WifiPeer
    private lateinit var status: TextView
    private lateinit var readings: TextView
    private lateinit var history: TextView
    private lateinit var relay: CompoundButton
    private var temp = 22.5
    private var humidity = 55.0
    private var isHost = false

    private val relayListener = CompoundButton.OnCheckedChangeListener { _, checked ->
        if (isHost) {
            db.addEvent("RELAY", if (checked) "Activado localmente" else "Desactivado localmente")
            broadcastState()
        } else {
            peer.send("RELAY|${if (checked) 1 else 0}")
        }
        render()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        db = IoTDatabase(this)
        peer = WifiPeer()
        status = findViewById(R.id.status)
        readings = findViewById(R.id.readings)
        history = findViewById(R.id.history)
        relay = findViewById(R.id.relay)

        findViewById<TextView>(R.id.localIp).text = "IP local: ${localIp()}"
        findViewById<Button>(R.id.hostButton).setOnClickListener { startHost() }
        findViewById<Button>(R.id.connectButton).setOnClickListener { connectToHost() }
        
        relay.setOnCheckedChangeListener(relayListener)

        findViewById<Button>(R.id.refreshButton).setOnClickListener { showHistory() }
        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            peer.stop()
            AuthStore(this).logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        peer.onStatus = { text ->
            runOnUiThread {
                val icon = when {
                    text.contains("activo", ignoreCase = true) || text.contains("conectado", ignoreCase = true) -> "🟢"
                    text.contains("error", ignoreCase = true) || text.contains("detenido", ignoreCase = true) -> "🔴"
                    else -> "🔵"
                }
                status.text = "$icon $text"
            }
        }
        peer.onMessage = { message -> handleMessage(message) }
        showHistory()
        render()
    }

    private fun startHost() {
        isHost = true
        status.text = "🟡 Iniciando servidor Wi-Fi…"
        peer.startServer()
        thread {
            while (isHost) {
                Thread.sleep(5000)
                temp = 20 + Random.nextDouble(0.0, 8.0)
                humidity = 45 + Random.nextDouble(0.0, 25.0)
                db.addMeasurement(temp, humidity, relay.isChecked)
                runOnUiThread { render(); showHistory() }
                broadcastState()
            }
        }
    }

    private fun connectToHost() {
        val ip = findViewById<EditText>(R.id.hostIp).text.toString().trim()
        if (ip.isBlank()) {
            status.text = "⚠️ Escribe la IP del dispositivo servidor"
            return
        }
        isHost = false
        peer.connect(ip)
    }

    private fun broadcastState() {
        peer.send("STATE|${String.format(Locale.US, "%.1f", temp)}|${String.format(Locale.US, "%.1f", humidity)}|${if (relay.isChecked) 1 else 0}")
    }

    private fun handleMessage(message: String) {
        val p = message.split("|")
        if (p[0] == "STATE" && p.size >= 4) {
            temp = p[1].toDoubleOrNull() ?: temp
            humidity = p[2].toDoubleOrNull() ?: humidity
            val r = p[3] == "1"
            runOnUiThread {
                relay.setOnCheckedChangeListener(null)
                relay.isChecked = r
                relay.setOnCheckedChangeListener(relayListener)
                db.addMeasurement(temp, humidity, r)
                render()
                showHistory()
            }
        } else if (p[0] == "RELAY" && isHost) {
            val r = p.getOrNull(1) == "1"
            runOnUiThread {
                relay.setOnCheckedChangeListener(null)
                relay.isChecked = r
                relay.setOnCheckedChangeListener(relayListener)
                db.addEvent("RELAY", "Control remoto: ${if (r) "ON" else "OFF"}")
                render()
            }
            broadcastState()
        }
    }

    private fun render() {
        val relayState = if (relay.isChecked) "🟢 ACTIVADO" else "⚪ DESACTIVADO"
        readings.text = "🌡️ Temperatura:  ${String.format(Locale.US, "%.1f", temp)} °C\n💧 Humedad:      ${String.format(Locale.US, "%.1f", humidity)} %\n⚡ Ventilación:  $relayState"
    }

    private fun showHistory() {
        history.text = db.latest().joinToString("\n").ifBlank { "Sin mediciones todavía" }
    }

    private fun localIp(): String {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val intf = interfaces.nextElement()
                val addrs = intf.inetAddresses
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: "No disponible"
                    }
                }
            }
            "No disponible"
        } catch (_: Exception) {
            "No disponible"
        }
    }

    override fun onDestroy() {
        isHost = false
        peer.stop()
        super.onDestroy()
    }
}
