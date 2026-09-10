import { database } from '@/src/data/database';
import { aiDb, currentWeekKey } from './data/aiDb';
import { resolveProfileId } from './resolveProfileId';
import {
  RATE_LIMIT_MS,
  WEEKLY_BASE_IMPORT_CREDITS,
  WEEKLY_MAX_IMPORT_CREDITS,
  WORKOUTS_FOR_BONUS,
  type QuotaStatus,
} from './types';

function startOfIsoWeekMillis(now = Date.now()): number {
  const d = new Date(now);
  const day = d.getDay() || 7;
  d.setHours(0, 0, 0, 0);
  d.setDate(d.getDate() - (day - 1));
  return d.getTime();
}

export async function countCompletedWorkoutsThisWeek(): Promise<number> {
  // getAllSessions is already scoped to the active profile
  const sessions = await database.getAllSessions();
  const start = startOfIsoWeekMillis();
  return sessions.filter(
    (s) => s.state === 'COMPLETED' && (s.completedAtMillis ?? 0) >= start
  ).length;
}

export async function getQuotaStatus(
  profileId?: string | null
): Promise<QuotaStatus> {
  const resolved = await resolveProfileId(profileId);
  await aiDb.ensureReady();
  const weekKey = currentWeekKey();
  const row = await aiDb.getQuotaRow(resolved, weekKey);
  const used = row?.used_count ?? 0;
  const lastImportAtMillis = row?.last_import_at_millis ?? null;
  const workouts = await countCompletedWorkoutsThisWeek();
  const bonusCredits = Math.min(
    WEEKLY_MAX_IMPORT_CREDITS - WEEKLY_BASE_IMPORT_CREDITS,
    Math.floor(workouts / WORKOUTS_FOR_BONUS)
  );
  const allowance = WEEKLY_BASE_IMPORT_CREDITS + bonusCredits;
  const remaining = Math.max(0, allowance - used);

  let rateLimitedUntilMillis: number | null = null;
  if (lastImportAtMillis != null) {
    const until = lastImportAtMillis + RATE_LIMIT_MS;
    if (until > Date.now()) rateLimitedUntilMillis = until;
  }

  return {
    weekKey,
    used,
    baseAllowance: WEEKLY_BASE_IMPORT_CREDITS,
    bonusCredits,
    remaining,
    lastImportAtMillis,
    canImport: remaining > 0 && rateLimitedUntilMillis == null,
    rateLimitedUntilMillis,
  };
}

export async function consumeImportCredit(
  profileId?: string | null
): Promise<void> {
  const resolved = await resolveProfileId(profileId);
  await aiDb.recordImport(resolved, currentWeekKey(), Date.now());
}
