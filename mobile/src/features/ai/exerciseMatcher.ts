import type { Exercise, Equipment, SubMuscle } from '@/src/domain/types';

function normalize(name: string): string {
  return name
    .toLowerCase()
    .replace(/[^a-z0-9\s]/g, ' ')
    .replace(/\b(the|a|an|of|and|with)\b/g, ' ')
    .replace(/\s+/g, ' ')
    .trim();
}

function tokenSet(name: string): Set<string> {
  return new Set(normalize(name).split(' ').filter((t) => t.length > 1));
}

function jaccard(a: Set<string>, b: Set<string>): number {
  if (!a.size || !b.size) return 0;
  let inter = 0;
  for (const t of a) if (b.has(t)) inter += 1;
  const union = a.size + b.size - inter;
  return union === 0 ? 0 : inter / union;
}

export type ExerciseMatch = {
  exerciseId: string | null;
  confidence: 'exact' | 'fuzzy' | 'none';
  equipment: Equipment;
  primarySubMuscle: SubMuscle;
  resolvedName: string;
};

/**
 * Prefer exact case-insensitive name match, then fuzzy token overlap against the seeded library.
 */
export function matchExerciseToLibrary(
  canonicalName: string,
  library: Exercise[]
): ExerciseMatch {
  const exact = library.find((e) => e.name.toLowerCase() === canonicalName.trim().toLowerCase());
  if (exact) {
    return {
      exerciseId: exact.id,
      confidence: 'exact',
      equipment: exact.equipment,
      primarySubMuscle: exact.primarySubMuscle,
      resolvedName: exact.name,
    };
  }

  const needle = tokenSet(canonicalName);
  let best: Exercise | null = null;
  let bestScore = 0;
  for (const ex of library) {
    const score = jaccard(needle, tokenSet(ex.name));
    if (score > bestScore) {
      bestScore = score;
      best = ex;
    }
  }

  if (best && bestScore >= 0.55) {
    return {
      exerciseId: best.id,
      confidence: 'fuzzy',
      equipment: best.equipment,
      primarySubMuscle: best.primarySubMuscle,
      resolvedName: best.name,
    };
  }

  return {
    exerciseId: null,
    confidence: 'none',
    equipment: guessEquipment(canonicalName),
    primarySubMuscle: guessSubMuscle(canonicalName),
    resolvedName: canonicalName.trim(),
  };
}

function guessEquipment(name: string): Equipment {
  const n = name.toLowerCase();
  if (n.includes('dumbbell') || n.includes('db ')) return 'DUMBBELL';
  if (n.includes('cable') || n.includes('rope')) return 'CABLE';
  if (n.includes('machine') || n.includes('smith')) return n.includes('smith') ? 'SMITH_MACHINE' : 'MACHINE';
  if (n.includes('kettle')) return 'KETTLEBELL';
  if (n.includes('bodyweight') || n.includes('push-up') || n.includes('pushup') || n.includes('dip')) {
    return 'BODYWEIGHT';
  }
  return 'BARBELL';
}

function guessSubMuscle(name: string): SubMuscle {
  const n = name.toLowerCase();
  if (n.includes('incline') && (n.includes('press') || n.includes('fly'))) return 'UPPER_CHEST';
  if (n.includes('decline')) return 'LOWER_CHEST';
  if (n.includes('bench') || n.includes('chest press') || n.includes('pec')) return 'MID_CHEST';
  if (n.includes('lateral') || n.includes('side delt')) return 'SIDE_DELTS';
  if (n.includes('rear delt') || n.includes('face pull')) return 'REAR_DELTS';
  if (n.includes('overhead press') || n.includes('shoulder press') || n.includes('front raise')) {
    return 'FRONT_DELTS';
  }
  if (n.includes('lat ') || n.includes('pulldown') || n.includes('pull-up') || n.includes('row')) {
    return n.includes('row') ? 'UPPER_BACK_TRAPS' : 'LATS';
  }
  if (n.includes('tricep')) return 'TRICEPS';
  if (n.includes('bicep') || n.includes('curl')) return 'BICEPS';
  if (n.includes('squat') || n.includes('leg press') || n.includes('quad')) return 'QUADS';
  if (n.includes('rdl') || n.includes('hamstring') || n.includes('leg curl')) return 'HAMSTRINGS';
  if (n.includes('glute') || n.includes('hip thrust')) return 'GLUTES';
  if (n.includes('calf')) return 'CALVES';
  if (n.includes('abs') || n.includes('crunch')) return 'UPPER_ABS';
  return 'MID_CHEST';
}

/** Fallback seed id used when nothing matches (Android mirrors this). */
export const FALLBACK_EXERCISE_ID = 'ex_flat_bb_bench';
