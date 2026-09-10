import * as SQLite from 'expo-sqlite';
import { EXERCISE_SEED } from './seed/exercises';
import type {
  Exercise,
  PersonalRecord,
  Routine,
  RoutineExercise,
  ExerciseGroup,
  UserProfile,
  WorkoutSession,
  WorkoutSet,
} from '@/src/domain/types';

const SCHEMA_VERSION = 2;
const META = {
  schemaVersion: 'schema_version',
  exercisesSeeded: 'exercises_seeded',
  activeProfileId: 'active_profile_id',
  storageConsent: 'storage_consent',
  storageConsentAt: 'storage_consent_at',
  backupDirectoryUri: 'backup_directory_uri',
  guestMode: 'guest_mode',
  unlockConfigured: 'unlock_configured',
} as const;

let dbPromise: Promise<SQLite.SQLiteDatabase> | null = null;

async function getDb() {
  if (!dbPromise) {
    dbPromise = (async () => {
      const db = await SQLite.openDatabaseAsync('repforge.db');
      await db.execAsync(`
        PRAGMA journal_mode = WAL;
        PRAGMA foreign_keys = ON;
      `);
      await migrate(db);
      await seedExercises(db);
      return db;
    })();
  }
  return dbPromise;
}

async function tableExists(db: SQLite.SQLiteDatabase, name: string): Promise<boolean> {
  const row = await db.getFirstAsync<{ name: string }>(
    `SELECT name FROM sqlite_master WHERE type='table' AND name=?`,
    name
  );
  return !!row;
}

async function columnExists(
  db: SQLite.SQLiteDatabase,
  table: string,
  column: string
): Promise<boolean> {
  const rows = await db.getAllAsync<{ name: string }>(`PRAGMA table_info(${table})`);
  return rows.some((r) => r.name === column);
}

async function ensureColumn(
  db: SQLite.SQLiteDatabase,
  table: string,
  column: string,
  ddl: string
) {
  if (!(await tableExists(db, table))) return;
  if (await columnExists(db, table, column)) return;
  await db.execAsync(`ALTER TABLE ${table} ADD COLUMN ${ddl}`);
}

async function getMeta(db: SQLite.SQLiteDatabase, key: string): Promise<string | null> {
  if (!(await tableExists(db, 'app_meta'))) return null;
  const row = await db.getFirstAsync<{ value: string }>(
    `SELECT value FROM app_meta WHERE key = ?`,
    key
  );
  return row?.value ?? null;
}

async function setMeta(db: SQLite.SQLiteDatabase, key: string, value: string) {
  await db.runAsync(`INSERT OR REPLACE INTO app_meta (key, value) VALUES (?, ?)`, key, value);
}

