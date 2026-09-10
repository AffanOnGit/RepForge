import { useEffect, useState } from 'react';
import {
  Modal,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { RestTimerBar } from '@/src/components/RestTimerBar';
import { ForgeButton } from '@/src/components/ForgeButton';
import { SetRow } from '@/src/components/SetRow';
import { PlateMath } from '@/src/domain/engines';
import { database } from '@/src/data/database';
import type { Exercise } from '@/src/domain/types';
import { useSessionStore } from '@/src/stores/sessionStore';
import { colors, spacing } from '@/src/theme/colors';

export default function ActiveSessionScreen() {
  const { routineId } = useLocalSearchParams<{ routineId: string }>();
  const router = useRouter();
  const store = useSessionStore();
  const [swapIndex, setSwapIndex] = useState<number | null>(null);
  const [suggestions, setSuggestions] = useState<Exercise[]>([]);
  const [plateOpen, setPlateOpen] = useState(false);
  const [finishing, setFinishing] = useState(false);

  useEffect(() => {
    if (routineId === 'freestyle' || !routineId) {
      store.startFreestyle();
    } else {
      store.startFromRoutine(routineId);
    }
    return () => {
      // leave timers if navigating to summary after finish; otherwise clear on unmount mid-session
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [routineId]);

  const openSwap = async (index: number) => {
    const ex = store.exercises[index]?.exercise;
    if (!ex) return;
    const list = await database.getSwapSuggestions(ex.id);
    setSuggestions(list);
    setSwapIndex(index);
  };

  const finish = async () => {
    if (finishing) return;
    setFinishing(true);
    const sessionId = await store.finishWorkout();
    router.replace(`/workout-summary/${sessionId}`);
  };

  const mm = String(Math.floor(store.elapsedSeconds / 60)).padStart(2, '0');
  const ss = String(store.elapsedSeconds % 60).padStart(2, '0');
  const plates = PlateMath.calculatePlatesPerSide(
    store.exercises[0]?.sets[0]?.set.weightKg ?? 60,
    20,
    store.unitSystem
  );

  return (
    <View style={styles.container}>
      <View style={styles.hud}>
        <View>
          <Text style={styles.routine}>{store.routineName}</Text>
          <Text style={styles.timer}>
            {mm}:{ss}
          </Text>
        </View>
        <View style={styles.hudRight}>
          <Text style={styles.stat}>{Math.round(store.totalTonnageKg)} kg</Text>
          <Text style={styles.statSub}>~{store.estimatedCalories} kcal</Text>
          {store.isProfileMissing ? (
            <Text style={styles.warn}>Using default biometrics</Text>
          ) : null}
        </View>
      </View>

      <ScrollView contentContainerStyle={styles.list}>
        {store.exercises.map((ex, ei) => (
          <View key={`${ex.exercise.id}-${ei}`} style={styles.card}>
            <View style={styles.cardHeader}>
              <Text style={styles.exName}>{ex.exercise.name}</Text>
              <Pressable onPress={() => openSwap(ei)}>
                <Text style={styles.swap}>Swap</Text>
              </Pressable>
            </View>
            {ex.sets.map((setUi, si) => (
              <SetRow
                key={setUi.set.id}
                setNumber={setUi.set.setNumber}
                setType={setUi.set.setType}
                weightKg={setUi.set.weightKg}
                reps={setUi.set.repsCompleted}
                isCompleted={setUi.set.isCompleted}
                ghostText={setUi.ghostText}
                unitSystem={store.unitSystem}
                onToggleComplete={() => store.toggleSetComplete(ei, si)}
                onCycleType={() => store.cycleSetType(ei, si)}
                onWeightChange={(kg) => store.updateWeight(ei, si, kg)}
                onRepsChange={(r) => store.updateReps(ei, si, r)}
              />
            ))}
            <View style={styles.cardActions}>
              <ForgeButton title="Add set" variant="ghost" onPress={() => store.addSet(ei)} />
              <ForgeButton
                title="Plate math"
                variant="outline"
                onPress={() => setPlateOpen(true)}
              />
            </View>
          </View>
        ))}
      </ScrollView>

      {store.isRestTimerVisible ? (
        <RestTimerBar
          remaining={store.restTimerRemainingSeconds}
          total={store.restTimerTotalSeconds}
          onMinus15={store.subtract15sRest}
          onPlus30={store.add30sRest}
          onSkip={store.skipRestTimer}
        />
      ) : null}

      <View style={styles.footer}>
        <ForgeButton title={finishing ? 'Saving…' : 'Finish workout'} fullWidth onPress={finish} disabled={finishing} />
      </View>

      <Modal visible={swapIndex !== null} transparent animationType="slide">
        <View style={styles.modalBackdrop}>
          <View style={styles.sheet}>
            <Text style={styles.sheetTitle}>Swap exercise</Text>
            <ScrollView>
              {suggestions.map((s) => (
                <Pressable
                  key={s.id}
                  style={styles.suggestion}
                  onPress={async () => {
                    if (swapIndex == null) return;
                    await store.swapExercise(swapIndex, s);
                    setSwapIndex(null);
                  }}
                >
                  <Text style={styles.suggestionName}>{s.name}</Text>
                  <Text style={styles.suggestionMeta}>{s.equipment}</Text>
                </Pressable>
              ))}
            </ScrollView>
            <ForgeButton title="Cancel" variant="ghost" onPress={() => setSwapIndex(null)} />
          </View>
        </View>
      </Modal>

      <Modal visible={plateOpen} transparent animationType="fade">
        <View style={styles.modalBackdrop}>
          <View style={styles.sheet}>
            <Text style={styles.sheetTitle}>Plates per side</Text>
            <Text style={styles.suggestionMeta}>
              {plates.length ? plates.join(' + ') : 'Bar only'}
            </Text>
            <ForgeButton title="Close" onPress={() => setPlateOpen(false)} />
          </View>
        </View>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.carbonSlate },
  hud: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.md,
    borderBottomWidth: 1,
    borderBottomColor: colors.carbonSlateCard,
  },
  routine: { color: colors.textSecondary, fontFamily: 'SpaceGrotesk_500Medium' },
  timer: {
    color: colors.textPrimary,
    fontFamily: 'IBMPlexMono_500Medium',
    fontSize: 36,
    fontVariant: ['tabular-nums'],
  },
  hudRight: { alignItems: 'flex-end' },
  stat: { color: colors.kineticLime, fontFamily: 'IBMPlexMono_500Medium', fontSize: 18 },
  statSub: { color: colors.textTertiary, fontFamily: 'IBMPlexMono_400Regular' },
  warn: { color: colors.warningAmber, fontSize: 11, marginTop: 4 },
  list: { padding: spacing.lg, paddingBottom: 120, gap: 14 },
  card: {
    backgroundColor: colors.carbonSlateLight,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: colors.carbonSlateCard,
    padding: spacing.lg,
    marginBottom: 12,
  },
  cardHeader: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 8 },
  exName: { color: colors.textPrimary, fontFamily: 'SpaceGrotesk_600SemiBold', fontSize: 17, flex: 1 },
  swap: { color: colors.forgeAmber, fontFamily: 'SpaceGrotesk_500Medium' },
  cardActions: { flexDirection: 'row', justifyContent: 'space-between', marginTop: 8 },
  footer: {
    padding: spacing.lg,
    borderTopWidth: 1,
    borderTopColor: colors.carbonSlateCard,
    backgroundColor: colors.carbonSlateLight,
  },
  modalBackdrop: {
    flex: 1,
    backgroundColor: '#00000099',
    justifyContent: 'flex-end',
  },
  sheet: {
    backgroundColor: colors.carbonSlateLight,
    borderTopLeftRadius: 20,
    borderTopRightRadius: 20,
    padding: spacing.xl,
    maxHeight: '70%',
    gap: 10,
  },
  sheetTitle: { color: colors.textPrimary, fontFamily: 'SpaceGrotesk_700Bold', fontSize: 20 },
  suggestion: {
    paddingVertical: 12,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: colors.carbonSlateCard,
  },
  suggestionName: { color: colors.textPrimary, fontFamily: 'SpaceGrotesk_500Medium' },
  suggestionMeta: { color: colors.textTertiary, marginTop: 2 },
});
