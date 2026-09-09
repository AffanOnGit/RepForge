# ⚡ RepForge

<p align="center">
  <img src="https://raw.githubusercontent.com/AffanOnGit/RepForge/main/docs/assets/banner.png" alt="RepForge Banner" width="100%" onerror="this.style.display='none'"/>
</p>

<p align="center">
  <strong>Hyper-focused, local-first, native Android strength training application.</strong><br/>
  Built with Modern Kotlin, Jetpack Compose (Material 3), Clean Architecture, and zero corporate bloat.
</p>

<p align="center">
  <a href="https://android.com"><img src="https://img.shields.io/badge/Platform-Android%2015%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android"/></a>
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-2.0.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin"/></a>
  <a href="https://developer.android.com/jetpack/compose"><img src="https://img.shields.io/badge/Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Compose"/></a>
  <a href="https://developer.android.com/health-and-fitness/guides/health-connect"><img src="https://img.shields.io/badge/Health%20Connect-Integrated-00875A?style=for-the-badge&logo=googlefit&logoColor=white" alt="Health Connect"/></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-FF6600?style=for-the-badge" alt="License"/></a>
  <a href="#-100-free--open-philosophy"><img src="https://img.shields.io/badge/Price-100%25%20Free-D4FF00?style=for-the-badge&logoColor=black&labelColor=0B0D10" alt="Free"/></a>
</p>

---

## 🛡️ 100% Free & Open Philosophy

RepForge is built for lifters who respect honest software:
- ❌ **No Subscriptions**: No monthly paywalls, premium tiers, or hidden charges.
- ❌ **No Ads & No Tracking**: Zero telemetry tracking, ad banners, or telemetry monetization.
- ❌ **No Locked Routines**: Unlimited routine slots and access to all 250+ canonical exercises.
- ✅ **100% Local-First & Offline**: Operates fully without an internet connection via a Room database.
- ✅ **Complete Data Sovereignty**: 1-tap export of your entire training history as CSV (Excel/Sheets) or raw JSON.

---

## 📸 Key Features & Innovations

### 1. 🩸 Sweaty-Hands UX Design
Engineered from the ground up for use in intense gym conditions:
- **Large Touch Targets**: Every button and clickable element is strictly $\ge 48\text{dp}$ (primary volume steppers are $52\text{dp}$).
- **Haptic Confirmation**: Crisp vibration pulses on set completion checkboxes provide tactile feedback through sweat and chalk.
- **High-Contrast Dark Palette**: Built on **Carbon Slate** (`#0B0D10`), **Forge Amber** (`#FF6600`), and **Kinetic Lime** (`#D4FF00`) for glare-proof legibility under bright gym fluorescent lights.
- **Tabular Monospace Figures**: Eliminates number jiggle during rapid rep counters and rest timer ticks.

### 2. 🧬 19 Sub-Muscle Gym Taxonomy
Moving beyond generic "Chest / Back / Legs" models into precise biomechanical sub-targets:
- **Chest**: Upper (Clavicular), Mid (Sternal), Lower (Costal)
- **Back**: Lats, Upper Back / Traps, Lower Back (Erector Spinae)
- **Shoulders**: Front Delts, Side Delts, Rear Delts
- **Arms**: Biceps (Long Head), Biceps (Short Head), Triceps (Long Head), Triceps (Lateral/Medial), Forearms
- **Legs**: Quads, Hamstrings, Glutes, Calves
- **Core**: Abs & Obliques
- Seeded with **250+ canonical strength movements** categorized across 7 equipment types (Barbell, Dumbbell, Cable, Machine, Bodyweight, Smith Machine, Bands).

