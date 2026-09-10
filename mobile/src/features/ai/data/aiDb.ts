import * as SQLite from 'expo-sqlite';
import type { ParsedExerciseItem, ParsedWorkoutData } from '../types';

let aiDbPromise: Promise<SQLite.SQLiteDatabase> | null = null;

/**
 * Isolated AI tables in the same SQLite file — avoids editing core bootstrap migrations.
 */
async function getAiDb() {
  if (!aiDbPromise) {
    aiDbPromise = (async () => {
      const db = await SQLite.openDatabaseAsync('repforge.db');
      await db.execAsync(`
        PRAGMA foreign_keys = ON;

        CREATE TABLE IF NOT EXISTS ai_import_quota (
          profile_id TEXT NOT NULL,
          week_key TEXT NOT NULL,
          used_count INTEGER NOT NULL DEFAULT 0,
          last_import_at_millis INTEGER,
          PRIMARY KEY (profile_id, week_key)
        );

        CREATE TABLE IF NOT EXISTS ai_video_cache (
          video_id TEXT PRIMARY KEY NOT NULL,
          payload_json TEXT NOT NULL,
          verified INTEGER NOT NULL DEFAULT 0,
          updated_at_millis INTEGER NOT NULL
        );

        CREATE TABLE IF NOT EXISTS ai_ingested_routines (
          routine_id TEXT PRIMARY KEY NOT NULL,
          profile_id TEXT NOT NULL,
          video_id TEXT,
          source TEXT NOT NULL,
          parse_mode TEXT NOT NULL,
          ingested_at_millis INTEGER NOT NULL
        );
      `);
      return db;
    })();
  }
  return aiDbPromise;
}

export function currentWeekKey(now = Date.now()): string {
  const d = new Date(now);
  // ISO week-ish: year + week number (Mon-based)
  const tmp = new Date(Date.UTC(d.getFullYear(), d.getMonth(), d.getDate()));
  const dayNum = tmp.getUTCDay() || 7;
  tmp.setUTCDate(tmp.getUTCDate() + 4 - dayNum);
  const yearStart = new Date(Date.UTC(tmp.getUTCFullYear(), 0, 1));
  const weekNo = Math.ceil(((tmp.getTime() - yearStart.getTime()) / 86400000 + 1) / 7);
  return `${tmp.getUTCFullYear()}-W${String(weekNo).padStart(2, '0')}`;
}

export const aiDb = {
  async ensureReady() {
    await getAiDb();
  },

  async getQuotaRow(profileId: string, weekKey: string) {
    const db = await getAiDb();
    return db.getFirstAsync<{
      used_count: number;
      last_import_at_millis: number | null;
    }>(
      `SELECT used_count, last_import_at_millis FROM ai_import_quota
       WHERE profile_id = ? AND week_key = ?`,
      profileId,
      weekKey
    );
  },

  async recordImport(profileId: string, weekKey: string, atMillis: number) {
    const db = await getAiDb();
    const existing = await this.getQuotaRow(profileId, weekKey);
    if (existing) {
      await db.runAsync(
        `UPDATE ai_import_quota SET used_count = used_count + 1, last_import_at_millis = ?
         WHERE profile_id = ? AND week_key = ?`,
        atMillis,
        profileId,
        weekKey
      );
    } else {
      await db.runAsync(
        `INSERT INTO ai_import_quota (profile_id, week_key, used_count, last_import_at_millis)
         VALUES (?, ?, 1, ?)`,
        profileId,
        weekKey,
        atMillis
      );
    }
  },

  async getCachedVideo(videoId: string): Promise<ParsedWorkoutData | null> {
    const db = await getAiDb();
    const row = await db.getFirstAsync<{ payload_json: string }>(
      `SELECT payload_json FROM ai_video_cache WHERE video_id = ?`,
      videoId
    );
    if (!row) return null;
    try {
      const parsed = JSON.parse(row.payload_json) as ParsedWorkoutData;
      return { ...parsed, isFromGlobalCache: true, usedAiCredits: false, parseMode: 'cache' };
    } catch {
      return null;
    }
  },

  async putCachedVideo(data: ParsedWorkoutData, verified = false) {
    if (!data.videoId) return;
    const db = await getAiDb();
    const payload: ParsedWorkoutData = {
      ...data,
      isFromGlobalCache: true,
      usedAiCredits: false,
      parseMode: 'cache',
    };
    await db.runAsync(
      `INSERT OR REPLACE INTO ai_video_cache (video_id, payload_json, verified, updated_at_millis)
       VALUES (?, ?, ?, ?)`,
      data.videoId,
      JSON.stringify(payload),
      verified ? 1 : 0,
      Date.now()
    );
  },

  async markVerified(videoId: string) {
    const db = await getAiDb();
    await db.runAsync(
      `UPDATE ai_video_cache SET verified = 1, updated_at_millis = ? WHERE video_id = ?`,
      Date.now(),
      videoId
    );
  },

  async linkIngestedRoutine(args: {
    routineId: string;
    profileId: string;
    videoId: string | null;
    source: string;
    parseMode: string;
  }) {
    const db = await getAiDb();
    await db.runAsync(
      `INSERT OR REPLACE INTO ai_ingested_routines
        (routine_id, profile_id, video_id, source, parse_mode, ingested_at_millis)
       VALUES (?, ?, ?, ?, ?, ?)`,
      args.routineId,
      args.profileId,
      args.videoId,
      args.source,
      args.parseMode,
      Date.now()
    );
  },
};

export type CachedSeedWorkout = {
  videoId: string;
  videoTitle: string;
  creatorName: string;
  exercises: Omit<ParsedExerciseItem, 'matchedExerciseId' | 'matchConfidence'>[];
};
