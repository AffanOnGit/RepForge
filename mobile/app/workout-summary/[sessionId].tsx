import { useEffect, useState } from 'react';
import { Modal, StyleSheet, Text, View } from 'react-native';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { ForgeButton } from '@/src/components/ForgeButton';
import { CaloricEngine } from '@/src/domain/engines';
import { database } from '@/src/data/database';
import type { PersonalRecord, WorkoutSession, WorkoutSet } from '@/src/domain/types';
import { useSessionStore } from '@/src/stores/sessionStore';
import { colors, spacing } from '@/src/theme/colors';

export default function WorkoutSummaryScreen() {
  const { sessionId } = useLocalSearchParams<{ sessionId: string }>();
  const router = useRouter();
  const healthConnectSynced = useSessionStore((s) => s.healthConnectSynced);
  const [session, setSession] = useState<WorkoutSession | null>(null);
  const [sets, setSets] = useState<WorkoutSet[]>([]);
  const [prs, setPrs] = useState<PersonalRecord[]>([]);
  const [diffOpen, setDiffOpen] = useState(false);
  const [diffSummary, setDiffSummary] = useState('');

  useEffect(() => {
    (async () => {
      if (!sessionId) return;
      const s = await database.getSessionById(sessionId);
      const logged = await database.getSetsForSession(sessionId);
      const allPrs = await database.getAllPersonalRecords();
      const profile = await database.getActiveProfile();
      setSession(s);
      setSets(logged);
      setPrs(allPrs.filter((p) => p.sessionId === sessionId));

      if (s?.routineId) {
        const routine = await database.getRoutineById(s.routineId);
        if (routine) {
          const originalIds = new Set(routine.exercises.map((e) => e.exerciseId));
          const loggedIds = new Set(logged.map((x) => x.exerciseId));
          const swapped = [...loggedIds].filter((id) => !originalIds.has(id));
          const prescribed = routine.exercises.reduce((n, e) => n + e.prescribedSets, 0);
          if (swapped.length > 0 || logged.length !== prescribed) {
            setDiffSummary(
              `Changes detected from base routine (${swapped.length} swapped/added movements).`
            );
            setDiffOpen(true);
          }
        }
      }

      // Recompute calories if needed for display
      if (s && profile) {
        const duration = Math.max(
          1,
          ((s.completedAtMillis ?? Date.now()) - (s.startedAtMillis ?? Date.now())) / 60000
        );
        CaloricEngine.estimateSessionCalories(profile, duration, logged);
      }
    })();
  }, [sessionId]);

  const working = sets.filter((s) => s.isCompleted && s.setType !== 'WARMUP');
  const tonnage = working.reduce((n, s) => n + s.weightKg * s.repsCompleted, 0);
  const duration = Math.max(
    1,
    Math.round(
      ((session?.completedAtMillis ?? 0) - (session?.startedAtMillis ?? 0)) / 60000
    )
  );

  const updateBase = async () => {
    if (!session?.routineId) return;
    const base = await database.getRoutineById(session.routineId);
    if (!base) return;
    const completed = sets.filter((s) => s.isCompleted);
    const distinct = [...new Set(completed.map((s) => s.exerciseId))];
    const groups = distinct.map((_, index) => ({
      id: crypto.randomUUID(),
      groupType: 'SINGLE' as const,
      orderInRoutine: index,
      restAfterGroupSeconds: 90,
      timeCapSeconds: null,
    }));
    const exercises = distinct.map((exId, index) => {
      const setsForEx = completed.filter((s) => s.exerciseId === exId);
      return {
        id: crypto.randomUUID(),
        exerciseGroupId: groups[index].id,
        exerciseId: exId,
        orderInGroup: 0,
        prescribedSets: Math.max(1, setsForEx.length),
        prescribedRepsMin: Math.min(...setsForEx.map((s) => s.repsCompleted)),
        prescribedRepsMax: Math.max(...setsForEx.map((s) => s.repsCompleted)),
        restSeconds: 90,
        executionNotes: '',
      };
    });
    await database.updateRoutine(
      {
        id: base.id,
        profileId: base.profileId,
        name: base.name,
        description: base.description,
        estimatedDurationMinutes: base.estimatedDurationMinutes,
        creatorName: base.creatorName,
        sourceVideoId: base.sourceVideoId,
        createdAtMillis: base.createdAtMillis,
        updatedAtMillis: Date.now(),
      },
      groups,
      exercises
    );
    setDiffOpen(false);
  };

  const saveVariation = async () => {
    if (!session?.routineId) return;
    const base = await database.getRoutineById(session.routineId);
    if (!base) return;
    await database.duplicateRoutine(session.routineId, `${base.name} (Updated)`);
    setDiffOpen(false);
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>{session?.routineName ?? 'Workout complete'}</Text>
      <Text style={styles.sub}>Rough estimate · ±15% confidence band</Text>

      <View style={styles.grid}>
        <View style={styles.stat}>
          <Text style={styles.statValue}>{duration}</Text>
          <Text style={styles.statLabel}>min</Text>
        </View>
        <View style={styles.stat}>
          <Text style={styles.statValue}>{Math.round(tonnage)}</Text>
          <Text style={styles.statLabel}>kg tonnage</Text>
        </View>
        <View style={styles.stat}>
          <Text style={styles.statValue}>
            {Math.round(session?.estimatedCaloriesLow ?? 0)}–
            {Math.round(session?.estimatedCaloriesHigh ?? 0)}
          </Text>
          <Text style={styles.statLabel}>kcal</Text>
        </View>
        <View style={styles.stat}>
          <Text style={styles.statValue}>{working.length}</Text>
          <Text style={styles.statLabel}>working sets</Text>
        </View>
      </View>

      {prs.length > 0 ? (
        <View style={styles.prBox}>
          <Text style={styles.prTitle}>New PRs</Text>
          {prs.map((pr) => (
            <Text key={pr.id} style={styles.prItem}>
              {pr.exerciseName} · {pr.value.toFixed(1)} kg
            </Text>
          ))}
        </View>
      ) : null}

      {healthConnectSynced ? (
        <View style={styles.hcBox}>
          <Text style={styles.hcTitle}>Health Connect</Text>
          <Text style={styles.hcBody}>
            Synced strength session & calories to Health Connect
          </Text>
        </View>
      ) : null}

      <ForgeButton title="Done" fullWidth onPress={() => router.replace('/(tabs)/today')} />

      <Modal visible={diffOpen} transparent animationType="fade">
        <View style={styles.backdrop}>
          <View style={styles.sheet}>
            <Text style={styles.sheetTitle}>Routine changed</Text>
            <Text style={styles.sheetBody}>{diffSummary}</Text>
            <ForgeButton title="Update base routine" fullWidth onPress={updateBase} />
            <ForgeButton title="Save as new variation" variant="outline" fullWidth onPress={saveVariation} />
            <ForgeButton
              title="Log for today only"
              variant="ghost"
              fullWidth
              onPress={() => setDiffOpen(false)}
            />
          </View>
        </View>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.carbonSlate, padding: spacing.xxl, gap: spacing.md },
  title: { fontFamily: 'SpaceGrotesk_700Bold', fontSize: 28, color: colors.textPrimary },
  sub: { color: colors.textTertiary, marginBottom: 8 },
  grid: { flexDirection: 'row', flexWrap: 'wrap', gap: 10 },
  stat: {
    width: '47%',
    backgroundColor: colors.carbonSlateLight,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: colors.carbonSlateCard,
    padding: 14,
  },
  statValue: {
    color: colors.kineticLime,
    fontFamily: 'IBMPlexMono_500Medium',
    fontSize: 22,
  },
  statLabel: { color: colors.textSecondary, marginTop: 4 },
  prBox: {
    backgroundColor: '#FF660022',
    borderColor: colors.forgeAmber,
    borderWidth: 1,
    borderRadius: 14,
    padding: 14,
    gap: 6,
  },
  prTitle: { color: colors.forgeAmber, fontFamily: 'SpaceGrotesk_600SemiBold' },
  prItem: { color: colors.textPrimary, fontFamily: 'IBMPlexMono_400Regular' },
  hcBox: {
    backgroundColor: '#D4FF0018',
    borderColor: colors.kineticLime,
    borderWidth: 1,
    borderRadius: 14,
    padding: 14,
    gap: 4,
  },
  hcTitle: { color: colors.kineticLime, fontFamily: 'SpaceGrotesk_600SemiBold' },
  hcBody: { color: colors.textSecondary, fontSize: 13, lineHeight: 18 },
  backdrop: {
    flex: 1,
    backgroundColor: '#000000aa',
    justifyContent: 'center',
    padding: 24,
  },
  sheet: {
    backgroundColor: colors.carbonSlateLight,
    borderRadius: 18,
    padding: 20,
    gap: 10,
  },
  sheetTitle: { color: colors.textPrimary, fontFamily: 'SpaceGrotesk_700Bold', fontSize: 20 },
  sheetBody: { color: colors.textSecondary, marginBottom: 8, lineHeight: 20 },
});
