import { Tabs } from 'expo-router';
import { Text } from 'react-native';
import { colors } from '@/src/theme/colors';

function TabIcon({ label, focused }: { label: string; focused: boolean }) {
  return (
    <Text style={{ color: focused ? colors.forgeAmber : colors.textTertiary, fontSize: 11, fontFamily: 'SpaceGrotesk_600SemiBold' }}>
      {label}
    </Text>
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
          height: 64,
          paddingBottom: 8,
          paddingTop: 8,
        },
        tabBarActiveTintColor: colors.forgeAmber,
        tabBarInactiveTintColor: colors.textTertiary,
        tabBarLabelStyle: { fontFamily: 'SpaceGrotesk_500Medium', fontSize: 11 },
      }}
    >
      <Tabs.Screen
        name="today"
        options={{
          title: 'Today',
          tabBarIcon: ({ focused }) => <TabIcon label="●" focused={focused} />,
        }}
      />
      <Tabs.Screen
        name="routines"
        options={{
          title: 'Routines',
          tabBarIcon: ({ focused }) => <TabIcon label="≡" focused={focused} />,
        }}
      />
      <Tabs.Screen
        name="heatmap"
        options={{
          title: 'Heatmap',
          tabBarIcon: ({ focused }) => <TabIcon label="◎" focused={focused} />,
        }}
      />
      <Tabs.Screen
        name="history"
        options={{
          title: 'History',
          tabBarIcon: ({ focused }) => <TabIcon label="▦" focused={focused} />,
        }}
      />
      <Tabs.Screen
        name="profile"
        options={{
          title: 'Profile',
          tabBarIcon: ({ focused }) => <TabIcon label="◉" focused={focused} />,
        }}
      />
      </Tabs>
  );
}
