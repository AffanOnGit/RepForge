# Health Connect (Android) — Expo setup

RepForge syncs with Google Health Connect via [`react-native-health-connect`](https://www.npmjs.com/package/react-native-health-connect) **v4+** (config plugin included; do **not** install deprecated `expo-health-connect`).

## Sync directions

| Direction | Data | When |
|-----------|------|------|
| **HC → RepForge** | Latest body weight (kg) | On Connect / Sync Now → writes `user_profile.weight_kg` |
| **RepForge → HC** | `ExerciseSession` (strength) + `TotalCaloriesBurned` | After a completed workout when sync is enabled |
| — | Heart rate | Permission reserved for future caloric blend; not written |

## Permissions

Manifest (also declared in `app.json` → `android.permissions`):

- `android.permission.health.WRITE_EXERCISE`
- `android.permission.health.WRITE_TOTAL_CALORIES_BURNED`
- `android.permission.health.READ_WEIGHT`
- `android.permission.health.READ_HEART_RATE`

Runtime requests match Android `HealthConnectManager`.

## Dependencies

```bash
cd mobile
npx expo install react-native-health-connect expo-build-properties
```

Already wired in `app.json` plugins:

- `react-native-health-connect` — rationale intent + Android 14 permission-usage alias
- `./plugins/withHealthConnectQueries.js` — `<queries>` for `com.google.android.apps.healthdata`
- `expo-build-properties` — `minSdkVersion: 26` (Health Connect requirement)

## Build (required for real sync)

Health Connect **does not run in Expo Go**. Use a custom dev client:

```bash
cd mobile
npx expo prebuild --platform android
npx expo run:android
# or: eas build --profile development --platform android
```

`npx expo start` still works: the feature module lazy-loads the native bridge and **fails soft** when unlinked.

## Limitations

- Android only (iOS shows an explanatory no-op).
- Expo Go / web: UI visible; connect/sync no-op with a clear message.
- Play Console health-data declaration required before production store listing.
- Turning sync off in Profile only stops RepForge sync calls; revoke access in the Health Connect system UI if needed.
- CaloricEngine is unchanged — session export uses its existing estimate midpoint (avg of low/high bounds).

## Privacy rationale

Onboarding + Profile explain: weight is read to keep Mifflin-St Jeor accurate; strength sessions and estimated kcal are written after workouts; data stays on-device.
