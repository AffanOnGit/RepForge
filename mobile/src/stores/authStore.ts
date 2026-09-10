import { create } from 'zustand';
import * as SecureStore from 'expo-secure-store';
import * as LocalAuthentication from 'expo-local-authentication';
import * as Crypto from 'expo-crypto';
import { database } from '@/src/data/database';
import type { UserProfile } from '@/src/domain/types';

type AuthState = {
  ready: boolean;
  onboardingComplete: boolean;
  storageConsentGranted: boolean;
  profile: UserProfile | null;
  profiles: UserProfile[];
  activeProfileId: string | null;
  isGuest: boolean;
  /** Session unlock — in-memory only; PIN hash lives in SecureStore */
  unlocked: boolean;
  unlockConfigured: boolean;
  biometricAvailable: boolean;
  error: string | null;
  bootstrap: () => Promise<void>;
  grantStorageConsent: () => Promise<void>;
  completeOnboarding: (profile: Omit<UserProfile, 'updatedAtMillis'> & { updatedAtMillis?: number }) => Promise<void>;
  refreshProfiles: () => Promise<void>;
  createProfile: (profile: Omit<UserProfile, 'updatedAtMillis'> & { updatedAtMillis?: number }) => Promise<UserProfile>;
  switchProfile: (profileId: string) => Promise<void>;
  deleteProfile: (profileId: string) => Promise<boolean>;
  setPin: (pin: string) => Promise<boolean>;
  clearPin: () => Promise<void>;
  unlockWithPin: (pin: string) => Promise<boolean>;
  unlockWithBiometric: () => Promise<boolean>;
  continueAsGuest: () => Promise<void>;
  lock: () => void;
  signOut: () => Promise<void>;
  clearError: () => void;
};

const PIN_HASH_KEY = 'repforge_pin_hash_v1';
/** Legacy plaintext password map — wiped on bootstrap */
const LEGACY_USERS_KEY = 'repforge_local_users';

async function hashPin(pin: string): Promise<string> {
  return Crypto.digestStringAsync(
    Crypto.CryptoDigestAlgorithm.SHA256,
    `repforge:${pin}`
  );
}

async function wipeLegacyPasswordStore() {
  try {
    await SecureStore.deleteItemAsync(LEGACY_USERS_KEY);
  } catch {
    // ignore
  }
}

