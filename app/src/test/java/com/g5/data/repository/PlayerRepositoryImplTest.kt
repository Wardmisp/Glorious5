package com.g5.data.repository

import com.g5.data.local.FakePlayerSeasonDao
import com.g5.data.local.NBA_PLAYERS
import com.g5.data.local.PlayerSeason
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Teste PlayerRepositoryImpl "en intégration" avec ses deux vraies dépendances de bord :
 * un [FakePlayerSeasonDao] en mémoire (au lieu de Room) et un SupabaseClient branché sur un faux
 * moteur HTTP (au lieu du réseau, voir [fakeSupabaseClient]) — seule la couche transport est
 * doublée, tout le reste (mapping PlayerSeason -> NBAPlayer, JSON -> NbaPlayerDto, fallback local)
 * est le vrai code de production.
 */
class PlayerRepositoryImplTest {

    private fun season(
        id: Int? = 1,
        player: String = "Nikola Jokic",
        season: String = "2023-24",
        team: String? = "DEN",
        position: String? = "C",
        pts: Double? = 26.4,
        reb: Double? = 12.4,
        ast: Double? = 9.0,
        stl: Double? = 1.4,
        blk: Double? = 0.9,
        fgPct: Double? = 0.58,
        fg3Pct: Double? = 0.35,
        ftPct: Double? = 0.81,
        per: Double? = 31.5,
        winShares: Double? = 15.2,
        games: Int? = 79,
        compositeScore: Double? = 95.0
    ) = PlayerSeason(
        id = id, player = player, season = season, team = team, position = position,
        age = 28, games = games, minutesPerGame = 34.0,
        pts = pts, reb = reb, ast = ast, stl = stl, blk = blk,
        fgPct = fgPct, fg3Pct = fg3Pct, ftPct = ftPct, per = per, winShares = winShares,
        compositeScore = compositeScore
    )

    private fun unreachableSupabaseClient() = fakeSupabaseClient { _ ->
        throw java.io.IOException("no network in this test")
    }

    // --- Room (via FakePlayerSeasonDao) ---

    @Test
    fun getAllPlayers_mapsRowsFromTheDao_whenItHasData() = runTest {
        val repo = PlayerRepositoryImpl(FakePlayerSeasonDao(listOf(season())), unreachableSupabaseClient())

        val players = repo.getAllPlayers()

        assertEquals(1, players.size)
        assertEquals("Nikola", players[0].firstName)
        assertEquals("Jokic", players[0].lastName)
    }

    @Test
    fun getAllPlayers_fallsBackToTheStaticPool_whenTheDaoIsEmpty() = runTest {
        val repo = PlayerRepositoryImpl(FakePlayerSeasonDao(emptyList()), unreachableSupabaseClient())

        val players = repo.getAllPlayers()

        assertEquals(NBA_PLAYERS, players)
    }

    @Test
    fun getAuctionPlayers_mapsRowsFromTheDao_whenItHasData() = runTest {
        val repo = PlayerRepositoryImpl(FakePlayerSeasonDao(listOf(season(id = 1), season(id = 2, player = "Luka Doncic"))), unreachableSupabaseClient())

        val players = repo.getAuctionPlayers(limit = 2)

        assertEquals(2, players.size)
        assertEquals(setOf(1, 2), players.map { it.id }.toSet())
    }

    @Test
    fun getAuctionPlayers_fallsBackToTheFirstNStaticPlayers_whenTheDaoIsEmpty() = runTest {
        val repo = PlayerRepositoryImpl(FakePlayerSeasonDao(emptyList()), unreachableSupabaseClient())

        val players = repo.getAuctionPlayers(limit = 3)

        assertEquals(NBA_PLAYERS.take(3), players)
    }

    @Test
    fun getPlayerById_returnsTheMappedRow_whenFoundInTheDao() = runTest {
        val repo = PlayerRepositoryImpl(FakePlayerSeasonDao(listOf(season(id = 42, player = "Test Player"))), unreachableSupabaseClient())

        val player = repo.getPlayerById(42)

        assertNotNull(player)
        assertEquals("Test", player!!.firstName)
        assertEquals("Player", player.lastName)
    }

    @Test
    fun getPlayerById_fallsBackToTheStaticPool_whenNotFoundInTheDao() = runTest {
        val repo = PlayerRepositoryImpl(FakePlayerSeasonDao(emptyList()), unreachableSupabaseClient())

        val player = repo.getPlayerById(NBA_PLAYERS[0].id)

        assertEquals(NBA_PLAYERS[0], player)
    }

    @Test
    fun getPlayerById_returnsNull_whenMissingFromBothTheDaoAndTheStaticPool() = runTest {
        val repo = PlayerRepositoryImpl(FakePlayerSeasonDao(emptyList()), unreachableSupabaseClient())

        assertNull(repo.getPlayerById(-999))
    }

    @Test
    fun getAllSeasons_mapsWhateverTheDaoHas_withNoFallbackWhenEmpty() = runTest {
        // Contrairement à getAllPlayers()/getAuctionPlayers(), getAllSeasons() ne retombe jamais
        // sur NBA_PLAYERS : un DAO vide donne bien une liste vide, pas le pool statique.
        val repo = PlayerRepositoryImpl(FakePlayerSeasonDao(emptyList()), unreachableSupabaseClient())

        assertTrue(repo.getAllSeasons().isEmpty())
    }

