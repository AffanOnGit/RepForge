# RepForge — Feature Implementation Plan

## Background

Four user-reported issues have been identified through code analysis. The plan below maps each issue to its root cause and prescribes the exact files to touch, in dependency order.

---

## Issue 1 — Onboarding Gate: App Launches Directly into Active Workout

### Root Cause
`MainActivity.kt` unconditionally passes `startDestination = TopLevelDestination.TODAY.route` to `RepForgeNavHost`. The `"onboarding"` route exists in the nav graph, but there is **no code that checks `isOnboardingComplete()` on launch** and redirects to it. The `UserProfileRepository.isOnboardingComplete()` API already exists and simply counts rows in the `user_profile` table.

### Proposed Fix

#### [MODIFY] [MainActivity.kt](file:///d:/GitHub/Personal%20Repos/RepForge/app/src/main/java/com/repforge/MainActivity.kt)
- Inject a lightweight `AppStartupViewModel` (or use `by viewModels()`) that calls `userProfileRepository.isOnboardingComplete()` in `init {}`.
- Hold a `startDestination: StateFlow<String?>` (null = still loading, string = resolved).
- In `setContent`, use `collectAsState()` and show a blank screen while `null`, then compose `RepForgeNavHost` with the resolved start destination.
- This ensures the splash screen stays up until the DB check resolves (≈ 1 frame).

#### [NEW] `app/src/main/java/com/repforge/AppStartupViewModel.kt`
```kotlin
@HiltViewModel
class AppStartupViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {
    val startDestination = flow {
        val done = userProfileRepository.isOnboardingComplete()
        emit(if (done) TopLevelDestination.TODAY.route else "onboarding")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
}
```

---

## Issue 2 — Calorie Counter Broken for Guest / Skip-Login Users

### Root Cause
`ActiveSessionViewModel` and `WorkoutSummaryViewModel` both collect `userProfileRepository.getUserProfile()`. When a user skips onboarding, the profile row does not exist, so the calorie formula receives `null` and either crashes or shows 0.

The **profile screen is behind a login wall** (Profile tab → `onNavigateToLogin`), so guests cannot reach it to fill in biometrics even if they wanted to.

### Two-Part Fix

#### Part A — Calorie Estimation Fallback
#### [MODIFY] Session / Summary ViewModels
- Wherever `userProfile?.weightKg` is used for calorie math, substitute sensible population defaults: `weightKg = 80.0`, `heightCm = 175.0`, `age = 30`, `sex = MALE` when the profile is `null`.
- Display a non-blocking banner: *"Using estimated calorie defaults — complete your profile for precision."*

#### Part B — Surface the Profile Setup Prompt in Active Session
#### [MODIFY] [ActiveSessionScreen.kt](file:///d:/GitHub/Personal%20Repos/RepForge/feature/feature-session/src/main/java/com/repforge/feature/session/ActiveSessionScreen.kt)
- If `state.isProfileMissing` is `true`, show a dismissible card under the calorie counter that says **"Set up your profile"** (tapping opens the Profile tab / onboarding biometrics step).

#### Part C — Allow Guest Access to Profile Setup
#### [MODIFY] [ProfileScreen.kt](file:///d:/GitHub/Personal%20Repos/RepForge/feature/feature-profile/src/main/java/com/repforge/feature/profile/ProfileScreen.kt)
- Show the biometrics entry form (weight, height, age, sex) **even when not logged in**, gated only by the presence/absence of a profile row, not by Firebase auth state.
- Login prompt should appear only for cloud sync features (e.g. "Sync to cloud requires sign-in"), not to block local profile setup.

---

## Issue 3 — Custom Routine Builder (From Exercise Database)

### Root Cause
The current `RapidTemplateWizardScreen` lets users pick **muscle groups and rep schemes** but never surfaces the actual `Exercise` catalog. Users pick muscles → the wizard auto-selects exercises. There is no flow where a user can:
1. Browse all exercises in the DB by name / muscle / equipment.
2. Hand-pick specific exercises and add them to a new blank routine.
3. Reorder them, set prescribed sets/reps, and save.

The `ExerciseDictionaryScreen` exists as a read-only catalogue. The `RoutineDetailScreen` shows a routine's exercises but does not expose an edit flow for adding new exercises.

### Proposed Architecture

```
RoutineListScreen
  └─ FAB → picker: "Template Wizard" or "Build From Scratch"
          └─ "Build From Scratch" → CustomRoutineBuilderScreen (NEW)
                  ├─ Inline name / description fields
                  ├─ Add Exercise button → ExercisePickerBottomSheet (NEW)
                  │     ├─ Search bar + muscle/equipment filters
                  │     └─ Tap exercise → added to builder list
                  ├─ Reorderable exercise list (drag handles)
                  ├─ Per-exercise: sets, min reps, max reps, rest time
                  └─ Save button → creates Routine + RoutineExercise rows in DB
```

#### [MODIFY] [RoutineListScreen.kt](file:///d:/GitHub/Personal%20Repos/RepForge/feature/feature-routines/src/main/java/com/repforge/feature/routines/RoutineListScreen.kt)
- FAB `onClick` → show a small modal or two-option card: **"Rapid Wizard (60s)"** vs **"Build Custom"**.

#### [NEW] `feature/feature-routines/…/CustomRoutineBuilderScreen.kt`
- Full-page `Scaffold` with a `LazyColumn` of added exercises.
- Each exercise row shows name, sets, rep range (editable inline with small +/- steppers), and a drag-handle icon.
- "Add Exercise" button opens `ExercisePickerBottomSheet`.

