import { Link, Stack } from 'expo-router';
import { StyleSheet, Text, View } from 'react-native';
import { colors } from '@/src/theme/colors';

export default function NotFoundScreen() {
  return (
    <>
      <Stack.Screen options={{ title: 'Not found' }} />
      <View style={styles.container}>
        <Text style={styles.title}>Screen not found</Text>
        <Link href="/(tabs)/today" style={styles.link}>
          Back to Today
        </Link>
      </View>
    </>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.carbonSlate,
    alignItems: 'center',
    justifyContent: 'center',
    gap: 12,
  },
  title: { color: colors.textPrimary, fontFamily: 'SpaceGrotesk_600SemiBold', fontSize: 20 },
  link: { color: colors.forgeAmber, fontFamily: 'SpaceGrotesk_500Medium' },
});
