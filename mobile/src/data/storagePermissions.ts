import { Platform } from 'react-native';
import * as FileSystem from 'expo-file-system/legacy';
import { database } from './database';

/**
 * App-private SQLite (expo-sqlite) does not need runtime storage permission.
 * User-visible “cloud-like” backup/export uses SAF via a user-picked directory.
 * This module owns consent + optional backup folder URI for export agents.
 */

export type StorageConsentState = {
  granted: boolean;
  grantedAtMillis: number | null;
  backupDirectoryUri: string | null;
};

export async function getStorageConsent(): Promise<StorageConsentState> {
  return database.getStorageConsent();
}

export async function grantAppDataConsent(): Promise<void> {
  await database.setStorageConsent(true);
}

export async function revokeAppDataConsent(): Promise<void> {
  await database.setStorageConsent(false);
}

/**
 * Opens Android Storage Access Framework so the user picks a folder
 * for future backup/export. Export feature reads this URI on “Save to folder”.
 */
export async function pickBackupDirectory(): Promise<string | null> {
  if (Platform.OS !== 'android') {
    return null;
  }
  const permissions =
    await FileSystem.StorageAccessFramework.requestDirectoryPermissionsAsync();
  if (!permissions.granted) return null;
  await database.setBackupDirectoryUri(permissions.directoryUri);
  return permissions.directoryUri;
}

export const STORAGE_CONSENT_COPY = {
  title: 'App data on this device',
  body:
    'RepForge stores your profiles, workouts, routines, and biometrics in a private database on this phone — like a personal cloud that never leaves the device.',
  why:
    'We ask for your OK before creating profiles so you know training history lives locally. Optional backup folders (Android Storage Access Framework) let you choose where exports go later — no cloud account required.',
  sandboxNote:
    'Core logging works in the app sandbox (no system storage permission). Choosing a backup folder is only needed when you want user-controlled export destinations.',
} as const;
