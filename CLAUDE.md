# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Kotlin Multiplatform (KMP) project using Compose Multiplatform, targeting Android, iOS, JS, and WebAssembly (Wasm).

## Build Commands

```bash
# Android
./gradlew :composeApp:assembleDebug

# Web (Wasm - modern browsers)
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

# Web (JS - older browser support)
./gradlew :composeApp:jsBrowserDevelopmentRun

# Run tests
./gradlew :composeApp:allTests

# iOS - open iosApp/ in Xcode
```

## Architecture

- **composeApp/src/commonMain/** - Shared Kotlin code for all platforms (UI, business logic)
- **composeApp/src/androidMain/** - Android-specific implementations
- **composeApp/src/iosMain/** - iOS-specific implementations (exports `MainViewController`)
- **composeApp/src/jsMain/** - JS target implementations
- **composeApp/src/wasmJsMain/** - WebAssembly target implementations
- **composeApp/src/webMain/** - Shared web entry point
- **composeApp/src/commonTest/** - Cross-platform tests
- **iosApp/** - iOS app entry point (SwiftUI shell that hosts Compose)

## Platform Pattern

Uses `expect`/`actual` for platform-specific code:
- `Platform.kt` in commonMain defines the interface with `expect fun getPlatform(): Platform`
- Each platform folder has `Platform.*.kt` with the `actual` implementation

## Key Dependencies

- Compose Multiplatform 1.9.1
- Kotlin 2.2.20
- Material3 for UI components
- AndroidX Lifecycle for ViewModel and runtime composition

## Claude Code Subagents

Specialized agents for KMP development tasks (from [kmp-claude-code-subagents](https://github.com/ChrisKruegerDev/kmp-claude-code-subagents)).

### compose-architect

Use this agent when building Compose UI views, implementing modern Android 16 (SDK 36) with Material 3 features, refactoring large views into smaller components, or needing guidance on proper Compose UI architecture patterns.

**Core Principles:**
- MVVM architecture via ViewModels with `UiState` (MutableStateFlow) and `UiEvent` patterns
- Component decomposition - break large views into small, single-purpose components
- Koin for dependency injection with annotation-based configuration
- Compose Navigation with type-safe patterns (Route → Screen)
- Performance: use `remember`, `mutableStateOf`, `derivedStateOf`
- Side effects via `LaunchedEffect`

**Code Style:**
- Small, focused `@Composable` functions
- `@Preview` at end of file using `AppThemePreview`
- UI components from `shared/src/commonMain/kotlin/app/ui/core`
- No comments/documentation - code should be self-explanatory
- Direct mutable state (no underscore prefix): `val uiState = MutableStateFlow(UiState())`

### datalayer-architect

Use this agent when building data layer, implementing modern KMP features, or refactoring data layer components.

**Core Principles:**
- Repository pattern with DataSource layer below
- Kotlin Coroutines for async operations
- Koin for dependency injection (`@Single` components)
- Ktor for networking
- Room for local storage (models with `Entity` suffix)
- Separate data models from domain models

**Code Style:**
- Package: `src/commonMain/kotlin/app/data`
- `sealed interface` over `sealed class`
- `@Serializable` for data classes
- `AppLogger` for error logging
- No unnecessary abstractions

### translation-updater

Use this agent for managing string resource translations across multiple languages.

**Note:** This agent template is designed for Android string resources. Adapt the script paths and language configuration for your project.

**Key Guidelines:**
- Never translate placeholders (`%1$s`, `%1$d`, `%2$s`)
- Preserve HTML entities (`&amp;` stays as `&amp;`)
- Maintain line breaks (`\n`) and special characters
- Keep consistent structure across all language files
- Update English source first, then run translation updates
