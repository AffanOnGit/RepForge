import Constants from 'expo-constants';

type Extra = {
  geminiApiKey?: string;
  geminiModel?: string;
};

function readExtra(): Extra {
  return (Constants.expoConfig?.extra ?? {}) as Extra;
}

/**
 * Bring-your-own Gemini key. Never commit secrets.
 * Prefer app.json / app.config `extra.geminiApiKey`, or EXPO_PUBLIC_GEMINI_API_KEY at build time.
 */
export function getGeminiApiKey(): string | null {
  const fromExtra = String(readExtra().geminiApiKey ?? '').trim();
  const fromEnv = String(process.env.EXPO_PUBLIC_GEMINI_API_KEY ?? '').trim();
  const key = fromExtra || fromEnv;
  return key.length > 0 ? key : null;
}

export function getGeminiModel(): string {
  const fromExtra = String(readExtra().geminiModel ?? '').trim();
  return fromExtra || 'gemini-2.0-flash';
}

export function isGeminiConfigured(): boolean {
  return getGeminiApiKey() != null;
}
