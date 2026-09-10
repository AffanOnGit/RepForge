import { useEffect, useMemo, useState } from 'react';
import {
  FlatList,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { useRouter } from 'expo-router';
import { ForgeButton } from '@/src/components/ForgeButton';
import { ForgeTextField } from '@/src/components/ForgeTextField';
import { database } from '@/src/data/database';
import type { Exercise } from '@/src/domain/types';
import { colors, spacing } from '@/src/theme/colors';

type Pick = { exercise: Exercise; sets: number; reps: number; rest: number };

export default function CreateRoutineScreen() {
  const router = useRouter();
  const [name, setName] = useState('Push A');
  const [all, setAll] = useState<Exercise[]>([]);
  const [query, setQuery] = useState('');
  const [picks, setPicks] = useState<Pick[]>([]);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    database.getAllExercises().then(setAll);
  }, []);

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return all.slice(0, 40);
    return all.filter((e) => e.name.toLowerCase().includes(q)).slice(0, 40);
  }, [all, query]);

  const toggle = (exercise: Exercise) => {
    setPicks((prev) => {
      if (prev.some((p) => p.exercise.id === exercise.id)) {
        return prev.filter((p) => p.exercise.id !== exercise.id);
      }
      return [...prev, { exercise, sets: 3, reps: 10, rest: 90 }];
    });
  };

  const save = async () => {
    if (!picks.length || saving) return;
    setSaving(true);
    const routineId = crypto.randomUUID();
    const groups = picks.map((_, i) => ({
      id: crypto.randomUUID(),
      groupType: 'SINGLE' as const,
      orderInRoutine: i,
      restAfterGroupSeconds: picks[i].rest,
      timeCapSeconds: null,
    }));
    const exercises = picks.map((p, i) => ({
      id: crypto.randomUUID(),
      exerciseGroupId: groups[i].id,
      exerciseId: p.exercise.id,
      orderInGroup: 0,
      prescribedSets: p.sets,
      prescribedRepsMin: Math.max(1, p.reps - 2),
      prescribedRepsMax: p.reps,
      restSeconds: p.rest,
      executionNotes: '',
    }));
    await database.createRoutine(
      {
        id: routineId,
        profileId: (await database.getActiveProfileId()) ?? '',
        name: name.trim() || 'Untitled',
        description: '',
        estimatedDurationMinutes: picks.length * 8,
        creatorName: null,
        sourceVideoId: null,
        createdAtMillis: Date.now(),
        updatedAtMillis: Date.now(),
      },
      groups,
      exercises
    );
    setSaving(false);
    router.replace(`/routine/${routineId}`);
  };

  return (
    <View style={styles.container}>
      <ForgeTextField label="Routine name" value={name} onChangeText={setName} />
      <ForgeTextField label="Search exercises" value={query} onChangeText={setQuery} />
      <Text style={styles.picked}>{picks.length} selected</Text>
      <FlatList
        data={filtered}
        keyExtractor={(item) => item.id}
        renderItem={({ item }) => {
          const on = picks.some((p) => p.exercise.id === item.id);
          return (
            <Pressable
              onPress={() => toggle(item)}
              style={[styles.row, on && styles.rowOn]}
            >
              <Text style={styles.name}>{item.name}</Text>
              <Text style={styles.meta}>{item.equipment}</Text>
            </Pressable>
          );
        }}
      />
      <ForgeButton
        title={saving ? 'Saving…' : 'Save routine'}
        fullWidth
        onPress={save}
        disabled={!picks.length || saving}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.carbonSlate, padding: spacing.lg, gap: 10 },
  picked: { color: colors.kineticLime, fontFamily: 'SpaceGrotesk_500Medium' },
  row: {
    paddingVertical: 12,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: colors.carbonSlateCard,
  },
  rowOn: { backgroundColor: '#FF660022' },
  name: { color: colors.textPrimary, fontFamily: 'SpaceGrotesk_500Medium' },
  meta: { color: colors.textTertiary, fontSize: 12, marginTop: 2 },
});