export const useAuthStore = create<AuthState>((set, get) => ({
  ready: false,
  onboardingComplete: false,
  storageConsentGranted: false,
  profile: null,
  profiles: [],
  activeProfileId: null,
  isGuest: false,
  unlocked: false,
  unlockConfigured: false,
  biometricAvailable: false,
  error: null,

  async bootstrap() {
    await database.init();
    await wipeLegacyPasswordStore();

    const [consent, profiles, activeProfileId, isGuest, unlockConfigured] =
      await Promise.all([
        database.getStorageConsent(),
        database.listProfiles(),
        database.getActiveProfileId(),
        database.getGuestMode(),
        database.isUnlockConfigured(),
      ]);

    let profile: UserProfile | null = null;
    if (activeProfileId) {
      profile = profiles.find((p) => p.id === activeProfileId) ?? null;
    }
    if (!profile) {
      profile = profiles[0] ?? null;
    }

    if (profile && activeProfileId !== profile.id) {
      await database.switchProfile(profile.id);
    }

    let biometricAvailable = false;
    try {
      const hasHardware = await LocalAuthentication.hasHardwareAsync();
      const enrolled = await LocalAuthentication.isEnrolledAsync();
      biometricAvailable = hasHardware && enrolled;
    } catch {
      biometricAvailable = false;
    }

    const onboardingComplete = consent.granted && profiles.length > 0;
    // No PIN → device access is enough after onboarding
    const unlocked = onboardingComplete && !unlockConfigured;

    set({
      ready: true,
      storageConsentGranted: consent.granted,
      onboardingComplete,
      profiles,
      profile,
      activeProfileId: profile?.id ?? null,
      isGuest,
      unlockConfigured,
      biometricAvailable,
      unlocked,
      error: null,
    });
  },

  async grantStorageConsent() {
    await database.setStorageConsent(true);
    set({ storageConsentGranted: true });
  },

  async completeOnboarding(profileInput) {
    if (!get().storageConsentGranted) {
      await database.setStorageConsent(true);
    }
    const profile = await database.createProfile({
      ...profileInput,
      updatedAtMillis: profileInput.updatedAtMillis ?? Date.now(),
    });
    await database.switchProfile(profile.id);
    const profiles = await database.listProfiles();
    set({
      storageConsentGranted: true,
      onboardingComplete: true,
      profile,
      profiles,
      activeProfileId: profile.id,
      unlocked: !get().unlockConfigured,
    });
  },

  async refreshProfiles() {
    const profiles = await database.listProfiles();
    const activeProfileId = await database.getActiveProfileId();
    const profile =
      profiles.find((p) => p.id === activeProfileId) ?? profiles[0] ?? null;
    set({
      profiles,
      profile,
      activeProfileId: profile?.id ?? null,
      onboardingComplete: get().storageConsentGranted && profiles.length > 0,
    });
  },

  async createProfile(profileInput) {
    const profile = await database.createProfile({
      ...profileInput,
      updatedAtMillis: profileInput.updatedAtMillis ?? Date.now(),
    });
    await get().refreshProfiles();
    return profile;
  },

  async switchProfile(profileId) {
    const profile = await database.switchProfile(profileId);
    const profiles = await database.listProfiles();
    set({
      profile,
      profiles,
      activeProfileId: profile.id,
    });
    // Keep AI quota / save scope aligned when the athlete switches profiles
    try {
      const { useAiIngestionStore } = await import('@/src/features/ai/store');
      const ai = useAiIngestionStore.getState();
      ai.setProfileId(profile.id);
      void ai.refreshQuota();
    } catch {
      // AI module optional during early boot
    }
  },

  async deleteProfile(profileId) {
    try {
      await database.deleteProfile(profileId);
      await get().refreshProfiles();
      set({ error: null });
      return true;
    } catch (e) {
      set({ error: e instanceof Error ? e.message : 'Could not delete profile' });
      return false;
    }
  },

  async setPin(pin) {
    const trimmed = pin.trim();
    if (!/^\d{4,8}$/.test(trimmed)) {
      set({ error: 'PIN must be 4–8 digits' });
      return false;
    }
    const hash = await hashPin(trimmed);
    await SecureStore.setItemAsync(PIN_HASH_KEY, hash);
    await database.setUnlockConfigured(true);
    set({ unlockConfigured: true, unlocked: true, error: null });
    return true;
  },

  async clearPin() {
    try {
      await SecureStore.deleteItemAsync(PIN_HASH_KEY);
    } catch {
      // ignore
    }
    await database.setUnlockConfigured(false);
    set({ unlockConfigured: false, unlocked: true, error: null });
  },

  async unlockWithPin(pin) {
    const stored = await SecureStore.getItemAsync(PIN_HASH_KEY);
    if (!stored) {
      set({ unlocked: true, unlockConfigured: false, error: null });
      return true;
    }
    const hash = await hashPin(pin.trim());
    if (hash !== stored) {
      set({ error: 'Incorrect PIN' });
      return false;
    }
    await database.setGuestMode(false);
    set({ unlocked: true, isGuest: false, error: null });
    return true;
  },

  async unlockWithBiometric() {
    try {
      const result = await LocalAuthentication.authenticateAsync({
        promptMessage: 'Unlock RepForge',
        cancelLabel: 'Use PIN',
        disableDeviceFallback: false,
      });
      if (!result.success) {
        set({ error: 'Biometric unlock cancelled' });
        return false;
      }
      await database.setGuestMode(false);
      set({ unlocked: true, isGuest: false, error: null });
      return true;
    } catch {
      set({ error: 'Biometric unlock unavailable' });
      return false;
    }
  },

  async continueAsGuest() {
    await database.setGuestMode(true);
    set({ isGuest: true, unlocked: true, error: null });
  },

  lock() {
    if (!get().unlockConfigured) return;
    set({ unlocked: false });
  },

  async signOut() {
    await database.setGuestMode(false);
    set({
      isGuest: false,
      unlocked: !get().unlockConfigured,
    });
  },

  clearError() {
    set({ error: null });
  },
}));
