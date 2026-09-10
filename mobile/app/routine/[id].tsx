import { useEffect, useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { ForgeButton } from '@/src/components/ForgeButton';
import { database } from '@/src/data/database';
import type { Routine } from '@/src/domain/types';
import { colors, spacing } from '@/src/theme/colors';

export default function RoutineDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const router = useRouter();
  const [routine, setRoutine] = useState<Routine | null>(null);

  useEffect(() => {
    if (id) database.getRoutineById(id).then(setRoutine);
  }, [id]);

  if (!routine) {
    return (
      <View style={styles.container}>
        <Text style={styles.meta}>Loading…</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>{routine.name}</Text>
      <Text style={styles.meta}>
        {routine.exercises.length} exercises
        {routine.estimatedDurationMinutes ? ` · ~${routine.estimatedDurationMinutes} min` : ''}
      </Text>
      {routine.exercises.map((e) => (
        <View key={e.id} style={styles.row}>
          <Text style={styles.name}>{e.exercise?.name ?? e.exerciseId}</Text>
          <Text style={styles.meta}>
            {e.prescribedSets}×{e.prescribedRepsMin}-{e.prescribedRepsMax} · {e.restSeconds}s rest
          </Text>
        </View>
      ))}
      <ForgeButton
        title="Start workout"
        fullWidth
        onPress={() => router.push(`/active-session/${routine.id}`)}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.carbonSlate, padding: spacing.xxl, gap: 10 },
  title: { fontFamily: 'SpaceGrotesk_700Bold', fontSize: 28, color: colors.textPrimary },
  row: {
    paddingVertical: 10,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: colors.carbonSlateCard,
  },
  name: { color: colors.textPrimary, fontFamily: 'SpaceGrotesk_500Medium' },
  meta: { color: colors.textTertiary, fontFamily: 'IBMPlexMono_400Regular', fontSize: 12 },
});
