# RepForge: Product Requirements Document (PRD) & System Specification

**Document Version:** 1.0.0  
**Target Platform:** Native Android (Jetpack, Offline-First)  
**Status:** Approved for Review & Build Preparation  

---

## 1. Executive Summary & Core Value Proposition

### 1.1 Product Vision
**RepForge** is an intelligent, high-retention native Android workout tracking application designed to bridge the gap between decentralized fitness media (YouTube, social workouts, coach routines) and frictionless in-gym execution. 

Unlike traditional workout apps that confine lifters to static in-app libraries or tedious manual data entry, RepForge leverages an AI parsing engine (Google Gemini Flash) to ingest instructional YouTube workouts and convert them into structured, trackable routines with personalized bio-energetic caloric expenditure calculations.

### 1.2 Core Differentiators
1. **Curated & Custom YouTube Ingestion:** Ingests instructional workout videos from curated creator playlists (Jeff Nippard, RP, Athlean-X, Squat U) or custom URLs into actionable workout routines via Gemini Flash.
2. **Human-in-the-Loop (HITL) Data Flywheel:** Users review and verify AI-extracted routines before saving. Verified workouts are cached globally, eliminating redundant AI API calls for popular workouts across the community.
3. **Mainstream Sub-Muscle Anatomical Mapping:** Categorizes exercises by functional sub-muscle heads (e.g., Upper Chest, Side Delts, Lats) rather than generic muscle groups, powering an interactive 2D anatomical volume heatmap.
4. **Frictionless In-Gym Companion:** Designed for sweaty-hands gym environments with ghost values for progressive overload, automatic haptic rest timers, barbell plate math, and smart mid-workout exercise swapping.
5. **Routine Sync & Diff Resolver:** Post-workout intelligence that detects changes made on the fly (swapped exercises, added sets) and prompts the user to update the base routine, create a variation, or keep changes for that session only.
6. **Local-First & Health Connect Native:** 100% operable offline in basement gyms, writing and reading data directly to Google Health Connect.

---

## 2. Platform & Engineering Constraints

| Parameter | Specification |
| :--- | :--- |
| **Operating System** | Native Android (API Level 26+ / Android 8.0 to Android 15) |
| **Primary Architecture** | Local-First, Offline-First with asynchronous background sync |
| **Local Database** | Android Room (SQLite) with relational mapping |
| **Health Platform** | Google Health Connect (Jetpack `androidx.health.connect:connect-client`) |
| **AI Model & API** | Google Gemini 2.5/2.0 Flash via Google AI Studio Developer API |
| **Weight Units** | Kilograms (kg) — Standard Olympic barbell (20kg) and metric plates |
| **Height Units** | Feet and Inches (ft, in) — e.g., 5'10" |
| **Network Resilience** | All logging, timers, plate math, and past PRs work with zero internet |

---

## 3. Data Models & Anatomical Taxonomy

### 3.1 Mainstream Sub-Muscle Taxonomy
The system rejects obscure Latin medical names in favor of clean, universally recognized gym terminology:

```
Chest (Pectorals)
 ├── Upper Chest
 ├── Mid Chest
 └── Lower Chest

Shoulders (Deltoids)
 ├── Front Delts
 ├── Side Delts
 └── Rear Delts

Back
 ├── Lats (Width)
 ├── Upper Back / Traps (Thickness)
 └── Lower Back (Spinal Erectors)

Arms
 ├── Biceps
 ├── Triceps
 └── Forearms

Legs
 ├── Quads
 ├── Hamstrings
 ├── Glutes
 └── Calves

Core
 ├── Upper Abs
 ├── Lower Abs
 └── Obliques
```

### 3.2 Canonical Exercise Dictionary Schema
Every exercise in RepForge adheres to a standard data contract:
* `id` (UUID): Unique canonical identifier.
* `name` (String): Standardized exercise name (e.g., `"Incline Dumbbell Press"`).
* `equipment` (Enum): `DUMBBELL`, `BARBELL`, `CABLE`, `MACHINE`, `BODYWEIGHT`, `KETTLEBELL`, `SMITH_MACHINE`.
* `primary_sub_muscle` (SubMuscleEnum): The main targeted sub-head (e.g., `UPPER_CHEST`).
* `secondary_sub_muscles` (List<SubMuscleEnum>): Stabilizers / secondary contributors (e.g., `[FRONT_DELTS, TRICEPS]`).
* `creator_tags` (List<String>): Attribution tags (e.g., `["#JeffNippard", "#Hypertrophy", "#PushDay"]`).
* `is_custom` (Boolean): Flag indicating user-defined custom movements.

### 3.3 User Profile & Biometrics Schema
* **Essential Metrics:**
  * `weight_kg` (Float): Current body weight (auto-synced from Health Connect if available).
  * `height_feet` (Int) & `height_inches` (Int): Height measurement.
  * `age` (Int) & `biological_sex` (Enum: `MALE`, `FEMALE`): For basal metabolic & caloric equations.
