import { create } from 'zustand';
import { database } from '@/src/data/database';
import { CaloricEngine } from '@/src/domain/engines';
import { healthSyncService } from '@/src/features/health';
import type {
  Exercise,
  PersonalRecord,
  SetType,
  UnitSystem,
  WorkoutSet,
} from '@/src/domain/types';
import { SET_TYPE_CYCLE } from '@/src/domain/types';

export type ActiveExerciseSetUi = {
  set: WorkoutSet;
  ghostText?: string | null;
};

export type ActiveExerciseUi = {
  exercise: Exercise;
  sets: ActiveExerciseSetUi[];
  defaultRestSeconds: number;
};

type SessionStore = {
  sessionId: string;
  routineId: string | null;
  routineName: string;
  exercises: ActiveExerciseUi[];
  startedAtMillis: number;
  elapsedSeconds: number;
  totalTonnageKg: number;
  estimatedCalories: number;
  unitSystem: UnitSystem;
  isRestTimerVisible: boolean;
  restTimerRemainingSeconds: number;
  restTimerTotalSeconds: number;
  latestPR: PersonalRecord | null;
  isProfileMissing: boolean;
  healthConnectSynced: boolean;
  timerHandle: ReturnType<typeof setInterval> | null;
  restHandle: ReturnType<typeof setInterval> | null;

  reset: () => void;
  startFreestyle: () => Promise<void>;
  startFromRoutine: (routineId: string) => Promise<void>;
  tickElapsed: () => void;
  toggleSetComplete: (exerciseIndex: number, setIndex: number) => Promise<void>;
  cycleSetType: (exerciseIndex: number, setIndex: number) => Promise<void>;
  updateWeight: (exerciseIndex: number, setIndex: number, weightKg: number) => Promise<void>;
  updateReps: (exerciseIndex: number, setIndex: number, reps: number) => Promise<void>;
  addSet: (exerciseIndex: number) => Promise<void>;
  startRestTimer: (seconds: number) => void;
  add30sRest: () => void;
  subtract15sRest: () => void;
  skipRestTimer: () => void;
  swapExercise: (exerciseIndex: number, newExercise: Exercise) => Promise<void>;
  finishWorkout: () => Promise<string>;
};

const initial = {
  sessionId: '',
  routineId: null as string | null,
  routineName: 'Freestyle Workout',
  exercises: [] as ActiveExerciseUi[],
  startedAtMillis: Date.now(),
  elapsedSeconds: 0,
  totalTonnageKg: 0,
  estimatedCalories: 0,
  unitSystem: 'metric' as UnitSystem,
  isRestTimerVisible: false,
  restTimerRemainingSeconds: 90,
  restTimerTotalSeconds: 90,
  latestPR: null as PersonalRecord | null,
  isProfileMissing: false,
  healthConnectSynced: false,
  timerHandle: null as ReturnType<typeof setInterval> | null,
  restHandle: null as ReturnType<typeof setInterval> | null,
};

function tonnage(exercises: ActiveExerciseUi[]) {
  return exercises
    .flatMap((e) => e.sets)
    .filter((s) => s.set.isCompleted && s.set.setType !== 'WARMUP')
    .reduce((sum, s) => sum + s.set.weightKg * s.set.repsCompleted, 0);
}

