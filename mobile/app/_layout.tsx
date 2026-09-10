import { useEffect } from 'react';
import { ActivityIndicator, View } from 'react-native';
import { Stack, useRouter, useSegments } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import {
  useFonts,
  SpaceGrotesk_500Medium,
  SpaceGrotesk_600SemiBold,
  SpaceGrotesk_700Bold,
} from '@expo-google-fonts/space-grotesk';
import {
  IBMPlexMono_400Regular,
  IBMPlexMono_500Medium,
} from '@expo-google-fonts/ibm-plex-mono';
import * as SplashScreen from 'expo-splash-screen';
import { useAuthStore } from '@/src/stores/authStore';
import { colors } from '@/src/theme/colors';

export { ErrorBoundary } from 'expo-router';

SplashScreen.preventAutoHideAsync();

function AuthGate({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const segments = useSegments();
  const ready = useAuthStore((s) => s.ready);
  const onboardingComplete = useAuthStore((s) => s.onboardingComplete);
  const unlocked = useAuthStore((s) => s.unlocked);
  const isGuest = useAuthStore((s) => s.isGuest);

  useEffect(() => {
    if (!ready) return;
    const inOnboarding = segments[0] === 'onboarding';
    const inAuth = segments[0] === 'auth';

    if (!onboardingComplete) {
      if (!inOnboarding) router.replace('/onboarding');
      return;
    }

    const signedIn = unlocked || isGuest;
    if (!signedIn && !inAuth) {
      router.replace('/auth/login');
      return;
    }

    if (signedIn && (inAuth || inOnboarding)) {
      // Allow setup-pin while signed in
      const path = segments as string[];
      const authScreen = path.length > 1 ? path[1] : '';
      if (inAuth && authScreen === 'setup-pin') return;
      if (inOnboarding || (inAuth && authScreen !== 'setup-pin')) {
        router.replace('/(tabs)/today');
      }
    }
  }, [ready, onboardingComplete, unlocked, isGuest, segments, router]);

  if (!ready) {
    return (
      <View style={{ flex: 1, backgroundColor: colors.carbonSlate, alignItems: 'center', justifyContent: 'center' }}>
        <ActivityIndicator color={colors.forgeAmber} size="large" />
      </View>
    );
  }

  return <>{children}</>;
}

export default function RootLayout() {
  const bootstrap = useAuthStore((s) => s.bootstrap);
  const [loaded, error] = useFonts({
    SpaceGrotesk_500Medium,
    SpaceGrotesk_600SemiBold,
    SpaceGrotesk_700Bold,
    IBMPlexMono_400Regular,
    IBMPlexMono_500Medium,
  });

  useEffect(() => {
    bootstrap();
  }, [bootstrap]);

  useEffect(() => {
    if (error) throw error;
  }, [error]);

  useEffect(() => {
    if (loaded) SplashScreen.hideAsync();
  }, [loaded]);

  if (!loaded) return null;

  return (
    <AuthGate>
      <StatusBar style="light" />
      <Stack
        screenOptions={{
          headerStyle: { backgroundColor: colors.carbonSlate },
          headerTintColor: colors.textPrimary,
          headerTitleStyle: { fontFamily: 'SpaceGrotesk_600SemiBold' },
          contentStyle: { backgroundColor: colors.carbonSlate },
        }}
      >
        <Stack.Screen name="onboarding" options={{ headerShown: false }} />
        <Stack.Screen name="auth" options={{ headerShown: false }} />
        <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
        <Stack.Screen
          name="active-session/[routineId]"
          options={{ title: 'Active Session', headerBackTitle: 'Back' }}
        />
        <Stack.Screen
          name="workout-summary/[sessionId]"
          options={{ title: 'Summary', headerBackVisible: false }}
        />
        <Stack.Screen name="routine/[id]" options={{ title: 'Routine' }} />
        <Stack.Screen name="routine/create" options={{ title: 'New Routine', presentation: 'modal' }} />
        <Stack.Screen name="exercises" options={{ title: 'Exercise Library' }} />
        <Stack.Screen
          name="ai/ingest"
          options={{ title: 'AI Ingestion', headerBackTitle: 'Back' }}
        />
      </Stack>
    </AuthGate>
  );
}
