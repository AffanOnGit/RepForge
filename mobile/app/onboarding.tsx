import { useMemo, useState } from 'react';
import {
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { ForgeButton } from '@/src/components/ForgeButton';
import { ForgeTextField } from '@/src/components/ForgeTextField';
import { STORAGE_CONSENT_COPY } from '@/src/data/storagePermissions';
import { UnitConverter } from '@/src/domain/engines';
import type {
  BiologicalSex,
  TrainingExperience,
  UnitSystem,
} from '@/src/domain/types';
import { useAuthStore } from '@/src/stores/authStore';
import { colors, spacing } from '@/src/theme/colors';

const FEATURES = [
  { title: 'Local-first', body: 'Profiles + workouts live on this phone — like a private cloud DB.' },
  { title: 'Ghost overload', body: 'Previous session weight × reps appear as you train.' },
  { title: 'Honest calories', body: 'Mifflin-St Jeor + tonnage with a clear ±15% band.' },
];

export default function OnboardingScreen() {
  const completeOnboarding = useAuthStore((s) => s.completeOnboarding);
  const grantStorageConsent = useAuthStore((s) => s.grantStorageConsent);
  const storageConsentGranted = useAuthStore((s) => s.storageConsentGranted);
  const [step, setStep] = useState(0);
  const [displayName, setDisplayName] = useState('Athlete');
  const [unitSystem, setUnitSystem] = useState<UnitSystem>('metric');
  const [weightInput, setWeightInput] = useState('75.0');
  const [heightCm, setHeightCm] = useState('175');
  const [heightFeet, setHeightFeet] = useState('5');
  const [heightInches, setHeightInches] = useState('9');
  const [age, setAge] = useState('25');
  const [sex, setSex] = useState<BiologicalSex>('MALE');
  const [experience, setExperience] = useState<TrainingExperience>('INTERMEDIATE');
  const [saving, setSaving] = useState(false);

  const toggleUnits = (next: UnitSystem) => {
    if (next === unitSystem) return;
    if (next === 'imperial') {
      setWeightInput(UnitConverter.kgToLb(parseFloat(weightInput) || 75).toFixed(1));
    } else {
      setWeightInput(UnitConverter.lbToKg(parseFloat(weightInput) || 165).toFixed(1));
    }
    setUnitSystem(next);
  };

  const canFinish = useMemo(() => {
    const w = parseFloat(weightInput);
    const a = parseInt(age, 10);
    return (
      storageConsentGranted &&
      displayName.trim().length > 0 &&
      Number.isFinite(w) &&
      w > 0 &&
      Number.isFinite(a) &&
      a > 10
    );
  }, [weightInput, age, storageConsentGranted, displayName]);

  const acceptConsent = async () => {
    await grantStorageConsent();
    setStep(1);
  };

  const finish = async () => {
    if (!canFinish || saving) return;
    setSaving(true);
    const weightKg =
      unitSystem === 'metric'
        ? parseFloat(weightInput) || 75
        : UnitConverter.lbToKg(parseFloat(weightInput) || 165);
    const height =
      unitSystem === 'metric'
        ? parseFloat(heightCm) || 175
        : UnitConverter.feetInchesToCm(
            parseInt(heightFeet, 10) || 5,
            parseInt(heightInches, 10) || 9
          );
    await completeOnboarding({
      id: crypto.randomUUID(),
      displayName: displayName.trim() || 'Athlete',
      email: '',
      weightKg,
      heightCm: height,
      age: parseInt(age, 10) || 25,
      biologicalSex: sex,
      bodyFatPercentage: null,
      trainingExperience: experience,
      unitSystem,
      createdAtMillis: Date.now(),
    });
    setSaving(false);
  };

  return (
    <SafeAreaView style={styles.safe}>
      <ScrollView contentContainerStyle={styles.content}>
        <Text style={styles.brand}>RepForge</Text>
        <View style={styles.dots}>
          {[0, 1, 2, 3].map((i) => (
            <View key={i} style={[styles.dot, step === i && styles.dotOn]} />
          ))}
        </View>

        {step === 0 && (
          <View style={styles.block}>
            <Text style={styles.headline}>{STORAGE_CONSENT_COPY.title}</Text>
            <Text style={styles.sub}>{STORAGE_CONSENT_COPY.body}</Text>
            <View style={styles.card}>
              <Text style={styles.cardTitle}>Why we ask</Text>
              <Text style={styles.cardBody}>{STORAGE_CONSENT_COPY.why}</Text>
            </View>
            <View style={styles.card}>
              <Text style={styles.cardTitle}>Permissions</Text>
              <Text style={styles.cardBody}>{STORAGE_CONSENT_COPY.sandboxNote}</Text>
            </View>
          </View>
        )}

        {step === 1 && (
          <View style={styles.block}>
            <Text style={styles.headline}>Train hard.{'\n'}Track honest.</Text>
            <Text style={styles.sub}>
              A sweaty-hands strength logger — offline, free, and built for progressive overload.
            </Text>
            {FEATURES.map((f) => (
              <View key={f.title} style={styles.feature}>
                <Text style={styles.featureTitle}>{f.title}</Text>
                <Text style={styles.featureBody}>{f.body}</Text>
              </View>
            ))}
          </View>
        )}

        {step === 2 && (
          <View style={styles.block}>
            <Text style={styles.headline}>Your profile</Text>
            <Text style={styles.sub}>
              Biometrics stay on this device and power calorie estimates. You can add more profiles later.
            </Text>

            <ForgeTextField
              label="Profile name"
              value={displayName}
              onChangeText={setDisplayName}
              autoCapitalize="words"
            />

            <View style={styles.row}>
              {(['metric', 'imperial'] as UnitSystem[]).map((u) => (
                <Pressable
                  key={u}
                  onPress={() => toggleUnits(u)}
                  style={[styles.chip, unitSystem === u && styles.chipOn]}
                >
                  <Text style={[styles.chipText, unitSystem === u && styles.chipTextOn]}>
                    {u === 'metric' ? 'kg / cm' : 'lb / ft'}
                  </Text>
                </Pressable>
              ))}
            </View>

            <ForgeTextField
              label={unitSystem === 'metric' ? 'Body weight (kg)' : 'Body weight (lb)'}
              keyboardType="decimal-pad"
              value={weightInput}
              onChangeText={setWeightInput}
            />
            {unitSystem === 'metric' ? (
              <ForgeTextField
                label="Height (cm)"
                keyboardType="number-pad"
                value={heightCm}
                onChangeText={setHeightCm}
              />
            ) : (
              <View style={styles.row}>
                <View style={{ flex: 1 }}>
                  <ForgeTextField label="Feet" keyboardType="number-pad" value={heightFeet} onChangeText={setHeightFeet} />
                </View>
                <View style={{ flex: 1 }}>
                  <ForgeTextField label="Inches" keyboardType="number-pad" value={heightInches} onChangeText={setHeightInches} />
                </View>
              </View>
            )}
            <ForgeTextField label="Age" keyboardType="number-pad" value={age} onChangeText={setAge} />

            <Text style={styles.section}>Biological sex</Text>
            <View style={styles.row}>
              {(['MALE', 'FEMALE'] as BiologicalSex[]).map((s) => (
                <Pressable key={s} onPress={() => setSex(s)} style={[styles.chip, sex === s && styles.chipOn]}>
                  <Text style={[styles.chipText, sex === s && styles.chipTextOn]}>{s === 'MALE' ? 'Male' : 'Female'}</Text>
                </Pressable>
              ))}
            </View>

            <Text style={styles.section}>Experience</Text>
            <View style={styles.rowWrap}>
              {(['BEGINNER', 'INTERMEDIATE', 'ADVANCED'] as TrainingExperience[]).map((e) => (
                <Pressable key={e} onPress={() => setExperience(e)} style={[styles.chip, experience === e && styles.chipOn]}>
                  <Text style={[styles.chipText, experience === e && styles.chipTextOn]}>
                    {e.charAt(0) + e.slice(1).toLowerCase()}
                  </Text>
                </Pressable>
              ))}
            </View>
          </View>
        )}

        {step === 3 && (
          <View style={styles.block}>
            <Text style={styles.headline}>Ready to train</Text>
            <Text style={styles.sub}>
              Everything stays on this device. Health Connect, CSV/JSON export, and AI import all
              scope to the active local profile — no cloud account required.
            </Text>
            <View style={styles.card}>
              <Text style={styles.cardTitle}>What we store</Text>
              <Text style={styles.cardBody}>
                Profile biometrics, routines, and every set you log — scoped to this profile on this
                device. Optional PIN / biometric unlock, Health Connect sync, and a backup folder for
                exports can be set from Profile after you enter the app.
              </Text>
            </View>
          </View>
        )}
      </ScrollView>

      <View style={styles.footer}>
        {step > 0 ? (
          <ForgeButton title="Back" variant="ghost" onPress={() => setStep((s) => s - 1)} />
        ) : (
          <View />
        )}
        {step === 0 ? (
          <ForgeButton title="Allow app data" onPress={acceptConsent} />
        ) : step < 3 ? (
          <ForgeButton title={step === 1 ? 'Continue' : 'Continue'} onPress={() => setStep((s) => s + 1)} />
        ) : (
          <ForgeButton
            title={saving ? 'Saving…' : 'Enter RepForge'}
            onPress={finish}
            disabled={!canFinish || saving}
          />
        )}
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: colors.carbonSlate },
  content: { padding: spacing.xxl, paddingBottom: 120, gap: spacing.lg },
  brand: {
    fontFamily: 'SpaceGrotesk_700Bold',
    fontSize: 28,
    color: colors.forgeAmber,
    letterSpacing: -0.5,
  },
  dots: { flexDirection: 'row', gap: 8 },
  dot: { width: 8, height: 8, borderRadius: 4, backgroundColor: colors.carbonSlateCard },
  dotOn: { backgroundColor: colors.kineticLime, width: 22 },
  block: { gap: spacing.md },
  headline: {
    fontFamily: 'SpaceGrotesk_700Bold',
    fontSize: 34,
    lineHeight: 40,
    color: colors.textPrimary,
  },
  sub: { color: colors.textSecondary, fontFamily: 'SpaceGrotesk_500Medium', fontSize: 16, lineHeight: 24 },
  feature: {
    backgroundColor: colors.carbonSlateLight,
    borderRadius: 14,
    padding: spacing.lg,
    borderWidth: 1,
    borderColor: colors.carbonSlateCard,
    gap: 4,
  },
  featureTitle: { color: colors.kineticLime, fontFamily: 'SpaceGrotesk_600SemiBold', fontSize: 15 },
  featureBody: { color: colors.textSecondary, fontSize: 14, lineHeight: 20 },
  row: { flexDirection: 'row', gap: 10 },
  rowWrap: { flexDirection: 'row', flexWrap: 'wrap', gap: 10 },
  chip: {
    minHeight: 44,
    paddingHorizontal: 14,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: colors.carbonSlateCard,
    backgroundColor: colors.carbonSlateSurface,
    alignItems: 'center',
    justifyContent: 'center',
  },
  chipOn: { borderColor: colors.forgeAmber, backgroundColor: '#FF660022' },
  chipText: { color: colors.textSecondary, fontFamily: 'SpaceGrotesk_500Medium' },
  chipTextOn: { color: colors.forgeAmber },
  section: { color: colors.textTertiary, marginTop: 8, fontFamily: 'SpaceGrotesk_500Medium' },
  card: {
    backgroundColor: colors.carbonSlateLight,
    borderRadius: 14,
    padding: spacing.lg,
    borderWidth: 1,
    borderColor: colors.carbonSlateCard,
    gap: 8,
  },
  cardTitle: { color: colors.textPrimary, fontFamily: 'SpaceGrotesk_600SemiBold', fontSize: 16 },
  cardBody: { color: colors.textSecondary, lineHeight: 22 },
  footer: {
    position: 'absolute',
    left: 0,
    right: 0,
    bottom: 0,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: spacing.xl,
    backgroundColor: colors.carbonSlateLight,
    borderTopWidth: 1,
    borderTopColor: colors.carbonSlateCard,
  },
});
