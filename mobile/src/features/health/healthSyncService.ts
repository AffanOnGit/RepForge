import { database } from '@/src/data/database';
import { healthConnectManager } from './HealthConnectManager';
import { healthPreferences } from './healthPreferences';
import type {
  WorkoutExportResult,
  WorkoutHealthExportInput,
  WeightSyncResult,
} from './types';

/**
 * Sync orchestration:
 * - Pull: latest Health Connect weight → active local profile biometrics
 * - Push: completed workout session + estimated kcal → Health Connect
 */
export const healthSyncService = {
  async pullWeightIntoActiveProfile(): Promise<WeightSyncResult> {
    const available = await healthConnectManager.isAvailable();
    if (!available) {
      return {
        synced: false,
        weightKg: null,
        message: healthConnectManager.availabilityLabel(
          await healthConnectManager.getAvailability()
        ),
      };
    }

    const syncEnabled = await healthPreferences.isSyncEnabled();
    if (!syncEnabled) {
      return {
        synced: false,
        weightKg: null,
        message: 'Health Connect sync is turned off in Profile.',
      };
    }

    const hasPerms = await healthConnectManager.hasAllPermissions();
    if (!hasPerms) {
      return {
        synced: false,
        weightKg: null,
        message: 'Health Connect permissions required.',
      };
    }

    const weightKg = await healthConnectManager.readLatestWeightKg();
    if (weightKg == null || !Number.isFinite(weightKg) || weightKg <= 0) {
      return {
        synced: false,
        weightKg: null,
        message: 'No recent weight found in Health Connect (last 30 days).',
      };
    }

    const profile = await database.getActiveProfile();
    if (!profile) {
      return {
        synced: false,
        weightKg,
        message: 'No local profile yet — finish onboarding first.',
      };
    }

    await database.upsertProfile({ ...profile, weightKg });
    await healthPreferences.setLastSyncedAtMillis(Date.now());
    return {
      synced: true,
      weightKg,
      message: `Synced weight ${weightKg.toFixed(1)} kg into “${profile.displayName}”.`,
    };
  },

  async exportCompletedWorkout(
    input: WorkoutHealthExportInput
  ): Promise<WorkoutExportResult> {
    try {
      const available = await healthConnectManager.isAvailable();
      if (!available) {
        return {
          exported: false,
          message: healthConnectManager.availabilityLabel(
            await healthConnectManager.getAvailability()
          ),
        };
      }

      const syncEnabled = await healthPreferences.isSyncEnabled();
      if (!syncEnabled) {
        return {
          exported: false,
          message: 'Health Connect sync is turned off.',
        };
      }

      const ok = await healthConnectManager.writeWorkoutSession(input);
      if (ok) {
        await healthPreferences.setLastSyncedAtMillis(Date.now());
        return {
          exported: true,
          message: 'Synced strength session & calories to Health Connect.',
        };
      }
      return {
        exported: false,
        message: 'Could not write workout to Health Connect.',
      };
    } catch {
      return {
        exported: false,
        message: 'Health Connect export skipped (unavailable).',
      };
    }
  },

  async connectAndEnable(): Promise<{ ok: boolean; message: string }> {
    const availability = await healthConnectManager.getAvailability();
    if (availability !== 'available') {
      return {
        ok: false,
        message: healthConnectManager.availabilityLabel(availability),
      };
    }
    const granted = await healthConnectManager.requestPermissions();
    if (!granted) {
      return {
        ok: false,
        message: 'Permissions were not granted. You can retry anytime in Profile.',
      };
    }
    await healthPreferences.setSyncEnabled(true);
    const pull = await this.pullWeightIntoActiveProfile();
    return {
      ok: true,
      message: pull.synced
        ? pull.message
        : 'Health Connect connected. Weight sync will run when data is available.',
    };
  },
};
