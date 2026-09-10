import { useCallback, useState } from 'react';
import {
  FlatList,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { useFocusEffect, useRouter } from 'expo-router';
import { ForgeButton } from '@/src/components/ForgeButton';
import { database } from '@/src/data/database';
import type { Routine } from '@/src/domain/types';
import { useAuthStore } from '@/src/stores/authStore';
import { colors, spacing } from '@/src/theme/colors';

export default function RoutinesScreen() {
  const router = useRouter();
  const profileId = useAuthStore((s) => s.activeProfileId);
  const [routines, setRoutines] = useState<Routine[]>([]);

  useFocusEffect(
    useCallback(() => {
      let alive = true;
      database.getAllRoutines().then((r) => {
        if (alive) setRoutines(r);
      });
      return () => {
        alive = false;
      };
    }, [profileId])
  );

  return (
    <View style={styles.container}>
      <View style={styles.actions}>
        <ForgeButton title="Quick create" onPress={() => router.push('/routine/create')} />
        <ForgeButton title="YouTube AI" variant="secondary" onPress={() => router.push('/ai/ingest')} />
        <ForgeButton title="Library" variant="outline" onPress={() => router.push('/exercises')} />
      </View>
      <FlatList
        data={routines}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.list}
        ListEmptyComponent={
          <Text style={styles.empty}>
            No routines yet. Build one in under a minute — pick exercises, prescribe sets, train.
          </Text>
        }
        renderItem={({ item }) => (
          <Pressable
            style={styles.card}
            onPress={() => router.push(`/routine/${item.id}`)}
          >
            <Text style={styles.name}>{item.name}</Text>
            <Text style={styles.meta}>
              {item.exercises.length} exercises
              {item.estimatedDurationMinutes ? ` · ~${item.estimatedDurationMinutes} min` : ''}
            </Text>
            <ForgeButton
              title="Start"
              onPress={() => router.push(`/active-session/${item.id}`)}
            />
          </Pressable>
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.carbonSlate },
  actions: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 10,
    padding: spacing.lg,
    justifyContent: 'flex-start',
  },
  list: { padding: spacing.lg, gap: 12, paddingBottom: 40 },
  empty: { color: colors.textSecondary, lineHeight: 22, padding: spacing.lg },
  card: {
    backgroundColor: colors.carbonSlateLight,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: colors.carbonSlateCard,
    padding: spacing.lg,
    gap: 10,
    marginBottom: 12,
  },
  name: { color: colors.textPrimary, fontFamily: 'SpaceGrotesk_600SemiBold', fontSize: 18 },
  meta: { color: colors.textTertiary, fontFamily: 'IBMPlexMono_400Regular' },
});