    @Test
    fun getAllPlayersFlow_emitsTheMappedRowsFromTheDaoFlow() = runTest {
        val repo = PlayerRepositoryImpl(FakePlayerSeasonDao(listOf(season(id = 7))), unreachableSupabaseClient())

        val emitted = repo.getAllPlayersFlow().first()

        assertEquals(listOf(7), emitted.map { it.id })
    }

    // --- mapping PlayerSeason -> NBAPlayer (toNBAPlayer) ---

    @Test
    fun toNBAPlayer_splitsFullNameIntoFirstAndLastName() {
        assertEquals("Nikola" to "Jokic", season(player = "Nikola Jokic").toNBAPlayer().let { it.firstName to it.lastName })
    }

    @Test
    fun toNBAPlayer_singleWordName_leavesLastNameBlank() {
        val player = season(player = "Zion").toNBAPlayer()

        assertEquals("Zion", player.firstName)
        assertEquals("", player.lastName)
    }

    @Test
    fun toNBAPlayer_stripsDiacriticsFromTheName() {
        val player = season(player = "Luka Dončić").toNBAPlayer()

        assertEquals("Luka", player.firstName)
        assertEquals("Doncic", player.lastName)
    }

    @Test
    fun toNBAPlayer_missingNumericStats_defaultToZeroInsteadOfNull() {
        val player = season(pts = null, reb = null, ast = null, stl = null, blk = null, winShares = null, games = null).toNBAPlayer()

        assertEquals(0.0, player.pts, 0.0)
        assertEquals(0.0, player.winShares, 0.0)
        assertEquals(0, player.games)
    }

    @Test
    fun toNBAPlayer_missingTeam_defaultsToNbaPlaceholderAndGenericColor() {
        val player = season(team = null).toNBAPlayer()

        assertEquals("NBA", player.team)
        assertEquals("#1D428A", player.teamColor)
    }

    @Test
    fun toNBAPlayer_missingId_defaultsToZero() {
        val player = season(id = null).toNBAPlayer()

        assertEquals(0, player.id)
    }

    // --- Supabase (via fakeSupabaseClient) ---

    private val nbaBest1000Json = """
        [
          {"id": 101, "player": "Test Player", "position": "PG", "team": "GSW",
           "season": "2023-24", "pts": 20.0, "reb": 5.0, "ast": 6.0}
        ]
    """.trimIndent()

    @Test
    fun getSupabaseAuctionPlayers_decodesTheHttpResponse_andAppliesDisplayFormatting() = runTest {
        val client = fakeSupabaseClient { jsonResponse(nbaBest1000Json) }
        val repo = PlayerRepositoryImpl(FakePlayerSeasonDao(emptyList()), client)

        val players = repo.getSupabaseAuctionPlayers(limit = 10)

        assertEquals(1, players.size)
        val player = players.single()
        assertEquals(101, player.id)
        assertEquals("PG", player.position) // déjà un code stable, formatPosition ne fait que normaliser
        assertEquals("#1D428A", player.teamColor) // GSW mappé par getTeamColor
    }

    @Test
    fun getSupabaseAuctionPlayers_neverReturnsMoreThanTheRequestedLimit() = runTest {
        val threePlayersJson = """
            [
              {"id": 1, "player": "A", "position": "PG", "team": "GSW", "season": "2023-24", "pts": 1.0, "reb": 1.0, "ast": 1.0},
              {"id": 2, "player": "B", "position": "SG", "team": "GSW", "season": "2023-24", "pts": 1.0, "reb": 1.0, "ast": 1.0},
              {"id": 3, "player": "C", "position": "SF", "team": "GSW", "season": "2023-24", "pts": 1.0, "reb": 1.0, "ast": 1.0}
            ]
        """.trimIndent()
        val client = fakeSupabaseClient { jsonResponse(threePlayersJson) }
        val repo = PlayerRepositoryImpl(FakePlayerSeasonDao(emptyList()), client)

        val players = repo.getSupabaseAuctionPlayers(limit = 2)

        assertEquals(2, players.size)
    }

    @Test
    fun getSupabaseAuctionPlayers_onHttpFailure_fallsBackToTheLocalDao() = runTest {
        val localSeason = season(id = 55, player = "Local Fallback")
        val client = fakeSupabaseClient { jsonResponse("{\"message\":\"boom\"}", HttpStatusCode.InternalServerError) }
        val repo = PlayerRepositoryImpl(FakePlayerSeasonDao(listOf(localSeason)), client)

        val players = repo.getSupabaseAuctionPlayers(limit = 10)

        assertEquals(listOf(55), players.map { it.id })
    }

    @Test
    fun getSupabaseAuctionPlayers_onNetworkException_fallsBackToTheLocalDao() = runTest {
        val localSeason = season(id = 55, player = "Local Fallback")
        val repo = PlayerRepositoryImpl(FakePlayerSeasonDao(listOf(localSeason)), unreachableSupabaseClient())

        val players = repo.getSupabaseAuctionPlayers(limit = 10)

        assertEquals(listOf(55), players.map { it.id })
    }
}
