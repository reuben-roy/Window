# Window

> An Android app that watches your screen — tracking app usage, scraping visible UI text, and running on-device Gemini Nano AI to summarize your digital activity.

## What It Does

Window sits in the background via an Android Accessibility Service and builds a local, privacy-first log of how you use your phone:

- **App Usage Sessions** — Detects when you switch apps and records foreground duration per app.
- **UI Content Scraping** — Traverses the accessibility node tree to capture visible text, content descriptions, and button labels on every meaningful event (clicks, focus changes, content changes).
- **On-Device AI Summaries** — Periodically runs Gemini Nano (via Google AI Edge LocalAI SDK) against recent UI text batches to generate human-readable activity summaries — entirely offline.
- **Dashboard** — Review daily app usage totals, session history, and AI-generated summaries in a Jetpack Compose UI.
- **Settings** — Toggle service behavior, configure inference intervals, and manage data retention.
- **Data Pruning** — Background WorkManager job to prune old events and keep the database lean.

## Tech Stack

| Layer | Technology |
|-------|------------|
| Platform | Android (API 24+) |
| Language | Kotlin |
| UI | [Jetpack Compose](https://developer.android.com/jetpack/compose) |
| Architecture | MVVM + Repository pattern |
| DI | [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) |
| Local DB | [Room](https://developer.android.com/training/data-storage/room) (SQLite) |
| Background | [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) |
| AI | Google AI Edge LocalAI SDK (Gemini Nano on-device) |
| Navigation | [Compose Navigation](https://developer.android.com/jetpack/compose/navigation) |

## Quick Start

### Prerequisites

- Android Studio Ladybug or newer
- JDK 17
- An Android device or emulator (API 26+ recommended)
- For AI features: a Pixel 8+ or Android 14+ device with AICore

### Build & Run

```bash
# Clone and open in Android Studio
# Or build from CLI:
./gradlew assembleDebug

# Install to connected device
./gradlew installDebug
```

### Enable the Accessibility Service

After install:
1. Open **Settings → Accessibility**
2. Find **Window** and enable it.
3. Grant the required permission.

The service starts automatically and shows a persistent notification (required for foreground operation on Android 8+).

## Project Structure

```
app/src/main/kotlin/com/window/app/
  MainActivity.kt                    # Single-Activity Compose host
  WindowApplication.kt               # Application class + Hilt entry point
  service/
    WindowAccessibilityService.kt    # Core monitoring engine (A11y service)
  data/
    db/
      WindowDatabase.kt              # Room database (version 1)
      AppUsageSession.kt             # Session entity + DAO
      WindowContentEvent.kt          # Scraped UI text event entity + DAO
    ai/
      GeminiRepository.kt            # On-device Gemini Nano wrapper
  di/
    DatabaseModule.kt                # Hilt provider for Room DB
  ui/
    dashboard/
      DashboardScreen.kt             # Daily usage + summaries UI
      DashboardViewModel.kt          # State holder + data aggregation
    settings/
      SettingsScreen.kt              # Service toggles + config
      SettingsViewModel.kt
    navigation/
      WindowNavHost.kt               # Compose Navigation graph
      Screen.kt                      # Route definitions
    theme/
      WindowTheme.kt                 # Material 3 theme
  util/
    DateUtil.kt                      # ISO date helpers
  worker/
    PruneEventsWorker.kt             # Periodic old-event cleanup
```

## How It Works

### Accessibility Service Lifecycle

1. `onServiceConnected()` — Configures event filters, starts foreground notification, warms up Gemini Nano.
2. `onAccessibilityEvent()` — Receives system events:
   - `TYPE_WINDOW_STATE_CHANGED` → App switch detected → close old session, open new session in Room.
   - `TYPE_WINDOW_CONTENT_CHANGED` / `TYPE_VIEW_CLICKED` / `TYPE_VIEW_FOCUSED` → Scrape root node tree → insert `WindowContentEvent`.
3. `onUnbind()` — Closes the active session and cancels the coroutine scope.

### UI Scraping

`scrapeNodeTree()` recursively traverses `AccessibilityNodeInfo` children, collecting:
- `node.text`
- `node.contentDescription`
- `node.hintText`

Bounded by `MAX_NODES = 200` to prevent runaway CPU on dense UIs.

### AI Inference

Every 30 seconds (debounced), the service:
1. Pulls the last 50 `WindowContentEvent`s for the current package.
2. Calls `GeminiRepository.summarizeActivity()` with the node texts.
3. Writes the resulting summary back to the most recent event row.

> **Note:** The Gemini Nano SDK integration is currently stubbed (library commented out) because the correct Google AI Edge artifact version is not yet available in public repositories. The repository structure and wiring are ready; re-enable the imports when the library stabilizes.

### Database Schema

| Table | Purpose |
|-------|---------|
| `app_usage_sessions` | Foreground sessions per app (start, end, duration, date) |
| `window_content_events` | Scraped UI text snapshots with timestamps, interaction types, and optional AI summaries |

### Data Pruning

`PruneEventsWorker` (WorkManager) runs periodically to delete events older than a configurable retention window, preventing unbounded database growth.

## Configuration

### `gradle.properties`

No API keys are required — the app is fully offline. The only external dependency is the Gemini Nano model, which is provisioned by AICore on supported devices.

### Accessibility Service Config

`res/xml/accessibility_service_config.xml` declares the service capabilities:
- `accessibilityEventTypes` — window changes, content changes, clicks, focus
- `canRetrieveWindowContent` — required for node tree scraping
- `notificationTimeout` — 100ms

## Architecture

```
Accessibility Service
    ↓
Room Database (SQLite)
    ↓
Repository / DAO
    ↓
ViewModel (StateFlow)
    ↓
Compose UI (Dashboard / Settings)
```

## License

No explicit LICENSE file. Assume all rights reserved unless otherwise stated.

---

Built with curiosity by [Reuben Roy](https://github.com/reuben-roy).
