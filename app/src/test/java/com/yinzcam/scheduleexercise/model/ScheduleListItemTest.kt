package com.yinzcam.scheduleexercise.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleListItemTest {

    private fun response(
        sections: List<GameSection>,
        filters: List<ScheduleFilter>? = listOf(
            ScheduleFilter(name = "Season Start Year", queryParameter = "year", current = "2020")
        )
    ) = ScheduleResponse(
        team = TeamInfo(triCode = "GB", name = "PACKERS", record = "13-3"),
        gameSections = sections,
        filters = filters
    )

    @Test
    fun `section headings are prefixed with the season year`() {
        val items = response(
            listOf(GameSection(heading = "REGULAR SEASON", games = listOf(Game(type = "F"))))
        ).toListItems()

        val header = items.first() as ScheduleListItem.SectionHeader
        assertEquals("2020 REGULAR SEASON", header.heading)
    }

    @Test
    fun `heading already containing the year is not double-prefixed`() {
        val items = response(
            listOf(GameSection(heading = "2020 PRESEASON", games = listOf(Game(type = "F"))))
        ).toListItems()

        assertEquals("2020 PRESEASON", (items.first() as ScheduleListItem.SectionHeader).heading)
    }

    @Test
    fun `heading is left alone when the feed has no year filter`() {
        val items = response(
            listOf(GameSection(heading = "REGULAR SEASON", games = listOf(Game(type = "F")))),
            filters = null
        ).toListItems()

        assertEquals("REGULAR SEASON", (items.first() as ScheduleListItem.SectionHeader).heading)
    }

    @Test
    fun `each section contributes one header followed by its games in order`() {
        val items = response(
            listOf(
                GameSection("REGULAR SEASON", listOf(Game(id = 1, type = "F"), Game(id = 2, type = "B"))),
                GameSection("POSTSEASON", listOf(Game(id = 3, type = "S")))
            )
        ).toListItems()

        assertEquals(5, items.size)
        assertTrue(items[0] is ScheduleListItem.SectionHeader)
        assertEquals(1L, (items[1] as ScheduleListItem.GameRow).game.id)
        assertEquals(2L, (items[2] as ScheduleListItem.GameRow).game.id)
        assertTrue(items[3] is ScheduleListItem.SectionHeader)
        assertEquals(3L, (items[4] as ScheduleListItem.GameRow).game.id)
    }

    @Test
    fun `empty sections are skipped entirely`() {
        val items = response(
            listOf(
                GameSection("PRESEASON", emptyList()),
                GameSection("REGULAR SEASON", listOf(Game(id = 9, type = "F")))
            )
        ).toListItems()

        assertEquals(2, items.size)
        assertEquals("2020 REGULAR SEASON", (items[0] as ScheduleListItem.SectionHeader).heading)
    }

    @Test
    fun `seasonYear is null when the filter list is empty`() {
        assertNull(response(emptyList(), filters = emptyList()).seasonYear())
    }
}
