import { useEffect, useMemo, useState } from 'react';
import { FlatList, StyleSheet, Text, View } from 'react-native';
import { ForgeTextField } from '@/src/components/ForgeTextField';
import { database } from '@/src/data/database';
import { SUB_MUSCLE_META, type Exercise } from '@/src/domain/types';
import { colors, spacing } from '@/src/theme/colors';

export default function ExercisesScreen() {
  const [all, setAll] = useState<Exercise[]>([]);
  const [query, setQuery] = useState('');

  useEffect(() => {
    database.getAllExercises().then(setAll);
  }, []);

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return all;
    return all.filter(
      (e) =>
        e.name.toLowerCase().includes(q) ||
        e.equipment.toLowerCase().includes(q) ||
        SUB_MUSCLE_META[e.primarySubMuscle].displayName.toLowerCase().includes(q)
    );
  }, [all, query]);

  return (
    <View style={styles.container}>
      <ForgeTextField
        label={`Search ${all.length} exercises`}
        value={query}
        onChangeText={setQuery}
      />
      <FlatList
        data={filtered}
        keyExtractor={(item) => item.id}
        renderItem={({ item }) => (
          <View style={styles.row}>
            <Text style={styles.name}>{item.name}</Text>
            <Text style={styles.meta}>
              {item.equipment} · {SUB_MUSCLE_META[item.primarySubMuscle].displayName}
            </Text>
          </View>
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.carbonSlate, padding: spacing.lg, gap: 10 },
  row: {
    paddingVertical: 12,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: colors.carbonSlateCard,
  },
  name: { color: colors.textPrimary, fontFamily: 'SpaceGrotesk_500Medium' },
  meta: { color: colors.textTertiary, marginTop: 2, fontSize: 12 },
});
