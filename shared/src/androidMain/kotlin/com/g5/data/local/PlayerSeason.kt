package com.g5.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Les noms de colonnes SQLite (snake_case, générés par le script Python) sont
// mappés explicitement vers des propriétés Kotlin en camelCase.
@Entity(
    tableName = "player_seasons",
    indices = [
        Index(value = ["player"], name = "idx_player"),
        Index(value = ["composite_score"], orders = [Index.Order.DESC], name = "idx_score")
    ]
)
data class PlayerSeason(
    @PrimaryKey val id: Int?,
    val player: String,
    val season: String,
    @ColumnInfo(name = "team") val team: String?,
    @ColumnInfo(name = "position") val position: String?,
    @ColumnInfo(name = "age") val age: Int?,
    @ColumnInfo(name = "games") val games: Int?,
    @ColumnInfo(name = "minutes_per_game") val minutesPerGame: Double?,
    @ColumnInfo(name = "pts") val pts: Double?,
    @ColumnInfo(name = "reb") val reb: Double?,
    @ColumnInfo(name = "ast") val ast: Double?,
    @ColumnInfo(name = "stl") val stl: Double?,
    @ColumnInfo(name = "blk") val blk: Double?,
    @ColumnInfo(name = "fg_pct") val fgPct: Double?,
    @ColumnInfo(name = "fg3_pct") val fg3Pct: Double?,
    @ColumnInfo(name = "ft_pct") val ftPct: Double?,
    @ColumnInfo(name = "per") val per: Double?,
    @ColumnInfo(name = "win_shares") val winShares: Double?,
    @ColumnInfo(name = "composite_score") val compositeScore: Double?
)
