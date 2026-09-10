import { ActivityIndicator, Pressable, StyleSheet, Switch, Text, View } from 'react-native';
import { ForgeButton } from '@/src/components/ForgeButton';
import { colors, spacing } from '@/src/theme/colors';
import type { HealthConnectStatus } from './types';

type Props = {
  status: HealthConnectStatus;
  busy: boolean;
  onConnect: () => void;
  onSync: () => void;
  onToggleSync: (enabled: boolean) => void;
  onOpenSettings?: () => void;
};

function statusTitle(status: HealthConnectStatus): string {
  if (status.availability === 'unsupported_platform') return 'Android only';
  if (status.availability === 'module_unavailable') return 'Needs development build';
  if (status.availability === 'sdk_unavailable') return 'Not installed on device';
  if (status.availability === 'update_required') return 'Update Health Connect';
  if (status.hasPermissions && status.syncEnabled) return 'Connected & syncing';
  if (status.hasPermissions) return 'Connected · sync paused';
  return 'Permissions pending';
}

function statusColor(status: HealthConnectStatus): string {
  if (status.hasPermissions && status.syncEnabled) return colors.kineticLime;
  if (status.availability === 'available') return colors.forgeAmber;
  return colors.textTertiary;
}

export function HealthConnectCard({
  status,
  busy,
  onConnect,
  onSync,
  onToggleSync,
  onOpenSettings,
}: Props) {
  const available = status.availability === 'available';

  return (
    <View style={styles.card}>
      <View style={styles.header}>
        <View style={{ flex: 1, gap: 4 }}>
          <Text style={styles.eyebrow}>HEALTH CONNECT</Text>
          <Text style={[styles.title, { color: statusColor(status) }]}>
            {statusTitle(status)}
          </Text>
        </View>
        {busy ? (
          <ActivityIndicator color={colors.kineticLime} />
        ) : available && status.hasPermissions ? (
          <Pressable onPress={onSync} style={styles.syncChip} accessibilityRole="button">
            <Text style={styles.syncChipText}>Sync Now</Text>
          </Pressable>
        ) : null}
      </View>

      <Text style={styles.body}>
        Reads body weight into your profile biometrics. Writes finished strength sessions and
        estimated calories after workouts. All sync is on-device.
      </Text>

      {available ? (
        <View style={styles.toggleRow}>
          <Text style={styles.toggleLabel}>Sync with Health Connect</Text>
          <Switch
            value={status.syncEnabled && status.hasPermissions}
            onValueChange={(v) => {
              if (v && !status.hasPermissions) onConnect();
              else onToggleSync(v);
            }}
            trackColor={{ false: colors.carbonSlateCard, true: colors.forgeAmberDark }}
            thumbColor={status.syncEnabled ? colors.forgeAmber : colors.textSecondary}
          />
        </View>
      ) : null}

      {!status.hasPermissions && available ? (
        <ForgeButton title="Connect Health Connect" fullWidth onPress={onConnect} disabled={busy} />
      ) : null}

      {(status.availability === 'sdk_unavailable' ||
        status.availability === 'update_required' ||
        status.availability === 'module_unavailable') &&
      onOpenSettings ? (
        <ForgeButton
          title={
            status.availability === 'module_unavailable'
              ? 'Requires custom Android build'
              : 'Open Health Connect'
          }
          variant="outline"
          fullWidth
          onPress={onOpenSettings}
          disabled={status.availability === 'module_unavailable' || busy}
        />
      ) : null}

      {status.lastMessage ? <Text style={styles.message}>{status.lastMessage}</Text> : null}
      {status.lastSyncedAtMillis ? (
        <Text style={styles.meta}>
          Last sync {new Date(status.lastSyncedAtMillis).toLocaleString()}
        </Text>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: colors.carbonSlateLight,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: colors.carbonSlateCard,
    padding: spacing.lg,
    gap: spacing.sm,
  },
  header: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  eyebrow: {
    color: colors.textTertiary,
    fontSize: 11,
    fontFamily: 'SpaceGrotesk_600SemiBold',
    letterSpacing: 0.6,
  },
  title: { fontFamily: 'SpaceGrotesk_600SemiBold', fontSize: 15 },
  body: { color: colors.textTertiary, fontSize: 13, lineHeight: 18 },
  toggleRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginTop: 4,
  },
  toggleLabel: { color: colors.textSecondary, fontFamily: 'SpaceGrotesk_500Medium', flex: 1 },
  syncChip: {
    backgroundColor: colors.carbonSlateSurface,
    borderRadius: 10,
    paddingHorizontal: 12,
    paddingVertical: 8,
  },
  syncChipText: { color: colors.textPrimary, fontSize: 12, fontFamily: 'SpaceGrotesk_600SemiBold' },
  message: { color: colors.kineticLime, fontSize: 13, lineHeight: 18 },
  meta: { color: colors.textGhost, fontSize: 11 },
});
