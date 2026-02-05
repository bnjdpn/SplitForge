# SplitForge

SplitForge is a local-first split planner for hybrid race training. Build run + station templates, plan solo or doubles strategies, and see live time estimates. Pro unlock adds auto-split suggestions, export/share, and what-if deltas.

## Tech Stack
- Kotlin Multiplatform + Compose Multiplatform
- MVVM + StateFlow
- Persistence: Multiplatform Settings (single AppState JSON)
- Serialization: kotlinx.serialization
- IAP: RevenueCat purchases-kmp (one-time Pro entitlement)

## Versions (stable + compatible)
- Kotlin: 2.2.10
- Compose Multiplatform: 1.10.0
- Android Gradle Plugin: 9.0.0
- Gradle distribution: 8.11.1
- Coroutines: 1.10.1
- Serialization: 1.8.0
- Multiplatform Settings: 1.3.0
- RevenueCat purchases-kmp: 2.2.10+17.19.1

Reasoning: these versions are stable, widely compatible, and align with AGP 9 + Kotlin 2.2 + Compose MPP 1.10.

## Prerequisites
- JDK 17
- Android Studio (for Android)
- Xcode 15+ (for iOS)
- macOS for iOS builds
- `curl` or `wget` and `unzip` (the local Gradle wrapper script downloads Gradle)

## Bootstrap
```sh
./scripts/bootstrap.sh
```

## Run Android
From repo root:
```sh
./gradlew test
./gradlew :androidApp:assembleDebug
./gradlew :androidApp:lint
```

## Run iOS (Xcode)
1. Open `iosApp/iosApp.xcodeproj` in Xcode
2. Select scheme `iosApp`
3. Build for any iPhone simulator

## Run iOS (CLI)
From repo root:
```sh
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -destination 'platform=iOS Simulator,name=iPhone 15' build
```

## RevenueCat Configuration
The app runs without keys using a no-op billing implementation.

### Android
Add this to `local.properties` (not committed):
```
REVENUECAT_API_KEY=your_android_public_api_key
```

### iOS
Set the key in:
- `iosApp/Configs/Debug.xcconfig`
- `iosApp/Configs/Release.xcconfig`

```
REVENUECAT_API_KEY = your_ios_public_api_key
```

Entitlement id: `pro`
Offering id: `default`

## Lint/Format
```sh
./gradlew ktlintCheck
./gradlew ktlintFormat
```

## Release Checklist
1. Update `CHANGELOG.md`
2. Verify localization EN/FR
3. Set RevenueCat keys in CI/Release builds
4. Run Android release build and lint
5. Build iOS release in Xcode
6. Validate Pro purchase + restore
7. Export/share summary image on both platforms

## Privacy Policy
See:
- `docs/PRIVACY_POLICY_EN.md`
- `docs/PRIVACY_POLICY_FR.md`

## Notes
- No accounts, analytics, or ads.
- Brand safety: no trademarked event names used.
