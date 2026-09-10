import { database } from '@/src/data/database';
import type { WorkoutSet } from '@/src/domain/types';
import {
  EXPORT_SCHEMA_VERSION,
  type ExportBundle,
  type RepForgeBackupPayload,
  type SessionWithSets,
} from './types';

function toLocalDate(millis: number | null | undefined): string {
  if (!millis || millis <= 0) return '';
  const d = new Date(millis);
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

function durationMinutes(
  startedAtMillis: number | null,
  completedAtMillis: number | null
): number {
  if (startedAtMillis == null || completedAtMillis == null) return 0;
  return Math.max(0, Math.floor((completedAtMillis - startedAtMillis) / 60000));
}

/** Escape a CSV field; commas / quotes / newlines are quoted RFC4180-style. */
function csvField(value: string | number | boolean | null | undefined): string {
  if (value == null) return '';
  const raw = String(value);
  if (/[",\n\r]/.test(raw)) {
    return `"${raw.replace(/"/g, '""')}"`;
  }
  return raw;
}

function csvRow(fields: Array<string | number | boolean | null | undefined>): string {
  return fields.map(csvField).join(',');
}

/**
 * Android HistoryViewModel.exportWorkoutsAsCsv parity (session summary),
 * plus ProfileId for multi-profile readiness.
 */
export function buildSessionsCsv(
  profileId: string,
  sessions: SessionWithSets[]
): string {
  const lines = [
    csvRow([
      'ProfileId',
      'SessionId',
      'Date',
      'RoutineName',
      'DurationMinutes',
      'TotalTonnageKg',
      'CaloriesLow',
      'CaloriesHigh',
      'Notes',
    ]),
  ];

  for (const s of sessions) {
    const started = s.startedAtMillis ?? s.completedAtMillis ?? 0;
    lines.push(
      csvRow([
        profileId,
        s.id,
        toLocalDate(started),
        s.routineName.replace(/,/g, ';'),
        durationMinutes(s.startedAtMillis, s.completedAtMillis),
        s.totalTonnageKg,
        s.estimatedCaloriesLow,
        s.estimatedCaloriesHigh,
        s.notes.replace(/,/g, ';').replace(/\n/g, ' '),
      ])
    );
  }

  return lines.join('\n') + '\n';
}

/**
 * Denormalized workout log — one row per set (or one empty-set row per session).
 */
export function buildSetsCsv(
  profileId: string,
  sessions: SessionWithSets[],
  exerciseNameById: Map<string, string>
): string {
  const lines = [
    csvRow([
      'ProfileId',
      'SessionId',
      'Date',
      'RoutineName',
      'DurationMinutes',
      'TotalTonnageKg',
      'CaloriesLow',
      'CaloriesHigh',
      'SessionNotes',
      'ExerciseId',
      'ExerciseName',
      'SetNumber',
      'SetType',
      'WeightKg',
      'RepsCompleted',
      'TargetReps',
      'Rpe',
      'IsCompleted',
      'CompletedAtMillis',
    ]),
  ];

  for (const s of sessions) {
    const started = s.startedAtMillis ?? s.completedAtMillis ?? 0;
    const date = toLocalDate(started);
    const duration = durationMinutes(s.startedAtMillis, s.completedAtMillis);
    const sets: Array<WorkoutSet | null> =
      s.sets.length > 0 ? s.sets : [null];

    for (const set of sets) {
      lines.push(
        csvRow([
          profileId,
          s.id,
          date,
          s.routineName,
          duration,
          s.totalTonnageKg,
          s.estimatedCaloriesLow,
          s.estimatedCaloriesHigh,
          s.notes,
          set?.exerciseId ?? '',
          set ? exerciseNameById.get(set.exerciseId) ?? '' : '',
          set?.setNumber ?? '',
          set?.setType ?? '',
          set?.weightKg ?? '',
          set?.repsCompleted ?? '',
          set?.targetReps ?? '',
          set?.rpe ?? '',
          set == null ? '' : set.isCompleted ? 1 : 0,
          set?.completedAtMillis ?? '',
        ])
      );
    }
  }

  return lines.join('\n') + '\n';
}

export function buildBackupJson(payload: RepForgeBackupPayload): string {
  return JSON.stringify(payload, null, 2);
}

/**
 * Loads active-profile local data via existing database APIs and builds
 * JSON + CSV export strings. Database getters already scope by active profileId.
 */
export async function buildExportBundle(
  profileIdOverride?: string
): Promise<ExportBundle> {
  const [profile, exercises, routines, sessions, personalRecords, activeId] =
    await Promise.all([
      database.getActiveProfile(),
      database.getAllExercises(),
      database.getAllRoutines(),
      database.getAllSessions(),
      database.getAllPersonalRecords(),
      database.getActiveProfileId(),
    ]);

  // DB getters already scope by active profile — label must match that scope
  const profileId =
    activeId ?? profile?.id ?? profileIdOverride ?? 'unknown';

  const sessionsWithSets: SessionWithSets[] = [];
  let setCount = 0;
  for (const session of sessions) {
    const sets = await database.getSetsForSession(session.id);
    setCount += sets.length;
    sessionsWithSets.push({ ...session, sets });
  }

  const exerciseNameById = new Map(exercises.map((e) => [e.id, e.name]));

  const payload: RepForgeBackupPayload = {
    schemaVersion: EXPORT_SCHEMA_VERSION,
    format: 'repforge-backup',
    exportedAt: new Date().toISOString(),
    profileId,
    profile,
    exercises,
    routines,
    sessions: sessionsWithSets,
    personalRecords,
  };

  return {
    profileId,
    sessionCount: sessionsWithSets.length,
    setCount,
    json: buildBackupJson(payload),
    sessionsCsv: buildSessionsCsv(profileId, sessionsWithSets),
    setsCsv: buildSetsCsv(profileId, sessionsWithSets, exerciseNameById),
  };
}