* **Optional Metrics:**
  * `body_fat_percentage` (Float?): Refines lean mass calculations.
  * `training_experience` (Enum: `BEGINNER`, `INTERMEDIATE`, `ADVANCED`): Calibrates EPOC and work capacity.

### 3.4 Workout Session & Set Schema
* **Set Types:**
  * `WARMUP` (W): Excluded from maximum volume / PR calculations; included in warm-up tonnage.
  * `WORKING` (1, 2, 3...): Standard progressive overload sets.
  * `DROP_SET` (D): Immediate lower-weight set; elevated fatigue weighting.
  * `FAILURE` (F): Taken to absolute concentric failure (RIR 0).
* **Set Data Fields:**
  * `set_number` (Int), `set_type` (Enum), `weight_kg` (Float), `reps_completed` (Int), `target_reps` (Int), `is_completed` (Boolean), `rpe` (Float? - Rate of Perceived Exertion 6.0–10.0).

---

## 4. AI Ingestion Engine & Google AI Studio Architecture

```
                                [ YouTube Video URL / Video ID ]
                                               │
                                               ▼
                                  ┌─────────────────────────┐
                                  │ Global Video Cache      │
                                  │ (Has this ID been run?) │
                                  └────────────┬────────────┘
                                               │
                                ┌──────────────┴──────────────┐
                             YES│                             │NO
                                ▼                             ▼
                    ┌──────────────────────┐      ┌─────────────────────────┐
                    │ Return Cached Record │      │ Check User Import Quota │
                    │ (0 API Calls Used)   │      │ (3-5 credits / week)    │
                    └──────────────────────┘      └───────────┬─────────────┘
                                                              │
                                                              ▼
                                                  ┌─────────────────────────┐
                                                  │ Extract Video Metadata  │
                                                  │ (Transcript/Timestamps) │
                                                  └───────────┬─────────────┘
                                                              │
                                                              ▼
                                                  ┌─────────────────────────┐
                                                  │ Google Gemini Flash API │
                                                  │ (Strict JSON Schema)    │
                                                  └───────────┬─────────────┘
                                                              │
                                                              ▼
                                                  ┌─────────────────────────┐
                                                  │ Human-in-the-Loop Modal │
                                                  │ (User Edits & Confirms) │
                                                  └───────────┬─────────────┘
                                                              │
                                                              ▼
                                                  ┌─────────────────────────┐
                                                  │ Persist to Local Room   │
                                                  │ & Global Verified DB    │
                                                  └─────────────────────────┘
```

### 4.1 Ingestion Sources
1. **Curated Creator Hub:** In-app directory of verified playlists from respected creators (Jeff Nippard, Renaissance Periodization, Athlean-X, Squat University).
2. **Custom URL Ingestion:** User pastes any valid YouTube video link.

### 4.2 Gemini Flash Strict JSON Schema Output Contract
The API is invoked with `response_mime_type="application/json"` enforcing this structured schema:

```json
{
  "workout_title": "Upper Body Push Hypertrophy",
  "creator_name": "Jeff Nippard",
  "estimated_duration_minutes": 55,
  "exercises": [
    {
      "canonical_name": "Incline Dumbbell Press",
      "target_sub_muscle": "Upper Chest",
      "prescribed_sets": 3,
      "prescribed_reps_min": 8,
      "prescribed_reps_max": 10,
      "rest_seconds": 120,
      "execution_notes": "30-degree incline, 2-second eccentric pause at chest"
    },
    {
      "canonical_name": "Cable Lateral Raise",
      "target_sub_muscle": "Side Delts",
      "prescribed_sets": 3,
      "prescribed_reps_min": 12,
      "prescribed_reps_max": 15,
      "rest_seconds": 90,
      "execution_notes": "Set pulley to wrist height at bottom of range"
    }
  ]
}
```

### 4.3 Human-in-the-Loop (HITL) Verification Modal
* Upon parsing, the user is presented with a **"Review Parsed Workout"** card displaying the video thumbnail, extracted exercise list, target sub-muscles, and set/rep targets.
* The user can adjust values with 1 tap, delete misidentified exercises, or add an exercise.
* Tapping **"Confirm & Save"** writes the verified workout to their local library and marks it as verified in the global cloud database.

### 4.4 API Protection & Quota Safeguards
1. **Global Deduplication Cache:** If any user has previously parsed a YouTube video ID, subsequent requests return the verified database entry instantly—consuming zero AI API tokens.
2. **Weekly Import Quotas:** Free users receive **3 to 5 AI Video Import credits per week**.
3. **Gamified Credit Earn Loop:** Completing 3 logged workouts in a week grants **+1 Bonus Import Credit**, reinforcing gym consistency.
4. **Rate Throttling:** Maximum 1 AI video import per user every 10 minutes to prevent abuse.

