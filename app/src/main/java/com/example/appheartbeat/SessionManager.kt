package com.example.appheartbeat

import android.content.SharedPreferences
import android.os.Handler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.google.gson.Gson
import timber.log.Timber

class SessionManager(
    private val sharedPreferences: SharedPreferences,
    lifecycleOwner: LifecycleOwner,
    private val gson: Gson,
    private val onSessionTimeoutListener: OnSessionTimeoutListener
) {

    private var hasUserInteracted = true

    init {
        lifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun startSession() {
                Timber.d("Starting Session for activity: ${lifecycleOwner.javaClass.name}")
                handler.post(runnable)
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            fun pauseSession() {
                Timber.d("Pausing Session for activity: ${lifecycleOwner.javaClass.name}")
                handler.removeCallbacks(runnable)
            }
        })
    }

    private val handler = Handler()
    private val runnable: Runnable = object : Runnable {
        override fun run() {
            // TODO:: Perhaps wrap this with lifecycle.whenResumed {} ?
            handler.postDelayed(this, Session.HEARTBEAT_DURATION)
            heartBeat()
        }
    }

    fun onUserInteraction() {
        hasUserInteracted = true
    }

    fun resumeSession() {
        hasUserInteracted = true

        handler.removeCallbacks(runnable)
        handler.post(runnable)
    }

    private fun heartBeat() {
        if (hasUserInteracted) {
            val currentSession = getLastSession()
            if (currentSession != null && currentSession.isActive()) {
                updateCurrentSession()
            } else {
                createNewSession()
            }
        } else {
            handler.removeCallbacks(runnable)
            onSessionTimeoutListener.onTimeout()
        }

        hasUserInteracted = false
    }

    private fun getLastSession(): Session? {
        val heartBeats = getUnsyncedHeartBeats()
        return if (heartBeats.isNotEmpty()) {
            Session.fromString(heartBeats.last())
        } else {
            null
        }
    }

    private fun updateCurrentSession() {
        val sessions = getUnsyncedHeartBeats()
        val currentSession = Session.fromString(sessions.removeAt(sessions.size - 1))

        sessions.add(currentSession.update().toString())
        persistUnsyncedSessions(sessions)

        Timber.d("$sessions")
    }

    private fun createNewSession() {
        val sessions = getUnsyncedHeartBeats()
        sessions.add(Session.newSession().toString())
        persistUnsyncedSessions(sessions)

        Timber.d("$sessions")
    }

    private fun getUnsyncedHeartBeats(): MutableList<String> {
        val stringValue = sharedPreferences.getString(UNSYNCED_HEARTBEATS, "[]")
        return gson.fromJson(stringValue, mutableListOf<String>().javaClass)
    }

    private fun persistUnsyncedSessions(sessions: List<String>) {
        sharedPreferences
            .edit()
            .putString(UNSYNCED_HEARTBEATS, gson.toJson(sessions))
            .apply()
    }

    companion object {
        const val SHARED_PREFS_NAME = "SessionManagerPrefs"
        const val UNSYNCED_HEARTBEATS = "unsynced sessions"
    }
}