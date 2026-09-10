# RepForge — Project Master Task Tracker

## 🏋️‍♂️ Project Overview
RepForge is a hyper-focused, local-first, native Android strength training application built with Modern Kotlin, Jetpack Compose (Material 3), Clean Architecture, and a multi-module Gradle structure.

---

### Sprint 1: Foundation & Core Data Layer (100% Complete) ✅
- [x] **Project Architecture & Scaffolding**
  - [x] Root Gradle build (`build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`)
  - [x] Version catalog (`libs.versions.toml`) with Android 15 / Compose BOM / Room / Hilt / Firebase / Health Connect
  - [x] Convention plugins (`repforge.android.feature`, `repforge.android.library.compose`, `repforge.android.application.compose`, `repforge.android.hilt`, `repforge.jvm.library`)
  - [x] Module layout (`app`, `core-domain`, `core-data`, `core-ui`, `core-network`, `core-health`, and 8 feature modules)
  - [x] CI/CD Pipeline (`.github/workflows/android.yml` for automated build, lint, and test validation)
  - [x] AndroidManifest with Health Connect permissions & privacy policy declarations
- [x] **Clean Domain Layer (`core-domain`)**
  - [x] 19 Sub-Muscle gym taxonomy (`SubMuscle`, `MuscleGroup`)
  - [x] Canonical exercise taxonomy with 7 equipment types (`Exercise`, `Equipment`)
  - [x] Routine models with Superset/Triset/Circuit/AMRAP/EMOM grouping (`ExerciseGroup`, `RoutineExercise`, `Routine`)
  - [x] Active workout session FSM (`WorkoutSession`, `WorkoutSet`, `SessionState`, `SetType`)
  - [x] Personal record engine (`PersonalRecord`, `PRType`, `ProgressionTrend`)
  - [x] User biometrics & preferences (`UserProfile`, `BiologicalSex`, `TrainingExperience`, `UnitSystem`)
  - [x] Unit converter utilities (kg ↔ lb, cm ↔ ft-in, display formatters)
  - [x] Greedy Plate Math calculator (Olympic 20kg, Women's 15kg, EZ Curl 10kg, Trap 25kg)
- [x] **Local Room Database Layer (`core-data`)**
  - [x] 8 Room entities with foreign key constraints & cascading deletes
  - [x] Reactive Flow DAOs (`ExerciseDao`, `RoutineDao`, `WorkoutSessionDao`, `UserProfileDao`)
  - [x] Canonical seed dictionary (`ExerciseSeedData` with 250+ strength exercises)
  - [x] Repository implementations (`ExerciseRepositoryImpl`, `RoutineRepositoryImpl`, `WorkoutSessionRepositoryImpl`, `UserProfileRepositoryImpl`)
  - [x] Hilt `DatabaseModule` & `RepositoryModule`
- [x] **High-Contrast Design System (`core-ui`)**
  - [x] Palette: Carbon Slate (`#0B0D10`), Forge Amber (`#FF6600`), Kinetic Lime (`#D4FF00`)
  - [x] Sweaty-hands UX compliance: all touch targets ≥ 48dp (primary steppers 52dp)
  - [x] WCAG AA accessibility: shape + color + letter set indicators (`SetTypeBadge`)
  - [x] Tabular monospace typography for numeric timers, weights, and reps
  - [x] Design components: `ForgeButton`, `ForgeTextField`, `StepperCounter`, `SetRow`, `ExerciseCard`, `PlateMathVisualizer`
- [x] **Onboarding & Authentication (`feature-onboarding`, `feature-auth`, `core-network`)**
  - [x] 3-step onboarding flow (`OnboardingViewModel`, `OnboardingScreen`)
  - [x] Unit toggle preference (Metric vs Imperial)
  - [x] Biometrics capture (weight, height, age, sex, training experience)
  - [x] Health Connect rationale step
  - [x] Firebase email/password authentication with offline guest mode fallback (`AuthRepositoryImpl`, `AuthViewModel`, `LoginScreen`, `SignUpScreen`)

---

### Sprint 2: Routine Management & Template Builder (100% Complete) ✅
- [x] **Routine List & Discovery (`feature-routines`)**
  - [x] `RoutineListViewModel` & `RoutineListScreen` with search, filter, and swipe options
  - [x] Duplicate routine as variation
  - [x] One-tap workout launch
- [x] **Rapid Template Wizard (< 60s Flow)**
  - [x] `RapidTemplateWizardViewModel` & `RapidTemplateWizardScreen`
  - [x] Sub-muscle chip selector across 19 categories
  - [x] Superset link pairing toggle
  - [x] Volume steppers with live tonnage & time projections
- [x] **Exercise Dictionary & Swap System**
  - [x] `ExerciseDictionaryViewModel` & `ExerciseDictionaryScreen`
  - [x] Search & multi-filter across 250+ canonical exercises
  - [x] Custom exercise creator with sub-muscle mapping
  - [x] `RoutineDetailViewModel` & `RoutineDetailScreen` with mid-routine exercise swap bottom sheet

---

### Sprint 3: Active Workout Session Engine (100% Complete) ✅
- [x] **Active Session Execution (`feature-session`)**
  - [x] `ActiveSessionViewModel` & `ActiveSessionScreen` with live FSM persistence
  - [x] Real-time session elapsed timer & active calorie burn dial
  - [x] Ghost text indicators displaying previous session performance per exercise
  - [x] Haptic feedback pulse on set completion checkbox
  - [x] Barbell Plate Math visualizer dialog
  - [x] Mid-workout live exercise swap bottom sheet
  - [x] Superset & Circuit visual grouping with connected indicators
- [x] **Auto-Rest Timer Bar**
  - [x] Floating timer bar (`RestTimerBar`)
  - [x] Progress countdown with quick adjustment buttons (-15s, +30s, Skip)
  - [x] Rest duration auto-advance upon set completion

---

### Sprint 4: Post-Workout Intelligence & Health Connect (100% Complete) ✅
- [x] **Scientific Caloric Engine (`core-domain`)**
  - [x] `CaloricEngine`: Mifflin-St Jeor BMR + mechanical tonnage scaling + EPOC modifier
  - [x] Honest ±15% confidence interval explicitly labeled "Rough Estimate"
- [x] **Health Connect Integration (`core-health`)**
  - [x] `HealthConnectManager` with permission controller
  - [x] Export `ExerciseSessionRecord` (Strength Training) and `TotalCaloriesBurnedRecord`
  - [x] Read `WeightRecord` for automatic metabolic recalibration
- [x] **Post-Workout Summary & 3-Way Diff Resolver**
  - [x] `WorkoutSummaryViewModel` & `WorkoutSummaryScreen`
  - [x] Personal Record trophy badges (Weight PR, Reps PR, Volume PR)
  - [x] Sub-muscle volume distribution breakdown
  - [x] 3-Way Diff Resolver dialog (Update Base Routine, Save Variation, Log for Today Only)

---

### Sprint 5: AI Ingestion Pipeline & Cloud Backend (100% Complete) ✅
- [x] **Ingestion Architecture (`feature-ingestion`)**
  - [x] `IngestionModels` with strict error taxonomy (Invalid URL, Private Video, No Routine Detected, Parse Error)
  - [x] `YouTubeUrlHelper` supporting standard, short, and mobile YouTube links
  - [x] `AIIngestionService` featuring global cache flywheel + AI ingestion simulation
  - [x] Curated creator presets (Jeff Nippard Hypertrophy, Dr. Mike Renaissance Periodization, Athlean-X)
  - [x] Human-in-the-loop (HITL) review bottom sheet with exercise match validation, rep targets, and confidence score

---

### Sprint 6: Analytics, History, Profile & Delivery (100% Complete) ✅
- [x] **Interactive 2D Sub-Muscle Heatmap (`feature-heatmap`)**
  - [x] `HeatmapViewModel` aggregating rolling 7d, 14d, and 30d volume
  - [x] `AnatomyMapComposable` featuring custom 2D vector body rendering (Front and Rear views)
  - [x] Continuous color interpolation from Carbon Slate to Kinetic Lime based on volume
  - [x] Touch-responsive sub-muscle detail modal with recovery status & volume breakdown
  - [x] `HeatmapScreen` wired to Tab 3
- [x] **Training History & Logbook (`feature-history`)**
  - [x] `HistoryViewModel` calculating lifetime tonnage, weekly consistency streaks, and monthly groupings
  - [x] GitHub-style 10-week consistency heatmap matrix
  - [x] Expandable workout cards with individual set breakdowns and PR indicators
  - [x] PR Trophy Room modal sheet displaying all-time weight, rep, and volume personal records
  - [x] Data portability: 1-tap CSV and JSON export via Android Share Sheet
  - [x] `HistoryScreen` wired to Tab 4
- [x] **Athlete Profile & Preferences (`feature-profile`)**
  - [x] `ProfileViewModel` managing biometrics, unit conversions, and barbell presets
  - [x] Instant reactive unit toggle: Metric (kg/cm) ↔ Imperial (lb/ft-in)
  - [x] Biometrics editor for Mifflin-St Jeor metabolic calibration (Weight, Height, Age, Sex, Body Fat)
  - [x] Training experience selector (Beginner, Intermediate, Advanced)
  - [x] Barbell baseline selector (Olympic 20kg, Women's 15kg, EZ Curl 10kg, Trap 25kg)
  - [x] Health Connect connection status card with live sync trigger
  - [x] 100% Free & Open Philosophy manifesto card
  - [x] `ProfileScreen` wired to Tab 5
- [x] **Navigation & Shell Unification (`app`)**
  - [x] `RepForgeNavHost` uniting all 5 primary tabs + onboarding + auth + active workout engine
  - [x] `BottomNavBar` with smart suppression during active workouts, wizard creation, and summaries
