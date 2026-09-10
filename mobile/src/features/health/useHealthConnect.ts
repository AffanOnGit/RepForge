import { useCallback, useEffect, useState } from 'react';
import { Platform } from 'react-native';
import { healthConnectManager } from './HealthConnectManager';
import { healthPreferences } from './healthPreferences';
import { healthSyncService } from './healthSyncService';
import type { HealthConnectStatus } from './types';

const initialStatus: HealthConnectStatus = {
  availability: Platform.OS === 'android' ? 'module_unavailable' : 'unsupported_platform',
  initialized: false,
  hasPermissions: false,
  syncEnabled: false,
  lastSyncedAtMillis: null,
  lastMessage: null,
};

export function useHealthConnect() {
  const [status, setStatus] = useState<HealthConnectStatus>(initialStatus);
  const [busy, setBusy] = useState(false);

  const refresh = useCallback(async () => {
    const availability = await healthConnectManager.getAvailability();
    const syncEnabled = await healthPreferences.isSyncEnabled();
    const lastSyncedAtMillis = await healthPreferences.getLastSyncedAtMillis();
    let hasPermissions = false;
    let initialized = false;
    if (availability === 'available') {
      initialized = await healthConnectManager.ensureInitialized();
      hasPermissions = initialized ? await healthConnectManager.hasAllPermissions() : false;
    }
    setStatus({
      availability,
      initialized,
      hasPermissions,
      syncEnabled,
      lastSyncedAtMillis,
      lastMessage: null,
    });
  }, []);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const connect = useCallback(async () => {
    setBusy(true);
    const result = await healthSyncService.connectAndEnable();
    await refresh();
    setStatus((s) => ({ ...s, lastMessage: result.message }));
    setBusy(false);
    return result;
  }, [refresh]);

  const setSyncEnabled = useCallback(
    async (enabled: boolean) => {
      setBusy(true);
      await healthPreferences.setSyncEnabled(enabled);
      if (enabled) {
        const has = await healthConnectManager.hasAllPermissions();
        if (!has) {
          const result = await healthSyncService.connectAndEnable();
          await refresh();
          setStatus((s) => ({ ...s, lastMessage: result.message }));
          setBusy(false);
          return;
        }
      }
      await refresh();
      setStatus((s) => ({
        ...s,
        lastMessage: enabled
          ? 'Health Connect sync enabled.'
          : 'Health Connect sync paused in RepForge (local data unchanged).',
      }));
      setBusy(false);
    },
    [refresh]
  );

  const syncNow = useCallback(async () => {
    setBusy(true);
    const result = await healthSyncService.pullWeightIntoActiveProfile();
    await refresh();
    setStatus((s) => ({ ...s, lastMessage: result.message }));
    setBusy(false);
    return result;
  }, [refresh]);

  const openSettings = useCallback(() => {
    healthConnectManager.openSettings();
  }, []);

  return {
    status,
    busy,
    refresh,
    connect,
    setSyncEnabled,
    syncNow,
    openSettings,
  };
}
