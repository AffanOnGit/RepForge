import { Platform } from 'react-native';
import { HEALTH_CONNECT_PERMISSIONS } from './permissions';
import { getNativeHealthConnect } from './nativeBridge';
import type { HealthConnectAvailability, WorkoutHealthExportInput } from './types';

/**
 * Expo port of Android `core-health` / `HealthConnectManager`.
 * All methods fail soft when Health Connect or the native module is missing.
 */
class HealthConnectManagerImpl {
  private initialized = false;

  availabilityLabel(availability: HealthConnectAvailability): string {
    switch (availability) {
      case 'unsupported_platform':
        return 'Health Connect is Android-only.';
      case 'module_unavailable':
        return 'Native Health Connect module not linked. Use a development build (not Expo Go).';
      case 'sdk_unavailable':
        return 'Health Connect is not installed on this device.';
      case 'update_required':
        return 'Health Connect needs an update before RepForge can sync.';
      case 'available':
        return 'Health Connect is available.';
    }
  }

  async getAvailability(): Promise<HealthConnectAvailability> {
    if (Platform.OS !== 'android') return 'unsupported_platform';
    const native = getNativeHealthConnect();
    if (!native) return 'module_unavailable';
    try {
      const status = await native.getSdkStatus();
      const { SdkAvailabilityStatus } = native;
      if (status === SdkAvailabilityStatus.SDK_AVAILABLE) return 'available';
      if (status === SdkAvailabilityStatus.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
        return 'update_required';
      }
      return 'sdk_unavailable';
    } catch {
      return 'module_unavailable';
    }
  }

  async isAvailable(): Promise<boolean> {
    return (await this.getAvailability()) === 'available';
  }

  async ensureInitialized(): Promise<boolean> {
    if (this.initialized) return true;
    const native = getNativeHealthConnect();
    if (!native) return false;
    if (!(await this.isAvailable())) return false;
    try {
      const ok = await native.initialize();
      this.initialized = !!ok;
      return this.initialized;
    } catch {
      this.initialized = false;
      return false;
    }
  }

  async hasAllPermissions(): Promise<boolean> {
    const native = getNativeHealthConnect();
    if (!native || !(await this.ensureInitialized())) return false;
    try {
      const granted = await native.getGrantedPermissions();
      return HEALTH_CONNECT_PERMISSIONS.every((needed) =>
        granted.some(
          (g) =>
            'accessType' in g &&
            g.accessType === needed.accessType &&
            g.recordType === needed.recordType
        )
      );
    } catch {
      return false;
    }
  }

  async requestPermissions(): Promise<boolean> {
    const native = getNativeHealthConnect();
    if (!native || !(await this.ensureInitialized())) return false;
    try {
      const granted = await native.requestPermission(HEALTH_CONNECT_PERMISSIONS);
      return HEALTH_CONNECT_PERMISSIONS.every((needed) =>
        granted.some(
          (g) =>
            'accessType' in g &&
            g.accessType === needed.accessType &&
            g.recordType === needed.recordType
        )
      );
    } catch {
      return false;
    }
  }

  async readLatestWeightKg(): Promise<number | null> {
    const native = getNativeHealthConnect();
    if (!native || !(await this.ensureInitialized())) return null;
    if (!(await this.hasAllPermissions())) return null;
    try {
      const start = new Date(Date.now() - 30 * 86400 * 1000).toISOString();
      const result = await native.readRecords('Weight', {
        timeRangeFilter: { operator: 'after', startTime: start },
        ascendingOrder: false,
        pageSize: 1,
      });
      const first = result.records[0];
      return first?.weight?.inKilograms ?? null;
    } catch {
      return null;
    }
  }

  async writeWorkoutSession(input: WorkoutHealthExportInput): Promise<boolean> {
    const native = getNativeHealthConnect();
    if (!native || !(await this.ensureInitialized())) return false;
    if (!(await this.hasAllPermissions())) return false;

    const startTime = new Date(input.startTimeMillis).toISOString();
    const endTime = new Date(
      Math.max(input.endTimeMillis, input.startTimeMillis + 60_000)
    ).toISOString();

    try {
      await native.insertRecords([
        {
          recordType: 'ExerciseSession',
          startTime,
          endTime,
          exerciseType: native.ExerciseType.STRENGTH_TRAINING,
          title: input.title,
          metadata: input.sessionId
            ? {
                clientRecordId: input.sessionId,
                clientRecordVersion: 1,
              }
            : undefined,
        },
        {
          recordType: 'TotalCaloriesBurned',
          startTime,
          endTime,
          energy: {
            value: Math.max(0, input.caloriesKcal),
            unit: 'kilocalories',
          },
          metadata: input.sessionId
            ? {
                clientRecordId: `${input.sessionId}-kcal`,
                clientRecordVersion: 1,
              }
            : undefined,
        },
      ]);
      return true;
    } catch {
      return false;
    }
  }

  openSettings(): void {
    const native = getNativeHealthConnect();
    if (!native) return;
    try {
      native.openHealthConnectSettings();
    } catch {
      // no-op
    }
  }
}

export const healthConnectManager = new HealthConnectManagerImpl();
