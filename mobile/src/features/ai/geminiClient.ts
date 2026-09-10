import type { Exercise, Equipment, SubMuscle } from '@/src/domain/types';
import { getGeminiApiKey, getGeminiModel } from './config';
import { matchExerciseToLibrary } from './exerciseMatcher';
import type { ParsedExerciseItem, ParsedWorkoutData } from './types';
import { youtubeThumbnailUrl } from './youtubeUrl';

type GeminiExercise = {
  canonical_name: string;
  target_sub_muscle?: string;
  prescribed_sets?: number;
  prescribed_reps_min?: number;
  prescribed_reps_max?: number;
  rest_seconds?: number;
  execution_notes?: string;
};

type GeminiPayload = {
  workout_title?: string;
  creator_name?: string;
  estimated_duration_minutes?: number;
  exercises?: GeminiExercise[];
};

const SUB_MUSCLE_ALIASES: Record<string, SubMuscle> = {
  'upper chest': 'UPPER_CHEST',
  'mid chest': 'MID_CHEST',
  'middle chest': 'MID_CHEST',
  'lower chest': 'LOWER_CHEST',
  'front delts': 'FRONT_DELTS',
  'side delts': 'SIDE_DELTS',
  'rear delts': 'REAR_DELTS',
  lats: 'LATS',
  'upper back': 'UPPER_BACK_TRAPS',
  traps: 'UPPER_BACK_TRAPS',
  'lower back': 'LOWER_BACK',
  biceps: 'BICEPS',
  triceps: 'TRICEPS',
  forearms: 'FOREARMS',
  quads: 'QUADS',
  hamstrings: 'HAMSTRINGS',
  glutes: 'GLUTES',
  calves: 'CALVES',
  abs: 'UPPER_ABS',
  'upper abs': 'UPPER_ABS',
  'lower abs': 'LOWER_ABS',
  obliques: 'OBLIQUES',
};

function mapSubMuscle(raw: string | undefined, fallback: SubMuscle): SubMuscle {
  if (!raw) return fallback;
  const key = raw.trim().toLowerCase();
  return SUB_MUSCLE_ALIASES[key] ?? fallback;
}

function hydrateExercises(
  items: GeminiExercise[],
  library: Exercise[]
): ParsedExerciseItem[] {
  return items.map((item, index) => {
    const name = item.canonical_name?.trim() || `Exercise ${index + 1}`;
    const match = matchExerciseToLibrary(name, library);
    return {
      id: `gem_${index}_${Math.random().toString(36).slice(2, 7)}`,
      name: match.resolvedName,
      matchedExerciseId: match.exerciseId,
      matchConfidence: match.confidence,
      equipment: match.equipment as Equipment,
      primarySubMuscle: mapSubMuscle(item.target_sub_muscle, match.primarySubMuscle),
      sets: Math.min(10, Math.max(1, item.prescribed_sets ?? 3)),
      repsMin: Math.max(1, item.prescribed_reps_min ?? 8),
      repsMax: Math.max(
        item.prescribed_reps_min ?? 8,
        item.prescribed_reps_max ?? 12
      ),
      restSeconds: item.rest_seconds ?? 90,
      isSuperset: false,
      notes: item.execution_notes ?? '',
    };
  });
}

/**
 * Optional live Gemini Flash call. Returns null when no key is configured.
 */
export async function parseWithGemini(args: {
  videoId: string;
  library: Exercise[];
  programHint?: string;
}): Promise<ParsedWorkoutData | null> {
  const apiKey = getGeminiApiKey();
  if (!apiKey) return null;

  const model = getGeminiModel();
  const prompt = `Extract a resistance-training workout from this YouTube video ID: ${args.videoId}.
${args.programHint ? `Additional context:\n${args.programHint}` : ''}
Return ONLY JSON matching:
{
  "workout_title": string,
  "creator_name": string,
  "estimated_duration_minutes": number,
  "exercises": [
    {
      "canonical_name": string,
      "target_sub_muscle": string,
      "prescribed_sets": number,
      "prescribed_reps_min": number,
      "prescribed_reps_max": number,
      "rest_seconds": number,
      "execution_notes": string
    }
  ]
}
Use mainstream hypertrophy exercise names. If the video is not a workout, return {"exercises":[]}.`;

  const url = `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${encodeURIComponent(apiKey)}`;

  const response = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      contents: [{ parts: [{ text: prompt }] }],
      generationConfig: {
        responseMimeType: 'application/json',
        temperature: 0.2,
      },
    }),
  });

  if (!response.ok) {
    const body = await response.text().catch(() => '');
    throw new Error(`Gemini HTTP ${response.status}: ${body.slice(0, 180)}`);
  }

  const json = (await response.json()) as {
    candidates?: Array<{ content?: { parts?: Array<{ text?: string }> } }>;
  };
  const text = json.candidates?.[0]?.content?.parts?.[0]?.text;
  if (!text) throw new Error('Gemini returned an empty response');

  let payload: GeminiPayload;
  try {
    payload = JSON.parse(text) as GeminiPayload;
  } catch {
    throw new Error('Gemini returned invalid JSON');
  }

  const exercises = hydrateExercises(payload.exercises ?? [], args.library);
  if (exercises.length === 0) {
    throw new Error('NOT_A_WORKOUT');
  }

  return {
    videoId: args.videoId,
    videoTitle: payload.workout_title?.trim() || `Imported YouTube Workout (${args.videoId})`,
    creatorName: payload.creator_name?.trim() || 'YouTube',
    thumbnailUrl: youtubeThumbnailUrl(args.videoId),
    exercises,
    isFromGlobalCache: false,
    source: 'youtube',
    estimatedDurationMinutes:
      payload.estimated_duration_minutes ??
      Math.min(90, Math.max(20, exercises.reduce((s, e) => s + e.sets * 2, 15))),
    usedAiCredits: true,
    parseMode: 'gemini',
  };
}
