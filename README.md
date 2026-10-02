# AutoCheck

Android-App für Fahrzeugübersicht und Wartungsverwaltung.

## Enthalten

- Kotlin + Jetpack Compose
- dunkle AutoCheck-Oberfläche
- zentrale Startgrafik (`bild_3`)
- sechs Hauptbereiche
- 2,5-Sekunden-Slide-in-Animation der Startbuttons
- „Mein Auto“ mit bis zu 5 Fahrzeugen
- lokale Speicherung der Fahrzeugdaten
- GitHub Actions Workflow zum Erzeugen eines Debug-APK

## Bilder

Die sechs im Chat bereitgestellten Bilder sind als `bild_1` bis `bild_6` im Ordner
`app/src/main/res/drawable-nodpi/` enthalten. `bild_3` wird aktuell als festes
Hauptbild auf der Startseite verwendet.

## Öffnen

Projekt in Android Studio öffnen und Gradle synchronisieren. Für den Build werden
Android SDK 37, JDK 17 und Gradle 9.6 verwendet.
