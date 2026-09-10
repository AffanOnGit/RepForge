import { useCallback, useState } from 'react';
import {
  ActivityIndicator,
  Modal,
  Platform,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { ForgeButton } from '@/src/components/ForgeButton';
import { colors, spacing } from '@/src/theme/colors';
import { runExport } from './shareExport';
import type { ExportDelivery, ExportFormat } from './types';

type Props = {
  /** Optional override; defaults to active profile from DB. */
  profileId?: string;
  sessionCountHint?: number;
  compact?: boolean;
};

export function ExportDataSection({
  profileId,
  sessionCountHint,
  compact,
}: Props) {
  const [open, setOpen] = useState(false);
  const [busy, setBusy] = useState(false);
  const [status, setStatus] = useState<string | null>(null);

  const run = useCallback(
    async (
      format: ExportFormat,
      delivery: ExportDelivery,
      csvVariant?: 'sets' | 'sessions'
    ) => {
      setBusy(true);
      setStatus(null);
      const result = await runExport({
        format,
        delivery,
        csvVariant,
        profileId,
      });
      setBusy(false);
      if (result.ok) {
        setStatus(
          delivery === 'share'
            ? `Shared ${result.fileName}`
            : `Saved ${result.fileName}`
        );
        setOpen(false);
      } else if (!result.canceled) {
        setStatus(result.message);
      }
    },
    [profileId]
  );

  return (
    <View style={[styles.wrap, compact && styles.wrapCompact]}>
      <Text style={styles.eyebrow}>DATA PORTABILITY</Text>
      <Text style={styles.title}>Export training data</Text>
      <Text style={styles.body}>
        You own 100% of your training data
        {sessionCountHint != null ? ` (${sessionCountHint} sessions)` : ''}.
        Export stays on-device — share sheet or Files app. No watermarks.
      </Text>

      <ForgeButton
        title="Export…"
        variant="outline"
        fullWidth
        onPress={() => setOpen(true)}
      />

      {status ? <Text style={styles.status}>{status}</Text> : null}

      <Modal
        visible={open}
        transparent
        animationType="slide"
        onRequestClose={() => !busy && setOpen(false)}
      >
        <Pressable style={styles.backdrop} onPress={() => !busy && setOpen(false)}>
          <Pressable style={styles.sheet} onPress={(e) => e.stopPropagation()}>
            <Text style={styles.eyebrow}>EXPORT</Text>
            <Text style={styles.sheetTitle}>Choose format</Text>
            <Text style={styles.body}>
              JSON is a full backup-shaped dump (profile, routines, sets, PRs).
              CSV is a denormalized workout log for Sheets / Excel.
            </Text>

            {busy ? (
              <ActivityIndicator
                color={colors.forgeAmber}
                style={{ marginVertical: 24 }}
              />
            ) : (
              <View style={styles.actions}>
                <ForgeButton
                  title="Share JSON backup"
                  fullWidth
                  onPress={() => run('json', 'share')}
                />
                <ForgeButton
                  title="Share CSV workout log"
                  variant="secondary"
                  fullWidth
                  onPress={() => run('csv', 'share', 'sets')}
                />
                <ForgeButton
                  title="Share CSV sessions (Android)"
                  variant="ghost"
                  fullWidth
                  onPress={() => run('csv', 'share', 'sessions')}
                />
                {Platform.OS === 'android' ? (
                  <>
                    <View style={styles.divider} />
                    <ForgeButton
                      title="Save JSON to folder"
                      variant="outline"
                      fullWidth
                      onPress={() => run('json', 'saf')}
                    />
                    <ForgeButton
                      title="Save CSV to folder"
                      variant="outline"
                      fullWidth
                      onPress={() => run('csv', 'saf', 'sets')}
                    />
                  </>
                ) : null}
                <ForgeButton
                  title="Cancel"
                  variant="ghost"
                  fullWidth
                  onPress={() => setOpen(false)}
                />
              </View>
            )}
          </Pressable>
        </Pressable>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: {
    marginTop: spacing.xl,
    gap: spacing.sm,
    padding: spacing.lg,
    borderRadius: 14,
    backgroundColor: colors.carbonSlateLight,
    borderWidth: 1,
    borderColor: colors.carbonSlateCard,
  },
  wrapCompact: {
    marginTop: spacing.md,
  },
  eyebrow: {
    color: colors.forgeAmber,
    fontFamily: 'SpaceGrotesk_600SemiBold',
    fontSize: 11,
    letterSpacing: 1.2,
  },
  title: {
    color: colors.textPrimary,
    fontFamily: 'SpaceGrotesk_700Bold',
    fontSize: 18,
  },
  sheetTitle: {
    color: colors.textPrimary,
    fontFamily: 'SpaceGrotesk_700Bold',
    fontSize: 20,
    marginBottom: 4,
  },
  body: {
    color: colors.textSecondary,
    fontSize: 13,
    lineHeight: 18,
    marginBottom: spacing.sm,
  },
  status: {
    color: colors.kineticLime,
    fontFamily: 'IBMPlexMono_400Regular',
    fontSize: 12,
    marginTop: 4,
  },
  backdrop: {
    flex: 1,
    backgroundColor: '#00000099',
    justifyContent: 'flex-end',
  },
  sheet: {
    backgroundColor: colors.carbonSlate,
    borderTopLeftRadius: 20,
    borderTopRightRadius: 20,
    padding: spacing.xxl,
    paddingBottom: spacing.xxxl,
    borderWidth: 1,
    borderColor: colors.carbonSlateCard,
  },
  actions: {
    gap: spacing.sm,
    marginTop: spacing.md,
  },
  divider: {
    height: 1,
    backgroundColor: colors.carbonSlateCard,
    marginVertical: spacing.sm,
  },
});
