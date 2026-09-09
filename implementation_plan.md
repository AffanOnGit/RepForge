# RepForge: Critical PRD Review & Comprehensive Build Plan

## Part 1 — Honest Critical Review of the PRD

I've read [REPFORGE_PRD_SPECIFICATION.md](file:///d:/GitHub/Personal%20Repos/RepForge/REPFORGE_PRD_SPECIFICATION.md) end-to-end. The vision is strong and the gym-UX thinking is genuinely sharp—but there are real gaps, some questionable design decisions, and several areas where the spec is either silent or misleading. Here's what I'd push back on:

---

### 🟢 What the PRD Gets Right

| Strength | Why it matters |
|---|---|
| **Offline-first as a first-class constraint** | Gym basements and parking garages have zero signal. This is the right call. |
| **Sweaty-hands UX philosophy** | 48dp touch targets, haptic-only timers, no audio clutter—this shows real empathy for the user context. |
| **Global Video Cache / HITL data flywheel** | Amortizing Gemini API cost across all users is clever economics. The human-in-the-loop verification adds real data quality. |
| **Sub-muscle taxonomy** | Gym-friendly naming (not Latin) is correct. It matches how lifters actually talk and think. |
| **Diff Resolver concept** | Detecting on-the-fly workout changes and prompting the user to update/fork/discard is a genuinely novel UX pattern. |
| **Health Connect integration** | Bidirectional read/write is the right scope. Not trying to replace HC, but using it as the system health hub. |

---

### 🔴 Critical Issues & Gaps

#### 1. No Authentication or User Identity Model — **Blocker**

> [!CAUTION]
> The PRD mentions per-user weekly quotas, a "global verified DB," and gamified credit earn loops—but there is **zero specification for user accounts, authentication, or identity**. You cannot enforce per-user quotas without knowing who the user is. You cannot contribute to a global cloud cache without some form of identity. This is not a "nice to have"—it's a structural dependency for half the features in the document.

**Recommendation:** Add Firebase Auth with email/password authentication. Standard sign-up/login flow to unlock cloud sync, quota tracking, and global cache contributions.

---

#### 2. "Global Verified DB" Backend Architecture — **Completely Unspecified**

The PRD repeatedly references a "Global Video Cache" and "Cloud Backup DB" but provides **no backend architecture at all**. Questions that have no answer in the document:

- Where is the global cache hosted? (Firebase? Custom server? Supabase?)
- What's the sync protocol between local Room DB and the cloud?
- How is conflict resolution handled when two users verify the same video differently?
- What are the data retention and privacy policies?
- How do you handle GDPR/CCPA if you're storing user workout data in the cloud?

**Recommendation:** Use Firebase (Firestore + Cloud Functions) as the backend. Firestore's offline SDK complements Room for the global cache layer. Cloud Functions handles Gemini API calls server-side to protect the API key from client exposure.

---

#### 3. YouTube API/ToS Compliance — **Legal Risk**

> [!WARNING]
> Extracting YouTube transcripts and feeding them to Gemini has **Terms of Service implications**. The YouTube Data API v3 has specific policies about data usage, and scraping captions programmatically outside the official API may violate YouTube ToS. The PRD doesn't mention YouTube Data API v3 at all.

**Recommendation:** Use the official YouTube Data API v3 for metadata/captions. For videos without captions, use Gemini's native multimodal video understanding (pass the video URL directly to Gemini rather than scraping transcripts). Document the ToS compliance strategy.

---

#### 4. Caloric Engine — **Scientifically Overclaimed**

The ±5% confidence interval (Section 7.1) is **misleadingly precise**. Published research shows that resistance training calorie estimation has error margins of **±25-40%** even with heart rate monitors. The PRD's formula combining BMR, MET, tonnage, and EPOC stacks three independently imprecise models.

**My pushback:** Presenting `380 ± 20 kcal` implies a precision level that doesn't exist. This risks user trust when they compare against a Garmin watch showing a wildly different number.

**Recommendation:**
- Widen the confidence band to ±15-20% minimum, not ±5%
- Label it explicitly as "rough estimate" in the UI
- When heart rate data IS available from Health Connect, weight the HR-based model more heavily
- Consider showing relative comparisons ("This session was ~15% more intense than your average") rather than absolute kcal numbers—relative trends are more actionable and more defensible

---

#### 5. No Superset/Circuit/AMRAP Support — **Feature Gap**

The data model only supports linear exercise sequences (Exercise A → B → C). There is no concept of:

- **Supersets** (A1+A2 done back-to-back)
- **Tri-sets / Giant sets**
- **Circuits** (3-5 exercises cycled with minimal rest)
- **AMRAP** (As Many Reps As Possible in a time window)
- **EMOM** (Every Minute On the Minute)
- **Tempo prescriptions** (e.g., 3-1-2-0 eccentric-pause-concentric-rest)

