import { SymbolView, type AndroidSymbol } from 'expo-symbols';
import MaterialSymbolsLight from 'expo-symbols/androidWeights/light';
import { StyleSheet, View, type ColorValue } from 'react-native';
import type { SFSymbol } from 'sf-symbols-typescript';
import { colors, touchTarget } from '@/src/theme/colors';

export type TabBarIconName = 'today' | 'routines' | 'heatmap' | 'history' | 'profile';

/** Material Symbols (Android/web) + SF Symbols (iOS) — gym vernacular, light stroke. */
const ICONS: Record<
  TabBarIconName,
  { ios: SFSymbol; android: AndroidSymbol; web: AndroidSymbol }
> = {
  today: {
    ios: 'figure.strengthtraining.traditional',
    android: 'fitness_center',
    web: 'fitness_center',
  },
  routines: {
    ios: 'list.bullet.rectangle',
    android: 'list_alt',
    web: 'list_alt',
  },
  heatmap: {
    ios: 'square.grid.3x3.fill',
    android: 'grid_view',
    web: 'grid_view',
  },
  history: {
    ios: 'clock.arrow.circlepath',
    android: 'history',
    web: 'history',
  },
  profile: {
    ios: 'person.crop.circle',
    android: 'account_circle',
    web: 'account_circle',
  },
};

type Props = {
  name: TabBarIconName;
  color: ColorValue;
  focused: boolean;
};

/** Gym-floor tab icons: 28pt glyphs inside a sweaty-hands touch plate. */
export function TabBarIcon({ name, color, focused }: Props) {
  const icon = ICONS[name];

  return (
    <View style={[styles.plate, focused && styles.plateFocused]} accessibilityElementsHidden>
      <SymbolView
        name={{ ios: icon.ios, android: icon.android, web: icon.web }}
        size={ICON_SIZE}
        tintColor={color}
        weight={{ ios: 'light', android: MaterialSymbolsLight }}
        resizeMode="scaleAspectFit"
        style={styles.symbol}
      />
    </View>
  );
}

const ICON_SIZE = 28;

const styles = StyleSheet.create({
  plate: {
    minWidth: touchTarget.min,
    minHeight: 40,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 10,
    paddingHorizontal: 10,
  },
  plateFocused: {
    backgroundColor: colors.carbonSlateCard,
  },
  symbol: {
    width: ICON_SIZE,
    height: ICON_SIZE,
  },
});
