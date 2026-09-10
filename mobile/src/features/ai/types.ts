import type { Equipment, SubMuscle } from '@/src/domain/types';

export type IngestionSource = 'youtube' | 'text';

export type IngestionStage =
  | 'IDLE'
  | 'CHECKING_GLOBAL_CACHE'
  | 'CHECKING_QUOTA'
  | 'ANALYZING_METADATA'
  | 'GEMINI_PARSING'
  | 'SIMULATED_PARSING'
  | 'TEXT_PARSING'
  | 'READY_FOR_REVIEW';

export type IngestionErrorCode =
  | 'INVALID_YOUTUBE_URL'
  | 'EMPTY_INPUT'
  | 'NOT_A_WORKOUT'
  | 'QUOTA_EXCEEDED'
  | 'RATE_LIMITED'
  | 'NETWORK'
  | 'PARSING_FAILED';

export type IngestionError = {
  code: IngestionErrorCode;
  message: string;
  recoveryAction: string;
};

export type ParsedExerciseItem = {
  id: string;
  name: string;
  /** Matched seed library id when found; null until HITL save resolves fallback. */
  matchedExerciseId: string | null;
  matchConfidence: 'exact' | 'fuzzy' | 'none';
  equipment: Equipment;
  primarySubMuscle: SubMuscle;
  sets: number;
  repsMin: number;
  repsMax: number;
  restSeconds: number;
  isSuperset: boolean;
  notes: string;
};

export type ParsedWorkoutData = {
  videoId: string | null;
  videoTitle: string;
  creatorName: string;
  thumbnailUrl: string | null;
  exercises: ParsedExerciseItem[];
  isFromGlobalCache: boolean;
  source: IngestionSource;
  estimatedDurationMinutes: number;
  usedAiCredits: boolean;
  parseMode: 'cache' | 'gemini' | 'simulator' | 'text';
};

export type QuotaStatus = {
  weekKey: string;
  used: number;
  baseAllowance: number;
  bonusCredits: number;
  remaining: number;
  lastImportAtMillis: number | null;
  canImport: boolean;
  rateLimitedUntilMillis: number | null;
};

export const WEEKLY_BASE_IMPORT_CREDITS = 3;
export const WEEKLY_MAX_IMPORT_CREDITS = 5;
export const RATE_LIMIT_MS = 10 * 60 * 1000;
export const WORKOUTS_FOR_BONUS = 3;

/** Legacy sentinel — prefer resolveProfileId() / getActiveProfileId(); never write this when an active profile exists. */
export const DEFAULT_LOCAL_PROFILE_ID = 'default-local-profile';