#### [NEW] `feature/feature-routines/…/CustomRoutineBuilderViewModel.kt`
- Holds `builderState`: `routineName`, `description`, `List<BuilderExerciseEntry>`.
- `addExercise(exercise: Exercise)`, `removeExercise(id)`, `reorder(from, to)`, `updateSets(id, count)`, `saveRoutine(): String` (returns routineId).

#### [NEW] `feature/feature-routines/…/ExercisePickerBottomSheet.kt`
- Reuses `ExerciseDictionaryViewModel`'s search + filter logic.
- Confirms a selection and calls back with the chosen `Exercise`.

#### [MODIFY] [RepForgeNavHost.kt](file:///d:/GitHub/Personal%20Repos/RepForge/app/src/main/java/com/repforge/navigation/RepForgeNavHost.kt)
- Register route `"custom_routine_builder"` → `CustomRoutineBuilderScreen`.
- `BottomNavBar.kt` — add `"custom_routine_builder"` to `hideOnRoutes`.

#### [MODIFY] [RoutineDetailScreen.kt](file:///d:/GitHub/Personal%20Repos/RepForge/feature/feature-routines/src/main/java/com/repforge/feature/routines/RoutineDetailScreen.kt)
- "Edit" action → navigate to `custom_routine_builder?routineId={id}` (builder pre-populated from existing routine).

---

## Issue 4 — Heatmap: Replace Box Matrix with Proper 2D Human Body SVG Paths

### Root Cause
`AnatomyMapComposable.kt` uses `drawMuscleBox()` (Canvas `drawRoundRect`) as rectangular blobs arranged manually to approximate a body. This reads as a coloured grid, not an anatomical silhouette.

### Proposed Fix: Canvas SVG-Path Body

Replace every `drawMuscleBox()` call with proper **muscle-shaped filled `Path`** objects drawn on a `Canvas`, scaled to the composable's `BoxWithConstraints` dimensions. No external library or asset file is needed — all paths are defined in code as relative Bezier/line segments.

#### [MODIFY] [AnatomyMapComposable.kt](file:///d:/GitHub/Personal Repos/RepForge/feature/feature-heatmap/src/main/java/com/repforge/feature/heatmap/AnatomyMapComposable.kt)

Replace `drawFrontAnatomy` and `drawRearAnatomy` helpers with `drawFrontSilhouette` / `drawRearSilhouette` that build `Path` objects using:
- `moveTo`, `lineTo`, `cubicTo` for organic muscle shapes (pecs, delts, biceps, quads, etc.)
- An outer body silhouette stroke (head, torso, arms, legs) in `CarbonSlateSurface` underneath all muscle layers.
- Each named muscle region filled with `calculateHeatColor(intensity)`.

The hit-detection `detectMuscleHit()` switches from raw pixel comparisons to **`Path.contains(offset)`** checks using Android `android.graphics.Path` mapped from Compose paths (or a lookup list of scaled `Rect` bounds per muscle, which is simpler and more reliable).

**Body scale:** All path coordinates are normalised to a `1000 × 2000` viewport, then uniformly scaled to the actual `Canvas` size at draw time via `scale(w/1000f, h/2000f)`.

#### Muscle regions to implement (Front View):
Head, neck, upper-chest (left/right), mid-chest (left/right), lower-chest (left/right), front delts (left/right), side delts (left/right), biceps (left/right), forearms (left/right), upper abs, lower abs, obliques (left/right), quads (left/right), calves (left/right).

#### Muscle regions to implement (Rear View):
Head, neck, upper-back/traps, rear delts (left/right), triceps (left/right), lats (left/right), lower back, glutes (left/right), hamstrings (left/right), calves (left/right).

> [!IMPORTANT]
> The paths should look like an anatomical figure, not a stick figure. Organic Bezier curves must be used for pec shapes, shoulder contours, quad sweeps, and calf silhouettes. The outer body silhouette is drawn first, then each muscle region is painted on top.

---

## Verification Plan

### Build Verification
```
./gradlew assembleDebug
```
Should produce 0 errors after all changes.

### Manual QA Checklist
| # | Scenario | Expected |
|---|----------|----------|
| 1 | Fresh install, no profile | Onboarding screen appears, not workout |
| 2 | Complete onboarding → relaunch | Goes directly to Today tab |
| 3 | Skip onboarding entirely | Today tab shows; calorie counter shows estimated defaults with banner |
| 4 | Guest user taps Profile tab | Biometric form visible without login |
| 5 | Routines tab → FAB | Two creation options visible |
| 6 | "Build Custom" | Can search/filter exercises, add to list, set sets/reps, save |
| 7 | Heatmap tab | Front/Rear body shows human silhouette with organic muscle shapes, not boxes |
| 8 | Tap a muscle region | Correct muscle name appears in bottom sheet |

---

## Open Questions

> [!IMPORTANT]
> **Q1 — Onboarding Skip:** Should "Skip for Now" at step 3 of onboarding mark `isOnboardingComplete = true` (so the splash gate never shows again) or keep it `false` (re-show onboarding next launch)? Current code calls `completeOnboarding()` on skip, which writes a profile row — this means once-and-done. Is that correct?

> [!IMPORTANT]
> **Q2 — Routine Builder Access Level:** Should the custom routine builder also be accessible as an "Edit" entry point from `RoutineDetailScreen`, or remain creation-only for now?

> [!IMPORTANT]
> **Q3 — Heatmap Path Fidelity:** Should the 2D body paths be drawn using pure Compose Canvas Path primitives (no assets), or is it acceptable to bundle a vector SVG drawable (XML VectorDrawable) and tint/colorise individual `<path>` elements using `DrawableCompat`? The SVG approach is faster to implement but harder to apply per-muscle heat coloring programmatically.
