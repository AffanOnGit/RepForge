import type { Permission } from 'react-native-health-connect';

/** Parity with Android `HealthConnectManager.permissions`. */
export const HEALTH_CONNECT_PERMISSIONS: Permission[] = [
  { accessType: 'write', recordType: 'ExerciseSession' },
  { accessType: 'write', recordType: 'TotalCaloriesBurned' },
  { accessType: 'read', recordType: 'Weight' },
  { accessType: 'read', recordType: 'HeartRate' },
];

export const ANDROID_HEALTH_PERMISSIONS = [
  'android.permission.health.WRITE_EXERCISE',
  'android.permission.health.WRITE_TOTAL_CALORIES_BURNED',
  'android.permission.health.READ_WEIGHT',
  'android.permission.health.READ_HEART_RATE',
] as const;
