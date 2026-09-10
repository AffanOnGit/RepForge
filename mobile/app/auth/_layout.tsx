import { Stack } from 'expo-router';
import { colors } from '@/src/theme/colors';

export default function AuthLayout() {
  return (
    <Stack
      screenOptions={{
        headerStyle: { backgroundColor: colors.carbonSlate },
        headerTintColor: colors.textPrimary,
        contentStyle: { backgroundColor: colors.carbonSlate },
      }}
    >
      <Stack.Screen name="login" options={{ title: 'Unlock' }} />
      <Stack.Screen name="setup-pin" options={{ title: 'Device PIN' }} />
    </Stack>
  );
}
