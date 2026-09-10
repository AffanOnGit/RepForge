import { useCallback, useMemo, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { useFocusEffect } from 'expo-router';
import { database } from '@/src/data/database';
import { SUB_MUSCLE_META, type SubMuscle } from '@/src/domain/types';
import { useAuthStore } from '@/src/stores/authStore';
import { colors, spacing } from '@/src/theme/colors';

const WINDOWS = [
  { label: '7d', days: 7 },
  { label: '14d', days: 14 },
  { label: '30d', days: 30 },
] as const;

export default function HeatmapScreen() {
  const profileId = useAuthStore((s) => s.activeProfileId);
  const [windowDays, setWindowDays] = useState(7);
  const [volume, setVolume] = useState<Record<string, number>>({});

  useFocusEffect(
    useCallback(() => {
      let alive = true;
      (async () => {
        const sessions = await database.getAllSessions();
        const cutoff = Date.now() - windowDays * 24 * 60 * 60 * 1000;
        const map: Record<string, number> = {};
        for (const session of sessions) {
          if ((session.completedAtMillis ?? 0) < cutoff) continue;
          const sets = await database.getSetsForSession(session.id);
          for (const set of sets) {
            if (!set.isCompleted || set.setType === 'WARMUP') continue;
            const ex = await database.getExerciseById(set.exerciseId);
            if (!ex) continue;
            map[ex.primarySubMuscle] =
              (map[ex.primarySubMuscle] ?? 0) + set.weightKg * set.repsCompleted;
          }
        }
        if (alive) setVolume(map);
      })();
      return () => {
        alive = false;
      };
    }, [windowDays, profileId])
  );

  const max = useMemo(() => Math.max(1, ...Object.values(volume)), [volume]);
  const muscles = Object.keys(SUB_MUSCLE_META) as SubMuscle[];

  return (
    <View style={styles.container}>
      <Text style={styles.sub}>Working-set tonnage by sub-muscle (rolling window).</Text>
      <View style={styles.row}>
        {WINDOWS.map((w) => (
          <Pressable
            key={w.label}
            onPress={() => setWindowDays(w.days)}
            style={[styles.chip, windowDays === w.days && styles.chipOn]}
          >
            <Text style={[styles.chipText, windowDays === w.days && styles.chipTextOn]}>{w.label}</Text>
          </Pressable>
        ))}
      </View>
      <View style={styles.grid}>
        {muscles.map((m) => {
          const tonnage = volume[m] ?? 0;
          const intensity = tonnage / max;
          return (
            <View
              key={m}
              style={[
                styles.cell,
                {
                  backgroundColor: `rgba(212,255,0,${0.08 + intensity * 0.75})`,
                  borderColor: intensity > 0.4 ? colors.kineticLime : colors.carbonSlateCard,
                },
              ]}
            >
              <Text style={styles.cellLabel}>{SUB_MUSCLE_META[m].displayName}</Text>
              <Text style={styles.cellValue}>{Math.round(tonnage)} kg</Text>
            </View>
          );
        })}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.carbonSlate, padding: spacing.lg, gap: 12 },
  sub: { color: colors.textSecondary },
  row: { flexDirection: 'row', gap: 8 },
  chip: {
    paddingHorizontal: 14,
    minHeight: 40,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: colors.carbonSlateCard,
    alignItems: 'center',
    justifyContent: 'center',
  },
  chipOn: { borderColor: colors.kineticLime },
  chipText: { color: colors.textSecondary, fontFamily: 'SpaceGrotesk_500Medium' },
  chipTextOn: { color: colors.kineticLime },
  grid: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  cell: {
    width: '48%',
    borderRadius: 12,
    borderWidth: 1,
    padding: 12,
    gap: 4,
  },
  cellLabel: { color: colors.textPrimary, fontFamily: 'SpaceGrotesk_500Medium', fontSize: 13 },
  cellValue: { color: colors.textTertiary, fontFamily: 'IBMPlexMono_400Regular', fontSize: 12 },
});
