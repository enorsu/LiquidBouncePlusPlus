/*
 * LiquidBounce++ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/PlusPlusMC/LiquidBouncePlusPlus/
 */
package net.ccbluex.liquidbounce.event

// Thank you very much https://github.com/0x16000/

/**
 * Base class for all events in the event system.
 */
open class Event {
    /**
     * The timestamp when the event was created (in nanoseconds).
     * Useful for event ordering and profiling.
     */
    val timestamp: Long = System.nanoTime()
}

/**
 * Base class for cancellable events.
 *
 * @property isCancelled Indicates whether this event has been cancelled.
 *                       Once cancelled, most event handlers should ignore the event.
 */
open class CancellableEvent : Event() {
    @Volatile
    var isCancelled: Boolean = false
        private set

    /**
     * Cancels this event, preventing further processing by most handlers.
     * @return true if the event was successfully cancelled (false if already cancelled)
     */
    fun cancelEvent(): Boolean {
        return !isCancelled.also { isCancelled = true }
    }

    /**
     * Resets the cancellation state of this event.
     * Use with caution - this should typically only be used by the event system.
     */
    fun resetCancellation() {
        isCancelled = false
    }
}

/**
 * Represents the state of an event in its lifecycle.
 *
 * @property PRE Event is about to be processed (before the main action occurs)
 * @property POST Event has been processed (after the main action occurred)
 */
enum class EventState(val stateName: String) {
    PRE("PRE"),
    POST("POST");

    companion object {
        /**
         * Converts a string to an EventState (case-insensitive).
         * @throws IllegalArgumentException if the string doesn't match any state
         */
        fun fromString(value: String): EventState {
            return values().first { it.stateName.equals(value, ignoreCase = true) }
        }
    }
}