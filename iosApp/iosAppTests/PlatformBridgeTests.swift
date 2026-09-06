import XCTest
import shared

/// Proves the Kotlin/Swift bridge actually works end to end -- not just that the framework
/// links (see ContentView.swift's plain `import shared`), but that calling into commonMain
/// Kotlin code from Swift and getting a real value back works on a real iOS Simulator.
final class PlatformBridgeTests: XCTestCase {
    func testPlatformNameComesFromKotlin() {
        XCTAssertEqual(Platform_iosKt.platformName(), "iOS")
    }

    /// Prouve que la base de joueurs pré-remplie (nba_top300.db, copiée du bundle iOS vers
    /// Application Support puis ouverte par Room -- voir AppDatabase.ios.kt) répond réellement
    /// sur le simulateur, pas seulement que le framework compile et se lie.
    func testPlayerDatabaseLoadsOnIOS() {
        let firstPlayer = DatabaseSmokeTestKt.debugFirstPlayerName()
        XCTAssertNotNil(firstPlayer)
    }
}