async function migrate(db: SQLite.SQLiteDatabase) {
  await db.execAsync(`
    CREATE TABLE IF NOT EXISTS app_meta (
      key TEXT PRIMARY KEY NOT NULL,
      value TEXT NOT NULL
    );

    CREATE TABLE IF NOT EXISTS profiles (
      id TEXT PRIMARY KEY NOT NULL,
      display_name TEXT NOT NULL DEFAULT 'Athlete',
      email TEXT NOT NULL DEFAULT '',
      weight_kg REAL,
      height_cm REAL,
      age INTEGER,
      biological_sex TEXT,
      body_fat_percentage REAL,
      training_experience TEXT NOT NULL DEFAULT 'INTERMEDIATE',
      unit_system TEXT NOT NULL DEFAULT 'metric',
      created_at_millis INTEGER NOT NULL,
      updated_at_millis INTEGER NOT NULL
    );

    CREATE TABLE IF NOT EXISTS exercises (
      id TEXT PRIMARY KEY NOT NULL,
      name TEXT NOT NULL,
      equipment TEXT NOT NULL,
      primary_sub_muscle TEXT NOT NULL,
      secondary_sub_muscles TEXT NOT NULL DEFAULT '',
      creator_tags TEXT NOT NULL DEFAULT '',
      is_custom INTEGER NOT NULL DEFAULT 0,
      profile_id TEXT
    );

    CREATE TABLE IF NOT EXISTS routines (
      id TEXT PRIMARY KEY NOT NULL,
      profile_id TEXT NOT NULL DEFAULT '',
      name TEXT NOT NULL,
      description TEXT NOT NULL DEFAULT '',
      estimated_duration_minutes INTEGER,
      creator_name TEXT,
      source_video_id TEXT,
      created_at_millis INTEGER NOT NULL,
      updated_at_millis INTEGER NOT NULL
    );

    CREATE TABLE IF NOT EXISTS exercise_groups (
      id TEXT PRIMARY KEY NOT NULL,
      routine_id TEXT NOT NULL,
      group_type TEXT NOT NULL,
      order_in_routine INTEGER NOT NULL,
      rest_after_group_seconds INTEGER NOT NULL,
      time_cap_seconds INTEGER,
      FOREIGN KEY (routine_id) REFERENCES routines(id) ON DELETE CASCADE
    );

    CREATE TABLE IF NOT EXISTS routine_exercises (
      id TEXT PRIMARY KEY NOT NULL,
      exercise_group_id TEXT NOT NULL,
      exercise_id TEXT NOT NULL,
      order_in_group INTEGER NOT NULL,
      prescribed_sets INTEGER NOT NULL,
      prescribed_reps_min INTEGER NOT NULL,
      prescribed_reps_max INTEGER NOT NULL,
      rest_seconds INTEGER NOT NULL,
      execution_notes TEXT NOT NULL DEFAULT '',
      FOREIGN KEY (exercise_group_id) REFERENCES exercise_groups(id) ON DELETE CASCADE,
      FOREIGN KEY (exercise_id) REFERENCES exercises(id)
    );

    CREATE TABLE IF NOT EXISTS workout_sessions (
      id TEXT PRIMARY KEY NOT NULL,
      profile_id TEXT NOT NULL DEFAULT '',
      routine_id TEXT,
      routine_name TEXT NOT NULL DEFAULT '',
      state TEXT NOT NULL,
      started_at_millis INTEGER,
      completed_at_millis INTEGER,
      total_tonnage_kg REAL NOT NULL DEFAULT 0,
      estimated_calories_low REAL NOT NULL DEFAULT 0,
      estimated_calories_high REAL NOT NULL DEFAULT 0,
      notes TEXT NOT NULL DEFAULT ''
    );

    CREATE TABLE IF NOT EXISTS workout_sets (
      id TEXT PRIMARY KEY NOT NULL,
      session_id TEXT NOT NULL,
      exercise_id TEXT NOT NULL,
      exercise_group_id TEXT,
      set_number INTEGER NOT NULL,
      set_type TEXT NOT NULL,
      weight_kg REAL NOT NULL,
      reps_completed INTEGER NOT NULL,
      target_reps INTEGER,
      is_completed INTEGER NOT NULL DEFAULT 0,
      rpe REAL,
      completed_at_millis INTEGER,
      FOREIGN KEY (session_id) REFERENCES workout_sessions(id) ON DELETE CASCADE
    );

    CREATE TABLE IF NOT EXISTS personal_records (
      id TEXT PRIMARY KEY NOT NULL,
      profile_id TEXT NOT NULL DEFAULT '',
      exercise_id TEXT NOT NULL,
      exercise_name TEXT NOT NULL,
      type TEXT NOT NULL,
      value REAL NOT NULL,
      achieved_at_millis INTEGER NOT NULL,
      session_id TEXT NOT NULL
    );
  `);

  // Legacy single-profile table → profiles
  if (await tableExists(db, 'user_profile')) {
    const legacy = await db.getAllAsync(`SELECT * FROM user_profile`);
    for (const row of legacy) {
      const r = row as Record<string, unknown>;
      const id = String(r.id);
      const existing = await db.getFirstAsync(`SELECT id FROM profiles WHERE id = ?`, id);
      if (existing) continue;
      const now = Date.now();
      await db.runAsync(
        `INSERT INTO profiles
          (id, display_name, email, weight_kg, height_cm, age, biological_sex,
           body_fat_percentage, training_experience, unit_system, created_at_millis, updated_at_millis)
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
        id,
        'Athlete',
        String(r.email ?? ''),
        r.weight_kg == null ? null : Number(r.weight_kg),
        r.height_cm == null ? null : Number(r.height_cm),
        r.age == null ? null : Number(r.age),
        r.biological_sex == null ? null : String(r.biological_sex),
        r.body_fat_percentage == null ? null : Number(r.body_fat_percentage),
        String(r.training_experience ?? 'INTERMEDIATE'),
        String(r.unit_system ?? 'metric'),
        Number(r.created_at_millis ?? now),
        now
      );
    }
  }

  await ensureColumn(db, 'routines', 'profile_id', `profile_id TEXT NOT NULL DEFAULT ''`);
  await ensureColumn(db, 'workout_sessions', 'profile_id', `profile_id TEXT NOT NULL DEFAULT ''`);
  await ensureColumn(db, 'personal_records', 'profile_id', `profile_id TEXT NOT NULL DEFAULT ''`);
  await ensureColumn(db, 'exercises', 'profile_id', `profile_id TEXT`);
  await ensureColumn(db, 'profiles', 'display_name', `display_name TEXT NOT NULL DEFAULT 'Athlete'`);
  await ensureColumn(db, 'profiles', 'updated_at_millis', `updated_at_millis INTEGER NOT NULL DEFAULT 0`);

  // Backfill orphaned rows onto the first / active profile
  const active =
    (await getMeta(db, META.activeProfileId)) ??
    (
      await db.getFirstAsync<{ id: string }>(
        `SELECT id FROM profiles ORDER BY created_at_millis ASC LIMIT 1`
      )
    )?.id;

  if (active) {
    await db.runAsync(`UPDATE routines SET profile_id = ? WHERE profile_id = '' OR profile_id IS NULL`, active);
    await db.runAsync(
      `UPDATE workout_sessions SET profile_id = ? WHERE profile_id = '' OR profile_id IS NULL`,
      active
    );
    await db.runAsync(
      `UPDATE personal_records SET profile_id = ? WHERE profile_id = '' OR profile_id IS NULL`,
      active
    );
    if (!(await getMeta(db, META.activeProfileId))) {
      await setMeta(db, META.activeProfileId, active);
    }
  }

  await setMeta(db, META.schemaVersion, String(SCHEMA_VERSION));
}

async function seedExercises(db: SQLite.SQLiteDatabase) {
  const seeded = await getMeta(db, META.exercisesSeeded);
  if (seeded) return;
  for (const ex of EXERCISE_SEED) {
    await db.runAsync(
      `INSERT OR REPLACE INTO exercises
        (id, name, equipment, primary_sub_muscle, secondary_sub_muscles, creator_tags, is_custom, profile_id)
       VALUES (?, ?, ?, ?, ?, ?, ?, NULL)`,
      ex.id,
      ex.name,
      ex.equipment,
      ex.primarySubMuscle,
      ex.secondarySubMuscles.join(','),
      ex.creatorTags.join(','),
      ex.isCustom ? 1 : 0
    );
  }
  await setMeta(db, META.exercisesSeeded, '1');
}

function mapExercise(row: Record<string, unknown>): Exercise {
  return {
    id: String(row.id),
    name: String(row.name),
    equipment: row.equipment as Exercise['equipment'],
    primarySubMuscle: row.primary_sub_muscle as Exercise['primarySubMuscle'],
    secondarySubMuscles: String(row.secondary_sub_muscles || '')
      .split(',')
      .filter(Boolean) as Exercise['secondarySubMuscles'],
    creatorTags: String(row.creator_tags || '').split(',').filter(Boolean),
    isCustom: Boolean(row.is_custom),
  };
}

function mapProfile(row: Record<string, unknown>): UserProfile {
  return {
    id: String(row.id),
    displayName: String(row.display_name ?? 'Athlete'),
    email: String(row.email ?? ''),
    weightKg: row.weight_kg == null ? null : Number(row.weight_kg),
    heightCm: row.height_cm == null ? null : Number(row.height_cm),
    age: row.age == null ? null : Number(row.age),
    biologicalSex: (row.biological_sex as UserProfile['biologicalSex']) ?? null,
    bodyFatPercentage:
      row.body_fat_percentage == null ? null : Number(row.body_fat_percentage),
    trainingExperience:
      (row.training_experience as UserProfile['trainingExperience']) ?? 'INTERMEDIATE',
    unitSystem: (row.unit_system as UserProfile['unitSystem']) ?? 'metric',
    createdAtMillis: Number(row.created_at_millis),
    updatedAtMillis: Number(row.updated_at_millis ?? row.created_at_millis ?? 0),
  };
}

function mapSession(row: Record<string, unknown>): WorkoutSession {
  return {
    id: String(row.id),
    profileId: String(row.profile_id ?? ''),
    routineId: row.routine_id == null ? null : String(row.routine_id),
    routineName: String(row.routine_name ?? ''),
    state: row.state as WorkoutSession['state'],
    startedAtMillis: row.started_at_millis == null ? null : Number(row.started_at_millis),
    completedAtMillis:
      row.completed_at_millis == null ? null : Number(row.completed_at_millis),
    totalTonnageKg: Number(row.total_tonnage_kg ?? 0),
    estimatedCaloriesLow: Number(row.estimated_calories_low ?? 0),
    estimatedCaloriesHigh: Number(row.estimated_calories_high ?? 0),
    notes: String(row.notes ?? ''),
  };
}

function mapSet(row: Record<string, unknown>): WorkoutSet {
  return {
    id: String(row.id),
    sessionId: String(row.session_id),
    exerciseId: String(row.exercise_id),
    exerciseGroupId: row.exercise_group_id == null ? null : String(row.exercise_group_id),
    setNumber: Number(row.set_number),
    setType: row.set_type as WorkoutSet['setType'],
    weightKg: Number(row.weight_kg),
    repsCompleted: Number(row.reps_completed),
    targetReps: row.target_reps == null ? null : Number(row.target_reps),
    isCompleted: Boolean(row.is_completed),
    rpe: row.rpe == null ? null : Number(row.rpe),
    completedAtMillis:
      row.completed_at_millis == null ? null : Number(row.completed_at_millis),
  };
}

async function requireActiveProfileId(db: SQLite.SQLiteDatabase): Promise<string> {
  const id = await getMeta(db, META.activeProfileId);
  if (!id) throw new Error('No active profile — create a profile first');
  return id;
}

/**
 * Export-ready query helpers for other agents (CSV/JSON export, Health Connect, AI ingest).
 * Always scope by profile_id so multi-profile data never leaks across athletes.
 */
export const exportReadyQueries = {
  sessionsByProfile: `SELECT * FROM workout_sessions WHERE profile_id = ? ORDER BY completed_at_millis DESC`,
  setsByProfile: `
    SELECT s.* FROM workout_sets s
    INNER JOIN workout_sessions ws ON ws.id = s.session_id
    WHERE ws.profile_id = ?
    ORDER BY s.completed_at_millis ASC`,
  routinesByProfile: `SELECT * FROM routines WHERE profile_id = ? ORDER BY updated_at_millis DESC`,
  prsByProfile: `SELECT * FROM personal_records WHERE profile_id = ? ORDER BY achieved_at_millis DESC`,
  profileById: `SELECT * FROM profiles WHERE id = ?`,
} as const;

export const database = {
  async init() {
    await getDb();
  },

  /** @deprecated use getActiveProfile / listProfiles — kept for call-site compatibility */
  async isOnboardingComplete(): Promise<boolean> {
    const consent = await this.getStorageConsent();
    if (!consent.granted) return false;
    const profiles = await this.listProfiles();
    return profiles.length > 0;
  },

  async getStorageConsent(): Promise<{
    granted: boolean;
    grantedAtMillis: number | null;
    backupDirectoryUri: string | null;
  }> {
    const db = await getDb();
    const granted = (await getMeta(db, META.storageConsent)) === '1';
    const at = await getMeta(db, META.storageConsentAt);
    const backup = await getMeta(db, META.backupDirectoryUri);
    return {
      granted,
      grantedAtMillis: at ? Number(at) : null,
      backupDirectoryUri: backup,
    };
  },

  async setStorageConsent(granted: boolean): Promise<void> {
    const db = await getDb();
    await setMeta(db, META.storageConsent, granted ? '1' : '0');
    if (granted) {
      await setMeta(db, META.storageConsentAt, String(Date.now()));
    } else {
      await db.runAsync(`DELETE FROM app_meta WHERE key = ?`, META.storageConsentAt);
    }
  },

  async setBackupDirectoryUri(uri: string | null): Promise<void> {
    const db = await getDb();
    if (!uri) {
      await db.runAsync(`DELETE FROM app_meta WHERE key = ?`, META.backupDirectoryUri);
      return;
    }
    await setMeta(db, META.backupDirectoryUri, uri);
  },

  async listProfiles(): Promise<UserProfile[]> {
    const db = await getDb();
    const rows = await db.getAllAsync(
      `SELECT * FROM profiles ORDER BY created_at_millis ASC`
    );
    return rows.map((r) => mapProfile(r as Record<string, unknown>));
  },

  async getActiveProfileId(): Promise<string | null> {
    const db = await getDb();
    return getMeta(db, META.activeProfileId);
  },

  async getActiveProfile(): Promise<UserProfile | null> {
    const db = await getDb();
    const id = await getMeta(db, META.activeProfileId);
    if (!id) return null;
    return this.getProfileById(id);
  },

  /** @deprecated prefer getActiveProfile */
  async getUserProfile(): Promise<UserProfile | null> {
    return this.getActiveProfile();
  },

  async getProfileById(id: string): Promise<UserProfile | null> {
    const db = await getDb();
    const row = await db.getFirstAsync(`SELECT * FROM profiles WHERE id = ?`, id);
    return row ? mapProfile(row as Record<string, unknown>) : null;
  },

  async createProfile(
    profile: Omit<UserProfile, 'updatedAtMillis'> & { updatedAtMillis?: number }
  ): Promise<UserProfile> {
    const consent = await this.getStorageConsent();
    if (!consent.granted) {
      throw new Error('Grant app-data consent before creating a profile');
    }
    const db = await getDb();
    const now = Date.now();
    const full: UserProfile = {
      ...profile,
      displayName: profile.displayName?.trim() || 'Athlete',
      updatedAtMillis: profile.updatedAtMillis ?? now,
    };
    await db.runAsync(
      `INSERT INTO profiles
        (id, display_name, email, weight_kg, height_cm, age, biological_sex,
         body_fat_percentage, training_experience, unit_system, created_at_millis, updated_at_millis)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
      full.id,
      full.displayName,
      full.email,
      full.weightKg,
      full.heightCm,
      full.age,
      full.biologicalSex,
      full.bodyFatPercentage,
      full.trainingExperience,
      full.unitSystem,
      full.createdAtMillis,
      full.updatedAtMillis
    );
    const active = await getMeta(db, META.activeProfileId);
    if (!active) {
      await setMeta(db, META.activeProfileId, full.id);
    }
    return full;
  },

  async upsertProfile(profile: UserProfile): Promise<void> {
    const db = await getDb();
    const existing = await this.getProfileById(profile.id);
    if (!existing) {
      await this.createProfile(profile);
      return;
    }
    await db.runAsync(
      `UPDATE profiles SET
        display_name = ?, email = ?, weight_kg = ?, height_cm = ?, age = ?,
        biological_sex = ?, body_fat_percentage = ?, training_experience = ?,
        unit_system = ?, updated_at_millis = ?
       WHERE id = ?`,
      profile.displayName?.trim() || 'Athlete',
      profile.email,
      profile.weightKg,
      profile.heightCm,
      profile.age,
      profile.biologicalSex,
      profile.bodyFatPercentage,
      profile.trainingExperience,
      profile.unitSystem,
      Date.now(),
      profile.id
    );
  },

  async switchProfile(profileId: string): Promise<UserProfile> {
    const profile = await this.getProfileById(profileId);
    if (!profile) throw new Error('Profile not found');
    const db = await getDb();
    await setMeta(db, META.activeProfileId, profileId);
    return profile;
  },

  /**
   * Deletes a profile and all scoped workouts / routines / PRs.
   * Exercise catalog (global seed) is preserved. Custom exercises for this profile are removed.
   */
  async deleteProfile(profileId: string): Promise<void> {
    const db = await getDb();
    const profiles = await this.listProfiles();
    if (profiles.length <= 1) {
      throw new Error('Cannot delete the last profile');
    }
    const target = profiles.find((p) => p.id === profileId);
    if (!target) throw new Error('Profile not found');

    await db.withTransactionAsync(async () => {
      const sessions = await db.getAllAsync<{ id: string }>(
        `SELECT id FROM workout_sessions WHERE profile_id = ?`,
        profileId
      );
      for (const s of sessions) {
        await db.runAsync(`DELETE FROM workout_sets WHERE session_id = ?`, s.id);
      }
      await db.runAsync(`DELETE FROM workout_sessions WHERE profile_id = ?`, profileId);
      await db.runAsync(`DELETE FROM personal_records WHERE profile_id = ?`, profileId);

      const routines = await db.getAllAsync<{ id: string }>(
        `SELECT id FROM routines WHERE profile_id = ?`,
        profileId
      );
      for (const r of routines) {
        const groups = await db.getAllAsync<{ id: string }>(
          `SELECT id FROM exercise_groups WHERE routine_id = ?`,
          r.id
        );
        for (const g of groups) {
          await db.runAsync(`DELETE FROM routine_exercises WHERE exercise_group_id = ?`, g.id);
        }
        await db.runAsync(`DELETE FROM exercise_groups WHERE routine_id = ?`, r.id);
      }
      await db.runAsync(`DELETE FROM routines WHERE profile_id = ?`, profileId);
      await db.runAsync(
        `DELETE FROM exercises WHERE is_custom = 1 AND profile_id = ?`,
        profileId
      );
      await db.runAsync(`DELETE FROM profiles WHERE id = ?`, profileId);
    });

    const active = await getMeta(db, META.activeProfileId);
    if (active === profileId) {
      const next = (await this.listProfiles())[0];
      if (next) await setMeta(db, META.activeProfileId, next.id);
    }
  },

  async getAllExercises(): Promise<Exercise[]> {
    const db = await getDb();
    const profileId = await getMeta(db, META.activeProfileId);
    const rows = await db.getAllAsync(
      `SELECT * FROM exercises
       WHERE is_custom = 0 OR profile_id IS NULL OR profile_id = ?
       ORDER BY name ASC`,
      profileId ?? ''
    );
    return rows.map((r) => mapExercise(r as Record<string, unknown>));
  },

  async getExerciseById(id: string): Promise<Exercise | null> {
    const db = await getDb();
    const row = await db.getFirstAsync(`SELECT * FROM exercises WHERE id = ?`, id);
    return row ? mapExercise(row as Record<string, unknown>) : null;
  },

  async searchExercises(query: string): Promise<Exercise[]> {
    const all = await this.getAllExercises();
    const q = query.trim().toLowerCase();
    if (!q) return all;
    return all.filter((e) => e.name.toLowerCase().includes(q));
  },

  async getSwapSuggestions(exerciseId: string): Promise<Exercise[]> {
    const exercise = await this.getExerciseById(exerciseId);
    if (!exercise) return [];
    const all = await this.getAllExercises();
    return all
      .filter((e) => e.primarySubMuscle === exercise.primarySubMuscle && e.id !== exerciseId)
      .slice(0, 12);
  },

  async getAllRoutines(): Promise<Routine[]> {
    const db = await getDb();
    const profileId = await requireActiveProfileId(db);
    const rows = await db.getAllAsync(
      `SELECT id FROM routines WHERE profile_id = ? ORDER BY updated_at_millis DESC`,
      profileId
    );
    const routines: Routine[] = [];
    for (const row of rows) {
      const routine = await this.getRoutineById(String((row as { id: string }).id));
      if (routine) routines.push(routine);
    }
    return routines;
  },

  async getRoutineById(id: string): Promise<Routine | null> {
    const db = await getDb();
    const row = await db.getFirstAsync(`SELECT * FROM routines WHERE id = ?`, id);
    if (!row) return null;
    const r = row as Record<string, unknown>;
    const groups = await db.getAllAsync(
      `SELECT * FROM exercise_groups WHERE routine_id = ? ORDER BY order_in_routine ASC`,
      id
    );
    const exerciseGroups: ExerciseGroup[] = groups.map((g) => {
      const gr = g as Record<string, unknown>;
      return {
        id: String(gr.id),
        groupType: gr.group_type as ExerciseGroup['groupType'],
        orderInRoutine: Number(gr.order_in_routine),
        restAfterGroupSeconds: Number(gr.rest_after_group_seconds),
        timeCapSeconds: gr.time_cap_seconds == null ? null : Number(gr.time_cap_seconds),
      };
    });

    const exercises: RoutineExercise[] = [];
    for (const group of exerciseGroups) {
      const reRows = await db.getAllAsync(
        `SELECT * FROM routine_exercises WHERE exercise_group_id = ? ORDER BY order_in_group ASC`,
        group.id
      );
      for (const re of reRows) {
        const rr = re as Record<string, unknown>;
        const exercise = await this.getExerciseById(String(rr.exercise_id));
        exercises.push({
          id: String(rr.id),
          exerciseGroupId: String(rr.exercise_group_id),
          exerciseId: String(rr.exercise_id),
          exercise: exercise ?? undefined,
          orderInGroup: Number(rr.order_in_group),
          prescribedSets: Number(rr.prescribed_sets),
          prescribedRepsMin: Number(rr.prescribed_reps_min),
          prescribedRepsMax: Number(rr.prescribed_reps_max),
          restSeconds: Number(rr.rest_seconds),
          executionNotes: String(rr.execution_notes ?? ''),
        });
      }
    }

    return {
      id: String(r.id),
      profileId: String(r.profile_id ?? ''),
      name: String(r.name),
      description: String(r.description ?? ''),
      estimatedDurationMinutes:
        r.estimated_duration_minutes == null ? null : Number(r.estimated_duration_minutes),
      creatorName: r.creator_name == null ? null : String(r.creator_name),
      sourceVideoId: r.source_video_id == null ? null : String(r.source_video_id),
      exerciseGroups,
      exercises,
      createdAtMillis: Number(r.created_at_millis),
      updatedAtMillis: Number(r.updated_at_millis),
    };
  },

  async createRoutine(
    routine: Omit<Routine, 'exerciseGroups' | 'exercises'>,
    groups: ExerciseGroup[],
    exercises: RoutineExercise[]
  ): Promise<string> {
    const db = await getDb();
    const profileId = routine.profileId || (await requireActiveProfileId(db));
    await db.runAsync(
      `INSERT INTO routines
        (id, profile_id, name, description, estimated_duration_minutes, creator_name, source_video_id,
         created_at_millis, updated_at_millis)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
      routine.id,
      profileId,
      routine.name,
      routine.description,
      routine.estimatedDurationMinutes,
      routine.creatorName,
      routine.sourceVideoId,
      routine.createdAtMillis,
      routine.updatedAtMillis
    );
    for (const g of groups) {
      await db.runAsync(
        `INSERT INTO exercise_groups
          (id, routine_id, group_type, order_in_routine, rest_after_group_seconds, time_cap_seconds)
         VALUES (?, ?, ?, ?, ?, ?)`,
        g.id,
        routine.id,
        g.groupType,
        g.orderInRoutine,
        g.restAfterGroupSeconds,
        g.timeCapSeconds
      );
    }
    for (const e of exercises) {
      await db.runAsync(
        `INSERT INTO routine_exercises
          (id, exercise_group_id, exercise_id, order_in_group, prescribed_sets,
           prescribed_reps_min, prescribed_reps_max, rest_seconds, execution_notes)
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
        e.id,
        e.exerciseGroupId,
        e.exerciseId,
        e.orderInGroup,
        e.prescribedSets,
        e.prescribedRepsMin,
        e.prescribedRepsMax,
        e.restSeconds,
        e.executionNotes
      );
    }
    return routine.id;
  },

  async updateRoutine(
    routine: Omit<Routine, 'exerciseGroups' | 'exercises'>,
    groups: ExerciseGroup[],
    exercises: RoutineExercise[]
  ): Promise<void> {
    const db = await getDb();
    await db.runAsync(`DELETE FROM exercise_groups WHERE routine_id = ?`, routine.id);
    await db.runAsync(
      `UPDATE routines SET name = ?, description = ?, estimated_duration_minutes = ?,
        creator_name = ?, source_video_id = ?, updated_at_millis = ? WHERE id = ?`,
      routine.name,
      routine.description,
      routine.estimatedDurationMinutes,
      routine.creatorName,
      routine.sourceVideoId,
      Date.now(),
      routine.id
    );
    for (const g of groups) {
      await db.runAsync(
        `INSERT INTO exercise_groups
          (id, routine_id, group_type, order_in_routine, rest_after_group_seconds, time_cap_seconds)
         VALUES (?, ?, ?, ?, ?, ?)`,
        g.id,
        routine.id,
        g.groupType,
        g.orderInRoutine,
        g.restAfterGroupSeconds,
        g.timeCapSeconds
      );
    }
    for (const e of exercises) {
      await db.runAsync(
        `INSERT INTO routine_exercises
          (id, exercise_group_id, exercise_id, order_in_group, prescribed_sets,
           prescribed_reps_min, prescribed_reps_max, rest_seconds, execution_notes)
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
        e.id,
        e.exerciseGroupId,
        e.exerciseId,
        e.orderInGroup,
        e.prescribedSets,
        e.prescribedRepsMin,
        e.prescribedRepsMax,
        e.restSeconds,
        e.executionNotes
      );
    }
  },

  async deleteRoutine(id: string): Promise<void> {
    const db = await getDb();
    await db.runAsync(`DELETE FROM routines WHERE id = ?`, id);
  },

  async duplicateRoutine(sourceId: string, newName: string): Promise<string> {
    const source = await this.getRoutineById(sourceId);
    if (!source) throw new Error('Routine not found');
    const newId = crypto.randomUUID();
    const groupIdMap = new Map<string, string>();
    const groups = source.exerciseGroups.map((g) => {
      const nid = crypto.randomUUID();
      groupIdMap.set(g.id, nid);
      return { ...g, id: nid };
    });
    const exercises = source.exercises.map((e) => ({
      ...e,
      id: crypto.randomUUID(),
      exerciseGroupId: groupIdMap.get(e.exerciseGroupId) ?? e.exerciseGroupId,
    }));
    await this.createRoutine(
      {
        id: newId,
        profileId: source.profileId,
        name: newName,
        description: source.description,
        estimatedDurationMinutes: source.estimatedDurationMinutes,
        creatorName: source.creatorName,
        sourceVideoId: source.sourceVideoId,
        createdAtMillis: Date.now(),
        updatedAtMillis: Date.now(),
      },
      groups,
      exercises
    );
    return newId;
  },

  async startSession(session: WorkoutSession): Promise<string> {
    const db = await getDb();
    const profileId = session.profileId || (await requireActiveProfileId(db));
    await db.runAsync(
      `INSERT INTO workout_sessions
        (id, profile_id, routine_id, routine_name, state, started_at_millis, completed_at_millis,
         total_tonnage_kg, estimated_calories_low, estimated_calories_high, notes)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
      session.id,
      profileId,
      session.routineId,
      session.routineName,
      session.state,
      session.startedAtMillis,
      session.completedAtMillis,
      session.totalTonnageKg,
      session.estimatedCaloriesLow,
      session.estimatedCaloriesHigh,
      session.notes
    );
    return session.id;
  },

  async updateSession(session: WorkoutSession): Promise<void> {
    const db = await getDb();
    await db.runAsync(
      `UPDATE workout_sessions SET routine_id = ?, routine_name = ?, state = ?,
        started_at_millis = ?, completed_at_millis = ?, total_tonnage_kg = ?,
        estimated_calories_low = ?, estimated_calories_high = ?, notes = ?
       WHERE id = ?`,
      session.routineId,
      session.routineName,
      session.state,
      session.startedAtMillis,
      session.completedAtMillis,
      session.totalTonnageKg,
      session.estimatedCaloriesLow,
      session.estimatedCaloriesHigh,
      session.notes,
      session.id
    );
  },

  async completeSession(sessionId: string): Promise<void> {
    const db = await getDb();
    await db.runAsync(
      `UPDATE workout_sessions SET state = 'COMPLETED', completed_at_millis = ? WHERE id = ?`,
      Date.now(),
      sessionId
    );
  },

  async getSessionById(id: string): Promise<WorkoutSession | null> {
    const db = await getDb();
    const row = await db.getFirstAsync(`SELECT * FROM workout_sessions WHERE id = ?`, id);
    return row ? mapSession(row as Record<string, unknown>) : null;
  },

  async getAllSessions(): Promise<WorkoutSession[]> {
    const db = await getDb();
    const profileId = await requireActiveProfileId(db);
    const rows = await db.getAllAsync(
      `SELECT * FROM workout_sessions
       WHERE state = 'COMPLETED' AND profile_id = ?
       ORDER BY completed_at_millis DESC`,
      profileId
    );
    return rows.map((r) => mapSession(r as Record<string, unknown>));
  },

  async logSet(set: WorkoutSet): Promise<void> {
    const db = await getDb();
    await db.runAsync(
      `INSERT OR REPLACE INTO workout_sets
        (id, session_id, exercise_id, exercise_group_id, set_number, set_type, weight_kg,
         reps_completed, target_reps, is_completed, rpe, completed_at_millis)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
      set.id,
      set.sessionId,
      set.exerciseId,
      set.exerciseGroupId,
      set.setNumber,
      set.setType,
      set.weightKg,
      set.repsCompleted,
      set.targetReps,
      set.isCompleted ? 1 : 0,
      set.rpe,
      set.completedAtMillis
    );
  },

  async updateSet(set: WorkoutSet): Promise<void> {
    await this.logSet(set);
  },

  async getSetsForSession(sessionId: string): Promise<WorkoutSet[]> {
    const db = await getDb();
    const rows = await db.getAllAsync(
      `SELECT * FROM workout_sets WHERE session_id = ? ORDER BY set_number ASC`,
      sessionId
    );
    return rows.map((r) => mapSet(r as Record<string, unknown>));
  },

  async getLastSetsForExercise(exerciseId: string): Promise<WorkoutSet[]> {
    const db = await getDb();
    const profileId = await requireActiveProfileId(db);
    const session = await db.getFirstAsync<{ id: string }>(
      `SELECT ws.id FROM workout_sessions ws
       INNER JOIN workout_sets s ON s.session_id = ws.id
       WHERE ws.state = 'COMPLETED' AND ws.profile_id = ? AND s.exercise_id = ?
       ORDER BY ws.completed_at_millis DESC LIMIT 1`,
      profileId,
      exerciseId
    );
    if (!session) return [];
    const rows = await db.getAllAsync(
      `SELECT * FROM workout_sets WHERE session_id = ? AND exercise_id = ? AND is_completed = 1
       ORDER BY set_number ASC`,
      session.id,
      exerciseId
    );
    return rows.map((r) => mapSet(r as Record<string, unknown>));
  },

  async checkAndRecordPR(set: WorkoutSet, exerciseName: string): Promise<PersonalRecord | null> {
    if (!set.isCompleted || set.setType === 'WARMUP') return null;
    const db = await getDb();
    const profileId = await requireActiveProfileId(db);
    const existing = await db.getFirstAsync<{ value: number }>(
      `SELECT value FROM personal_records
       WHERE profile_id = ? AND exercise_id = ? AND type = 'WEIGHT'`,
      profileId,
      set.exerciseId
    );
    if (existing && existing.value >= set.weightKg) return null;

    const pr: PersonalRecord = {
      id: crypto.randomUUID(),
      profileId,
      exerciseId: set.exerciseId,
      exerciseName,
      type: 'WEIGHT',
      value: set.weightKg,
      achievedAtMillis: Date.now(),
      sessionId: set.sessionId,
    };
    await db.runAsync(
      `INSERT OR REPLACE INTO personal_records
        (id, profile_id, exercise_id, exercise_name, type, value, achieved_at_millis, session_id)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
      pr.id,
      pr.profileId,
      pr.exerciseId,
      pr.exerciseName,
      pr.type,
      pr.value,
      pr.achievedAtMillis,
      pr.sessionId
    );
    await db.runAsync(
      `DELETE FROM personal_records
       WHERE profile_id = ? AND exercise_id = ? AND type = 'WEIGHT' AND id != ?`,
      profileId,
      set.exerciseId,
      pr.id
    );
    return pr;
  },

  async getAllPersonalRecords(): Promise<PersonalRecord[]> {
    const db = await getDb();
    const profileId = await requireActiveProfileId(db);
    const rows = await db.getAllAsync(
      `SELECT * FROM personal_records WHERE profile_id = ?
       ORDER BY achieved_at_millis DESC`,
      profileId
    );
    return rows.map((r) => {
      const row = r as Record<string, unknown>;
      return {
        id: String(row.id),
        profileId: String(row.profile_id ?? ''),
        exerciseId: String(row.exercise_id),
        exerciseName: String(row.exercise_name),
        type: row.type as PersonalRecord['type'],
        value: Number(row.value),
        achievedAtMillis: Number(row.achieved_at_millis),
        sessionId: String(row.session_id),
      };
    });
  },

  async getGuestMode(): Promise<boolean> {
    const db = await getDb();
    return (await getMeta(db, META.guestMode)) === '1';
  },

  async setGuestMode(enabled: boolean): Promise<void> {
    const db = await getDb();
    await setMeta(db, META.guestMode, enabled ? '1' : '0');
  },

  async isUnlockConfigured(): Promise<boolean> {
    const db = await getDb();
    return (await getMeta(db, META.unlockConfigured)) === '1';
  },

  async setUnlockConfigured(configured: boolean): Promise<void> {
    const db = await getDb();
    await setMeta(db, META.unlockConfigured, configured ? '1' : '0');
  },
};
