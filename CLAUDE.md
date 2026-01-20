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

## About This Repository
This repository also contains resources for a backend written in the unison language. All Unison services and resources will as well as the scratch.u file will be listed under the ./unison subdirectory

## Unison Language Reference

The comprehensive language guide is at `knowledge-base/unison-language-guide.md`. Key points:

### Syntax Rules (Critical)
- No pattern matching to the left of `=` - always use `match ... with` or `cases`
- No `let` keyword - just write `name = expression`
- No `where` clauses - define helpers before using them
- No string interpolation - use `Text.++` for concatenation
- Use `None`/`Some` (not `Nothing`/`Just` like Haskell)

### Lists
- Lists are finger trees with O(1) append: use `:+` to append, `+:` to prepend
- Build lists in order using `:+`, never build in reverse and call `List.reverse`
- Always use tail recursion with accumulating parameters for list operations

### Abilities (Effects)
- Effects are declared in type signatures: `'{IO, Exception} ()` or `{g, Remote}`
- Make higher-order functions ability-polymorphic: `(a ->{g} b)` not `(a -> b)`
- Handlers use `handle ... with cases` pattern

### Style
- Use `cases` instead of `match x with` when pattern matching on last argument
- Name recursive helpers `go` or `loop`
- Use short names: `acc`, `rem`, `f`, `g`
- Tests must be named `<function-name>.tests.<test-name>`

## Service Architecture Pattern

A typical service follows this structure:

```unison
-- 1. Define types
type EntityId = Id Text
unique type Entity = { field1 : Text, field2 : Nat }

-- 2. Define storage ability
ability EntityStorage where
  get : EntityId ->{EntityStorage} Optional Entity
  upsert : Entity ->{EntityStorage} ()
  getAll : '{EntityStorage} [Entity]

-- 3. Implement handler with OrderedTable
EntityStorage.run : Database -> '{g, EntityStorage} a ->{g, Remote} a
EntityStorage.run db p =
  table = OrderedTable.named db "entities" Universal.ordering
  -- handler implementation with cases

-- 4. Define routes
entity.routes : '{Route, Exception, EntityStorage, Log} ()

-- 5. Main entry point
main.main : Database -> HttpRequest -> {Exception, Storage, Remote, Log} HttpResponse
main.main db req = EntityStorage.run db do Route.run entity.routes req
```

## Deployment

Services can be deployed to local, staging, or production environments:
- `deploy.deployLocal` - local development with `main.local.serve`
- `deploy.deployStage` - staging with named environment
- `deploy.deployProd` - production with `Environment.default()`
