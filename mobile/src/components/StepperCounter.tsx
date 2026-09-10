import { Pressable, StyleSheet, Text, View } from 'react-native';
import { colors, touchTarget } from '@/src/theme/colors';

type Props = {
  value: number;
  onChange: (next: number) => void;
  step?: number;
  min?: number;
  max?: number;
  decimals?: number;
  label?: string;
};

export function StepperCounter({
  value,
  onChange,
  step = 1,
  min = 0,
  max = 9999,
  decimals = 0,
  label,
}: Props) {
  const fmt = (n: number) => (decimals > 0 ? n.toFixed(decimals) : String(Math.round(n)));

  return (
    <View style={styles.wrap}>
      {label ? <Text style={styles.label}>{label}</Text> : null}
      <View style={styles.row}>
        <Pressable
          accessibilityLabel={`Decrease ${label ?? 'value'}`}
          style={({ pressed }) => [styles.btn, pressed && styles.pressed]}
          onPress={() => onChange(Math.max(min, +(value - step).toFixed(decimals)))}
        >
          <Text style={styles.btnText}>−</Text>
        </Pressable>
        <Text style={styles.value}>{fmt(value)}</Text>
        <Pressable
          accessibilityLabel={`Increase ${label ?? 'value'}`}
          style={({ pressed }) => [styles.btn, pressed && styles.pressed]}
          onPress={() => onChange(Math.min(max, +(value + step).toFixed(decimals)))}
        >
          <Text style={styles.btnText}>+</Text>
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: { gap: 4 },
  label: { color: colors.textTertiary, fontSize: 11, fontFamily: 'SpaceGrotesk_500Medium' },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: colors.carbonSlateSurface,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: colors.carbonSlateCard,
    overflow: 'hidden',
  },
  btn: {
    minWidth: touchTarget.stepper,
    minHeight: touchTarget.stepper,
    alignItems: 'center',
    justifyContent: 'center',
  },
  pressed: { opacity: 0.7 },
  btnText: {
    color: colors.forgeAmber,
    fontSize: 22,
    fontFamily: 'IBMPlexMono_500Medium',
  },
  value: {
    minWidth: 56,
    textAlign: 'center',
    color: colors.textPrimary,
    fontFamily: 'IBMPlexMono_500Medium',
    fontSize: 18,
    fontVariant: ['tabular-nums'],
  },
});
