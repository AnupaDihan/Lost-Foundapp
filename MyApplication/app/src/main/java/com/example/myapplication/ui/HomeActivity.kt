package com.example.myapplication.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

import com.google.android.material.button.MaterialButton

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        findViewById<MaterialButton>(R.id.buttonCreateAdvert).setOnClickListener {
            startActivity(Intent(this, PostActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.buttonShowItems).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}
