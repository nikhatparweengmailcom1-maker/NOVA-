# NOVA — Neural Omniscient Virtual Assistant

A fully-featured, production-ready **Kotlin + Jetpack Compose** AI assistant for Android.

---

## ✨ Features

| Category | Features |
|---|---|
| **AI** | OpenAI (GPT-4o-mini) & Gemini modular providers, 20-msg context window, system prompt |
| **Voice** | Wake phrase "Hi NOVA", live speech recognition, TTS with adjustable speed/pitch |
| **Reminders** | AlarmManager-backed exact/inexact alarms, timers, repeat modes |
| **To-Do** | Priority levels (Low/Medium/High/Critical), tags, due dates, undo delete |
| **System** | Flashlight, open apps by name, call/SMS contacts |
| **Camera** | QR/barcode scanner (ML Kit), OCR text extraction |
| **Calendar** | Read upcoming events, open create-event intent |
| **Security** | API keys stored in `EncryptedSharedPreferences` (never in code) |
| **Service** | Foreground service with Pause/Resume/Stop notification actions |
| **UI** | Dark holographic Compose UI, NOVA orb, voice waveform, animated bubbles |

---

## 🚀 Quick Start

### Prerequisites

- **Android Studio** Hedgehog (2023.1.1) or later
- **JDK 17**
- **Android SDK 34**, minimum SDK 26 (Android 8.0)
- An **OpenAI** or **Google Gemini** API key

### 1 — Clone / open the project

```bash
git clone <your-repo>
# Open the 'nova-android/' folder in Android Studio as a project
```

### 2 — Set your SDK path

Edit (or let Android Studio create) `nova-android/local.properties`:

```properties
sdk.dir=/Users/<you>/Library/Android/sdk   # macOS
# sdk.dir=C:\Users\<you>\AppData\Local\Android\Sdk  # Windows
```

### 3 — Sync Gradle

Android Studio will prompt you. Click **"Sync Now"**, or run:

```bash
./gradlew --refresh-dependencies
```

### 4 — Build & run

```bash
# Install debug APK on a connected device / emulator
./gradlew installDebug
```

Or use **Run ▶** in Android Studio with an emulator or USB-connected device.

---

## 🔑 Setting your API key

NOVA never stores API keys in source code. Enter your key at runtime:

1. Tap **⚙ Settings** (top-right gear icon)
2. Select your provider: **OpenAI** or **Gemini**
3. Paste your key in the text field and tap **Save API Key**

Keys are stored encrypted on-device via `EncryptedSharedPreferences`.

| Provider | Where to get a key |
|---|---|
| OpenAI | https://platform.openai.com/api-keys |
| Gemini | https://aistudio.google.com/apikey |

---

## 🔐 Permissions

NOVA requests the following permissions at runtime:

| Permission | Used for |
|---|---|
| `RECORD_AUDIO` | Voice input & wake phrase detection |
| `READ_CONTACTS` | "Call John" / "Text Sarah" voice commands |
| `CALL_PHONE` | Direct phone calls |
| `READ_CALENDAR` | Listing upcoming events |
| `WRITE_CALENDAR` | Creating calendar events |
| `CAMERA` | QR/barcode scanner, OCR |
| `RECEIVE_BOOT_COMPLETED` | Auto-restart NOVA service after reboot |
| `USE_EXACT_ALARM` | Exact alarm scheduling (Android 12+) |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Keep service alive |

All permissions are optional — NOVA gracefully degrades if denied.

---

## 🏗️ Architecture

```
nova-android/
└── app/src/main/kotlin/com/nova/assistant/
    ├── di/               # Hilt dependency injection modules
    ├── domain/
    │   ├── model/        # Pure Kotlin data/sealed classes
    │   ├── repository/   # Repository interfaces
    │   └── usecase/      # Single-responsibility use cases
    ├── data/
    │   ├── local/        # Room DB (entities, DAOs), DataStore prefs
    │   └── remote/ai/    # OpenAI & Gemini HTTP providers
    ├── features/         # Device features: flashlight, OCR, contacts, calendar, apps
    ├── service/          # Foreground service, TTS, voice recognition, wake word
    ├── presentation/
    │   ├── ui/
    │   │   ├── components/ # Reusable Compose components (orb, waveform, bubbles)
    │   │   ├── screens/    # ChatScreen, SettingsScreen, RemindersScreen, TodoScreen, QRScannerScreen
    │   │   └── theme/      # Color, Type, NovaTheme
    │   ├── viewmodel/    # HiltViewModel classes
    │   └── navigation/   # NavHost + route definitions
    ├── util/             # Constants, Extensions, DateTimeUtil, SecureStorage, Permissions
    ├── NovaApplication.kt
    └── MainActivity.kt
```

**Patterns:** MVVM · Clean Architecture · Hilt DI · Kotlin Coroutines + Flow · Room · DataStore

---

## 🧪 Tests

```bash
# Unit tests (JVM)
./gradlew test

# Instrumented tests (device required)
./gradlew connectedAndroidTest
```

Unit tests cover:
- `SendMessageUseCase` — success / error / voice paths
- `ProcessVoiceCommandUseCase` — 15+ voice intent classifications
- `DateTimeUtil` — duration parsing, natural-language date parsing

---

## 🔧 Build variants

| Variant | Description |
|---|---|
| `debug` | Logging enabled (Timber verbose), debuggable |
| `release` | ProGuard + R8 minification, logging stripped |

---

## 📦 Release APK

```bash
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release-unsigned.apk
```

Sign with `apksigner` or configure a `signingConfig` in `app/build.gradle.kts`.

---

## 🛠 Tech Stack

| Library | Version | Purpose |
|---|---|---|
| Kotlin | 1.9.22 | Language |
| Jetpack Compose BOM | 2024.02.00 | UI framework |
| Hilt | 2.50 | Dependency injection |
| Room | 2.6.1 | Local database |
| DataStore | 1.0.0 | Typed preferences |
| CameraX | 1.3.1 | Camera pipeline |
| ML Kit Text Recognition | 16.0.0 | OCR |
| ML Kit Barcode Scanning | 17.2.0 | QR / barcode |
| OkHttp | 4.12.0 | HTTP networking |
| Gson | 2.10.1 | JSON serialization |
| Timber | 5.0.1 | Logging |
| Coroutines | 1.7.3 | Async / Flow |
| EncryptedSharedPreferences | 1.1.0-alpha06 | Secure key storage |

---

## ⚠️ Known Limitations

- **Continuous wake word listening** is constrained by Android's background execution limits. NOVA uses burst-listening intervals inside a foreground service. For production-grade always-on detection, integrate a dedicated on-device model (e.g. Picovoice Porcupine).
- **Exact alarms** require `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` permission on Android 12+. NOVA falls back gracefully to inexact alarms if denied.
- UI tests require a real device or emulator with Google Play Services for ML Kit.

---

## 📄 License

MIT — use freely, attribution appreciated.
