package com.yinzcam.scheduleexercise.model

import com.google.gson.annotations.SerializedName


data class ScheduleResponse(
    @SerializedName("Team") val team: TeamInfo? = null,
    @SerializedName("DefaultGameId") val defaultGameId: Long = 0L,
    @SerializedName("GameSection") val gameSections: List<GameSection> = emptyList(),
    @SerializedName("Filters") val filters: List<ScheduleFilter>? = null
)

data class TeamInfo(
    @SerializedName("TriCode") val triCode: String = "",
    @SerializedName("FullName") val fullName: String = "",
    @SerializedName("Name") val name: String = "",
    @SerializedName("City") val city: String = "",
    @SerializedName("Record") val record: String = "",
    @SerializedName("Wins") val wins: String = "",
    @SerializedName("Losses") val losses: String = "",
    @SerializedName("WinPercentage") val winPercentage: String = "",
    @SerializedName("PrimaryColor") val primaryColor: String = ""
)

data class GameSection(
    @SerializedName("Heading") val heading: String = "",
    @SerializedName("Game") val games: List<Game> = emptyList()
)

/** Possible values for [type]: "S" (scheduled), "F" (final), "B" (bye). */
data class Game(
    @SerializedName("Id") val id: Long = 0L,
    @SerializedName("Type") val type: String = "",
    @SerializedName("Week") val week: String = "",
    @SerializedName("Label") val label: String = "",
    @SerializedName("ScheduleHeader") val scheduleHeader: String = "",
    @SerializedName("TV") val tv: String = "",
    @SerializedName("Radio") val radio: String = "",
    @SerializedName("Venue") val venue: String = "",
    @SerializedName("WLT") val wlt: String = "",
    @SerializedName("GameState") val gameState: String = "",
    @SerializedName("AwayScore") val awayScore: String = "",
    @SerializedName("HomeScore") val homeScore: String = "",
    @SerializedName("IsHome") val isHome: Boolean = false,
    @SerializedName("Home") val home: Boolean = false,
    @SerializedName("Result") val result: String = "",
    @SerializedName("Date") val date: GameDate? = null,
    @SerializedName("Opponent") val opponent: OpponentInfo? = null
)

data class GameDate(
    @SerializedName("Numeric") val numeric: String = "",
    @SerializedName("Text") val text: String = "",
    @SerializedName("Time") val time: String = "",
    @SerializedName("Timestamp") val timestamp: String = "",
    @SerializedName("IsTBA") val isTba: String = "false"
)

data class OpponentInfo(
    @SerializedName("TriCode") val triCode: String = "",
    @SerializedName("FullName") val fullName: String = "",
    @SerializedName("Name") val name: String = "",
    @SerializedName("City") val city: String = "",
    @SerializedName("Record") val record: String = ""
)

data class ScheduleFilter(
    @SerializedName("Name") val name: String = "",
    @SerializedName("QueryParameter") val queryParameter: String = "",
    @SerializedName("Current") val current: String = ""
)
