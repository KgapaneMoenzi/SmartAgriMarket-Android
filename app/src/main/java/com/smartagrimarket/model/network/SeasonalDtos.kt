package com.smartagrimarket.model.network

data class InSeasonItemDto(
    val emoji: String,
    val name: String,
    val status: String
)

data class PlantingReminderDto(
    val label: String
)

data class SeasonalResponse(
    val region: String,
    val month: Int,
    val monthLabel: String,
    val monthTip: String,
    val inSeason: List<InSeasonItemDto>,
    val upcoming: List<PlantingReminderDto>
)