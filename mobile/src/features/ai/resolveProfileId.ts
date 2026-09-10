import { database } from '@/src/data/database';
import { DEFAULT_LOCAL_PROFILE_ID } from './types';

/**
 * Resolve the profile to scope AI quota / saves against.
 * Never silently keeps DEFAULT_LOCAL_PROFILE_ID when a real active profile exists.
 */
export async function resolveProfileId(
  explicit?: string | null
): Promise<string> {
  const trimmed = explicit?.trim() ?? '';
  if (trimmed && trimmed !== DEFAULT_LOCAL_PROFILE_ID) {
    return trimmed;
  }

  const active = await database.getActiveProfileId();
  if (active) return active;

  if (trimmed) return trimmed;

  throw new Error('No active profile — finish onboarding first');
}
