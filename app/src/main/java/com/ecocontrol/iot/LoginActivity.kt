package com.ecocontrol.iot

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ecocontrol.iot.security.AuthStore

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val auth = AuthStore(this)
        if (auth.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        val user = findViewById<EditText>(R.id.user)
        val pass = findViewById<EditText>(R.id.password)
        val msg = findViewById<TextView>(R.id.loginMessage)

        // Convenience: clicking demo chip auto-fills credentials
        msg.setOnClickListener {
            user.setText("admin")
            pass.setText("IoT2026")
        }

        findViewById<Button>(R.id.loginButton).setOnClickListener {
            if (auth.login(user.text.toString().trim(), pass.text.toString())) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                msg.text = "⚠️ Credenciales inválidas. Usa admin / IoT2026."
            }
        }
    }
}
