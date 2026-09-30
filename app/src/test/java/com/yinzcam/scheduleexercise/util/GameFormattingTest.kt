package com.yinzcam.scheduleexercise.util

import com.yinzcam.scheduleexercise.model.Game
import com.yinzcam.scheduleexercise.model.GameDate
import com.yinzcam.scheduleexercise.model.OpponentInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

/**
 * These fixtures are taken directly from the live schedule.json feed (see README), not
 * invented, so a failure here means the formatting logic disagrees with real data.
 */
class GameFormattingTest {

    private val eastern = TimeZone.getTimeZone("America/New_York")
    private val utc = TimeZone.getTimeZone("UTC")

    private fun finalGame(
        home: Boolean,
        homeScore: String,
        awayScore: String,
        gameState: String = "Final"
    ) = Game(
        type = GameFormatting.TYPE_FINAL,
        week = "Week 1",
        gameState = gameState,
        homeScore = homeScore,
        awayScore = awayScore,
        home = home,
        date = GameDate(timestamp = "2020-09-13T17:00:00Z"),
        opponent = OpponentInfo(triCode = "MIN")
    )

    @Test
    fun `scheduled game whose timestamp crosses midnight UTC shows the previous local date`() {
        val game = Game(
            type = GameFormatting.TYPE_SCHEDULED,
            week = "Week 3",
            date = GameDate(timestamp = "2020-09-28T00:20:00Z"),
            opponent = OpponentInfo(triCode = "NO")
        )

        assertEquals("Sun, Sep 27", GameFormatting.formatGameDate(game, eastern))
        assertEquals("8:20 PM", GameFormatting.formatGameTime(game, eastern))

        // Same instant, but from UTC's point of view it's already the next day.
        assertEquals("Mon, Sep 28", GameFormatting.formatGameDate(game, utc))
        assertEquals("12:20 AM", GameFormatting.formatGameTime(game, utc))
    }

    @Test
    fun `right label is kickoff time for scheduled games`() {
        val game = Game(
            type = GameFormatting.TYPE_SCHEDULED,
            date = GameDate(timestamp = "2020-09-13T17:00:00Z")
        )
        assertEquals("1:00 PM", GameFormatting.formatRightLabel(game, eastern))
    }

    @Test
    fun `right label is game state for final games`() {
        val game = finalGame(home = false, homeScore = "34", awayScore = "43")
        assertEquals("Final", GameFormatting.formatRightLabel(game, eastern))
    }

    @Test
    fun `our score is away score when we are the away team`() {
        // Real Week 1 result from the feed: GB (away) 43, MIN (home) 34, "43-34 W".
        val game = finalGame(home = false, homeScore = "34", awayScore = "43")
        assertEquals("43", GameFormatting.homeTeamScore(game))
        assertEquals("34", GameFormatting.opponentScore(game))
    }

    @Test
    fun `our score is home score when we are the home team`() {
        // Real Week 2 result from the feed: GB (home) 42, DET (away) 21, "42-21 W".
        val game = finalGame(home = true, homeScore = "42", awayScore = "21")
        assertEquals("42", GameFormatting.homeTeamScore(game))
        assertEquals("21", GameFormatting.opponentScore(game))
    }

    @Test
    fun `hasScore is true only for final games with scores`() {
        assertTrue(GameFormatting.hasScore(finalGame(home = true, homeScore = "42", awayScore = "21")))

        val scheduled = Game(type = GameFormatting.TYPE_SCHEDULED, homeScore = "", awayScore = "")
        assertFalse(GameFormatting.hasScore(scheduled))

        val bye = Game(type = GameFormatting.TYPE_BYE)
        assertFalse(GameFormatting.hasScore(bye))
    }
}
