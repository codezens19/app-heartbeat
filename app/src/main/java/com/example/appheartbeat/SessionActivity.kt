package com.example.appheartbeat

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_session.*
import kotlin.random.Random

class SessionActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    private val onSessionTimeoutListener = object : OnSessionTimeoutListener {
        override fun onTimeout() {
            AlertDialog.Builder(this@SessionActivity).apply {
                setTitle("Session Alert!")
                setMessage("Session time out, click on RESUME to restart session")
                setCancelable(false)
                setPositiveButton("RESUME") { dialogInterface, _ ->
                    sessionManager.resumeSession()
                    dialogInterface.dismiss()
                }
                show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session)

        val sharedPrefs = getSharedPreferences(
            SessionManager.SHARED_PREFS_NAME,
            Context.MODE_PRIVATE
        )
        sessionManager = SessionManager(
            sharedPrefs, this, Gson(), onSessionTimeoutListener
        )

        btn.setOnClickListener { changeBackground() }
    }

    private fun changeBackground() {
        val random = Random
        val color = Color.argb(
            255,
            random.nextInt(255),
            random.nextInt(255),
            random.nextInt(255)
        )
        layout.setBackgroundColor(color)
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        sessionManager.onUserInteraction()
    }
}