These are **fundamental** to how many of the curated creators (RP, Athlean-X) actually program their workouts. If you're ingesting Jeff Nippard's "Push Pull Legs" series, many of those workouts use supersets.

**Recommendation:** Add an `exercise_group` concept:
```
ExerciseGroup {
  id: UUID
  group_type: SINGLE | SUPERSET | TRISET | CIRCUIT | AMRAP | EMOM
  exercises: List<Exercise>  // ordered within the group
  rest_after_group_seconds: Int
  time_cap_seconds: Int?  // for AMRAP/EMOM
}
```

---

#### 6. Mixed Unit System Is Confusing

> [!IMPORTANT]
> Weight in **kilograms only** but height in **feet and inches only**? This is a bizarre inconsistency. American users who measure height in feet/inches will overwhelmingly expect weight in **pounds**. International users who use kilograms will expect height in **centimeters**.

**Recommendation:** Make unit preferences fully configurable:
- Weight: kg or lb (with automatic conversion and display)
- Height: cm or ft/in
- Plate math: metric plates (1.25-25kg) or imperial plates (2.5-45lb)
- Default based on device locale, user-overridable

---

#### 7. Exercise Dictionary Cold-Start Problem

The PRD defines a canonical exercise dictionary schema but never explains where the initial seed data comes from. On day one:
- How many exercises are pre-loaded?
- Who curates them?
- How are sub-muscle mappings validated?
- What happens when Gemini returns an exercise name that doesn't match any dictionary entry?

**Recommendation:** Ship with a curated seed dictionary of ~200-300 exercises covering all sub-muscles and equipment types. Use fuzzy matching (Levenshtein distance or embedding similarity) when Gemini returns names that don't exactly match. Allow the HITL flow to create new dictionary entries.

---

#### 8. No Accessibility Considerations

For an app used "under physical duress" (post-set, shaking hands, sweaty), accessibility is **not optional**:
- No mention of TalkBack support
- No dynamic text sizing
- No mention of color blindness (the 🟢🟡🔴 progression indicators are red-green, which is the most common color deficiency)
- No contrast ratios specified beyond "high-contrast dark theme"

**Recommendation:** WCAG 2.1 AA minimum. Use icon+text+shape for progression indicators (not color alone). Support dynamic type. Test with TalkBack.

---

#### 9. No Data Export or Portability

Users have no way to export their workout history. This is:
- A retention risk (users feel "locked in" and may resent it)
- A GDPR Article 20 compliance issue (right to data portability) if you ever serve EU users
- A missed feature—CSV/JSON export is trivial to implement and builds trust

**Recommendation:** Add CSV and JSON export for workout history. Consider supporting the common `.fit` format for interop with other fitness platforms.

---

#### 10. No Monetization Model

The PRD defines quotas (3-5 free imports/week) which implies a freemium model, but there's no:
- Premium tier definition
- Pricing strategy
- In-app purchase or subscription architecture
- Google Play Billing integration spec

This isn't just a business gap—it affects architecture. Subscription state needs to be synced, validated server-side, and cached locally for offline access.

**Recommendation:** Define the monetization model before building. Even if the MVP is free, the architecture should accommodate Play Billing Library v7 from day one. Don't retrofit it later.

---

#### 11. No Error States or Edge Cases for AI Ingestion

What happens when:
- Gemini returns garbage or incomplete JSON?
- The YouTube video isn't actually a workout (it's a cooking video)?
- The video is in Spanish/Hindi/Arabic?
- The video has no captions and Gemini can't extract exercises from visuals alone?
- The user has no internet but tries to import?
- The global cache returns a workout that was incorrectly verified by a previous user?

**Recommendation:** Define an explicit error taxonomy for the ingestion pipeline with user-facing error messages and recovery flows for each case.

---

#### 12. "Recovery Status" on Heatmap Is Undefined

Section 9 mentions "recovery status per sub-muscle" on the heatmap but there is **no algorithm, model, or data source** for computing recovery. Recovery depends on volume, intensity, training age, sleep, nutrition, stress—none of which RepForge tracks beyond volume.