### 3. ⏱️ Active Workout Engine & Auto-Rest
- **Live Session HUD**: Real-time elapsed duration and calorie burn meter with Session State FSM (Idle $\rightarrow$ Active $\rightarrow$ Paused $\rightarrow$ Finishing $\rightarrow$ Completed).
- **Ghost Text Overload**: Displays your exact weight and rep performance from the previous session directly inside the input fields.
- **Auto-Rest Timer**: Floating countdown bar triggers automatically on set completion with **-15s**, **+30s**, and **Skip** quick adjustments.
- **Live Mid-Workout Swap**: Substitute exercises on-the-fly without breaking routine template integrity.
- **Barbell Plate Math**: Visualizer calculating exact plate loads per side (Olympic 20kg, Women's 15kg, EZ Curl 10kg, Trap 25kg).

### 4. 🔥 Scientific Caloric Engine & Health Connect
- **Evidence-Based Caloric Model**: Combines **Mifflin-St Jeor** basal metabolic rate (calibrated by weight, height, age, and sex) with mechanical work from lifted tonnage ($\sum \text{weight} \times \text{reps} \times 9.81$) and EPOC modifiers by training experience.
- **Honest Uncertainty**: Acknowledges physiological variance by displaying an honest $\pm 15\%$ confidence interval explicitly labeled *"Rough Estimate"*.
- **Bidirectional Health Connect**:
  - Automatically writes `ExerciseSessionRecord` (Strength Training) and `TotalCaloriesBurnedRecord`.
  - Reads `WeightRecord` to keep metabolic baselines updated automatically.

### 5. 🔀 Post-Workout 3-Way Diff Resolver
Did you swap an exercise, add a drop-set, or alter your reps during today's session? The Diff Resolver eliminates template corruption:
1. **Update Base Routine**: Permanently apply changes to your saved template.
2. **Save as New Variation**: Preserve your original template and create a new variation (e.g. *"Push A - Dumbbell Focus"*).
3. **Log for Today Only**: Keep the base routine unchanged while recording the exact modified workout in your history.

### 6. 🤖 AI Ingestion Pipeline (YouTube $\rightarrow$ Routine)
- Ingest strength training programs from YouTube URLs.
- **Global Cache Flywheel**: Instant $O(1)$ routine generation for popular fitness creators (Jeff Nippard, Dr. Mike Renaissance Periodization, Athlean-X).
- **Human-In-The-Loop (HITL) Review**: Review matched exercises, adjust target rep ranges and sets, and inspect AI confidence scores before committing to local storage.

### 7. 🗺️ Interactive 2D Sub-Muscle Heatmap
- **Front and Rear 2D Vector Bodies**: Visual representation of all 19 sub-muscles.
- **Continuous Color Interpolation**: Muscle targets dynamically transition from dark slate to glowing **Kinetic Lime** (`#D4FF00`) based on working set volume accumulated over rolling 7-day, 14-day, or 30-day windows.
- **Recovery Status Modal**: Tap any muscle to inspect working set totals, primary exercises used, and recovery readiness.

### 8. 📊 Consistency Matrix, Logbook & Trophy Room
- **GitHub-Style Consistency Grid**: 10-week contribution matrix visualizing training frequency and volume density.
- **Detailed Workout Archive**: Expandable monthly logs showing every completed set, RPE values, and personal records.
- **PR Trophy Room**: Gold and amber trophies tracking all-time achievements across Weight PRs, Rep PRs, and Volume PRs.
- **Data Portability**: Instant CSV and JSON export via the Android Share Sheet.

---

## 🏛️ System Architecture

RepForge follows strict **Clean Architecture** principles across 15 decoupled Gradle modules:

```
RepForge/
├── app/                               # Application entrypoint, DI graphs, navigation
│   └── src/main/java/com/repforge/
│       ├── navigation/                # RepForgeNavHost (5-tab graph + sub-flows)
│       └── RepForgeApp.kt             # Application class & Timber setup
│
├── build-logic/                       # Gradle convention plugins
│   └── convention/                    # AndroidFeature, AndroidCompose, Hilt plugins
│
├── core/
│   ├── core-domain/                   # Pure Kotlin/JVM domain models & engines (WearOS KMP boundary)
│   │   ├── engine/CaloricEngine.kt    # Mifflin-St Jeor + EPOC physics engine
│   │   ├── model/                     # Exercise, Routine, WorkoutSession, UserProfile
│   │   └── repository/                # Abstract repository interfaces
│   │
│   ├── core-data/                     # Room database, DAO queries, seed dictionary, repo impls
│   │   ├── database/                  # RepForgeDatabase, 8 Entities, 4 DAOs
│   │   ├── seed/ExerciseSeedData.kt   # 250+ canonical exercises
│   │   └── repository/                # Repository implementations
│   │
│   ├── core-ui/                       # Material 3 dark design system & sweaty-hands components
│   │   ├── components/                # ForgeButton, SetRow, StepperCounter, PlateMathVisualizer
│   │   └── theme/                     # Carbon Slate, Forge Amber, Kinetic Lime
│   │
│   ├── core-network/                  # Firebase Auth & offline guest mode fallback
│   └── core-health/                   # Android Health Connect client & record syncing
│
└── feature/
    ├── feature-session/               # Active workout logger, rest timer, post-workout summary
    ├── feature-routines/              # Routine list, Rapid Wizard (<60s), exercise dictionary
    ├── feature-heatmap/               # Interactive 2D vector body map (Front & Rear)
    ├── feature-history/               # Consistency heatmap, monthly logbook, PR Trophy Room
    ├── feature-profile/               # Biometrics editor, unit toggle, barbell defaults
    ├── feature-onboarding/            # 3-step first-run flow & Health Connect rationale
    ├── feature-auth/                  # Email/password authentication & guest mode
    └── feature-ingestion/             # YouTube AI routine ingestion & HITL review sheet
```

---

## 🛠️ Tech Stack & Dependencies

| Technology | Purpose |
|---|---|
| **Kotlin 2.0+** | Modern, expressive language with type safety and coroutines |
| **Jetpack Compose (Material 3)** | Declarative, dynamic UI with hardware-accelerated rendering |
| **Room 2.6+** | 100% offline-first local SQLite abstraction with reactive Flows |
| **Dagger Hilt** | Standardized, compile-time dependency injection across modules |
| **Android Health Connect** | Bidirectional fitness data synchronization |
| **Kotlin Coroutines & Flow** | Asynchronous reactive streams and state management |
| **Firebase Auth & Crashlytics** | Cloud authentication with offline guest fallback and diagnostics |
| **Timber** | Extensible debug and production logging |

---

## 🚀 Building & Running Locally

### Prerequisites
- **JDK 17** or higher
- **Android Studio** (Ladybug / Koala / Iguana or later)
- **Android SDK** with `compileSdk = 35` and `minSdk = 26`

### Setup Instructions

1. **Clone the repository:**
   ```bash
   git clone https://github.com/AffanOnGit/RepForge.git
   cd RepForge
   ```

2. **Configure local properties:**
   Create a `local.properties` file in the project root pointing to your Android SDK:
   ```properties
   sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk # macOS/Linux
   # or on Windows:
   # sdk.dir=C:\\Users\\YOUR_USERNAME\\AppData\\Local\\Android\\Sdk
   ```

3. **Build the project:**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run unit tests:**
   ```bash
   ./gradlew testDebugUnitTest
   ```

---

## 📄 License

RepForge is released under the **[MIT License](LICENSE)**.

```
Copyright (c) 2026 Affan Hameed
```

You are free to use, modify, distribute, and build upon this project for both personal and commercial purposes.
