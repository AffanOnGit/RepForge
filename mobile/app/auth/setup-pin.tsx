import { useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { useRouter } from 'expo-router';
import { ForgeButton } from '@/src/components/ForgeButton';
import { ForgeTextField } from '@/src/components/ForgeTextField';
import { useAuthStore } from '@/src/stores/authStore';
import { colors, spacing } from '@/src/theme/colors';

/** Optional local PIN — SHA-256 hash in SecureStore (never plaintext). */
export default function SetupPinScreen() {
  const router = useRouter();
  const setPin = useAuthStore((s) => s.setPin);
  const clearPin = useAuthStore((s) => s.clearPin);
  const unlockConfigured = useAuthStore((s) => s.unlockConfigured);
  const error = useAuthStore((s) => s.error);
  const clearError = useAuthStore((s) => s.clearError);
  const [pin, setPinValue] = useState('');
  const [confirm, setConfirm] = useState('');
  const [busy, setBusy] = useState(false);
  const [localError, setLocalError] = useState<string | null>(null);

  const onSave = async () => {
    clearError();
    setLocalError(null);
    if (pin !== confirm) {
      setLocalError('PINs do not match');
      return;
    }
    setBusy(true);
    const ok = await setPin(pin);
    setBusy(false);
    if (ok) router.replace('/(tabs)/today');
  };

  const onRemove = async () => {
    setBusy(true);
    await clearPin();
    setBusy(false);
    router.back();
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Device unlock</Text>
      <Text style={styles.sub}>
        Optional 4–8 digit PIN protects profile access on this phone. We store a hash only — no cloud
        passwords, no Firebase.
      </Text>
      <ForgeTextField
        label="PIN"
        secureTextEntry
        keyboardType="number-pad"
        value={pin}
        onChangeText={setPinValue}
        maxLength={8}
      />
      <ForgeTextField
        label="Confirm PIN"
        secureTextEntry
        keyboardType="number-pad"
        value={confirm}
        onChangeText={setConfirm}
        maxLength={8}
      />
      {localError || error ? <Text style={styles.error}>{localError ?? error}</Text> : null}
      <ForgeButton
        title={busy ? 'Saving…' : 'Save PIN'}
        fullWidth
        onPress={onSave}
        disabled={busy || pin.length < 4}
      />
      {unlockConfigured ? (
        <ForgeButton title="Remove PIN" variant="ghost" fullWidth onPress={onRemove} disabled={busy} />
      ) : (
        <ForgeButton title="Skip" variant="ghost" fullWidth onPress={() => router.back()} />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: spacing.xxl, gap: spacing.md, justifyContent: 'center' },
  title: { fontFamily: 'SpaceGrotesk_700Bold', fontSize: 28, color: colors.textPrimary },
  sub: { color: colors.textSecondary, lineHeight: 22, marginBottom: 8 },
  error: { color: colors.errorRed },
});
