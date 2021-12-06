package dev.jaym21.skanner.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if()

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}