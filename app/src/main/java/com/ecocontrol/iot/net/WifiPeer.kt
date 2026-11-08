package com.ecocontrol.iot.net

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

class WifiPeer(private val port: Int = 5050) {
    private val executor = Executors.newCachedThreadPool()
    private var server: ServerSocket? = null
    private var socket: Socket? = null
    private var writer: PrintWriter? = null
    var onMessage: ((String) -> Unit)? = null
    var onStatus: ((String) -> Unit)? = null

    fun startServer() {
        executor.execute {
            try {
                stop()
                server = ServerSocket(port)
                onStatus?.invoke("Servidor activo en puerto $port")
                while (true) {
                    try {
                        val clientSocket = server!!.accept()
                        socket = clientSocket
                        writer = PrintWriter(clientSocket.getOutputStream(), true)
                        onStatus?.invoke("Cliente Android conectado")
                        readLoop(clientSocket)
                    } catch (_: Exception) {
                        if (server?.isClosed == true) break
                        onStatus?.invoke("Cliente desconectado, esperando...")
                    }
                }
            } catch (e: Exception) {
                onStatus?.invoke("Servidor detenido: ${e.message}")
            }
        }
    }

    fun connect(host: String) {
        executor.execute {
            try {
                stop()
                socket = Socket(host, port)
                writer = PrintWriter(socket!!.getOutputStream(), true)
                onStatus?.invoke("Conectado a $host:$port")
                readLoop(socket!!)
            } catch (e: Exception) {
                onStatus?.invoke("Error de conexión: ${e.message}")
            }
        }
    }

    private fun readLoop(s: Socket) {
        BufferedReader(InputStreamReader(s.getInputStream())).useLines { lines ->
            lines.forEach { onMessage?.invoke(it) }
        }
    }

    fun send(message: String) {
        executor.execute {
            try {
                writer?.println(message)
            } catch (_: Exception) {}
        }
    }

    fun stop() {
        try {
            socket?.close()
            server?.close()
        } catch (_: Exception) {}
    }
}
