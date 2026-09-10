# ⚡ RepForge

<p align="center">
  <img src="docs/assets/banner.png" alt="RepForge Banner" width="100%"/>
</p>

<p align="center">
  <img src="docs/assets/logo.png" alt="RepForge App Icon" width="120" style="border-radius: 26px;"/>
</p>

<p align="center">
  <strong>Hyper-focused, local-first strength training application.</strong><br/>
  Primary client: <strong>Expo React Native</strong> in <code>mobile/</code> (Android-first).
</p>

<p align="center">
  <a href="https://expo.dev"><img src="https://img.shields.io/badge/Expo-SDK%2057-000020?style=for-the-badge&logo=expo&logoColor=white" alt="Expo"/></a>
  <a href="https://reactnative.dev"><img src="https://img.shields.io/badge/React%20Native-0.86-61DAFB?style=for-the-badge&logo=react&logoColor=black" alt="React Native"/></a>
  <a href="https://developer.android.com/health-and-fitness/guides/health-connect"><img src="https://img.shields.io/badge/Health%20Connect-Integrated-00875A?style=for-the-badge&logo=googlefit&logoColor=white" alt="Health Connect"/></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-FF6600?style=for-the-badge" alt="License"/></a>
  <a href="#-100-free--open-philosophy"><img src="https://img.shields.io/badge/Price-100%25%20Free-D4FF00?style=for-the-badge&logoColor=black&labelColor=0B0D10" alt="Free"/></a>
</p>

---

## 🛡️ 100% Free & Open Philosophy

RepForge is built for lifters who respect honest software:

- ❌ **No Subscriptions**: No monthly paywalls, premium tiers, or hidden charges.
- ❌ **No Ads & No Tracking**: Zero telemetry tracking or ad banners.
- ❌ **No Cloud Lock-In**: Local profiles, PIN / biometric unlock, and guest mode — no Firebase auth.
- ✅ **100% Local-First & Offline**: SQLite on device via `expo-sqlite`.
- ✅ **Complete Data Sovereignty**: Export training history as CSV or JSON.

---

## 📸 Key Features

### Sweaty-hands UX
Large touch targets, haptic set completion, high-contrast **Carbon Slate** / **Forge Amber** / **Kinetic Lime** palette, tabular monospace figures.

### Training engine
- Multi-profile local storage with onboarding + storage consent
- Routines, freestyle sessions, ghost overload, auto-rest (−15 / +30 / Skip), mid-workout exercise swap
- Plate math helper, caloric estimate (Mifflin–St Jeor + work + EPOC, ±15% band)
- Post-workout 3-way diff: update base / new variation / log today only

### Insights & portability
- Sub-muscle volume heatmap (7 / 14 / 30 day windows)
- History + PR trophy room
- CSV / JSON export (share sheet / SAF on Android)

### AI ingestion
YouTube URL or program text → HITL review → local routine. Optional Gemini (`EXPO_PUBLIC_GEMINI_API_KEY`); otherwise local simulator + curated cache.

### Health Connect (Android)
Bidirectional sync on a **dev client / prebuild** build (not Expo Go): write exercise + calories, read weight. See `mobile/src/features/health/SETUP.md`.

Parity notes vs the retired Kotlin client: [docs/KOTLIN_EXPO_PARITY.md](docs/KOTLIN_EXPO_PARITY.md).

---

## 🏛️ Project layout

```
RepForge/
├── mobile/                 # Expo React Native app (primary)
│   ├── app/                # Expo Router screens
│   ├── src/data/           # SQLite, seed, storage consent
│   ├── src/domain/         # Models, PlateMath, CaloricEngine
│   ├── src/stores/         # Auth + session Zustand stores
│   ├── src/features/       # AI, Health Connect, export
│   └── plugins/            # Health Connect manifest queries
├── docs/                   # Assets + parity notes
└── .github/workflows/      # Expo mobile CI
```

---

## 🛠️ Tech stack

| Technology | Purpose |
|---|---|
| **Expo SDK 57** | App tooling, native modules, EAS |
| **React Native / Expo Router** | UI + file-based navigation |
| **expo-sqlite** | Offline-first local database |
| **Zustand + SecureStore** | Session state + PIN hash |
| **expo-local-authentication** | Biometric unlock |
| **react-native-health-connect** | Android Health Connect |
| **TypeScript** | App-wide typing |

---

## 🚀 Building & running

```bash
cd mobile
npm install
npx expo start
```

- Expo Go: core flows (PIN, routines, session, export, AI simulator).
- Android Health Connect: `npx expo prebuild --platform android` then `npx expo run:android`, or an EAS development build (`@affanonexpo/repforge`).
- Optional Gemini: copy `mobile/.env.example` → `mobile/.env` and set `EXPO_PUBLIC_GEMINI_API_KEY`.

---

## 📄 License

RepForge is released under the **[MIT License](LICENSE)**.

```
Copyright (c) 2026 Affan Hameed
```

You are free to use, modify, distribute, and build upon this project for both personal and commercial purposes.
