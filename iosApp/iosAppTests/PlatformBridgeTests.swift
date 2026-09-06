import XCTest
import shared

/// Proves the Kotlin/Swift bridge actually works end to end -- not just that the framework
/// links (see ContentView.swift's plain `import shared`), but that calling into commonMain
/// Kotlin code from Swift and getting a real value back works on a real iOS Simulator.
final class PlatformBridgeTests: XCTestCase {
    func testPlatformNameComesFromKotlin() {
        XCTAssertEqual(PlatformKt.platformName(), "iOS")
    }
}
