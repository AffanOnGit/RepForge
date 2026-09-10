import { database } from '@/src/data/database';
import { aiDb } from './data/aiDb';
import { FALLBACK_EXERCISE_ID } from './exerciseMatcher';
import { resolveProfileId } from './resolveProfileId';
import type { ParsedExerciseItem, ParsedWorkoutData } from './types';

/**
 * Persist HITL-confirmed workout as a local routine template.
 * Attaches to `profileId` on the routines row (multi-profile aware).
 * Also writes `ai_ingested_routines` for source/video audit metadata.
 */
export async function saveIngestedRoutine(args: {
  parsed: ParsedWorkoutData;
  exercises: ParsedExerciseItem[];
  /** Active profile from authStore; resolves via getActiveProfileId when omitted */
  profileId?: string;
}): Promise<string> {
  const profileId = await resolveProfileId(args.profileId);
  const exercises = args.exercises;
  if (!exercises.length) {
    throw new Error('Add at least one exercise before saving');
  }

  const library = await database.getAllExercises();
  const routineId = crypto.randomUUID();
  const now = Date.now();

  const groups = exercises.map((ex, index) => ({
    id: crypto.randomUUID(),
    groupType: (ex.isSuperset ? 'SUPERSET' : 'SINGLE') as 'SUPERSET' | 'SINGLE',
    orderInRoutine: index,
    restAfterGroupSeconds: ex.restSeconds,
    timeCapSeconds: null as number | null,
  }));

  const routineExercises = exercises.map((ex, index) => {
    const matched =
      ex.matchedExerciseId ??
      library.find((e) => e.name.toLowerCase() === ex.name.toLowerCase())?.id ??
      FALLBACK_EXERCISE_ID;

    return {
      id: crypto.randomUUID(),
      exerciseGroupId: groups[index].id,
      exerciseId: matched,
      orderInGroup: 0,
      prescribedSets: ex.sets,
      prescribedRepsMin: ex.repsMin,
      prescribedRepsMax: ex.repsMax,
      restSeconds: ex.restSeconds,
      executionNotes: ex.notes,
    };
  });

  await database.createRoutine(
    {
      id: routineId,
      profileId,
      name: args.parsed.videoTitle,
      description: args.parsed.videoId
        ? `Imported from ${args.parsed.creatorName}'s YouTube workout.`
        : `Imported program (${args.parsed.creatorName}).`,
      estimatedDurationMinutes: args.parsed.estimatedDurationMinutes,
      creatorName: args.parsed.creatorName,
      sourceVideoId: args.parsed.videoId,
      createdAtMillis: now,
      updatedAtMillis: now,
    },
    groups,
    routineExercises
  );

  await aiDb.linkIngestedRoutine({
    routineId,
    profileId,
    videoId: args.parsed.videoId,
    source: args.parsed.source,
    parseMode: args.parsed.parseMode,
  });

  if (args.parsed.videoId) {
    await aiDb.putCachedVideo(
      { ...args.parsed, exercises, isFromGlobalCache: true, usedAiCredits: false, parseMode: 'cache' },
      true
    );
    await aiDb.markVerified(args.parsed.videoId);
  }

  return routineId;
}
