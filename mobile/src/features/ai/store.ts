import { create } from 'zustand';
import { ingestWorkoutInput } from './ingestionService';
import { getQuotaStatus } from './quota';
import { resolveProfileId } from './resolveProfileId';
import { saveIngestedRoutine } from './saveRoutine';
import type {
  IngestionError,
  IngestionStage,
  ParsedExerciseItem,
  ParsedWorkoutData,
  QuotaStatus,
} from './types';

type AiIngestionState = {
  input: string;
  inputMode: 'url' | 'text';
  stage: IngestionStage;
  parsedData: ParsedWorkoutData | null;
  editedExercises: ParsedExerciseItem[];
  error: IngestionError | null;
  isSaving: boolean;
  quota: QuotaStatus | null;
  /** Mirrors auth active profile; empty until set / resolved from DB */
  profileId: string;
  setInput: (value: string) => void;
  setInputMode: (mode: 'url' | 'text') => void;
  setProfileId: (profileId: string | null | undefined) => void;
  refreshQuota: () => Promise<void>;
  startIngestion: (overrideInput?: string) => Promise<void>;
  removeExercise: (id: string) => void;
  updateExerciseSets: (id: string, sets: number) => void;
  updateExerciseReps: (id: string, min: number, max: number) => void;
  confirmAndSave: () => Promise<string | null>;
  reset: () => void;
};

const initial = {
  input: '',
  inputMode: 'url' as const,
  stage: 'IDLE' as IngestionStage,
  parsedData: null,
  editedExercises: [] as ParsedExerciseItem[],
  error: null,
  isSaving: false,
  quota: null,
  profileId: '',
};

export const useAiIngestionStore = create<AiIngestionState>((set, get) => ({
  ...initial,

  setInput(value) {
    set({ input: value, error: null });
  },

  setInputMode(mode) {
    set({ inputMode: mode, error: null });
  },

  setProfileId(profileId) {
    const trimmed = profileId?.trim() ?? '';
    set({ profileId: trimmed });
  },

  async refreshQuota() {
    try {
      const profileId = await resolveProfileId(get().profileId || null);
      set({ profileId });
      const quota = await getQuotaStatus(profileId);
      set({ quota });
    } catch {
      set({ quota: null });
    }
  },

  async startIngestion(overrideInput?: string) {
    const input = (overrideInput ?? get().input).trim();
    if (overrideInput != null) {
      set({ input: overrideInput, error: null });
    }

    let profileId: string;
    try {
      profileId = await resolveProfileId(get().profileId || null);
      set({ profileId });
    } catch (e) {
      set({
        stage: 'IDLE',
        error: {
          code: 'PARSING_FAILED',
          message: e instanceof Error ? e.message : 'No active profile',
          recoveryAction: 'Finish onboarding, then retry',
        },
      });
      return;
    }

    set({ stage: 'CHECKING_GLOBAL_CACHE', error: null });
    const result = await ingestWorkoutInput({
      input,
      profileId,
      onStage: (stage) => set({ stage }),
    });

    if ('error' in result) {
      set({ stage: 'IDLE', error: result.error });
      await get().refreshQuota();
      return;
    }

    set({
      parsedData: result.data,
      editedExercises: result.data.exercises,
      stage: 'READY_FOR_REVIEW',
      error: null,
    });
    await get().refreshQuota();
  },

  removeExercise(id) {
    set((s) => ({
      editedExercises: s.editedExercises.filter((e) => e.id !== id),
    }));
  },

  updateExerciseSets(id, sets) {
    set((s) => ({
      editedExercises: s.editedExercises.map((e) =>
        e.id === id ? { ...e, sets: Math.min(10, Math.max(1, sets)) } : e
      ),
    }));
  },

  updateExerciseReps(id, min, max) {
    set((s) => ({
      editedExercises: s.editedExercises.map((e) =>
        e.id === id
          ? { ...e, repsMin: Math.max(1, min), repsMax: Math.max(min, max) }
          : e
      ),
    }));
  },

  async confirmAndSave() {
    const { parsedData, editedExercises, isSaving } = get();
    if (!parsedData || !editedExercises.length || isSaving) return null;
    set({ isSaving: true, error: null });
    try {
      const profileId = await resolveProfileId(get().profileId || null);
      set({ profileId });
      const routineId = await saveIngestedRoutine({
        parsed: parsedData,
        exercises: editedExercises,
        profileId,
      });
      set({ isSaving: false });
      return routineId;
    } catch (e) {
      set({
        isSaving: false,
        error: {
          code: 'PARSING_FAILED',
          message: e instanceof Error ? e.message : 'Save failed',
          recoveryAction: 'Retry save',
        },
      });
      return null;
    }
  },

  reset() {
    const { profileId, quota } = get();
    set({ ...initial, profileId, quota });
  },
}));
