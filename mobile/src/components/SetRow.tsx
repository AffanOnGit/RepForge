import { Pressable, StyleSheet, Text, View } from 'react-native';
import * as Haptics from 'expo-haptics';
import { StepperCounter } from './StepperCounter';
import { colors, touchTarget } from '@/src/theme/colors';
import { SET_TYPE_LABEL, type SetType, type UnitSystem } from '@/src/domain/types';
import { UnitConverter } from '@/src/domain/engines';

type Props = {
  setNumber: number;
  setType: SetType;
  weightKg: number;
  reps: number;
  isCompleted: boolean;
  ghostText?: string | null;
  unitSystem: UnitSystem;
  onToggleComplete: () => void;
  onCycleType: () => void;
  onWeightChange: (kg: number) => void;
  onRepsChange: (reps: number) => void;
};

export function SetRow({
  setNumber,
  setType,
  weightKg,
  reps,
  isCompleted,
  ghostText,
  unitSystem,
  onToggleComplete,
  onCycleType,
  onWeightChange,
  onRepsChange,
}: Props) {
  const displayWeight = UnitConverter.kgToDisplayWeight(weightKg, unitSystem);
  const weightStep = unitSystem === 'metric' ? 2.5 : 5;

  return (
    <View style={[styles.row, isCompleted && styles.completed]}>
      <Pressable
        accessibilityLabel={`Set type ${SET_TYPE_LABEL[setType]}, tap to cycle`}
        onPress={onCycleType}
        style={styles.typeBadge}
      >
        <Text style={styles.typeText}>
          {setType === 'WORKING' ? String(setNumber) : SET_TYPE_LABEL[setType]}
        </Text>
      </Pressable>

      <View style={styles.inputs}>
        <StepperCounter
          label={unitSystem === 'metric' ? 'kg' : 'lb'}
          value={displayWeight}
          step={weightStep}
          decimals={1}
          onChange={(v) => onWeightChange(UnitConverter.displayWeightToKg(v, unitSystem))}
        />
        <StepperCounter label="reps" value={reps} step={1} onChange={onRepsChange} />
      </View>

      <Pressable
        accessibilityRole="checkbox"
        accessibilityState={{ checked: isCompleted }}
        accessibilityLabel={`Mark set ${setNumber} complete`}
        onPress={async () => {
          await Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium);
          onToggleComplete();
        }}
        style={[styles.check, isCompleted && styles.checkOn]}
      >
        <Text style={[styles.checkMark, isCompleted && styles.checkMarkOn]}>
          {isCompleted ? '✓' : ''}
        </Text>
      </Pressable>

      {ghostText ? <Text style={styles.ghost}>{ghostText}</Text> : null}
    </View>
  );
}

const styles = StyleSheet.create({
  row: {
    gap: 10,
    paddingVertical: 10,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: colors.carbonSlateCard,
  },
  completed: { opacity: 0.95 },
  typeBadge: {
    alignSelf: 'flex-start',
    minWidth: 36,
    minHeight: 36,
    borderRadius: 8,
    backgroundColor: colors.carbonSlateSurface,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 8,
  },
  typeText: {
    color: colors.forgeAmber,
    fontFamily: 'IBMPlexMono_500Medium',
    fontSize: 13,
  },
  inputs: { flexDirection: 'row', gap: 10 },
  check: {
    position: 'absolute',
    right: 0,
    top: 10,
    width: touchTarget.min,
    height: touchTarget.min,
    borderRadius: 12,
    borderWidth: 2,
    borderColor: colors.carbonSlateCard,
    backgroundColor: colors.carbonSlateSurface,
    alignItems: 'center',
    justifyContent: 'center',
  },
  checkOn: {
    backgroundColor: colors.kineticLime,
    borderColor: colors.kineticLime,
  },
  checkMark: { fontSize: 20, color: colors.carbonSlate },
  checkMarkOn: { color: colors.carbonSlate, fontWeight: '700' },
  ghost: {
    color: colors.textGhost,
    fontFamily: 'IBMPlexMono_400Regular',
    fontSize: 12,
    marginTop: -4,
  },
});
