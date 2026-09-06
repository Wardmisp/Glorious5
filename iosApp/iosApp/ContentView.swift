import SwiftUI
import shared

struct ContentView: View {
    var body: some View {
        Text("Glorious5 running on \(PlatformKt.platformName())")
            .padding()
    }
}

#Preview {
    ContentView()
}
