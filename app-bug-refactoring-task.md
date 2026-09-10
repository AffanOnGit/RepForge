# RepForge Implementation Tasks

## Phase 1 — Onboarding Gate
- [x] Read all relevant ViewModels / NavHost
- [x] Create `AppStartupViewModel.kt`
- [x] Modify `MainActivity.kt` to use startup VM

## Phase 2 — Calorie / Guest Profile Fix
- [x] Modify `ActiveSessionViewModel` — add `isProfileMissing` + calorie fallback
- [x] Modify `ActiveSessionScreen` — show "Complete profile" banner
- [x] Modify `ProfileScreen` — show biometrics form without login

## Phase 3 — Custom Routine Builder (full CRUD)
- [x] Create `CustomRoutineBuilderViewModel.kt`
- [x] Create `CustomRoutineBuilderScreen.kt`
- [x] Create `ExercisePickerBottomSheet.kt` (inline in builder screen file)
- [x] Modify `RoutineListScreen` — FAB shows choice modal
- [x] Modify `RoutineDetailScreen` — Edit action opens builder with routineId
- [x] Modify `RepForgeNavHost` — register `custom_routine_builder` route
- [x] Modify `BottomNavBar` — hide on builder route

## Phase 4 — Heatmap: Compose Canvas Path Human Body
- [x] Rewrite `AnatomyMapComposable.kt` — full front+rear Bezier path anatomy
- [x] Modify `HeatmapViewModel` — add per-session heatmap mode
- [x] Modify `HeatmapScreen` — weekly/session toggle
- [x] Modify `WorkoutSummaryScreen` — embed per-session anatomy heatmap
- [x] Modify `WorkoutSummaryViewModel` — provide per-session muscle data

## Phase 5 — Build Verification
- [x] Run `./gradlew assembleDebug`
- [x] Fix any compile errors
