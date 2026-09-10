import { Platform } from 'react-native';

/**
 * Lazy-loads `react-native-health-connect` only on Android.
 * Fail-soft: Expo Go / unlinked builds throw on TurboModule require — we catch and return null
 * so `npx expo start` and non-dev-client runs stay usable.
 */
export type NativeHealthConnectModule = typeof import('react-native-health-connect');

let cached: NativeHealthConnectModule | null | undefined;

export function getNativeHealthConnect(): NativeHealthConnectModule | null {
  if (Platform.OS !== 'android') {
    return null;
  }
  if (cached !== undefined) {
    return cached;
  }
  try {
    // Dynamic require so Metro can still resolve the dep, but missing native code does not crash import graphs.
    // eslint-disable-next-line @typescript-eslint/no-require-imports
    cached = require('react-native-health-connect') as NativeHealthConnectModule;
    return cached;
  } catch {
    cached = null;
    return null;
  }
}

export function isAndroidHealthConnectSupported(): boolean {
  return Platform.OS === 'android';
}
