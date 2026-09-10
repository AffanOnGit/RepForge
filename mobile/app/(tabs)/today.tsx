import { StyleSheet, Text, View } from 'react-native';
import { useRouter } from 'expo-router';
import { ForgeButton } from '@/src/components/ForgeButton';
import { colors, spacing } from '@/src/theme/colors';
import { useAuthStore } from '@/src/stores/authStore';

export default function TodayScreen() {
  const router = useRouter();
  const profile = useAuthStore((s) => s.profile);

  return (
    <View style={styles.container}>
      <Text style={styles.eyebrow}>Session ready</Text>
      <Text style={styles.title}>Start lifting</Text>
      <Text style={styles.sub}>
        Freestyle from Today, or launch a saved routine. Ghost values and auto-rest kick in as soon as
        you complete a set.
      </Text>
      {profile ? (
        <Text style={styles.meta}>
          {profile.displayName} · {profile.weightKg ? `${profile.weightKg.toFixed(0)} kg · ` : ''}
          {profile.trainingExperience.toLowerCase()}
        </Text>
      ) : null}
      <ForgeButton
        title="Start freestyle workout"
        fullWidth
        onPress={() => router.push('/active-session/freestyle')}
      />
      <ForgeButton
        title="Browse routines"
        variant="outline"
        fullWidth
        onPress={() => router.push('/(tabs)/routines')}
      />
      <ForgeButton
        title="Ingest from YouTube / text"
        variant="ghost"
        fullWidth
        onPress={() => router.push('/ai/ingest')}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.carbonSlate,
    padding: spacing.xxl,
    gap: spacing.md,
    justifyContent: 'center',
  },
  eyebrow: {
    color: colors.kineticLime,
    fontFamily: 'SpaceGrotesk_600SemiBold',
    textTransform: 'uppercase',
    letterSpacing: 1.2,
    fontSize: 12,
  },
  title: {
    fontFamily: 'SpaceGrotesk_700Bold',
    fontSize: 40,
    color: colors.textPrimary,
    letterSpacing: -1,
  },
  sub: { color: colors.textSecondary, lineHeight: 22, marginBottom: spacing.md },
  meta: { color: colors.textTertiary, fontFamily: 'IBMPlexMono_400Regular', marginBottom: 8 },
});
