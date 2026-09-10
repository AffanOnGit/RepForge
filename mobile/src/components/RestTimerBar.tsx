import { StyleSheet, Text, View } from 'react-native';
import { ForgeButton } from './ForgeButton';
import { colors } from '@/src/theme/colors';

type Props = {
  remaining: number;
  total: number;
  onMinus15: () => void;
  onPlus30: () => void;
  onSkip: () => void;
};

export function RestTimerBar({ remaining, total, onMinus15, onPlus30, onSkip }: Props) {
  const progress = total > 0 ? remaining / total : 0;
  const mm = String(Math.floor(remaining / 60)).padStart(2, '0');
  const ss = String(remaining % 60).padStart(2, '0');

  return (
    <View style={styles.wrap} accessibilityLiveRegion="polite">
      <View style={styles.header}>
        <Text style={styles.title}>Rest</Text>
        <Text style={styles.time}>
          {mm}:{ss}
        </Text>
      </View>
      <View style={styles.track}>
        <View style={[styles.fill, { width: `${Math.max(0, Math.min(1, progress)) * 100}%` }]} />
      </View>
      <View style={styles.actions}>
        <ForgeButton title="−15s" variant="outline" onPress={onMinus15} />
        <ForgeButton title="+30s" variant="outline" onPress={onPlus30} />
        <ForgeButton title="Skip" variant="secondary" onPress={onSkip} />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: {
    backgroundColor: colors.carbonSlateLight,
    borderTopWidth: 1,
    borderTopColor: colors.carbonSlateCard,
    paddingHorizontal: 16,
    paddingVertical: 12,
    gap: 10,
  },
  header: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  title: {
    color: colors.textSecondary,
    fontFamily: 'SpaceGrotesk_500Medium',
    fontSize: 13,
    textTransform: 'uppercase',
    letterSpacing: 1,
  },
  time: {
    color: colors.kineticLime,
    fontFamily: 'IBMPlexMono_500Medium',
    fontSize: 28,
    fontVariant: ['tabular-nums'],
  },
  track: {
    height: 6,
    borderRadius: 3,
    backgroundColor: colors.carbonSlateCard,
    overflow: 'hidden',
  },
  fill: { height: '100%', backgroundColor: colors.forgeAmber },
  actions: { flexDirection: 'row', gap: 8, justifyContent: 'space-between' },
});
