import type {
  BiologicalSex,
  TrainingExperience,
  UnitSystem,
  UserProfile,
  WorkoutSet,
} from './types';

const KG_TO_LB = 2.20462;
const LB_TO_KG = 0.453592;
const CM_TO_INCH = 0.393701;
const INCH_TO_CM = 2.54;

export const UnitConverter = {
  kgToLb: (kg: number) => kg * KG_TO_LB,
  lbToKg: (lb: number) => lb * LB_TO_KG,
  cmToFeetInches(cm: number): { feet: number; inches: number } {
    const totalInches = cm * CM_TO_INCH;
    return { feet: Math.floor(totalInches / 12), inches: Math.floor(totalInches % 12) };
  },
  feetInchesToCm(feet: number, inches: number): number {
    return (feet * 12 + inches) * INCH_TO_CM;
  },
  formatWeight(kg: number, unitSystem: UnitSystem): string {
    return unitSystem === 'metric'
      ? `${kg.toFixed(1)} kg`
      : `${this.kgToLb(kg).toFixed(1)} lb`;
  },
  formatHeight(cm: number, unitSystem: UnitSystem): string {
    if (unitSystem === 'metric') return `${Math.round(cm)} cm`;
    const { feet, inches } = this.cmToFeetInches(cm);
    return `${feet}'${inches}"`;
  },
  displayWeightToKg(value: number, unitSystem: UnitSystem): number {
    return unitSystem === 'metric' ? value : this.lbToKg(value);
  },
  kgToDisplayWeight(kg: number, unitSystem: UnitSystem): number {
    return unitSystem === 'metric' ? kg : this.kgToLb(kg);
  },
};

const METRIC_PLATES = [25, 20, 15, 10, 5, 2.5, 1.25];
const IMPERIAL_PLATES = [45, 35, 25, 10, 5, 2.5];

export const PlateMath = {
  standardBarWeights: {
    'Olympic Barbell (20kg)': 20,
    "Women's Barbell (15kg)": 15,
    'EZ Curl Bar (10kg)': 10,
    'Trap Bar (25kg)': 25,
  } as Record<string, number>,

  calculatePlatesPerSide(
    totalWeightKg: number,
    barWeightKg = 20,
    unitSystem: UnitSystem = 'metric'
  ): number[] {
    const weightPerSide =
      unitSystem === 'metric'
        ? (totalWeightKg - barWeightKg) / 2
        : (UnitConverter.kgToLb(totalWeightKg) - UnitConverter.kgToLb(barWeightKg)) / 2;

    if (weightPerSide <= 0) return [];

    const plates = unitSystem === 'metric' ? METRIC_PLATES : IMPERIAL_PLATES;
    const result: number[] = [];
    let remaining = weightPerSide;
    for (const plate of plates) {
      while (remaining >= plate - 0.001) {
        result.push(plate);
        remaining -= plate;
      }
    }
    return result;
  },
};

export interface CalorieEstimate {
  basalKcal: number;
  mechanicalKcal: number;
  epocKcal: number;
  totalKcal: number;
  lowBoundKcal: number;
  highBoundKcal: number;
  confidenceBandPercentage: number;
}

/** Mifflin-St Jeor + mechanical tonnage + EPOC; ±15% confidence band. */
export const CaloricEngine = {
  calculateBMR(
    weightKg: number,
    heightCm: number,
    age: number,
    sex: BiologicalSex
  ): number {
    const base = 10 * weightKg + 6.25 * heightCm - 5 * age;
    return sex === 'MALE' ? base + 5 : base - 161;
  },

  estimateSessionCalories(
    userProfile: UserProfile | null | undefined,
    durationMinutes: number,
    completedSets: WorkoutSet[],
    averageHeartRateBpm?: number | null
  ): CalorieEstimate {
    const weightKg = userProfile?.weightKg ?? 75;
    const heightCm = userProfile?.heightCm ?? 175;
    const age = userProfile?.age ?? 25;
    const sex = userProfile?.biologicalSex ?? 'MALE';

    const dailyBMR = this.calculateBMR(weightKg, heightCm, age, sex);
    const bmrPerMinute = dailyBMR / 1440;
    const met = averageHeartRateBpm != null && averageHeartRateBpm > 130 ? 6.0 : 5.0;
    const basalKcal = bmrPerMinute * met * durationMinutes;

    const totalTonnage = completedSets
      .filter((s) => s.isCompleted && s.setType !== 'WARMUP')
      .reduce((sum, s) => sum + s.weightKg * s.repsCompleted, 0);

    const weightScaling = Math.min(1.5, Math.max(0.7, weightKg / 75));
    const mechanicalKcal = totalTonnage * 0.018 * weightScaling;

    const failureCount = completedSets.filter((s) => s.isCompleted && s.setType === 'FAILURE').length;
    const dropSetCount = completedSets.filter((s) => s.isCompleted && s.setType === 'DROP_SET').length;
    const workingCount = completedSets.filter((s) => s.isCompleted && s.setType === 'WORKING').length;

    const experienceFactor: Record<TrainingExperience, number> = {
      BEGINNER: 1.15,
      INTERMEDIATE: 1.0,
      ADVANCED: 0.9,
    };
    const exp = userProfile?.trainingExperience ?? 'INTERMEDIATE';
    const epocKcal = (failureCount * 8 + dropSetCount * 6 + workingCount * 2.5) * experienceFactor[exp];

    const totalKcal = basalKcal + mechanicalKcal + epocKcal;
    return {
      basalKcal,
      mechanicalKcal,
      epocKcal,
      totalKcal,
      lowBoundKcal: totalKcal * 0.85,
      highBoundKcal: totalKcal * 1.15,
      confidenceBandPercentage: 15,
    };
  },
};
