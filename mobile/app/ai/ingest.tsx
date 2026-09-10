import { useEffect } from 'react';
import {
  ActivityIndicator,
  Modal,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { useRouter } from 'expo-router';
import { ForgeButton } from '@/src/components/ForgeButton';
import { ForgeTextField } from '@/src/components/ForgeTextField';
import { StepperCounter } from '@/src/components/StepperCounter';
import {
  CURATED_PRESETS,
  isGeminiConfigured,
  useAiIngestionStore,
} from '@/src/features/ai';
import { SUB_MUSCLE_META } from '@/src/domain/types';
import { useAuthStore } from '@/src/stores/authStore';
import { colors, spacing } from '@/src/theme/colors';

const EQUIPMENT_LABEL: Record<string, string> = {
  BARBELL: 'Barbell',
  DUMBBELL: 'Dumbbell',
  CABLE: 'Cable',
  MACHINE: 'Machine',
  BODYWEIGHT: 'Bodyweight',
  KETTLEBELL: 'Kettlebell',
  SMITH_MACHINE: 'Smith',
};

function stageLabel(stage: string): string {
  switch (stage) {
    case 'CHECKING_GLOBAL_CACHE':
      return 'Checking Global Video Cache (0 AI Tokens)...';
    case 'CHECKING_QUOTA':
      return 'Checking weekly import credits...';
    case 'ANALYZING_METADATA':
      return 'Extracting YouTube chapters & metadata...';
    case 'GEMINI_PARSING':
      return 'Gemini AI structuring exercise sequence...';
    case 'SIMULATED_PARSING':
      return 'Simulated AI structuring exercise sequence...';
    case 'TEXT_PARSING':
      return 'Parsing pasted program text...';
    default:
      return 'Processing...';
  }
}

export default function AiIngestScreen() {
  const router = useRouter();
  const profile = useAuthStore((s) => s.profile);
  const activeProfileId = useAuthStore((s) => s.activeProfileId);
  const {
    input,
    inputMode,
    stage,
    parsedData,
    editedExercises,
    error,
    isSaving,
    quota,
    setInput,
    setInputMode,
    setProfileId,
    refreshQuota,
    startIngestion,
    removeExercise,
    updateExerciseSets,
    updateExerciseReps,
    confirmAndSave,
    reset,
  } = useAiIngestionStore();

  const busy = stage !== 'IDLE' && stage !== 'READY_FOR_REVIEW';
  const geminiOn = isGeminiConfigured();

  useEffect(() => {
    setProfileId(activeProfileId ?? profile?.id);
    refreshQuota();
  }, [activeProfileId, profile?.id, setProfileId, refreshQuota]);

  const onConfirm = async () => {
    const routineId = await confirmAndSave();
    if (routineId) {
      reset();
      router.replace(`/routine/${routineId}`);
    }
  };

  return (
    <View style={styles.container}>
      <ScrollView contentContainerStyle={styles.scroll} keyboardShouldPersistTaps="handled">
        <Text style={styles.eyebrow}>AI WORKOUT INGESTION</Text>
        <Text style={styles.title}>YouTube to Workout Routine</Text>
        <Text style={styles.sub}>
          Paste a YouTube workout URL or a written program. Review the extracted exercises, then save
          as a local routine template.
        </Text>

        <View style={styles.modeRow}>
          <Pressable
            onPress={() => setInputMode('url')}
            style={[styles.modeChip, inputMode === 'url' && styles.modeChipOn]}
          >
            <Text style={[styles.modeText, inputMode === 'url' && styles.modeTextOn]}>YouTube URL</Text>
          </Pressable>
          <Pressable
            onPress={() => setInputMode('text')}
            style={[styles.modeChip, inputMode === 'text' && styles.modeChipOn]}
          >
            <Text style={[styles.modeText, inputMode === 'text' && styles.modeTextOn]}>
              Program text
            </Text>
          </Pressable>
        </View>

        {quota ? (
          <Text style={styles.quota}>
            Weekly credits · {quota.remaining} left ({quota.used} used · +{quota.bonusCredits} bonus)
            {' · '}
            {geminiOn ? 'Gemini key detected' : 'Simulator / HITL mode (no API key)'}
          </Text>
        ) : null}

        <ForgeTextField
          label={inputMode === 'url' ? 'YOUTUBE VIDEO LINK' : 'PROGRAM TEXT'}
          placeholder={
            inputMode === 'url'
              ? 'https://youtube.com/watch?v=...'
              : 'Push Day\nIncline Dumbbell Press 3x8-10\nCable Lateral Raise 4x12-15'
          }
          value={input}
          onChangeText={setInput}
          multiline={inputMode === 'text'}
          style={inputMode === 'text' ? styles.textArea : undefined}
          autoCapitalize="none"
          autoCorrect={false}
        />

        <ForgeButton
          title={busy ? 'Ingesting…' : 'Ingest Workout'}
          fullWidth
          disabled={busy}
          onPress={() => {
            void startIngestion();
          }}
        />

        {busy ? (
          <View style={styles.progress}>
            <ActivityIndicator color={colors.forgeAmber} />
            <Text style={styles.progressText}>{stageLabel(stage)}</Text>
          </View>
        ) : null}

        {error ? (
          <View style={styles.errorBox}>
            <Text style={styles.errorTitle}>{error.message}</Text>
            <Text style={styles.errorSub}>{error.recoveryAction}</Text>
          </View>
        ) : null}

        <Text style={styles.sectionLabel}>CURATED CREATOR PRESETS (0 TOKENS)</Text>
        {CURATED_PRESETS.map((preset) => (
          <Pressable
            key={preset.title}
            style={styles.preset}
            onPress={() => {
              setInputMode('url');
              setInput(preset.url);
              startIngestion(preset.url);
            }}
          >
            <View style={{ flex: 1 }}>
              <Text style={styles.presetTitle}>{preset.title}</Text>
              <Text style={styles.presetMeta}>
                {preset.creator} · {preset.tags}
              </Text>
            </View>
            <Text style={styles.presetCta}>Import ⚡</Text>
          </Pressable>
        ))}
      </ScrollView>

      <Modal
        visible={stage === 'READY_FOR_REVIEW' && !!parsedData}
        animationType="slide"
        presentationStyle="pageSheet"
        onRequestClose={reset}
      >
        <View style={styles.modal}>
          <ScrollView contentContainerStyle={styles.modalScroll}>
            <View style={styles.modalHeader}>
              <Text style={styles.hitl}>HUMAN-IN-THE-LOOP VERIFICATION</Text>
              {parsedData?.isFromGlobalCache ? (
                <View style={styles.cacheBadge}>
                  <Text style={styles.cacheBadgeText}>VERIFIED CACHE</Text>
                </View>
              ) : null}
            </View>

            <Text style={styles.modalTitle}>{parsedData?.videoTitle}</Text>
            <Text style={styles.modalMeta}>
              Creator: {parsedData?.creatorName}
              {parsedData?.parseMode ? ` · ${parsedData.parseMode}` : ''}
            </Text>

            <Text style={styles.sectionLabel}>
              EXTRACTED EXERCISES ({editedExercises.length})
            </Text>

            {editedExercises.map((ex, index) => (
              <View key={ex.id} style={styles.exCard}>
                <View style={styles.exTop}>
                  <Text style={styles.exName}>
                    {index + 1}. {ex.name}
                  </Text>
                  <Pressable onPress={() => removeExercise(ex.id)} hitSlop={8}>
                    <Text style={styles.remove}>Remove</Text>
                  </Pressable>
                </View>
                <Text style={styles.exMeta}>
                  {EQUIPMENT_LABEL[ex.equipment] ?? ex.equipment} ·{' '}
                  {SUB_MUSCLE_META[ex.primarySubMuscle]?.displayName ?? ex.primarySubMuscle}
                  {ex.matchConfidence !== 'exact'
                    ? ` · match:${ex.matchConfidence}`
                    : ''}
                </Text>
                {ex.notes ? <Text style={styles.notes}>{ex.notes}</Text> : null}
                <View style={styles.steppers}>
                  <StepperCounter
                    label="Sets"
                    value={ex.sets}
                    min={1}
                    max={10}
                    onChange={(v) => updateExerciseSets(ex.id, v)}
                  />
                  <StepperCounter
                    label="Rep min"
                    value={ex.repsMin}
                    min={1}
                    max={30}
                    onChange={(v) => updateExerciseReps(ex.id, v, Math.max(v, ex.repsMax))}
                  />
                  <StepperCounter
                    label="Rep max"
                    value={ex.repsMax}
                    min={1}
                    max={30}
                    onChange={(v) => updateExerciseReps(ex.id, Math.min(ex.repsMin, v), v)}
                  />
                </View>
              </View>
            ))}

            <ForgeButton
              title={isSaving ? 'Saving…' : 'Confirm & Save Routine'}
              fullWidth
              disabled={isSaving || editedExercises.length === 0}
              onPress={onConfirm}
              style={{ marginTop: spacing.lg }}
            />
            <ForgeButton
              title="Cancel review"
              variant="outline"
              fullWidth
              onPress={reset}
              style={{ marginTop: spacing.sm }}
            />
          </ScrollView>
        </View>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.carbonSlate },
  scroll: { padding: spacing.xl, paddingBottom: 48, gap: spacing.md },
  eyebrow: {
    color: colors.textTertiary,
    fontFamily: 'IBMPlexMono_500Medium',
    fontSize: 11,
    letterSpacing: 1,
  },
  title: {
    color: colors.textPrimary,
    fontFamily: 'SpaceGrotesk_700Bold',
    fontSize: 24,
  },
  sub: { color: colors.textSecondary, lineHeight: 20, fontSize: 14 },
  modeRow: { flexDirection: 'row', gap: 8 },
  modeChip: {
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: colors.carbonSlateCard,
    backgroundColor: colors.carbonSlateLight,
  },
  modeChipOn: { borderColor: colors.forgeAmber, backgroundColor: colors.carbonSlateSurface },
  modeText: { color: colors.textSecondary, fontFamily: 'SpaceGrotesk_500Medium', fontSize: 13 },
  modeTextOn: { color: colors.forgeAmber },
  quota: {
    color: colors.textTertiary,
    fontFamily: 'IBMPlexMono_400Regular',
    fontSize: 11,
    lineHeight: 16,
  },
  textArea: { minHeight: 120, textAlignVertical: 'top', paddingTop: 12 },
  progress: {
    backgroundColor: colors.carbonSlateLight,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: colors.carbonSlateCard,
    padding: spacing.lg,
    alignItems: 'center',
    gap: 10,
  },
  progressText: {
    color: colors.forgeAmber,
    fontFamily: 'SpaceGrotesk_600SemiBold',
    fontSize: 13,
    textAlign: 'center',
  },
  errorBox: {
    backgroundColor: 'rgba(207,102,121,0.15)',
    borderColor: colors.errorRed,
    borderWidth: 1,
    borderRadius: 12,
    padding: spacing.md,
    gap: 4,
  },
  errorTitle: { color: colors.errorRed, fontFamily: 'SpaceGrotesk_600SemiBold', fontSize: 13 },
  errorSub: { color: colors.textSecondary, fontSize: 12 },
  sectionLabel: {
    color: colors.textTertiary,
    fontFamily: 'IBMPlexMono_500Medium',
    fontSize: 11,
    letterSpacing: 1,
    marginTop: spacing.md,
  },
  preset: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    backgroundColor: colors.carbonSlateLight,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: colors.carbonSlateCard,
    padding: spacing.md,
  },
  presetTitle: { color: colors.textPrimary, fontFamily: 'SpaceGrotesk_600SemiBold', fontSize: 14 },
  presetMeta: { color: colors.textSecondary, fontSize: 12, marginTop: 2 },
  presetCta: { color: colors.forgeAmber, fontFamily: 'SpaceGrotesk_600SemiBold', fontSize: 12 },
  modal: { flex: 1, backgroundColor: colors.carbonSlateSurface },
  modalScroll: { padding: spacing.xl, paddingBottom: 48, gap: spacing.sm },
  modalHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: 8,
  },
  hitl: {
    color: colors.forgeAmber,
    fontFamily: 'IBMPlexMono_500Medium',
    fontSize: 11,
    letterSpacing: 1,
    flex: 1,
  },
  cacheBadge: {
    backgroundColor: 'rgba(212,255,0,0.2)',
    borderRadius: 6,
    paddingHorizontal: 8,
    paddingVertical: 2,
  },
  cacheBadgeText: {
    color: colors.kineticLime,
    fontFamily: 'SpaceGrotesk_600SemiBold',
    fontSize: 10,
  },
  modalTitle: {
    color: colors.textPrimary,
    fontFamily: 'SpaceGrotesk_700Bold',
    fontSize: 18,
  },
  modalMeta: { color: colors.textSecondary, fontSize: 13, marginBottom: spacing.sm },
  exCard: {
    backgroundColor: colors.carbonSlateLight,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: colors.carbonSlateCard,
    padding: spacing.md,
    gap: 8,
    marginBottom: 8,
  },
  exTop: { flexDirection: 'row', justifyContent: 'space-between', gap: 8 },
  exName: { color: colors.textPrimary, fontFamily: 'SpaceGrotesk_600SemiBold', fontSize: 15, flex: 1 },
  remove: { color: colors.textSecondary, fontSize: 12 },
  exMeta: { color: colors.forgeAmber, fontSize: 12 },
  notes: { color: colors.textTertiary, fontSize: 12 },
  steppers: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
});
