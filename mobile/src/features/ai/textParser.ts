import type { Exercise } from '@/src/domain/types';
import { matchExerciseToLibrary } from './exerciseMatcher';
import type { ParsedExerciseItem, ParsedWorkoutData } from './types';

const LINE_PATTERNS: RegExp[] = [
  // Incline Dumbbell Press 3x8-10
  /^(.+?)\s+(\d+)\s*[x×]\s*(\d+)\s*[-–]\s*(\d+)\s*$/i,
  // Incline Dumbbell Press - 3 sets of 8-10
  /^(.+?)\s*[-–:]\s*(\d+)\s*sets?\s*(?:of\s*)?(\d+)\s*[-–]\s*(\d+)\s*$/i,
  // Incline Dumbbell Press 3x10
  /^(.+?)\s+(\d+)\s*[x×]\s*(\d+)\s*$/i,
  // 3x8-10 Incline Dumbbell Press
  /^(\d+)\s*[x×]\s*(\d+)\s*[-–]\s*(\d+)\s+(.+)$/i,
];

function newId(prefix: string) {
  return `${prefix}_${Math.random().toString(36).slice(2, 10)}`;
}

function toItem(
  name: string,
  sets: number,
  repsMin: number,
  repsMax: number,
  library: Exercise[]
): ParsedExerciseItem {
  const match = matchExerciseToLibrary(name, library);
  return {
    id: newId('pex'),
    name: match.resolvedName,
    matchedExerciseId: match.exerciseId,
    matchConfidence: match.confidence,
    equipment: match.equipment,
    primarySubMuscle: match.primarySubMuscle,
    sets: Math.min(10, Math.max(1, sets)),
    repsMin: Math.max(1, repsMin),
    repsMax: Math.max(repsMin, repsMax),
    restSeconds: sets >= 4 ? 120 : 90,
    isSuperset: false,
    notes: match.confidence === 'none' ? 'Unmatched — review before saving' : '',
  };
}

/**
 * Deterministic program-text parser for paste ingestion when Gemini is unavailable
 * or the user pastes a written workout instead of a YouTube URL.
 */
export function parseProgramText(
  raw: string,
  library: Exercise[]
): ParsedWorkoutData {
  const lines = raw
    .split(/\r?\n/)
    .map((l) => l.trim())
    .filter((l) => l.length > 0 && !l.startsWith('#'));

  const titleLine = lines.find((l) => !LINE_PATTERNS.some((p) => p.test(l)));
  const exercises: ParsedExerciseItem[] = [];

  for (const line of lines) {
    let matched = false;
    for (const pattern of LINE_PATTERNS) {
      const m = line.match(pattern);
      if (!m) continue;
      matched = true;
      // Pattern variants reorder capture groups
      if (pattern.source.startsWith('^(\\d+)')) {
        const sets = Number(m[1]);
        const repsMin = Number(m[2]);
        const repsMax = Number(m[3]);
        const name = m[4];
        exercises.push(toItem(name, sets, repsMin, repsMax, library));
      } else if (m.length === 4 && !m[0].includes('-') && !m[0].includes('–')) {
        // name + sets x reps (single)
        exercises.push(toItem(m[1], Number(m[2]), Number(m[3]), Number(m[3]), library));
      } else {
        exercises.push(toItem(m[1], Number(m[2]), Number(m[3]), Number(m[4]), library));
      }
      break;
    }

    if (!matched) {
      // Soft match: look for known exercise names inside the line
      const found = library.find((ex) =>
        line.toLowerCase().includes(ex.name.toLowerCase())
      );
      if (found) {
        exercises.push(toItem(found.name, 3, 8, 12, library));
      }
    }
  }

  if (exercises.length === 0) {
    throw new Error('Could not extract any exercises from the pasted text');
  }

  const title =
    titleLine && !exercises.some((e) => e.name === titleLine)
      ? titleLine.slice(0, 80)
      : 'Imported Program';

  return {
    videoId: null,
    videoTitle: title,
    creatorName: 'Pasted program',
    thumbnailUrl: null,
    exercises,
    isFromGlobalCache: false,
    source: 'text',
    estimatedDurationMinutes: Math.min(90, Math.max(20, exercises.reduce((s, e) => s + e.sets * 2, 15))),
    usedAiCredits: true,
    parseMode: 'text',
  };
}