**Recommendation:** Either:
- Remove "recovery status" from MVP scope (it's misleading without data)
- Replace with a simpler "time since last trained" indicator (verifiable from workout history)
- Or implement a basic volume-based recovery model (e.g., 48-72h after high-volume sessions for that sub-muscle) and label it clearly as an estimate

---

#### 13. No Testing or QA Strategy

The PRD is a product spec, but for a build plan it needs a testing strategy. Writing to Health Connect is an especially sensitive operation—you cannot ship bugs that corrupt a user's health data.

---

### 🟡 Minor Concerns

| Concern | Impact |
|---|---|
| **API key exposure:** The PRD says "Google AI Studio Developer API"—calling Gemini directly from the client exposes the API key in the APK. | Medium. Move API calls server-side. |
| **Barbell plate math assumes 20kg bar only.** Many gyms have EZ curl bars (10kg), trap bars (25kg), Smith machines (varies). Women's bars (15kg). | Low-Medium. Add bar weight as a configurable parameter. |
| **No workout duration estimate accounting for rest.** The "under 60 seconds" template builder shows estimated duration but the formula isn't specified. | Low. |
| **No notification strategy.** Workout reminders? Streak maintenance nudges? | Low. Deferrable to post-MVP. |
| **5-tab navigation may be too many for MVP.** Heatmap & Analytics could merge with History. | Low. UX preference. |

---

---

## Part 2 — Comprehensive Build Plan

> [!IMPORTANT]
> **Skills used for this plan:** `mobile-developer`, `architecture-patterns`, `backend-architect`, `ui-ux-designer`, `api-design-principles`, `error-handling-patterns`, `performance-engineer`

### Technology Stack Decision

| Layer | Technology | Rationale |
|---|---|---|
| **Language** | Kotlin | Native Android requirement per PRD. Modern, null-safe, coroutine-native. |
| **UI Framework** | Jetpack Compose + Material 3 | Modern declarative UI. Faster iteration than XML layouts. Native dark theme support. |
| **Architecture** | MVVM + Clean Architecture | Testable, maintainable, scales well with feature modules. |
| **Local Database** | Room (SQLite) | Per PRD spec. Excellent Kotlin coroutine/Flow integration. |
| **DI** | Hilt (Dagger) | Google-recommended. Compile-time safety. First-class Compose support. |
| **Networking** | Retrofit + OkHttp + Kotlin Serialization | Industry standard. Interceptors for auth, caching, retry. |
| **Backend** | Firebase (Auth + Firestore + Cloud Functions) | Offline SDK, scalable, minimal ops burden for a solo/small team. |
| **AI API** | Google Gemini via Cloud Functions (server-side) | Protects API key. Allows server-side caching and validation. |
| **Health** | `androidx.health.connect:connect-client` | Per PRD. Native Health Connect integration. |
| **Navigation** | Jetpack Navigation Compose | Type-safe navigation for 5-tab architecture. |
| **Async** | Kotlin Coroutines + Flow | Structured concurrency. First-class Room and Compose integration. |
| **Image Loading** | Coil | Kotlin-first, Compose-native, lightweight. |
| **Testing** | JUnit 5 + MockK + Turbine + Compose UI Testing | Modern Kotlin-native testing stack. |
| **CI/CD** | GitHub Actions + Fastlane | Automated builds, signing, Play Store deployment. |
| **Analytics/Crash** | Firebase Crashlytics + Analytics | Free, integrated with the Firebase stack. |

---

### Project Structure (Feature-Module Clean Architecture)

```
repforge/
├── app/                           # Application module (entry point, DI, navigation)
│   ├── src/main/
│   │   ├── RepForgeApp.kt
│   │   ├── MainActivity.kt
│   │   ├── navigation/
│   │   │   ├── RepForgeNavHost.kt
│   │   │   └── BottomNavBar.kt
│   │   └── di/
│   │       └── AppModule.kt
│   └── build.gradle.kts
│
├── core/                          # Shared core modules
│   ├── core-data/                 # Room DB, DAOs, Repositories, data models
│   │   ├── database/
│   │   │   ├── RepForgeDatabase.kt
│   │   │   ├── entities/          # Room entities
│   │   │   ├── dao/               # Room DAOs
│   │   │   └── converters/        # Type converters
│   │   ├── repository/            # Repository implementations
│   │   ├── sync/                  # Cloud sync engine
│   │   └── model/                 # Domain models shared across features
│   │
│   ├── core-domain/               # Pure domain layer (no Android deps) — FUTURE KMP BOUNDARY for WearOS
│   │   ├── model/                 # Domain entities (pure Kotlin, no Android imports)
│   │   ├── repository/            # Repository interfaces
│   │   └── usecase/               # Shared use cases
│   │
│   ├── core-ui/                   # Shared Compose components, theme, design tokens
│   │   ├── theme/
│   │   │   ├── RepForgeTheme.kt
│   │   │   ├── Color.kt
│   │   │   ├── Typography.kt
│   │   │   └── Shape.kt
│   │   ├── components/            # Reusable composables
│   │   └── util/                  # UI utilities
│   │
│   ├── core-network/              # Retrofit clients, Firebase wrappers
│   │
│   └── core-health/               # Health Connect abstraction layer
│       ├── HealthConnectManager.kt
│       └── mappers/
│
├── feature/                       # Feature modules (one per tab/flow)
│   ├── feature-session/           # Tab 1: Today / Active Session
│   │   ├── domain/
│   │   │   └── usecase/
│   │   ├── data/
│   │   │   └── repository/
│   │   └── ui/
│   │       ├── ActiveSessionScreen.kt
│   │       ├── ActiveSessionViewModel.kt
│   │       ├── SetRow.kt
│   │       ├── RestTimerBar.kt
│   │       ├── PlateMathSheet.kt
│   │       └── ExerciseSwapSheet.kt
│   │
│   ├── feature-routines/          # Tab 2: Routines & Discovery
│   │   ├── domain/
│   │   ├── data/
│   │   └── ui/
│   │       ├── RoutineListScreen.kt
│   │       ├── RoutineDetailScreen.kt
│   │       ├── RapidTemplateWizard.kt
│   │       ├── YouTubeHubScreen.kt
│   │       └── AIIngestionScreen.kt
│   │
│   ├── feature-heatmap/           # Tab 3: Heatmap & Analytics
│   │   ├── domain/
│   │   ├── data/
│   │   └── ui/
│   │       ├── HeatmapScreen.kt
│   │       ├── AnatomyMapComposable.kt  # 2D vector body renderer
│   │       └── ProgressionRadar.kt
│   │
│   ├── feature-history/           # Tab 4: History & Logbook
│   │   ├── domain/
│   │   ├── data/
│   │   └── ui/
│   │       ├── HistoryScreen.kt
│   │       ├── WorkoutDetailScreen.kt
│   │       ├── CalendarHeatmap.kt
│   │       └── PRTrophyRoom.kt
│   │
│   ├── feature-profile/           # Tab 5: Profile & Biometrics
│   │   ├── domain/
│   │   ├── data/
│   │   └── ui/
│   │       ├── ProfileScreen.kt
│   │       ├── BiometricsEditor.kt
│   │       ├── WeightChart.kt
│   │       └── HealthConnectSyncCard.kt
│   │
│   ├── feature-onboarding/        # First-run experience
│   │   └── ui/
│   │       ├── OnboardingScreen.kt
│   │       └── BiometricSetupScreen.kt
│   │
│   ├── feature-auth/              # Authentication (Firebase email/password)
│   │   ├── domain/
│   │   │   └── usecase/
│   │   │       ├── SignUpUseCase.kt
│   │   │       ├── LoginUseCase.kt
│   │   │       └── ResetPasswordUseCase.kt
│   │   ├── data/
│   │   │   └── AuthRepository.kt
│   │   └── ui/
│   │       ├── LoginScreen.kt
│   │       ├── SignUpScreen.kt
│   │       └── AuthViewModel.kt
│   │
│   └── feature-ingestion/         # AI Ingestion pipeline (shared across features)
│       ├── domain/
│       │   ├── IngestionUseCase.kt
│       │   └── HITLVerificationUseCase.kt
│       ├── data/
│       │   ├── GeminiApiService.kt    # Calls Cloud Function, not Gemini directly
│       │   └── YouTubeMetadataService.kt
│       └── ui/
│           ├── HITLVerificationModal.kt
│           └── IngestionProgressSheet.kt
│
├── firebase/                      # Cloud Functions (Node.js/TypeScript)
│   ├── functions/
│   │   ├── src/
│   │   │   ├── geminiIngestion.ts     # Gemini API call + schema enforcement
│   │   │   ├── videoCache.ts          # Global video dedup cache
│   │   │   ├── quotaManager.ts        # Per-user import quota tracking
│   │   │   └── index.ts
│   │   ├── package.json
│   │   └── tsconfig.json
│   ├── firestore.rules
│   └── firebase.json
│
└── build-logic/                   # Gradle convention plugins
    └── convention/
        ├── AndroidLibraryConvention.kt
        ├── ComposeConvention.kt
        └── HiltConvention.kt
```

---

### Phased Build Plan (6 Sprints, 2 weeks each)

---

#### Sprint 1 — Foundation & Core Data Layer (Weeks 1-2)

> **Goal:** Buildable project skeleton, database schema, design system, and onboarding flow.

**Tasks:**

- [ ] **Project Scaffolding**
  - Initialize multi-module Gradle project with Kotlin DSL
  - Configure convention plugins for consistent module setup
  - Set up Hilt dependency injection across all modules
  - Configure GitHub Actions CI (build + lint + test on PR)

- [ ] **Room Database & Domain Models**
  - Implement complete entity schema:
    - `ExerciseEntity` (canonical dictionary with sub-muscle mapping)
    - `RoutineEntity` + `RoutineExerciseEntity` (with `ExerciseGroup` for supersets)
    - `WorkoutSessionEntity` + `WorkoutSetEntity` (with set types: WARMUP, WORKING, DROP_SET, FAILURE)
    - `UserProfileEntity` (biometrics, unit preferences)
  - Implement all DAOs with Flow-based queries
  - Write Room database migrations strategy (export schemas)
  - **Seed the exercise dictionary** (~250 exercises, all sub-muscles, all equipment types)

- [ ] **Design System (core-ui)**
  - Implement `RepForgeTheme` with Material 3 dynamic theming
  - Define color palette: Carbon Slate `#0B0D10`, Forge Amber `#FF6600`, Kinetic Lime `#D4FF00`
  - Configure tabular/monospace numeric typography for timer displays
  - Build reusable composables: `ForgeButton`, `SetRow`, `StepperCounter`, `ExerciseCard`
  - Ensure all touch targets ≥ 48dp
  - Implement accessibility: content descriptions, semantic properties, shape+icon for status indicators (not color-only)

- [ ] **Onboarding Flow (feature-onboarding)**
  - 3-screen onboarding: Welcome → Biometric input → Health Connect permission request
  - Configurable unit preferences (kg/lb, cm/ft-in)
  - Store profile to Room
  - Health Connect availability check + graceful degradation if not installed

- [ ] **Firebase Auth — Email/Password (feature-auth)**
  - Set up Firebase project and connect Android app
  - Implement sign-up screen (email + password + confirm password)
  - Implement login screen with "Forgot Password" flow
  - Auth state observation via `FirebaseAuth.AuthStateListener`
  - Guard all cloud operations (cache read/write, quota) behind auth check
  - Offline graceful degradation: app fully usable without auth, cloud features disabled
  - Link auth UID to Firestore user document for quota tracking

- [ ] **Unit Preferences Architecture**
  - Create `UnitSystem` sealed class (Metric/Imperial)
  - All internal storage in metric (kg, cm); conversion at the display layer only
  - Plate math adapts to unit system (metric plates vs imperial plates)

**Verification:**
- `./gradlew assembleDebug` passes
- Room schema exports match entity definitions
- Onboarding flow navigable in Compose Preview
- All shared composables render correctly with accessibility scanner

---

#### Sprint 2 — Routine Management & Template Builder (Weeks 3-4)

> **Goal:** Users can create, browse, edit, and delete workout routines without any AI or network dependency.

**Tasks:**

- [ ] **Routine List Screen (feature-routines)**
  - Display all saved routines with exercise count, estimated duration, sub-muscle tags
  - Swipe-to-delete with undo snackbar
  - Search/filter by sub-muscle or equipment type
  - Empty state with CTA to create first routine

- [ ] **Guided Rapid Template Wizard**
  - Step 1: Sub-muscle multi-select chips (from taxonomy enum)
  - Step 2: Exercise picker, pre-filtered to selected sub-muscles, with auto-suggestions
  - Step 3: Set/rep/rest configuration with stepper counters
  - Step 4: Real-time projection panel (estimated duration, calorie range, sub-muscle distribution pie)
  - **Add ExerciseGroup support:** Allow users to mark 2-3 exercises as a superset/circuit during creation

- [ ] **Routine Detail & Edit Screen**
  - Full routine view with drag-to-reorder exercises
  - Inline editing of sets, reps, rest, and notes
  - Exercise swap (same sub-muscle suggestions)
  - Delete/add individual exercises

- [ ] **Exercise Dictionary Browser**
  - Searchable list of all canonical exercises
  - Filter by sub-muscle, equipment type
  - Exercise detail card showing primary/secondary muscles, creator tags
  - "Create Custom Exercise" flow for user-defined movements

- [ ] **Repository Layer**
  - `RoutineRepository` with Room-backed implementations
  - `ExerciseRepository` with dictionary queries
  - Use Cases: `CreateRoutineUseCase`, `GetRoutinesUseCase`, `UpdateRoutineUseCase`

**Verification:**
- Create routine via wizard → appears in list → editable → deletable
- Exercise search returns correct sub-muscle filtered results
- All flows work in airplane mode (pure offline)
- Compose UI tests for wizard flow

---

#### Sprint 3 — Active Workout Session Engine (Weeks 5-6)

> **Goal:** Complete in-gym workout tracking with all the UX features that make RepForge distinctive.

**Tasks:**

- [ ] **Active Session Screen (feature-session)**
  - Start workout from a routine (pre-fills exercises, sets, target reps)
  - Start empty/freestyle workout
  - Exercise cards with expandable set rows
  - Set type selector (Warmup / Working / Drop Set / Failure)

- [ ] **Progressive Overload Ghost Text**
  - Query last completed session for the same routine
  - Display ghost text per set: `Previous: 70 kg × 8 reps`
  - Rolling 3-session performance indicator with icons + text + shape (not color alone):
    - ▲ Progressing, ► Plateauing, ▼ Declining

- [ ] **Auto-Rest Timer**
  - Triggered on set completion (✓ tap)
  - Bottom-anchored countdown bar with large numerals
  - `+30s`, `-15s`, `Skip` controls
  - Dual-pulse haptic vibration at 0:00 (no audio)
  - Timer persists if user navigates to another exercise (doesn't reset)

- [ ] **Barbell Plate Math Helper**
  - 1-tap calculator icon on barbell exercises
  - Configurable bar weight (default 20kg Olympic, options: 15kg, 10kg, custom)
  - Available plates configurable per unit system
  - Visual plate diagram showing per-side loading
  - Greedy algorithm: largest plates first

- [ ] **Mid-Workout Exercise Swap**
  - "Swap ⇄" button per exercise card
  - Tier 1: Same sub-muscle alternatives (pre-computed from dictionary)
  - Tier 2: Full dictionary search
  - Tracks swap as a diff for post-workout resolution

- [ ] **Session State Management**
  - `ActiveSessionViewModel` with `StateFlow` for reactive UI
  - Persist in-progress session to Room on every set completion (crash recovery)
  - Track all diffs (swaps, added sets, modified weights) for the Diff Resolver

- [ ] **RPE Input (Optional)**
  - Post-set RPE slider (6.0-10.0 in 0.5 increments)
  - Optional—don't block set completion

**Verification:**
- Start workout → log sets → ghost text matches previous session → timer fires haptic → finish workout
- Kill app mid-workout → reopen → session restored
- Plate math calculates correctly for edge cases (odd weights, empty bar)
- All interactions work offline
- Haptic fires on physical device

---

#### Sprint 4 — Post-Workout Intelligence, Caloric Engine & Health Connect (Weeks 7-8)

> **Goal:** Workout completion flow, calorie calculation, Health Connect export, and the Diff Resolver.

**Tasks:**

- [ ] **Post-Workout Diff Resolver**
  - Compare completed session against source routine
  - Detect: swapped exercises, added/removed sets, modified rep ranges
  - Modal with 3 options:
    - "Update Base Routine" → merge diffs into master routine
    - "Save as New Variation" → fork routine with auto-generated name
    - "Log for Today Only" → persist session, leave routine untouched
  - If no diffs detected, skip the modal

- [ ] **Post-Workout Summary Screen**
  - Total duration, total tonnage, set count
  - Calorie confidence range (with clearly labeled "Estimate" badge)
  - Sub-muscle distribution chart (horizontal bar or pie)
  - PR detection + trophy banner for any beaten records (weight PR, rep PR, volume PR)
  - "Share Workout" placeholder (for Phase 3)

- [ ] **Bio-Energetic Caloric Engine**
  - Implement the 3-factor model from PRD Section 7.2:
    - Basal metabolic component (Mifflin-St Jeor BMR × MET × active minutes)
    - Mechanical volume load (tonnage × body-weight scaling factor)
    - EPOC modifier (elevated for drop sets, failure sets, short rest)
  - **Widen confidence band to ±15%** (not ±5%)
  - Label as "Rough Estimate" in UI
  - If heart rate data available from Health Connect, blend HR-based model
  - Real-time calorie dial during active session

- [ ] **Health Connect Integration (core-health)**
  - Permission request flow with explanation rationale
  - **WRITE on workout completion:**
    - `ExerciseSessionRecord` (type: STRENGTH_TRAINING)
    - `TotalCaloriesBurnedRecord`
  - **READ on app launch:**
    - `WeightRecord` → auto-update user profile weight
    - `HeartRateRecord` → feed into caloric engine during active sessions
  - Graceful degradation if Health Connect not installed or permissions denied
  - Offline-safe: queue writes and flush when Health Connect is available

- [ ] **PR Detection Engine**
  - Track per-exercise PRs: heaviest weight, most reps at weight, highest volume (weight × reps × sets)
  - Compare against all historical sessions
  - Celebrate new PRs with confetti animation + trophy banner

**Verification:**
- Modify workout mid-session → Diff Resolver detects changes → all 3 options work correctly
- Calorie calculation matches manual hand-calculation for test scenarios
- Health Connect receives correct ExerciseSessionRecord after workout
- Weight auto-syncs from Health Connect mock data
- PR detection fires for genuine new records, doesn't fire for warm-up sets

---

#### Sprint 5 — AI Ingestion Pipeline & Cloud Backend (Weeks 9-10)

> **Goal:** YouTube workout ingestion via Gemini, server-side Cloud Functions, global video cache, and quota management.

**Tasks:**

- [ ] **Firebase Project Setup**
  - Firebase Auth (anonymous + Google Sign-In)
  - Firestore collections: `verifiedWorkouts`, `userQuotas`, `userProfiles`
  - Security rules: users can only read/write their own data; verified workouts are read-only to all, writable via Cloud Functions only
  - Deployment pipeline for Cloud Functions

- [ ] **Cloud Functions — Gemini Ingestion**
  - `parseYouTubeWorkout` function:
    1. Receive video URL/ID from client
    2. Check Firestore `verifiedWorkouts` cache (by video ID)
    3. If cached → return immediately (0 AI tokens)
    4. If not cached → check user quota (reject if exhausted)
    5. Extract video metadata via YouTube Data API v3
    6. Call Gemini Flash with strict JSON schema (per PRD Section 4.2)
    7. Validate response schema, handle malformed responses
    8. Return parsed workout to client for HITL verification
  - `confirmVerifiedWorkout` function:
    1. Receive user-verified workout data
    2. Write to `verifiedWorkouts` collection
    3. Decrement user quota

- [ ] **Quota Manager Cloud Function**
  - Track per-user weekly import credits (default: 3)
  - Reset weekly (Cloud Scheduler)
  - Gamified bonus: +1 credit for 3 completed workouts in a week
  - Rate throttle: max 1 import per 10 minutes per user

- [ ] **Client-Side Ingestion Flow (feature-ingestion)**
  - URL input bar with YouTube URL validation
  - Progress states: Checking cache → Parsing video → AI processing → Ready for review
  - Error states with user-friendly messages:
    - "This doesn't look like a workout video" (Gemini couldn't extract exercises)
    - "You've used all your import credits this week" (quota exhausted)
    - "No internet connection" (offline)
    - "Something went wrong" (server error, with retry button)

- [ ] **HITL Verification Modal**
  - Display video thumbnail + title + creator
  - Editable exercise list with inline editing
  - Sub-muscle tag validation
  - Add/remove/reorder exercises
  - "Confirm & Save" → writes to local Room + triggers `confirmVerifiedWorkout`

- [ ] **Curated Creator Hub (feature-routines)**
  - Static directory of curated creator playlists
  - Browse by creator → browse by video → ingest
  - Creator cards with logos, workout count, description

- [ ] **Error Handling Taxonomy**
  - Define sealed class hierarchy for all ingestion errors
  - Map each error to a user-facing message and recovery action
  - Implement retry with exponential backoff for transient failures
  - Log errors to Firebase Crashlytics with context

**Verification:**
- Import a real YouTube workout URL → Gemini parses → HITL modal shows exercises → Confirm saves to local DB
- Import the same URL again → returned from cache instantly (verify 0 API calls)
- Exhaust quota → receive clear "out of credits" message
- Test with non-workout video → receive clear error
- Test offline → receive offline error with retry when connected
- Test with malformed Gemini response → graceful fallback

---

#### Sprint 6 — Heatmap, History, Polish & Launch Prep (Weeks 11-12)

> **Goal:** Complete the remaining tabs, polish UX, add data export, accessibility pass, and prepare for Play Store launch.

**Tasks:**

- [ ] **Interactive 2D Sub-Muscle Heatmap (feature-heatmap)**
  - Front and rear body vector illustrations (SVG or Canvas-based)
  - Color intensity mapped to weekly volume per sub-muscle (sets × weight)
  - "Time since last trained" indicator per sub-muscle (replaces undefined "recovery status")
  - Tappable regions → show sub-muscle detail: volume this week, last session, trend
  - Toggle: 7-day / 14-day / 30-day view

- [ ] **Progressive Overload Radar Chart**
  - Spider/radar chart showing relative volume across all sub-muscles
  - Compare current week vs previous week
  - Highlight imbalances (e.g., chest volume >> back volume)

- [ ] **History & Logbook (feature-history)**
  - Chronological workout feed (grouped by week)
  - Calendar heatmap showing workout frequency (GitHub contribution graph style)
  - Workout detail expansion: exercises, sets, tonnage, calories, PRs
  - Streak counter with visual streak badge

- [ ] **PR Trophy Room**
  - All-time PRs per exercise
  - Sortable by date, weight, exercise
  - PR cards with date, weight, reps, and trend graph

- [ ] **Data Export**
  - CSV export: workout history with all set data
  - JSON export: full data dump (routines + history + profile)
  - Share via Android share sheet

- [ ] **Accessibility Pass**
  - Full TalkBack audit
  - Content descriptions on all interactive elements
  - Dynamic text sizing support
  - Color-blind safe indicators (shape + icon, not just color)
  - Minimum contrast ratios per WCAG 2.1 AA

- [ ] **Performance Optimization**
  - Room query optimization (indices on frequently queried columns)
  - Compose recomposition audit (stability markers, remember/derivedStateOf)
  - List performance (LazyColumn with keys, avoid unnecessary recompositions)
  - App startup time profiling (Macrobenchmark)
  - Image/asset optimization

- [ ] **Testing Suite**
  - Unit tests: ViewModels, Use Cases, Caloric Engine, Plate Math, PR Detection
  - Integration tests: Room DAOs, Health Connect writes, Cloud Function calls
  - UI tests: Critical flows (onboarding, create routine, active session, finish workout)
  - Manual QA checklist for all offline scenarios

- [ ] **Play Store Launch Prep**
  - App signing with Play App Signing
  - Privacy policy (covering Health Connect data, Gemini data processing, Firebase data)
  - Play Store listing: screenshots, description, feature graphic
  - Health Connect declaration in Play Console
  - Internal testing track deployment
  - Crash monitoring dashboard (Firebase Crashlytics)
  - ProGuard/R8 rules for release build

**Verification:**
- Heatmap renders correctly with real workout data
- History shows accurate chronological data with calendar heatmap
- Export produces valid CSV/JSON files
- TalkBack navigation completes all critical flows
- Release APK size < 15MB
- Cold start < 1.5 seconds
- All tests pass in CI
- Internal test track installable on 3+ physical devices

---

### Risk Mitigation

| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| Gemini returns inconsistent/invalid JSON | High | Medium | Strict schema enforcement + validation layer + retry + fallback to "manual entry" |
| YouTube changes transcript API or blocks access | Medium | High | Use official YouTube Data API v3. Decouple transcript extraction as a replaceable module. |
| Health Connect not installed on user's device | Medium | Low | Graceful degradation. All features work without HC. Prompt to install if available. |
| Calorie numbers wildly disagree with user's watch | High | Medium | Widen confidence band. Label as estimate. Show "why this differs" explainer. |
| Room migration breaks on app update | Medium | High | Export Room schemas. Write migration tests. Never destructive migrate. |
| Firebase costs spike from viral growth | Low | High | Firestore read/write budgets. Cache aggressively. Rate limit Cloud Functions. |
| Play Store rejects for Health Connect policy violation | Medium | High | Follow Health Connect developer policy checklist. Submit privacy policy early. |

---

### Architecture Decision Records (Summary)

| Decision | Choice | Rationale | Alternatives Rejected |
|---|---|---|---|
| **UI Framework** | Jetpack Compose | Modern, declarative, better accessibility tooling, faster iteration | XML Views (slower, more boilerplate) |
| **Backend** | Firebase | Offline SDK, auth, hosting, functions—all in one. Minimal ops for small team. | Supabase (less Android-native), Custom server (too much ops overhead for MVP) |
| **AI Call Location** | Server-side (Cloud Functions) | Protects API key, enables server-side caching and validation | Client-side (exposes API key in APK) |
| **Unit Storage** | Internal metric, display-layer conversion | Single source of truth. No conversion bugs in business logic. | Store in user's preferred unit (conversion bugs, mixed data) |
| **Superset Support** | `ExerciseGroup` wrapper entity | Supports real-world programming patterns from target creators | Flat exercise list (can't represent supersets/circuits) |
| **Calorie Confidence** | ±15% band, labeled "estimate" | Honest representation of model uncertainty | ±5% (misleadingly precise) |
| **Recovery Heatmap** | "Time since last trained" for MVP | Verifiable from data. No pseudo-science. | Recovery algorithm (insufficient data inputs for accuracy) |

---

## Finalized Decisions

All open questions have been resolved. The following decisions are locked in:

| # | Question | Decision | Build Impact |
|---|---|---|---|
| 1 | **Authentication** | Firebase Auth with **email/password** | Standard sign-up/login flow. No anonymous auth, no Google Sign-In. |
| 2 | **Monetization** | **Completely free app** | No Play Billing Library needed. No subscription state management. Simplifies architecture. |
| 3 | **Superset/Circuit support** | **Yes, included in MVP** | `ExerciseGroup` entity added to data model in Sprint 1. Session engine supports grouped exercises in Sprint 3. |
| 4 | **Unit system** | **Full toggle: kg/lb + cm/ft-in** | `UnitSystem` preferences stored per user. All internal storage in metric; conversion at display layer. Plate math adapts to unit system. |
| 5 | **Calorie confidence** | **±15%, labeled "Rough Estimate"** | Wider band, honest labeling. Relative comparisons shown alongside absolute numbers. |
| 6 | **Curated Creator Hub** | **Included in MVP** | Creator has confirmed permission from YouTube creators. App is for select users with legal permission. Full Curated Hub ships in Sprint 5. |
| 7 | **WearOS** | **No timeline pressure; build provisions for future** | Session engine data layer will be structured for future KMP extraction. Domain models kept pure Kotlin (no Android deps). Shared `core-domain` module is the future KMP boundary. |

---

### WearOS Future Provisions

While WearOS is Phase 2 with no deadline, the following architectural decisions are baked in from Sprint 1 to ensure a smooth future extraction:

1. **`core-domain` module is pure Kotlin** — no `android.*` imports, no Room annotations, no Compose dependencies. This module becomes the future KMP `commonMain` source set.
2. **Repository interfaces live in `core-domain`** — implementations live in `core-data` (Android-specific). WearOS will provide its own thin `core-data-wear` implementation.
3. **Session state is modeled as a finite state machine** — `SessionState` sealed class (Idle → Active → Paused → Finishing → Completed) can be shared across phone and watch.
4. **Set completion events are emitted as domain events** — not tied to UI callbacks. WearOS companion can emit the same events from wrist interactions.
5. **Health Connect reads/writes are abstracted behind `HealthDataPort` interface** — WearOS has its own Health Services API; the port pattern allows swapping implementations.
