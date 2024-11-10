package com.example.appheartbeat

data class Session(
    val startTime: Long,
    var endTime: Long
) {

    override fun toString(): String {
        return "[$startTime,$endTime]"
    }

    fun isActive(): Boolean {
        return System.currentTimeMillis() - endTime < HEARTBEAT_DURATION + HEARTBEAT_BUFFER
    }

    fun update(): Session {
        endTime = System.currentTimeMillis()
        return this
    }

    companion object {

        const val HEARTBEAT_DURATION = 60_000L
        private const val HEARTBEAT_BUFFER = 2_000L

        fun fromString(str: String): Session {
            val (startTime, endTime) = str.subSequence(1, str.length - 1)
                .split(",").map { it.toLong() }
            return Session(startTime, endTime)
        }

        fun newSession() = Session(
            System.currentTimeMillis(),
            System.currentTimeMillis()
        )
    }
}