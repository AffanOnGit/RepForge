import { useCallback, useState } from 'react';
import { FlatList, StyleSheet, Text, View } from 'react-native';
import { useFocusEffect } from 'expo-router';
import { database } from '@/src/data/database';
import { ExportDataSection } from '@/src/features/export';
import { useAuthStore } from '@/src/stores/authStore';
import type { PersonalRecord, WorkoutSession } from '@/src/domain/types';
import { colors, spacing } from '@/src/theme/colors';

export default function HistoryScreen() {
  const profileId = useAuthStore((s) => s.activeProfileId) ?? undefined;
  const [sessions, setSessions] = useState<WorkoutSession[]>([]);
  const [prs, setPrs] = useState<PersonalRecord[]>([]);

  useFocusEffect(
    useCallback(() => {
      let alive = true;
      Promise.all([database.getAllSessions(), database.getAllPersonalRecords()]).then(
        ([s, p]) => {
          if (!alive) return;
          setSessions(s);
          setPrs(p);
        }
      );
      return () => {
        alive = false;
      };
    }, [profileId])
  );

  return (
    <View style={styles.container}>
      <FlatList
        data={sessions}
        keyExtractor={(item) => item.id}
        ListHeaderComponent={
          <View>
            <ExportDataSection
              compact
              profileId={profileId}
              sessionCountHint={sessions.length}
            />

            <Text style={styles.section}>Trophy room</Text>
            {prs.length === 0 ? (
              <Text style={styles.empty}>
                Hit a weight PR in a session to unlock trophies.
              </Text>
            ) : (
              prs.slice(0, 5).map((pr) => (
                <View key={pr.id} style={styles.prCard}>
                  <Text style={styles.prTitle}>{pr.exerciseName}</Text>
                  <Text style={styles.prMeta}>
                    {pr.type} · {pr.value.toFixed(1)} kg
                  </Text>
                </View>
              ))
            )}

            <Text style={styles.section}>Logbook</Text>
          </View>
        }
        ListEmptyComponent={<Text style={styles.empty}>No completed workouts yet.</Text>}
        renderItem={({ item }) => {
          const mins = Math.max(
            1,
            Math.round(
              ((item.completedAtMillis ?? 0) - (item.startedAtMillis ?? 0)) / 60000
            )
          );
          return (
            <View style={styles.card}>
              <Text style={styles.name}>{item.routineName || 'Workout'}</Text>
              <Text style={styles.meta}>
                {mins} min · {Math.round(item.totalTonnageKg)} kg tonnage · ~
                {Math.round(item.estimatedCaloriesLow)}–
                {Math.round(item.estimatedCaloriesHigh)} kcal
              </Text>
            </View>
          );
        }}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.carbonSlate, padding: spacing.lg },
  section: {
    color: colors.kineticLime,
    fontFamily: 'SpaceGrotesk_600SemiBold',
    marginTop: 8,
    marginBottom: 8,
    textTransform: 'uppercase',
    letterSpacing: 1,
    fontSize: 12,
  },
  empty: { color: colors.textSecondary, marginBottom: 12 },
  prCard: {
    backgroundColor: '#FF660022',
    borderColor: colors.forgeAmber,
    borderWidth: 1,
    borderRadius: 12,
    padding: 12,
    marginBottom: 8,
  },
  prTitle: { color: colors.textPrimary, fontFamily: 'SpaceGrotesk_600SemiBold' },
  prMeta: { color: colors.forgeAmber, fontFamily: 'IBMPlexMono_400Regular', marginTop: 4 },
  card: {
    backgroundColor: colors.carbonSlateLight,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: colors.carbonSlateCard,
    padding: 14,
    marginBottom: 10,
  },
  name: { color: colors.textPrimary, fontFamily: 'SpaceGrotesk_600SemiBold' },
  meta: {
    color: colors.textTertiary,
    fontFamily: 'IBMPlexMono_400Regular',
    marginTop: 4,
    fontSize: 12,
  },
});