export const useSessionStore = create<SessionStore>((set, get) => ({
  ...initial,

  reset() {
    const { timerHandle, restHandle } = get();
    if (timerHandle) clearInterval(timerHandle);
    if (restHandle) clearInterval(restHandle);
    set({ ...initial, startedAtMillis: Date.now() });
  },

  async startFreestyle() {
    get().reset();
    const profile = await database.getActiveProfile();
    const profileId = profile?.id ?? (await database.getActiveProfileId()) ?? '';
    const sessionId = crypto.randomUUID();
    const defaultEx =
      (await database.getExerciseById('ex_flat_bb_bench')) ??
      (await database.getAllExercises())[0];

    const exercises: ActiveExerciseUi[] = defaultEx
      ? [
          {
            exercise: defaultEx,
            defaultRestSeconds: 90,
            sets: [1, 2, 3].map((n) => ({
              set: {
                id: crypto.randomUUID(),
                sessionId,
                exerciseId: defaultEx.id,
                exerciseGroupId: null,
                setNumber: n,
                setType: 'WORKING',
                weightKg: 60,
                repsCompleted: 10,
                targetReps: 10,
                isCompleted: false,
                rpe: null,
                completedAtMillis: null,
              },
            })),
          },
        ]
      : [];

    await database.startSession({
      id: sessionId,
      profileId,
      routineId: null,
      routineName: 'Freestyle Workout',
      state: 'ACTIVE',
      startedAtMillis: Date.now(),
      completedAtMillis: null,
      totalTonnageKg: 0,
      estimatedCaloriesLow: 0,
      estimatedCaloriesHigh: 0,
      notes: '',
    });

    const timerHandle = setInterval(() => get().tickElapsed(), 1000);
    set({
      sessionId,
      routineName: 'Freestyle Workout',
      exercises,
      unitSystem: profile?.unitSystem ?? 'metric',
      isProfileMissing: !profile,
      startedAtMillis: Date.now(),
      timerHandle,
    });
  },

  async startFromRoutine(routineId: string) {
    get().reset();
    const profile = await database.getActiveProfile();
    const routine = await database.getRoutineById(routineId);
    if (!routine) throw new Error('Routine not found');
    const profileId = profile?.id ?? routine.profileId;
    if (!profileId) throw new Error('No active profile');

    const sessionId = crypto.randomUUID();
    const exercises: ActiveExerciseUi[] = [];

    for (const re of routine.exercises) {
      const exercise =
        re.exercise ?? (await database.getExerciseById(re.exerciseId));
      if (!exercise) continue;
      const lastSets = await database.getLastSetsForExercise(exercise.id);
      const sets = Array.from({ length: re.prescribedSets }, (_, i) => {
        const ghost = lastSets[i];
        return {
          ghostText: ghost
            ? `Prev: ${Math.round(ghost.weightKg)} kg × ${ghost.repsCompleted}`
            : null,
          set: {
            id: crypto.randomUUID(),
            sessionId,
            exerciseId: exercise.id,
            exerciseGroupId: re.exerciseGroupId,
            setNumber: i + 1,
            setType: 'WORKING' as SetType,
            weightKg: ghost?.weightKg ?? 40,
            repsCompleted: ghost?.repsCompleted ?? re.prescribedRepsMax,
            targetReps: re.prescribedRepsMax,
            isCompleted: false,
            rpe: null,
            completedAtMillis: null,
          },
        };
      });
      exercises.push({
        exercise,
        sets,
        defaultRestSeconds: re.restSeconds,
      });
    }

    await database.startSession({
      id: sessionId,
      profileId,
      routineId,
      routineName: routine.name,
      state: 'ACTIVE',
      startedAtMillis: Date.now(),
      completedAtMillis: null,
      totalTonnageKg: 0,
      estimatedCaloriesLow: 0,
      estimatedCaloriesHigh: 0,
      notes: '',
    });

    const timerHandle = setInterval(() => get().tickElapsed(), 1000);
    set({
      sessionId,
      routineId,
      routineName: routine.name,
      exercises,
      unitSystem: profile?.unitSystem ?? 'metric',
      isProfileMissing: !profile,
      startedAtMillis: Date.now(),
      timerHandle,
    });
  },

  tickElapsed() {
    set((state) => {
      const elapsedSeconds = state.elapsedSeconds + 1;
      const minutes = elapsedSeconds / 60;
      return {
        elapsedSeconds,
        estimatedCalories: Math.round(minutes * 5.5 + state.totalTonnageKg * 0.015),
      };
    });
  },

  async toggleSetComplete(exerciseIndex, setIndex) {
    const state = get();
    const currentEx = state.exercises[exerciseIndex];
    const currentSetUi = currentEx?.sets[setIndex];
    if (!currentEx || !currentSetUi) return;

    const toggled: WorkoutSet = {
      ...currentSetUi.set,
      isCompleted: !currentSetUi.set.isCompleted,
      completedAtMillis: !currentSetUi.set.isCompleted ? Date.now() : null,
    };
    await database.logSet(toggled);

    let latestPR: PersonalRecord | null = state.latestPR;
    if (toggled.isCompleted) {
      latestPR =
        (await database.checkAndRecordPR(toggled, currentEx.exercise.name)) ?? latestPR;
    }

    const exercises = state.exercises.map((ex, ei) =>
      ei !== exerciseIndex
        ? ex
        : {
            ...ex,
            sets: ex.sets.map((s, si) =>
              si === setIndex ? { ...s, set: toggled } : s
            ),
          }
    );

    set({ exercises, totalTonnageKg: tonnage(exercises), latestPR });
    if (toggled.isCompleted) get().startRestTimer(currentEx.defaultRestSeconds);
  },

  async cycleSetType(exerciseIndex, setIndex) {
    const state = get();
    const currentEx = state.exercises[exerciseIndex];
    const currentSetUi = currentEx?.sets[setIndex];
    if (!currentEx || !currentSetUi) return;
    const idx = SET_TYPE_CYCLE.indexOf(currentSetUi.set.setType);
    const next = SET_TYPE_CYCLE[(idx + 1) % SET_TYPE_CYCLE.length];
    const updated = { ...currentSetUi.set, setType: next };
    await database.updateSet(updated);
    const exercises = state.exercises.map((ex, ei) =>
      ei !== exerciseIndex
        ? ex
        : {
            ...ex,
            sets: ex.sets.map((s, si) =>
              si === setIndex ? { ...s, set: updated } : s
            ),
          }
    );
    set({ exercises, totalTonnageKg: tonnage(exercises) });
  },

  async updateWeight(exerciseIndex, setIndex, weightKg) {
    const state = get();
    const currentEx = state.exercises[exerciseIndex];
    const currentSetUi = currentEx?.sets[setIndex];
    if (!currentEx || !currentSetUi) return;
    const updated = { ...currentSetUi.set, weightKg };
    await database.updateSet(updated);
    const exercises = state.exercises.map((ex, ei) =>
      ei !== exerciseIndex
        ? ex
        : {
            ...ex,
            sets: ex.sets.map((s, si) =>
              si === setIndex ? { ...s, set: updated } : s
            ),
          }
    );
    set({ exercises });
  },

  async updateReps(exerciseIndex, setIndex, reps) {
    const state = get();
    const currentEx = state.exercises[exerciseIndex];
    const currentSetUi = currentEx?.sets[setIndex];
    if (!currentEx || !currentSetUi) return;
    const updated = { ...currentSetUi.set, repsCompleted: reps };
    await database.updateSet(updated);
    const exercises = state.exercises.map((ex, ei) =>
      ei !== exerciseIndex
        ? ex
        : {
            ...ex,
            sets: ex.sets.map((s, si) =>
              si === setIndex ? { ...s, set: updated } : s
            ),
          }
    );
    set({ exercises });
  },

  async addSet(exerciseIndex) {
    const state = get();
    const currentEx = state.exercises[exerciseIndex];
    if (!currentEx) return;
    const last = currentEx.sets[currentEx.sets.length - 1]?.set;
    const newSet: WorkoutSet = {
      id: crypto.randomUUID(),
      sessionId: state.sessionId,
      exerciseId: currentEx.exercise.id,
      exerciseGroupId: last?.exerciseGroupId ?? null,
      setNumber: currentEx.sets.length + 1,
      setType: 'WORKING',
      weightKg: last?.weightKg ?? 40,
      repsCompleted: last?.repsCompleted ?? 10,
      targetReps: last?.targetReps ?? null,
      isCompleted: false,
      rpe: null,
      completedAtMillis: null,
    };
    await database.logSet(newSet);
    const exercises = state.exercises.map((ex, ei) =>
      ei !== exerciseIndex ? ex : { ...ex, sets: [...ex.sets, { set: newSet }] }
    );
    set({ exercises });
  },

  startRestTimer(seconds) {
    const { restHandle } = get();
    if (restHandle) clearInterval(restHandle);
    set({
      isRestTimerVisible: true,
      restTimerTotalSeconds: seconds,
      restTimerRemainingSeconds: seconds,
    });
    const handle = setInterval(() => {
      const remaining = get().restTimerRemainingSeconds - 1;
      if (remaining <= 0) {
        clearInterval(handle);
        set({
          restTimerRemainingSeconds: 0,
          isRestTimerVisible: false,
          restHandle: null,
        });
      } else {
        set({ restTimerRemainingSeconds: remaining });
      }
    }, 1000);
    set({ restHandle: handle });
  },

  add30sRest() {
    set((s) => ({
      restTimerTotalSeconds: s.restTimerTotalSeconds + 30,
      restTimerRemainingSeconds: s.restTimerRemainingSeconds + 30,
    }));
  },

  subtract15sRest() {
    set((s) => {
      const remaining = Math.max(0, s.restTimerRemainingSeconds - 15);
      return {
        restTimerRemainingSeconds: remaining,
        isRestTimerVisible: remaining > 0,
      };
    });
  },

  skipRestTimer() {
    const { restHandle } = get();
    if (restHandle) clearInterval(restHandle);
    set({ isRestTimerVisible: false, restTimerRemainingSeconds: 0, restHandle: null });
  },

  async swapExercise(exerciseIndex, newExercise) {
    const state = get();
    const currentEx = state.exercises[exerciseIndex];
    if (!currentEx) return;
    const updatedSets = currentEx.sets.map((s) => ({
      ...s,
      set: { ...s.set, exerciseId: newExercise.id },
    }));
    for (const s of updatedSets) await database.updateSet(s.set);
    const exercises = state.exercises.map((ex, ei) =>
      ei !== exerciseIndex
        ? ex
        : { ...ex, exercise: newExercise, sets: updatedSets }
    );
    set({ exercises });
  },

  async finishWorkout() {
    const state = get();
    if (state.timerHandle) clearInterval(state.timerHandle);
    if (state.restHandle) clearInterval(state.restHandle);

    const profile = await database.getActiveProfile();
    const allSets = state.exercises.flatMap((e) => e.sets.map((s) => s.set));
    for (const s of allSets) {
      if (s.isCompleted) await database.logSet(s);
    }
    const durationMinutes = Math.max(1, state.elapsedSeconds / 60);
    const calories = CaloricEngine.estimateSessionCalories(
      profile,
      durationMinutes,
      allSets
    );
    await database.updateSession({
      id: state.sessionId,
      profileId: profile?.id ?? '',
      routineId: state.routineId,
      routineName: state.routineName,
      state: 'COMPLETED',
      startedAtMillis: state.startedAtMillis,
      completedAtMillis: Date.now(),
      totalTonnageKg: state.totalTonnageKg,
      estimatedCaloriesLow: calories.lowBoundKcal,
      estimatedCaloriesHigh: calories.highBoundKcal,
      notes: '',
    });
    await database.completeSession(state.sessionId);

    const midKcal = (calories.lowBoundKcal + calories.highBoundKcal) / 2;
    const exportResult = await healthSyncService.exportCompletedWorkout({
      title: state.routineName || 'RepForge Strength',
      startTimeMillis: state.startedAtMillis,
      endTimeMillis: Date.now(),
      caloriesKcal: midKcal,
      sessionId: state.sessionId,
    });
    set({ healthConnectSynced: exportResult.exported });

    return state.sessionId;
  },
}));
