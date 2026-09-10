import { Platform } from 'react-native';
import * as FileSystem from 'expo-file-system/legacy';
import * as Sharing from 'expo-sharing';
import type { ExportDelivery, ExportFormat, ExportResult } from './types';
import { buildExportBundle } from './buildExport';

function stamp(): string {
  const d = new Date();
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  const h = String(d.getHours()).padStart(2, '0');
  const min = String(d.getMinutes()).padStart(2, '0');
  return `${y}${m}${day}_${h}${min}`;
}

function fileBase(format: ExportFormat, variant?: 'sessions' | 'sets'): string {
  if (format === 'json') return `repforge_backup_${stamp()}`;
  if (variant === 'sessions') return `repforge_workouts_export_${stamp()}`;
  return `repforge_workout_log_${stamp()}`;
}

function mimeFor(format: ExportFormat): string {
  return format === 'json' ? 'application/json' : 'text/csv';
}

async function writeCacheFile(
  fileName: string,
  contents: string
): Promise<string> {
  const dir = FileSystem.cacheDirectory;
  if (!dir) {
    throw new Error('Cache directory unavailable');
  }
  const uri = `${dir}${fileName}`;
  await FileSystem.writeAsStringAsync(uri, contents, {
    encoding: FileSystem.EncodingType.UTF8,
  });
  return uri;
}

/**
 * Soft-read of storage-agent backup folder URI (app_meta via storagePermissions).
 * Never throws — export works even if storage APIs are mid-merge.
 */
async function tryGetBackupDirectoryUri(): Promise<string | null> {
  try {
    const mod = await import('@/src/data/storagePermissions');
    const consent = await mod.getStorageConsent();
    return consent.backupDirectoryUri;
  } catch {
    return null;
  }
}

async function tryPersistBackupDirectoryUri(uri: string): Promise<void> {
  try {
    const { database } = await import('@/src/data/database');
    await database.setBackupDirectoryUri(uri);
  } catch {
    // Ignore if storage meta write fails.
  }
}

async function shareUri(
  uri: string,
  format: ExportFormat,
  fileName: string
): Promise<ExportResult> {
  const available = await Sharing.isAvailableAsync();
  if (!available) {
    return {
      ok: false,
      message: 'Sharing is not available on this device.',
    };
  }
  await Sharing.shareAsync(uri, {
    mimeType: mimeFor(format),
    dialogTitle: 'Export RepForge Data',
    UTI: format === 'json' ? 'public.json' : 'public.comma-separated-values-text',
  });
  return { ok: true, format, delivery: 'share', fileName, uri };
}

/**
 * Android: write into a user-chosen directory via Storage Access Framework.
 * Uses cached backup folder from storage agent when present; otherwise prompts.
 */
async function saveViaSaf(
  contents: string,
  fileNameWithoutExt: string,
  format: ExportFormat
): Promise<ExportResult> {
  if (Platform.OS !== 'android') {
    return {
      ok: false,
      message: 'Save to folder uses Android Storage Access Framework. Use Share instead.',
    };
  }

  const { StorageAccessFramework } = FileSystem;
  let directoryUri = await tryGetBackupDirectoryUri();

  if (!directoryUri) {
    const permissions =
      await StorageAccessFramework.requestDirectoryPermissionsAsync();
    if (!permissions.granted) {
      return { ok: false, canceled: true, message: 'Folder access canceled.' };
    }
    directoryUri = permissions.directoryUri;
    await tryPersistBackupDirectoryUri(directoryUri);
  }

  const mime = mimeFor(format);
  try {
    const fileUri = await StorageAccessFramework.createFileAsync(
      directoryUri,
      fileNameWithoutExt,
      mime
    );
    await StorageAccessFramework.writeAsStringAsync(fileUri, contents, {
      encoding: FileSystem.EncodingType.UTF8,
    });
    return {
      ok: true,
      format,
      delivery: 'saf',
      fileName: `${fileNameWithoutExt}.${format}`,
      uri: fileUri,
    };
  } catch (err) {
    // Cached URI may be stale — re-prompt once.
    const permissions =
      await StorageAccessFramework.requestDirectoryPermissionsAsync();
    if (!permissions.granted) {
      return { ok: false, canceled: true, message: 'Folder access canceled.' };
    }
    await tryPersistBackupDirectoryUri(permissions.directoryUri);
    try {
      const fileUri = await StorageAccessFramework.createFileAsync(
        permissions.directoryUri,
        fileNameWithoutExt,
        mime
      );
      await StorageAccessFramework.writeAsStringAsync(fileUri, contents, {
        encoding: FileSystem.EncodingType.UTF8,
      });
      return {
        ok: true,
        format,
        delivery: 'saf',
        fileName: `${fileNameWithoutExt}.${format}`,
        uri: fileUri,
      };
    } catch (inner) {
      const message =
        inner instanceof Error ? inner.message : err instanceof Error ? err.message : 'Save failed';
      return { ok: false, message };
    }
  }
}

export type RunExportOptions = {
  format: ExportFormat;
  delivery: ExportDelivery;
  /** CSV variant: denormalized sets log (default) or Android session summary. */
  csvVariant?: 'sets' | 'sessions';
  profileId?: string;
};

export async function runExport(options: RunExportOptions): Promise<ExportResult> {
  try {
    const bundle = await buildExportBundle(options.profileId);
    const format = options.format;

    let contents: string;
    let base: string;
    if (format === 'json') {
      contents = bundle.json;
      base = fileBase('json');
    } else {
      const variant = options.csvVariant ?? 'sets';
      contents = variant === 'sessions' ? bundle.sessionsCsv : bundle.setsCsv;
      base = fileBase('csv', variant);
    }

    const fileName = `${base}.${format}`;

    if (options.delivery === 'saf') {
      return saveViaSaf(contents, base, format);
    }

    const uri = await writeCacheFile(fileName, contents);
    return shareUri(uri, format, fileName);
  } catch (err) {
    return {
      ok: false,
      message: err instanceof Error ? err.message : 'Export failed',
    };
  }
}