---

## 5. Routine Creation & Modification Workflows

### 5.1 Guided Rapid Template (Form-Based Creation)
For non-video routine creation, a structured 4-step wizard builds workouts in under 60 seconds:
1. **Focus Selection:** Multi-select target sub-muscles (e.g., `[Upper Chest]`, `[Side Delts]`, `[Triceps]`).
2. **Speed Picker:** Exercise selector pre-filtered to the chosen sub-muscles with auto-suggestions for balanced volume.
3. **Prescribed Volume:** Quick stepper counters for sets (1–6), rep ranges (6–10, 8–12, 12–15), and rest intervals (60s, 90s, 120s, 180s).
4. **Real-Time Projection:** Live calculation displaying estimated session duration, calorie burn range, and sub-muscle distribution.

### 5.2 Mid-Workout Exercise Swapping
When equipment is occupied during an active session:
* User taps **"Swap Exercise ⇄"** on any exercise card.
* **Tier 1 Suggestions (Default):** 1-tap alternatives targeting the **exact same sub-muscle** (e.g., Incline Dumbbell Press -> Incline Machine Press or Low-to-High Cable Fly).
* **Tier 2 Search:** Option to search the full canonical dictionary to pivot to any muscle group.

### 5.3 Post-Workout Routine Sync & Diff Resolver
When the user taps **"Finish Workout"**, RepForge analyzes session diffs:
* If exercises were swapped, added, or sets modified, a modal prompts:
  * **Option 1: Update Base Routine:** Overwrites the original template with today's modifications.
  * **Option 2: Save as New Variation:** Creates a new routine (e.g., "Chest & Delts - Machine Variation").
  * **Option 3: Log for Today Only:** Retains modifications in session history while leaving the master routine untouched.

---

## 6. In-Session Gym UX & Hardware/Sensor Integration

### 6.1 Screen Hierarchy & Ergonomics
* **Optimized for In-Gym Reality:** Sweaty hands, chalk, and high physical fatigue.
* **Large Touch Targets:** Minimum 48 x 48 dp touch targets for all interactive elements (set completion checks, stepper adjustments).
* **Tabular Numeric Figures:** Fixed-width numeric typography to eliminate layout shifting during timer countdowns.
* **High-Contrast Dark Theme:** Carbon Slate (`#0B0D10`) background with high-visibility accent colors (Forge Amber `#FF6600` or Kinetic Lime `#D4FF00`).

### 6.2 Progressive Overload Ghost Text & Trend Status
* Each set row displays the previous session's performance in faint ghost text:
  * `Previous: 70 kg × 8 reps`
* Automated rolling 3-session performance indicator:
  * **Progressing 🟢:** Increasing weight, reps, or total volume load.
  * **Plateauing 🟡:** Static volume load across 3 consecutive sessions.
  * **Declining 🔴:** Reduced volume load or missed rep targets.

### 6.3 Auto-Rest Timers & Haptic Signaling
* Tapping the **`[✓]` Done** button on a set automatically triggers the rest countdown bar at the bottom of the screen.
* Quick adjustment controls: `+30s`, `-15s`, and `Skip`.
* **Zero Audio Clutter:** No intrusive voice or sound effects that interrupt music/podcasts.
* **Distinct Haptic Pattern:** A dual-pulse vibration pattern triggers when the timer reaches 0:00, easily felt in a pocket without looking at the screen.

### 6.4 Barbell Plate Math Helper
* A 1-tap calculator icon next to any barbell exercise.
* Automatically deducts the standard **20kg Olympic barbell** and calculates required plates per side:
  * Available plates: 25 kg, 20 kg, 15 kg, 10 kg, 5 kg, 2.5 kg, 1.25 kg.
  * *Example:* 100 kg total load -> Displays: `[Bar 20kg] + [40kg per side: 1×20kg, 1×15kg, 1×5kg]`.

---

## 7. Bio-Energetic & Caloric Engine

### 7.1 Calorie Confidence Range Model
To avoid false precision, caloric expenditure is presented as a confidence interval:
`Estimated Calorie Range = Calculated Calories ± 5%`

### 7.2 Dynamic Calculation Algorithm
The engine synthesizes three bio-energetic drivers:
1. **Basal Active Metabolic Rate:** `BMR_minute * MET_resistance * Active Time` (MET ranges from 3.5 for light isolation to 6.0 for heavy compound lifts).
2. **Mechanical Volume Load (Tonnage Work):** `Tonnage = Sum(Weight_kg * Reps)`. Energy expenditure scaled by mechanical work and user body weight ratio.
3. **EPOC (Excess Post-Exercise Oxygen Consumption / "Afterburn"):** Elevated for sets marked `DROP_SET` or `FAILURE`, and short rest intervals (< 60s).

