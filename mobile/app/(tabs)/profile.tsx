import { useEffect, useState } from 'react';
import {
  Alert,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { useRouter } from 'expo-router';
import { ForgeButton } from '@/src/components/ForgeButton';
import { ForgeTextField } from '@/src/components/ForgeTextField';
import { pickBackupDirectory } from '@/src/data/storagePermissions';
import { UnitConverter } from '@/src/domain/engines';
import type { UnitSystem, UserProfile } from '@/src/domain/types';
import { database } from '@/src/data/database';
import { ExportDataSection } from '@/src/features/export';
import { HealthConnectCard, useHealthConnect } from '@/src/features/health';
import { useAuthStore } from '@/src/stores/authStore';
import { colors, spacing } from '@/src/theme/colors';

export default function ProfileScreen() {
  const router = useRouter();
  const profile = useAuthStore((s) => s.profile);
  const profiles = useAuthStore((s) => s.profiles);
  const activeProfileId = useAuthStore((s) => s.activeProfileId);
  const refreshProfiles = useAuthStore((s) => s.refreshProfiles);
  const switchProfile = useAuthStore((s) => s.switchProfile);
  const createProfile = useAuthStore((s) => s.createProfile);
  const deleteProfile = useAuthStore((s) => s.deleteProfile);
  const signOut = useAuthStore((s) => s.signOut);
  const lock = useAuthStore((s) => s.lock);
  const isGuest = useAuthStore((s) => s.isGuest);
  const unlockConfigured = useAuthStore((s) => s.unlockConfigured);
  const error = useAuthStore((s) => s.error);
  const health = useHealthConnect();

  const [unitSystem, setUnitSystem] = useState<UnitSystem>(profile?.unitSystem ?? 'metric');
  const [displayName, setDisplayName] = useState(profile?.displayName ?? '');
  const [weight, setWeight] = useState(
    profile?.weightKg
      ? UnitConverter.kgToDisplayWeight(profile.weightKg, unitSystem).toFixed(1)
      : ''
  );
  const [newName, setNewName] = useState('');
  const [saved, setSaved] = useState(false);
  const [backupUri, setBackupUri] = useState<string | null>(null);

  useEffect(() => {
    if (!profile) return;
    setDisplayName(profile.displayName);
    setUnitSystem(profile.unitSystem);
    setWeight(
      profile.weightKg
        ? UnitConverter.kgToDisplayWeight(profile.weightKg, profile.unitSystem).toFixed(1)
        : ''
    );
    setSaved(false);
  }, [profile?.id, profile?.weightKg, profile?.displayName, profile?.unitSystem]);

  useEffect(() => {
    database.getStorageConsent().then((c) => setBackupUri(c.backupDirectoryUri));
  }, []);

  const save = async () => {
    if (!profile) return;
    const weightKg = UnitConverter.displayWeightToKg(parseFloat(weight) || 75, unitSystem);
    await database.upsertProfile({
      ...profile,
      displayName: displayName.trim() || profile.displayName,
      weightKg,
      unitSystem,
      updatedAtMillis: Date.now(),
    });
    await refreshProfiles();
    setSaved(true);
  };

  const onSwitch = async (id: string) => {
    if (id === activeProfileId) return;
    await switchProfile(id);
  };

  const onCreate = async () => {
    const name = newName.trim() || `Athlete ${profiles.length + 1}`;
    const created = await createProfile({
      id: crypto.randomUUID(),
      displayName: name,
      email: '',
      weightKg: profile?.weightKg ?? 75,
      heightCm: profile?.heightCm ?? 175,
      age: profile?.age ?? 25,
      biologicalSex: profile?.biologicalSex ?? 'MALE',
      bodyFatPercentage: null,
      trainingExperience: profile?.trainingExperience ?? 'INTERMEDIATE',
      unitSystem: profile?.unitSystem ?? 'metric',
      createdAtMillis: Date.now(),
    });
    setNewName('');
    await switchProfile(created.id);
  };

  const onDelete = (p: UserProfile) => {
    Alert.alert(
      'Delete profile?',
      `This removes ${p.displayName}'s workouts, routines, and PRs on this device. Cannot undo.`,
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Delete',
          style: 'destructive',
          onPress: async () => {
            await deleteProfile(p.id);
          },
        },
      ]
    );
  };

  const onPickBackup = async () => {
    const uri = await pickBackupDirectory();
    if (uri) setBackupUri(uri);
  };

  const onLock = () => {
    lock();
    router.replace('/auth/login');
  };

  return (
    <ScrollView style={styles.scroll} contentContainerStyle={styles.container}>
      <Text style={styles.title}>Profile</Text>
      <Text style={styles.meta}>
        {isGuest ? 'Guest session' : 'Local device'} · active{' '}
        <Text style={styles.mono}>{activeProfileId?.slice(0, 8) ?? '—'}…</Text>
      </Text>

      <Text style={styles.section}>Profiles on this device</Text>
      {profiles.map((p) => {
        const active = p.id === activeProfileId;
        return (
          <Pressable
            key={p.id}
            onPress={() => onSwitch(p.id)}
            onLongPress={() => onDelete(p)}
            style={[styles.profileRow, active && styles.profileRowOn]}
          >
            <View style={{ flex: 1 }}>
              <Text style={styles.profileName}>{p.displayName}</Text>
              <Text style={styles.profileMeta}>
                {p.trainingExperience.toLowerCase()}
                {p.weightKg ? ` · ${p.weightKg.toFixed(0)} kg` : ''}
              </Text>
            </View>
            {active ? <Text style={styles.activeTag}>Active</Text> : null}
          </Pressable>
        );
      })}
      {error ? <Text style={styles.error}>{error}</Text> : null}
      <Text style={styles.hint}>Long-press a profile to delete (keeps at least one).</Text>

      <ForgeTextField
        label="New profile name"
        value={newName}
        onChangeText={setNewName}
        autoCapitalize="words"
      />
      <ForgeButton title="Create profile" variant="outline" fullWidth onPress={onCreate} />

      <Text style={styles.section}>Active profile settings</Text>
      <ForgeTextField
        label="Display name"
        value={displayName}
        onChangeText={setDisplayName}
        autoCapitalize="words"
      />
      <View style={styles.row}>
        {(['metric', 'imperial'] as UnitSystem[]).map((u) => (
          <Pressable
            key={u}
            onPress={() => {
              if (profile?.weightKg) {
                setWeight(UnitConverter.kgToDisplayWeight(profile.weightKg, u).toFixed(1));
              }
              setUnitSystem(u);
            }}
            style={[styles.chip, unitSystem === u && styles.chipOn]}
          >
            <Text style={[styles.chipText, unitSystem === u && styles.chipTextOn]}>
              {u === 'metric' ? 'Metric' : 'Imperial'}
            </Text>
          </Pressable>
        ))}
      </View>

      <ForgeTextField
        label={unitSystem === 'metric' ? 'Weight (kg)' : 'Weight (lb)'}
        keyboardType="decimal-pad"
        value={weight}
        onChangeText={setWeight}
      />
      <ForgeButton title={saved ? 'Saved' : 'Save preferences'} fullWidth onPress={save} />

      <Text style={styles.section}>Health Connect</Text>
      <HealthConnectCard
        status={health.status}
        busy={health.busy}
        onConnect={async () => {
          await health.connect();
          await refreshProfiles();
        }}
        onSync={async () => {
          await health.syncNow();
          await refreshProfiles();
        }}
        onToggleSync={health.setSyncEnabled}
        onOpenSettings={health.openSettings}
      />

      <Text style={styles.section}>Device storage & export</Text>
      <Text style={styles.hint}>
        Workouts live in the app sandbox. Choose a backup folder once — Export Save to folder reuses
        it via Storage Access Framework.
      </Text>
      <ForgeButton
        title={backupUri ? 'Change backup location' : 'Choose backup folder'}
        variant="outline"
        fullWidth
        onPress={onPickBackup}
      />
      {backupUri ? (
        <Text style={styles.mono} numberOfLines={2}>
          {backupUri}
        </Text>
      ) : null}
      <ExportDataSection profileId={activeProfileId ?? undefined} />

      <ForgeButton
        title={unlockConfigured ? 'Manage PIN' : 'Set unlock PIN'}
        variant="outline"
        fullWidth
        onPress={() => router.push('/auth/setup-pin')}
      />
      {unlockConfigured ? (
        <ForgeButton title="Lock app" variant="ghost" fullWidth onPress={onLock} />
      ) : null}
      <ForgeButton title="Sign out" variant="ghost" fullWidth onPress={signOut} />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  scroll: { flex: 1, backgroundColor: colors.carbonSlate },
  container: { padding: spacing.xxl, gap: spacing.md, paddingBottom: 48 },
  title: { fontFamily: 'SpaceGrotesk_700Bold', fontSize: 28, color: colors.textPrimary },
  meta: { color: colors.textSecondary, marginBottom: 8 },
  mono: { fontFamily: 'IBMPlexMono_400Regular', color: colors.textTertiary, fontSize: 12 },
  section: {
    color: colors.kineticLime,
    fontFamily: 'SpaceGrotesk_600SemiBold',
    marginTop: spacing.md,
    textTransform: 'uppercase',
    letterSpacing: 1,
    fontSize: 12,
  },
  profileRow: {
    minHeight: 56,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: spacing.lg,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: colors.carbonSlateCard,
    backgroundColor: colors.carbonSlateSurface,
    gap: spacing.md,
  },
  profileRowOn: { borderColor: colors.forgeAmber },
  profileName: { color: colors.textPrimary, fontFamily: 'SpaceGrotesk_600SemiBold', fontSize: 16 },
  profileMeta: { color: colors.textTertiary, fontSize: 13 },
  activeTag: { color: colors.forgeAmber, fontFamily: 'SpaceGrotesk_600SemiBold', fontSize: 12 },
  hint: { color: colors.textTertiary, fontSize: 13, lineHeight: 18 },
  error: { color: colors.errorRed },
  row: { flexDirection: 'row', gap: 10 },
  chip: {
    minHeight: 44,
    paddingHorizontal: 14,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: colors.carbonSlateCard,
    justifyContent: 'center',
  },
  chipOn: { borderColor: colors.forgeAmber },
  chipText: { color: colors.textSecondary },
  chipTextOn: { color: colors.forgeAmber },
});
