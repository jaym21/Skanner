package dev.jaym21.skanner.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import dev.jaym21.skanner.R

class PermissionNotGrantedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission_not_granted)
    }
}