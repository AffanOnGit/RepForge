import { database } from '@/src/data/database';
import { isGeminiConfigured } from './config';
import { aiDb } from './data/aiDb';
import { parseWithGemini } from './geminiClient';
import { consumeImportCredit, getQuotaStatus } from './quota';
import { simulateYouTubeParse } from './simulator';
import { parseProgramText } from './textParser';
import { resolveProfileId } from './resolveProfileId';
import type {
  IngestionError,
  IngestionStage,
  ParsedWorkoutData,
} from './types';
import { extractYouTubeVideoId, isLikelyYouTubeUrl } from './youtubeUrl';

function sleep(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

function err(
  code: IngestionError['code'],
  message: string,
  recoveryAction: string
): IngestionError {
  return { code, message, recoveryAction };
}

export type IngestProgress = (stage: IngestionStage) => void;

export async function ingestWorkoutInput(args: {
  input: string;
  profileId?: string;
  onStage?: IngestProgress;
}): Promise<{ data: ParsedWorkoutData } | { error: IngestionError }> {
  let profileId: string;
  try {
    profileId = await resolveProfileId(args.profileId);
  } catch (e) {
    return {
      error: err(
        'PARSING_FAILED',
        e instanceof Error ? e.message : 'No active profile',
        'Finish onboarding, then retry'
      ),
    };
  }
  const input = args.input.trim();
  const onStage = args.onStage ?? (() => undefined);

  if (!input) {
    return {
      error: err('EMPTY_INPUT', 'Paste a YouTube URL or a written workout program.', 'Add input and try again'),
    };
  }

  await aiDb.ensureReady();
  const library = await database.getAllExercises();

  // ── YouTube path ──────────────────────────────────────────────
  if (isLikelyYouTubeUrl(input)) {
    const videoId = extractYouTubeVideoId(input);
    if (!videoId) {
      return {
        error: err(
          'INVALID_YOUTUBE_URL',
          'Invalid YouTube link. Please provide a standard watch, share, or shorts link.',
          'Check and paste the URL again'
        ),
      };
    }

    onStage('CHECKING_GLOBAL_CACHE');
    await sleep(450);
    const cached = await aiDb.getCachedVideo(videoId);
    if (cached) {
      onStage('READY_FOR_REVIEW');
      return { data: { ...cached, isFromGlobalCache: true, usedAiCredits: false, parseMode: 'cache' } };
    }

    // Seed curated simulator entries into cache on first hit
    const seeded = simulateYouTubeParse(videoId, library);
    if (seeded.parseMode === 'cache') {
      await aiDb.putCachedVideo(seeded, true);
      onStage('READY_FOR_REVIEW');
      return { data: seeded };
    }

    onStage('CHECKING_QUOTA');
    const quota = await getQuotaStatus(profileId);
    if (quota.rateLimitedUntilMillis) {
      const mins = Math.ceil((quota.rateLimitedUntilMillis - Date.now()) / 60000);
      return {
        error: err(
          'RATE_LIMITED',
          `Rate limit: wait about ${mins} minute(s) between AI imports.`,
          'Try a curated preset (0 credits) or wait'
        ),
      };
    }
    if (quota.remaining <= 0) {
      return {
        error: err(
          'QUOTA_EXCEEDED',
          `You've used your ${quota.baseAllowance + quota.bonusCredits} weekly import credits. Complete ${3} workouts this week to earn +1 bonus credit!`,
          'Earn credits by logging workouts'
        ),
      };
    }

    onStage('ANALYZING_METADATA');
    await sleep(600);

    try {
      let parsed: ParsedWorkoutData;
      if (isGeminiConfigured()) {
        onStage('GEMINI_PARSING');
        const gemini = await parseWithGemini({ videoId, library });
        if (!gemini) {
          onStage('SIMULATED_PARSING');
          await sleep(700);
          parsed = simulateYouTubeParse(videoId, library);
        } else {
          parsed = gemini;
        }
      } else {
        onStage('SIMULATED_PARSING');
        await sleep(900);
        parsed = simulateYouTubeParse(videoId, library);
      }

      if (parsed.usedAiCredits) {
        await consumeImportCredit(profileId);
      }
      await aiDb.putCachedVideo(parsed, false);
      onStage('READY_FOR_REVIEW');
      return { data: parsed };
    } catch (e) {
      const detail = e instanceof Error ? e.message : 'Unknown error';
      if (detail === 'NOT_A_WORKOUT') {
        return {
          error: err(
            'NOT_A_WORKOUT',
            "This doesn't appear to be a resistance training or workout video.",
            'Try a dedicated fitness or hypertrophy video'
          ),
        };
      }
      return {
        error: err(
          'PARSING_FAILED',
          `AI parsing could not extract structured exercises: ${detail}`,
          'Retry or paste program text / create manually'
        ),
      };
    }
  }

  // ── Program text path ─────────────────────────────────────────
  onStage('CHECKING_QUOTA');
  const quota = await getQuotaStatus(profileId);
  if (quota.rateLimitedUntilMillis) {
    const mins = Math.ceil((quota.rateLimitedUntilMillis - Date.now()) / 60000);
    return {
      error: err(
        'RATE_LIMITED',
        `Rate limit: wait about ${mins} minute(s) between AI imports.`,
        'Wait or use a curated YouTube preset'
      ),
    };
  }
  if (quota.remaining <= 0) {
    return {
      error: err(
        'QUOTA_EXCEEDED',
        `You've used your ${quota.baseAllowance + quota.bonusCredits} weekly import credits.`,
        'Earn credits by logging workouts'
      ),
    };
  }

  onStage('TEXT_PARSING');
  await sleep(400);
  try {
    const parsed = parseProgramText(input, library);
    await consumeImportCredit(profileId);
    onStage('READY_FOR_REVIEW');
    return { data: parsed };
  } catch (e) {
    const detail = e instanceof Error ? e.message : 'Unknown error';
    return {
      error: err(
        'PARSING_FAILED',
        detail,
        'Use lines like "Incline Dumbbell Press 3x8-10"'
      ),
    };
  }
}
