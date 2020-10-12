package com.techcoder.googlemapexample.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.techcoder.googlemapexample.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        btn_loaction.setOnClickListener {
            startActivity(Intent(applicationContext, MapsActivity::class.java))
            Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show()
        }

    }


}