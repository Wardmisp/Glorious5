import SwiftUI
import UIKit
import shared

/// Héberge l'écran de menu Compose Multiplatform (`HomeScreenViewController`, côté Kotlin) dans
/// la hiérarchie SwiftUI. Les textes viennent des ressources Compose Multiplatform partagées
/// (shared/src/commonMain/composeResources) ; seul le numéro de version est fourni par la
/// plateforme.
struct HomeScreenView: UIViewControllerRepresentable {
    let onNavigate: (String) -> Void
    let onStartTutorial: () -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        let versionName = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "0.0.1"
        return HomeScreenViewControllerKt.HomeScreenViewController(
            versionName: versionName,
            onNavigate: onNavigate,
            onStartTutorial: onStartTutorial
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        HomeScreenView(
            onNavigate: { route in print("navigate: \(route)") },
            onStartTutorial: { print("start tutorial") }
        )
        .ignoresSafeArea()
    }
}

#Preview {
    ContentView()
}
