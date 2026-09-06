import SwiftUI
import UIKit
import shared

/// Héberge l'écran de menu Compose Multiplatform (`HomeScreenViewController`, côté Kotlin) dans
/// la hiérarchie SwiftUI. Les libellés sont fournis en dur ici en attendant une vraie
/// localisation multiplateforme (voir shared/src/commonMain/.../HomeScreen.kt).
struct HomeScreenView: UIViewControllerRepresentable {
    let onNavigate: (String) -> Void
    let onStartTutorial: () -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        let versionName = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "0.0.1"
        let strings = HomeScreenStrings(
            title: "GLORIOUS 5",
            tagline: "CONSTRUISEZ VOTRE ÉQUIPE",
            vsComputerLabel: "JOUER CONTRE L'IA",
            vsComputerSublabel: "Affronte l'ordinateur",
            splitScreenLabel: "SPLIT SCREEN",
            splitScreenSublabel: "2 joueurs sur 1 écran",
            onlineLabel: "JOUER EN LIGNE",
            onlineSublabel: "Défie un ami à distance",
            tutorialLabel: "PRÉSENTATION",
            tutorialSublabel: "Apprendre les règles",
            optionsLabel: "OPTIONS",
            optionsSublabel: "Réglages et préférences",
            versionFooter: "v\(versionName) · Saison 2025–26"
        )
        return HomeScreenViewControllerKt.HomeScreenViewController(
            strings: strings,
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
