import { useEffect, useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { Link, useRouter } from 'expo-router';
import { ForgeButton } from '@/src/components/ForgeButton';
import { ForgeTextField } from '@/src/components/ForgeTextField';
import { useAuthStore } from '@/src/stores/authStore';
import { colors, spacing } from '@/src/theme/colors';

/** Local unlock — no cloud passwords. PIN hash in SecureStore; optional biometrics. */
export default function UnlockScreen() {
  const router = useRouter();
  const unlockConfigured = useAuthStore((s) => s.unlockConfigured);
  const unlockWithPin = useAuthStore((s) => s.unlockWithPin);
  const unlockWithBiometric = useAuthStore((s) => s.unlockWithBiometric);
  const continueAsGuest = useAuthStore((s) => s.continueAsGuest);
  const biometricAvailable = useAuthStore((s) => s.biometricAvailable);
  const unlocked = useAuthStore((s) => s.unlocked);
  const error = useAuthStore((s) => s.error);
  const clearError = useAuthStore((s) => s.clearError);
  const [pin, setPin] = useState('');
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (unlocked) router.replace('/(tabs)/today');
  }, [unlocked, router]);

  const onUnlock = async () => {
    clearError();
    setBusy(true);
    const ok = await unlockWithPin(pin);
    setBusy(false);
    if (ok) router.replace('/(tabs)/today');
  };

  const onBiometric = async () => {
    clearError();
    setBusy(true);
    const ok = await unlockWithBiometric();
    setBusy(false);
    if (ok) router.replace('/(tabs)/today');
  };

  const onGuest = async () => {
    await continueAsGuest();
    router.replace('/(tabs)/today');
  };

  const onContinue = async () => {
    await continueAsGuest();
    router.replace('/(tabs)/today');
  };

  return (
    <View style={styles.container}>
      <Text style={styles.brand}>RepForge</Text>
      <Text style={styles.sub}>
        {unlockConfigured
          ? 'Unlock your local profiles. Data never leaves this device.'
          : 'Local profiles are ready — continue without a cloud account.'}
      </Text>

      {unlockConfigured ? (
        <>
          <ForgeTextField
            label="PIN"
            secureTextEntry
            keyboardType="number-pad"
            value={pin}
            onChangeText={setPin}
            maxLength={8}
          />
          {error ? <Text style={styles.error}>{error}</Text> : null}
          <ForgeButton
            title={busy ? 'Unlocking…' : 'Unlock'}
            fullWidth
            onPress={onUnlock}
            disabled={busy || pin.length < 4}
          />
          {biometricAvailable ? (
            <ForgeButton
              title="Use biometrics"
              variant="outline"
              fullWidth
              onPress={onBiometric}
              disabled={busy}
            />
          ) : null}
        </>
      ) : (
        <ForgeButton title="Continue" fullWidth onPress={onContinue} />
      )}

      <Link href="/auth/setup-pin" style={styles.link}>
        {unlockConfigured ? 'Change unlock PIN' : 'Set optional PIN'}
      </Link>
      <ForgeButton title="Continue as guest" variant="ghost" fullWidth onPress={onGuest} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: spacing.xxl, gap: spacing.md, justifyContent: 'center' },
  brand: { fontFamily: 'SpaceGrotesk_700Bold', fontSize: 36, color: colors.forgeAmber },
  sub: { color: colors.textSecondary, marginBottom: spacing.md, lineHeight: 22 },
  error: { color: colors.errorRed },
  link: { color: colors.kineticLime, textAlign: 'center', fontFamily: 'SpaceGrotesk_500Medium' },
});
