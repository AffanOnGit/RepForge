/** Domain types ported from Android core-domain. Storage is always metric. */

export type UnitSystem = 'metric' | 'imperial';
export type BiologicalSex = 'MALE' | 'FEMALE';
export type TrainingExperience = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';

export type Equipment =
  | 'BARBELL'
  | 'DUMBBELL'
  | 'CABLE'
  | 'MACHINE'
  | 'BODYWEIGHT'
  | 'KETTLEBELL'
  | 'SMITH_MACHINE';

export type MuscleGroup = 'CHEST' | 'SHOULDERS' | 'BACK' | 'ARMS' | 'LEGS' | 'CORE';

export type SubMuscle =
  | 'UPPER_CHEST'
  | 'MID_CHEST'
  | 'LOWER_CHEST'
  | 'FRONT_DELTS'
  | 'SIDE_DELTS'
  | 'REAR_DELTS'
  | 'LATS'
  | 'UPPER_BACK_TRAPS'
  | 'LOWER_BACK'
  | 'BICEPS'
  | 'TRICEPS'
  | 'FOREARMS'
  | 'QUADS'
  | 'HAMSTRINGS'
  | 'GLUTES'
  | 'CALVES'
  | 'UPPER_ABS'
  | 'LOWER_ABS'
  | 'OBLIQUES';

export const SUB_MUSCLE_META: Record<
  SubMuscle,
  { displayName: string; parentGroup: MuscleGroup }
> = {
  UPPER_CHEST: { displayName: 'Upper Chest', parentGroup: 'CHEST' },
  MID_CHEST: { displayName: 'Mid Chest', parentGroup: 'CHEST' },
  LOWER_CHEST: { displayName: 'Lower Chest', parentGroup: 'CHEST' },
  FRONT_DELTS: { displayName: 'Front Delts', parentGroup: 'SHOULDERS' },
  SIDE_DELTS: { displayName: 'Side Delts', parentGroup: 'SHOULDERS' },
  REAR_DELTS: { displayName: 'Rear Delts', parentGroup: 'SHOULDERS' },
  LATS: { displayName: 'Lats (Width)', parentGroup: 'BACK' },
  UPPER_BACK_TRAPS: { displayName: 'Upper Back / Traps', parentGroup: 'BACK' },
  LOWER_BACK: { displayName: 'Lower Back', parentGroup: 'BACK' },
  BICEPS: { displayName: 'Biceps', parentGroup: 'ARMS' },
  TRICEPS: { displayName: 'Triceps', parentGroup: 'ARMS' },
  FOREARMS: { displayName: 'Forearms', parentGroup: 'ARMS' },
  QUADS: { displayName: 'Quads', parentGroup: 'LEGS' },
  HAMSTRINGS: { displayName: 'Hamstrings', parentGroup: 'LEGS' },
  GLUTES: { displayName: 'Glutes', parentGroup: 'LEGS' },
  CALVES: { displayName: 'Calves', parentGroup: 'LEGS' },
  UPPER_ABS: { displayName: 'Upper Abs', parentGroup: 'CORE' },
  LOWER_ABS: { displayName: 'Lower Abs', parentGroup: 'CORE' },
  OBLIQUES: { displayName: 'Obliques', parentGroup: 'CORE' },
};

export type SetType = 'WARMUP' | 'WORKING' | 'DROP_SET' | 'FAILURE';
export type SessionState = 'IDLE' | 'ACTIVE' | 'PAUSED' | 'FINISHING' | 'COMPLETED';
export type ExerciseGroupType =
  | 'SINGLE'
  | 'SUPERSET'
  | 'TRISET'
  | 'CIRCUIT'
  | 'AMRAP'
  | 'EMOM';
export type PRType = 'WEIGHT' | 'REPS' | 'VOLUME';

export interface Exercise {
  id: string;
  name: string;
  equipment: Equipment;
  primarySubMuscle: SubMuscle;
  secondarySubMuscles: SubMuscle[];
  creatorTags: string[];
  isCustom: boolean;
}

export interface UserProfile {
  id: string;
  /** Display name for multi-profile switcher */
  displayName: string;
  /** Optional label only — not used for cloud auth */
  email: string;
  weightKg: number | null;
  heightCm: number | null;
  age: number | null;
  biologicalSex: BiologicalSex | null;
  bodyFatPercentage: number | null;
  trainingExperience: TrainingExperience;
  unitSystem: UnitSystem;
  createdAtMillis: number;
  updatedAtMillis: number;
}

export interface ExerciseGroup {
  id: string;
  groupType: ExerciseGroupType;
  orderInRoutine: number;
  restAfterGroupSeconds: number;
  timeCapSeconds: number | null;
}

export interface RoutineExercise {
  id: string;
  exerciseGroupId: string;
  exerciseId: string;
  exercise?: Exercise;
  orderInGroup: number;
  prescribedSets: number;
  prescribedRepsMin: number;
  prescribedRepsMax: number;
  restSeconds: number;
  executionNotes: string;
}

export interface Routine {
  id: string;
  /** Owner profile — all routine CRUD is scoped to this id */
  profileId: string;
  name: string;
  description: string;
  estimatedDurationMinutes: number | null;
  creatorName: string | null;
  sourceVideoId: string | null;
  exerciseGroups: ExerciseGroup[];
  exercises: RoutineExercise[];
  createdAtMillis: number;
  updatedAtMillis: number;
}

export interface WorkoutSession {
  id: string;
  profileId: string;
  routineId: string | null;
  routineName: string;
  state: SessionState;
  startedAtMillis: number | null;
  completedAtMillis: number | null;
  totalTonnageKg: number;
  estimatedCaloriesLow: number;
  estimatedCaloriesHigh: number;
  notes: string;
}

export interface WorkoutSet {
  id: string;
  sessionId: string;
  exerciseId: string;
  exerciseGroupId: string | null;
  setNumber: number;
  setType: SetType;
  weightKg: number;
  repsCompleted: number;
  targetReps: number | null;
  isCompleted: boolean;
  rpe: number | null;
  completedAtMillis: number | null;
}

export interface PersonalRecord {
  id: string;
  profileId: string;
  exerciseId: string;
  exerciseName: string;
  type: PRType;
  value: number;
  achievedAtMillis: number;
  sessionId: string;
}

/** Extension hook: AI ingestion / Health Connect / export should filter by this. */
export type ProfileScoped = { profileId: string };

export const SET_TYPE_LABEL: Record<SetType, string> = {
  WARMUP: 'W',
  WORKING: 'Working',
  DROP_SET: 'D',
  FAILURE: 'F',
};

export const SET_TYPE_CYCLE: SetType[] = ['WORKING', 'WARMUP', 'DROP_SET', 'FAILURE'];
