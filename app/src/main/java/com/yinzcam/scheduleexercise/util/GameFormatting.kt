package com.yinzcam.scheduleexercise.util

import com.yinzcam.scheduleexercise.model.Game
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Formatting helpers for the schedule screen. Kept as plain functions (no Android
 * framework calls) so the date-math can be unit tested / sanity-checked on a plain JVM.
 */
object GameFormatting {

    const val TYPE_SCHEDULED = "S"
    const val TYPE_FINAL = "F"
    const val TYPE_BYE = "B"

    /**
     * schedule.json encodes game timestamps as ISO-8601 UTC, e.g. "2020-09-13T17:00:00Z".
     * A fresh SimpleDateFormat is built per call rather than shared/cached - SimpleDateFormat
     * is not thread-safe, and this is cheap enough that it's not worth a ThreadLocal here.
     */
    private fun isoUtcFormat(): SimpleDateFormat =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

    private fun parseTimestamp(timestamp: String): Date? {
        if (timestamp.isBlank()) return null
        return try {
            isoUtcFormat().parse(timestamp)
        } catch (e: Exception) {
            null
        }
    }

    /** e.g. "Mon, Sep 14" - rendered in the device's local time zone. */
    fun formatGameDate(game: Game, deviceTimeZone: TimeZone = TimeZone.getDefault()): String {
        val timestamp = game.date?.timestamp.orEmpty()
        val date = parseTimestamp(timestamp) ?: return game.date?.text.orEmpty()
        val formatter = SimpleDateFormat("EEE, MMM d", Locale.US).apply {
            timeZone = deviceTimeZone
        }
        return formatter.format(date)
    }

    /**
     * Scheduled games show kickoff time in the user's own time zone (per spec).
     * Final games show the game state (e.g. "Final") instead - see [formatRightLabel].
     */
    fun formatGameTime(game: Game, deviceTimeZone: TimeZone = TimeZone.getDefault()): String {
        val timestamp = game.date?.timestamp.orEmpty()
        val date = parseTimestamp(timestamp) ?: return game.date?.time.orEmpty()
        val formatter = SimpleDateFormat("h:mm a", Locale.US).apply {
            timeZone = deviceTimeZone
        }
        return formatter.format(date)
    }

    /** Right-hand label on the info row: kickoff time for scheduled games, game state for finals. */
    fun formatRightLabel(game: Game, deviceTimeZone: TimeZone = TimeZone.getDefault()): String {
        return when (game.type) {
            TYPE_FINAL -> game.gameState.ifBlank { "Final" }
            TYPE_SCHEDULED -> formatGameTime(game, deviceTimeZone)
            else -> ""
        }
    }

    /** "Week 1" / "Preseason 1" / etc. - the feed already formats this for us. */
    fun formatWeekLabel(game: Game): String = game.week

    /** Our team's score for this game, taking home/away into account. */
    fun homeTeamScore(game: Game): String = if (game.home) game.homeScore else game.awayScore

    /** Opponent's score for this game, taking home/away into account. */
    fun opponentScore(game: Game): String = if (game.home) game.awayScore else game.homeScore

    fun hasScore(game: Game): Boolean =
        game.type == TYPE_FINAL && homeTeamScore(game).isNotBlank() && opponentScore(game).isNotBlank()
}
