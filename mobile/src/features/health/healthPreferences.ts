import AsyncStorage from '@react-native-async-storage/async-storage';

const SYNC_ENABLED_KEY = 'repforge.health.syncEnabled';
const LAST_SYNC_KEY = 'repforge.health.lastSyncedAtMillis';

export const healthPreferences = {
  async isSyncEnabled(): Promise<boolean> {
    const value = await AsyncStorage.getItem(SYNC_ENABLED_KEY);
    // Default on once the user has granted permissions; unset means "not opted in yet".
    return value === '1';
  },

  async setSyncEnabled(enabled: boolean): Promise<void> {
    await AsyncStorage.setItem(SYNC_ENABLED_KEY, enabled ? '1' : '0');
  },

  async getLastSyncedAtMillis(): Promise<number | null> {
    const raw = await AsyncStorage.getItem(LAST_SYNC_KEY);
    if (!raw) return null;
    const n = Number(raw);
    return Number.isFinite(n) ? n : null;
  },

  async setLastSyncedAtMillis(millis: number): Promise<void> {
    await AsyncStorage.setItem(LAST_SYNC_KEY, String(millis));
  },
};
