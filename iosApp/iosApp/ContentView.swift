import SwiftUI
// Proves the :shared Kotlin framework actually resolves and links into this app -- no Kotlin
// code is called yet, that's the next step (a minimal smoke test).
import shared

struct ContentView: View {
    var body: some View {
        Text("Glorious5 iOS shell")
            .padding()
    }
}

#Preview {
    ContentView()
}
