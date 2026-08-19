# 💱 Currency Converter

[![Android API](https://img.shields.io/badge/API-26%2B-brightgreen.svg)](https://android-arsenal.com/api?level=26)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Compose-Material3-purple.svg)](https://developer.android.com/jetpack/compose)

A modern, offline-first currency converter for Android. Built with the latest Jetpack libraries, Material 3 design, and a focus on reliability and customizability.

## ✨ Features

- **Multi-Source Data**: Integrated with multiple exchange rate providers:
  - [CoinGecko](https://www.coingecko.com)
  - [Frankfurter](https://frankfurter.dev)
  - [ExchangeRate-API](https://www.exchangerate-api.com)
  - [Open Exchange Rates](https://openexchangerates.org)
  - [CurrencyAPI](https://currencyapi.net)
- **Offline First**: All data is cached locally using **Room**, ensuring you can convert currencies even without an internet connection.
- **Background Sync**: Uses **WorkManager** to periodically update rates in the background based on your preferences.
- **Real-time Conversion**: Responsive UI that updates all currency rows instantly as you type.
- **Material 3 Design**: A beautiful, modern interface with support for **Dark Mode** and **Dynamic Colors**.
- **Privacy Focused**: No tracking. All settings and data stay on your device, with optional Android Auto-Backup support.
- **Customizable Experience**: Choose your data providers and set custom sync intervals.

## 📥 Download

You can download the latest APK from the [Releases](https://github.com/felixbrucker/currency-converter-app/releases) page.

### 🔄 Auto-updates with Obtainium

To get automatic updates directly from GitHub, we recommend using [Obtainium](https://obtainium.imranr.dev).

1. Install Obtainium on your Android device.
2. Click **Add App** in Obtainium.
3. Paste the repository URL: `https://github.com/felixbrucker/currency-converter-app`
4. Click **Add** to track and install updates.

## 🛠 Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Design System**: [Material 3](https://m3.material.io/)
- **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern
- **Local Database**: [Room](https://developer.android.com/training/data-storage/room)
- **Networking**: [Retrofit](https://square.github.io/retrofit/) & [OkHttp](https://square.github.io/okhttp/)
- **Serialization**: [Moshi](https://github.com/square/moshi)
- **Concurrency**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html)
- **Background Tasks**: [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- **Dependency Injection**: Manual injection / Repository Singleton
- **Image Loading**: [Coil](https://coil-kt.github.io/coil/)

## 📂 Project Structure

```text
app/src/main/java/com/felixbrucker/currencyconverter/
├── data/
│   ├── local/          # Room database, DAOs, and Entities
│   ├── remote/         # Retrofit services and API provider implementations
│   ├── repository/     # Single source of truth for data
│   └── worker/         # WorkManager sync logic
├── model/              # Domain and UI state models
├── ui/
│   ├── components/     # Reusable Compose components
│   ├── theme/          # Material 3 colors, typography, and theme
│   └── ...             # Screens and ViewModels
├── util/               # Formatters, connectivity observers, and helpers
└── extensions/         # Useful Kotlin extension functions
```

## 🚀 Getting Started

### Prerequisites

- Android Studio Koala (2024.1.1) or newer.
- Android SDK 34+.
- An API key for some providers (optional, Frankfurter, ExchangeRate-API, and CoinGecko work out-of-the-box).

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/felixbrucker/currency-converter-app.git
   ```
2. Open the project in Android Studio.
3. Sync Gradle and build the project.
4. (Optional) Add your API keys in the Settings screen within the app to enable more providers.

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
