import SwiftUI
import UIKit
import shared

/// Héberge l'app complète (`BasketballDraftAppViewController`, côté Kotlin — menu, IA,
/// split-screen, en ligne, tutoriel...) dans la hiérarchie SwiftUI. La navigation entre écrans
/// est gérée entièrement côté Kotlin (NavHost partagé, voir shared/.../BasketballDraftApp.kt) ;
/// ce pont n'a donc plus besoin de connaître les routes.
struct BasketballDraftAppView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        BasketballDraftAppViewControllerKt.BasketballDraftAppViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        BasketballDraftAppView()
            .ignoresSafeArea()
    }
}

#Preview {
    ContentView()
}
