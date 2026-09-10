import { Tabs } from 'expo-router';
import { Platform, type ColorValue } from 'react-native';
import { colors } from '@/src/theme/colors';
import { TabBarIcon, type TabBarIconName } from '@/src/components/TabBarIcon';

function icon(name: TabBarIconName) {
  return ({ color, focused }: { color: ColorValue; focused: boolean; size: number }) => (
    <TabBarIcon name={name} color={color} focused={focused} />
  );
}

export default function TabsLayout() {
  return (
    <Tabs
      screenOptions={{
        headerStyle: { backgroundColor: colors.carbonSlate },
        headerTintColor: colors.textPrimary,
        headerTitleStyle: { fontFamily: 'SpaceGrotesk_600SemiBold' },
        tabBarStyle: {
          backgroundColor: colors.carbonSlateLight,
          borderTopColor: colors.carbonSlateCard,
          borderTopWidth: 1,
          height: Platform.select({ ios: 88, default: 72 }),
          paddingTop: 6,
          paddingBottom: Platform.select({ ios: 24, default: 10 }),
        },
        tabBarItemStyle: {
          paddingVertical: 2,
        },
        tabBarIconStyle: {
          marginTop: 2,
        },
        tabBarActiveTintColor: colors.forgeAmber,
        tabBarInactiveTintColor: colors.textTertiary,
        tabBarLabelStyle: {
          fontFamily: 'SpaceGrotesk_500Medium',
          fontSize: 12,
          letterSpacing: 0.2,
          marginTop: 2,
        },
        tabBarHideOnKeyboard: true,
      }}
    >
      <Tabs.Screen
        name="today"
        options={{
          title: 'Today',
          tabBarIcon: icon('today'),
        }}
      />
      <Tabs.Screen
        name="routines"
        options={{
          title: 'Routines',
          tabBarIcon: icon('routines'),
        }}
      />
      <Tabs.Screen
        name="heatmap"
        options={{
          title: 'Heatmap',
          tabBarIcon: icon('heatmap'),
        }}
      />
      <Tabs.Screen
        name="history"
        options={{
          title: 'History',
          tabBarIcon: icon('history'),
        }}
      />
      <Tabs.Screen
        name="profile"
        options={{
          title: 'Profile',
          tabBarIcon: icon('profile'),
        }}
      />
    </Tabs>
  );
}
