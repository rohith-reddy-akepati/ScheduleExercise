package com.yinzcam.scheduleexercise.model


sealed class ScheduleListItem {

    data class SectionHeader(val heading: String) : ScheduleListItem()


    data class GameRow(val game: Game, val team: TeamInfo) : ScheduleListItem()
}


fun ScheduleResponse.seasonYear(): String? {
    val yearFilter = filters?.firstOrNull {
        it.queryParameter.equals("year", ignoreCase = true) ||
            it.name.contains("Season", ignoreCase = true)
    }
    return yearFilter?.current?.takeIf { it.isNotBlank() }
}


fun ScheduleResponse.toListItems(): List<ScheduleListItem> {
    val team = this.team ?: TeamInfo()
    val year = seasonYear()
    val items = mutableListOf<ScheduleListItem>()
    for (section in gameSections) {
        if (section.games.isEmpty()) continue
        val heading = when {
            year == null || section.heading.isBlank() -> section.heading
            section.heading.contains(year) -> section.heading
            else -> "$year ${section.heading}"
        }
        items += ScheduleListItem.SectionHeader(heading)
        for (game in section.games) {
            items += ScheduleListItem.GameRow(game, team)
        }
    }
    return items
}
