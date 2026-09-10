import {
  Pressable,
  StyleSheet,
  Text,
  type PressableProps,
  type StyleProp,
  type ViewStyle,
} from 'react-native';
import { colors, touchTarget } from '@/src/theme/colors';

type Variant = 'primary' | 'secondary' | 'ghost' | 'outline';

type Props = Omit<PressableProps, 'style'> & {
  title: string;
  variant?: Variant;
  fullWidth?: boolean;
  style?: StyleProp<ViewStyle>;
};

export function ForgeButton({
  title,
  variant = 'primary',
  fullWidth,
  disabled,
  style,
  ...rest
}: Props) {
  const bg =
    variant === 'primary'
      ? colors.forgeAmber
      : variant === 'secondary'
        ? colors.kineticLime
        : variant === 'outline'
          ? 'transparent'
          : colors.carbonSlateCard;

  const textColor =
    variant === 'primary' || variant === 'secondary' ? colors.carbonSlate : colors.forgeAmber;

  return (
    <Pressable
      accessibilityRole="button"
      disabled={disabled}
      style={({ pressed }) => [
        styles.base,
        {
          backgroundColor: bg,
          borderColor: variant === 'outline' ? colors.forgeAmber : 'transparent',
          borderWidth: variant === 'outline' ? 1.5 : 0,
          opacity: disabled ? 0.45 : pressed ? 0.85 : 1,
          alignSelf: fullWidth ? 'stretch' : 'auto',
          width: fullWidth ? ('100%' as const) : undefined,
        },
        style,
      ]}
      {...rest}
    >
      <Text style={[styles.label, { color: textColor }]}>{title}</Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  base: {
    minHeight: touchTarget.min,
    paddingHorizontal: 20,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
  },
  label: {
    fontFamily: 'SpaceGrotesk_600SemiBold',
    fontSize: 16,
    letterSpacing: 0.2,
  },
});
