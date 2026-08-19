package com.example.androididea

import com.example.androididea.data.PlayerSeason
import com.example.androididea.data.repository.formatPosition
import com.example.androididea.data.repository.getTeamColor
import com.example.androididea.data.repository.toNBAPlayer
import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {
    @Test
    fun testPlayerSeasonToNBAPlayerMapping() {
        val season = PlayerSeason(
            id = 42,
            player = "LeBron James",
            season = "2023-24",
            team = "LAL",
            position = "SF",
            age = 39,
            games = 71,
            minutesPerGame = 35.3,
            pts = 25.7,
            reb = 7.3,
            ast = 8.3,
            stl = 1.3,
            blk = 0.5,
            fgPct = 0.54,
            fg3Pct = 0.41,
            ftPct = 0.75,
            per = 23.6,
            winShares = 9.8,
            compositeScore = 32.5
        )

        val player = season.toNBAPlayer()
        assertEquals(42, player.id)
        assertEquals("LeBron", player.firstName)
        assertEquals("James", player.lastName)
        assertEquals("Ailier", player.position)
        assertEquals("LAL", player.team)
        assertEquals("#552583", player.teamColor)
        assertEquals("2023-24", player.season)
        assertEquals(25.7, player.pts, 0.001)
        assertEquals(7.3, player.reb, 0.001)
        assertEquals(8.3, player.ast, 0.001)
        assertEquals(1.3, player.stl, 0.001)
        assertEquals(0.5, player.blk, 0.001)
    }

    @Test
    fun testPositionAndColorFormatting() {
        assertEquals("Meneur", formatPosition("PG"))
        assertEquals("Pivot", formatPosition("C"))
        assertEquals("Ailier Fort", formatPosition("PF"))
        assertEquals("#007A33", getTeamColor("BOS"))
        assertEquals("#1D428A", getTeamColor("GSW"))
    }
}