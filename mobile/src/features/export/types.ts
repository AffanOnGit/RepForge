import type {
  Exercise,
  PersonalRecord,
  Routine,
  UserProfile,
  WorkoutSession,
  WorkoutSet,
} from '@/src/domain/types';

/** Bump when the backup JSON shape changes in a breaking way. */
export const EXPORT_SCHEMA_VERSION = 1;

export type ExportFormat = 'json' | 'csv';

export type ExportDelivery = 'share' | 'saf';

export type SessionWithSets = WorkoutSession & {
  sets: WorkoutSet[];
};

/**
 * Restore-friendly structured dump. Consumers can rehydrate local DB from this
 * shape; in-app restore is not implemented yet.
 */
export type RepForgeBackupPayload = {
  schemaVersion: typeof EXPORT_SCHEMA_VERSION;
  format: 'repforge-backup';
  exportedAt: string;
  /** Active / current user profile id (multi-profile selection not wired yet). */
  profileId: string;
  profile: UserProfile | null;
  exercises: Exercise[];
  routines: Routine[];
  sessions: SessionWithSets[];
  personalRecords: PersonalRecord[];
};

export type ExportBundle = {
  profileId: string;
  sessionCount: number;
  setCount: number;
  json: string;
  /** Android-parity session summary CSV. */
  sessionsCsv: string;
  /** Denormalized workout log (session + set columns). */
  setsCsv: string;
};

export type ExportResult = {
  ok: true;
  format: ExportFormat;
  delivery: ExportDelivery;
  fileName: string;
  uri: string;
} | {
  ok: false;
  canceled?: boolean;
  message: string;
};