### 7.3 Real-Time & Post-Workout Summary
* **During Workout:** Live dial increments as sets and tonnage are logged.
* **Post-Workout Screen:**
  * Finalized Calorie Confidence Range (e.g., 380 ± 20 kcal).
  * Total Tonnage Moved (e.g., 12,450 kg).
  * Sub-Muscle Distribution Chart (e.g., 45% Upper Chest, 35% Side Delts, 20% Triceps).
  * Personal Record (PR) Trophy Banner celebrating beaten historical targets.

---

## 8. Android System & Google Health Connect Integration

### 8.1 Architecture & Local Data Flow
Google Health Connect serves as the on-device health hub. It functions completely offline without internet connectivity.

```
┌────────────────────────────────────────────────────────┐
│                  RepForge Android App                  │
├──────────────────────────┬─────────────────────────────┤
│  Local Room SQLite DB    │  Health Connect Client SDK  │
└────────────┬─────────────┴──────────────┬──────────────┘
             │                            │
             ▼                            ▼
   [ Android WorkManager ]     [ Health Connect On-Device ]
             │                            │
             ▼ (When Online)              ▼ (Local Sync)
   [ Cloud Backup DB ]         [ Samsung Health / Pixel ]
```

### 8.2 Android Manifest Declarations
```xml
<manifest ...>
    <queries>
        <package android:name="com.google.android.apps.healthdata" />
    </queries>

    <!-- Permissions: Write Workouts & Calories -->
    <uses-permission android:name="androidx.health.permission.WRITE_EXERCISE" />
    <uses-permission android:name="androidx.health.permission.WRITE_TOTAL_CALORIES_BURNED" />

    <!-- Permissions: Read Biometrics -->
    <uses-permission android:name="androidx.health.permission.READ_WEIGHT" />
    <uses-permission android:name="androidx.health.permission.READ_HEART_RATE" />
</manifest>
```

### 8.3 Records Written & Read
* **Exported on Workout Completion (WRITE):**
  * `ExerciseSessionRecord`:
    * `exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING`
    * `title = session.routineTitle`
    * `startTime = session.startedAt`, `endTime = session.completedAt`
  * `TotalCaloriesBurnedRecord`:
    * `energy = Energy.kilocalories(session.calculatedCalories)`
* **Imported Biometrics (READ):**
  * `WeightRecord`: Automatically keeps the user's `weight_kg` updated from smart scales or health apps without manual input.
  * `HeartRateRecord` (Optional): Reads real-time heart rate samples during the session window if a smartwatch was worn, refining the caloric confidence range.

---

## 9. Information Architecture & Navigation

RepForge uses a clean 5-tab bottom navigation structure:

```
┌────────────────────────────────────────────────────────────────────────┐
│                                REPFORGE                                │
├────────────┬──────────────┬──────────────┬──────────────┬──────────────┤
│   TODAY    │   ROUTINES   │   HEATMAP    │   HISTORY    │   PROFILE    │
│ (Session)  │ & DISCOVERY  │ & ANALYTICS  │  (Logbook)   │ (Biometrics) │
└────────────┴──────────────┴──────────────┴──────────────┴──────────────┘
```

1. **Today / Active Session:** Active workout card, start empty workout button, last workout summary, quick plate math widget.
2. **Routines & Discovery:** Personal routines, Guided Rapid Template creator, Curated YouTube Hub, URL import bar.
3. **Sub-Muscle Heatmap & Analytics:** Interactive 2D vector body map (front & rear views) showing weekly volume and recovery status per sub-muscle; Progressive Overload progression radar.
4. **History & Logbook:** Chronological workout feed, calendar streak heatmap, PR Trophy Room.
5. **Profile & Biometrics:** Weight tracker chart, height, age, biological sex, Health Connect sync status, unit preferences.

---

## 10. Product Roadmap & Phased Execution

### Phase 1: MVP Core (Current Scope)
* Native Android app with Room local database.
* Curated YouTube Hub & custom URL ingestion via Gemini Flash.
* Human-in-the-Loop verification flow & Global Video Cache.
* Guided Rapid Template builder.
* In-gym active session tracking (ghost text, set categorizer, haptic rest timer, plate math).
* Mid-workout exercise swapping & post-workout Diff Resolver.
* Dynamic Calorie Confidence Range.
* Interactive 2D Sub-Muscle Heatmap.
* Bidirectional Google Health Connect integration.

### Phase 2: WearOS Companion (Documented Future Work)
* Dedicated WearOS application.
* Real-time heart rate sampling and set checking from the wrist.
* Autonomous workout recording on watch with phone sync.

### Phase 3: Social & Community Features (Documented Future Work)
* Routine sharing links and QR codes for gym partners.
* Exportable PR highlight cards for Instagram Stories / social media.
```