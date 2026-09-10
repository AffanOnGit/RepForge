import type { Exercise } from '@/src/domain/types';
import { matchExerciseToLibrary } from './exerciseMatcher';
import type { ParsedExerciseItem, ParsedWorkoutData } from './types';
import { youtubeThumbnailUrl } from './youtubeUrl';

type SeedExercise = {
  name: string;
  sets: number;
  repsMin: number;
  repsMax: number;
  restSeconds: number;
  notes?: string;
};

const CURATED: Record<
  string,
  { videoTitle: string; creatorName: string; exercises: SeedExercise[] }
> = {
  dQw4w9WgXcQ: {
    videoTitle: 'Scientific Push Workout: Chest, Delts & Triceps',
    creatorName: 'Jeff Nippard',
    exercises: [
      {
        name: 'Incline Dumbbell Press',
        sets: 3,
        repsMin: 8,
        repsMax: 10,
        restSeconds: 120,
        notes: '30-degree incline, control the eccentric stretch',
      },
      {
        name: 'Seated Machine Chest Press',
        sets: 3,
        repsMin: 10,
        repsMax: 12,
        restSeconds: 90,
        notes: 'Full lockout, squeeze mid chest',
      },
      {
        name: 'Cable Lateral Raise (Behind/Front)',
        sets: 4,
        repsMin: 12,
        repsMax: 15,
        restSeconds: 60,
        notes: 'Cuff around wrist, pull behind back',
      },
      {
        name: 'Overhead Rope Cable Tricep Extension',
        sets: 3,
        repsMin: 10,
        repsMax: 12,
        restSeconds: 90,
        notes: 'Long head stretch bias',
      },
    ],
  },
};

const DEFAULT_SIM: SeedExercise[] = [
  { name: 'Barbell Flat Bench Press', sets: 4, repsMin: 6, repsMax: 8, restSeconds: 120 },
  {
    name: 'Standing Dumbbell Lateral Raise',
    sets: 3,
    repsMin: 12,
    repsMax: 15,
    restSeconds: 60,
  },
  { name: 'Rope Cable Tricep Pushdown', sets: 3, repsMin: 10, repsMax: 12, restSeconds: 75 },
];

function toParsed(items: SeedExercise[], library: Exercise[]): ParsedExerciseItem[] {
  return items.map((item, index) => {
    const match = matchExerciseToLibrary(item.name, library);
    return {
      id: `sim_${index}_${Math.random().toString(36).slice(2, 7)}`,
      name: match.resolvedName,
      matchedExerciseId: match.exerciseId,
      matchConfidence: match.confidence,
      equipment: match.equipment,
      primarySubMuscle: match.primarySubMuscle,
      sets: item.sets,
      repsMin: item.repsMin,
      repsMax: item.repsMax,
      restSeconds: item.restSeconds,
      isSuperset: false,
      notes: item.notes ?? '',
    };
  });
}

export function getCuratedVideoIds(): string[] {
  return Object.keys(CURATED);
}

export function simulateYouTubeParse(
  videoId: string,
  library: Exercise[]
): ParsedWorkoutData {
  const curated = CURATED[videoId];
  if (curated) {
    const exercises = toParsed(curated.exercises, library);
    return {
      videoId,
      videoTitle: curated.videoTitle,
      creatorName: curated.creatorName,
      thumbnailUrl: youtubeThumbnailUrl(videoId),
      exercises,
      isFromGlobalCache: true,
      source: 'youtube',
      estimatedDurationMinutes: Math.min(90, Math.max(20, exercises.reduce((s, e) => s + e.sets * 2, 15))),
      usedAiCredits: false,
      parseMode: 'cache',
    };
  }

  const exercises = toParsed(DEFAULT_SIM, library);
  return {
    videoId,
    videoTitle: `Imported YouTube Workout (${videoId})`,
    creatorName: 'Curated Athlete',
    thumbnailUrl: youtubeThumbnailUrl(videoId),
    exercises,
    isFromGlobalCache: false,
    source: 'youtube',
    estimatedDurationMinutes: Math.min(90, Math.max(20, exercises.reduce((s, e) => s + e.sets * 2, 15))),
    usedAiCredits: true,
    parseMode: 'simulator',
  };
}

export const CURATED_PRESETS = [
  {
    title: 'Scientific Push Workout',
    creator: 'Jeff Nippard',
    tags: 'Chest • Shoulders • Triceps',
    url: 'https://www.youtube.com/watch?v=dQw4w9WgXcQ',
  },
  {
    title: 'High Volume Chest & Triceps',
    creator: 'Dr. Mike Israetel (RP)',
    tags: 'Upper Chest • Mid Chest • Triceps',
    url: 'https://www.youtube.com/watch?v=dQw4w9WgXcQ',
  },
] as const;
