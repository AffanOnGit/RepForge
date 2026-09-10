import { StyleSheet, Text, TextInput, View, type TextInputProps } from 'react-native';
import { colors, touchTarget } from '@/src/theme/colors';

type Props = TextInputProps & {
  label?: string;
  error?: string;
};

export function ForgeTextField({ label, error, style, ...rest }: Props) {
  return (
    <View style={styles.wrap}>
      {label ? <Text style={styles.label}>{label}</Text> : null}
      <TextInput
        placeholderTextColor={colors.textTertiary}
        style={[styles.input, error ? styles.inputError : null, style]}
        {...rest}
      />
      {error ? <Text style={styles.error}>{error}</Text> : null}
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: { gap: 6 },
  label: {
    color: colors.textSecondary,
    fontFamily: 'SpaceGrotesk_500Medium',
    fontSize: 13,
  },
  input: {
    minHeight: touchTarget.min,
    backgroundColor: colors.carbonSlateSurface,
    borderWidth: 1,
    borderColor: colors.carbonSlateCard,
    borderRadius: 12,
    paddingHorizontal: 14,
    color: colors.textPrimary,
    fontFamily: 'IBMPlexMono_400Regular',
    fontSize: 16,
  },
  inputError: { borderColor: colors.errorRed },
  error: { color: colors.errorRed, fontSize: 12 },
});
