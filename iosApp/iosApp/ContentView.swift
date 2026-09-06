import SwiftUI
import shared

struct ContentView: View {
    var body: some View {
        Text("Glorious5 running on \(Platform_iosKt.platformName())")
            .padding()
    }
}

#Preview {
    ContentView()
}
