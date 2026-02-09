# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SplitForge is a local-first split planner for hybrid race training (running + station/strength exercises). It's a Kotlin Multiplatform (KMP) app running on Android and iOS using Compose Multiplatform.

## Build Commands

```sh
# Run all tests
./gradlew test

# Build Android debug APK
./gradlew :androidApp:assembleDebug

# Lint Android code
./gradlew :androidApp:lint

# Check Kotlin formatting
./gradlew ktlintCheck

# Auto-format Kotlin code
./gradlew ktlintFormat

# iOS simulator build (macOS only)
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' build
```

## Architecture

**Pattern:** MVVM with StateFlow for reactive state management.

**Layer Structure:**
- `shared/src/commonMain/kotlin/com/splitforge/app/domain/` - Business logic (Models, EstimationEngine, AutoPlanEngine)
- `shared/src/commonMain/kotlin/com/splitforge/app/data/` - Repositories and persistence (AppStateRepository, BillingRepository)
- `shared/src/commonMain/kotlin/com/splitforge/app/ui/` - Compose screens, ViewModels, theme, localization
- `shared/src/commonMain/kotlin/com/splitforge/app/util/` - Utilities (ShareService, TimeFormatter, PaceParser)

**Platform Abstraction:** Uses Kotlin's expect/actual pattern for platform-specific implementations:
- `androidMain/` - Android Context-based implementations
- `iosMain/` - iOS/Swift interop implementations

**Composition Root:**
```
PlatformAppDependenciesFactory (platform-specific)
  → AppDependencies (all services)
     ├── AppStateRepository (StateFlow + persistence)
     ├── BillingRepository (RevenueCat or Noop)
     ├── SummaryImageExporter
     ├── ShareService
     └── Strings (i18n)
```

**Key Files:**
- `AppViewModel.kt` - Central ViewModel orchestrating all state mutations
- `SplitForgeApp.kt` - Main Compose entry point with bottom navigation (Templates, Athletes, Planner, Settings)
- `Models.kt` - Core domain models (Segment, Template, Athlete, Plan, AppState)
- `EstimationEngine.kt` - Race time calculations
- `AutoPlanEngine.kt` - Optimal station assignment suggestions

## Key Technologies

- Kotlin 2.2.10, Compose Multiplatform 1.10.0
- Material Design 3
- Multiplatform Settings for local persistence
- RevenueCat for in-app purchases
- kotlinx.serialization for JSON
- Gradle 8.11.1 with version catalog (`gradle/libs.versions.toml`)

## Localization

Two languages supported (EN, FR) via `Strings` data class and `LanguageProvider`. Localized strings are in `shared/src/commonMain/kotlin/com/splitforge/app/ui/Strings.kt`.
