const {
  createRunOncePlugin,
  withAndroidManifest,
} = require('@expo/config-plugins');

const HEALTH_CONNECT_PACKAGE = 'com.google.android.apps.healthdata';

/**
 * Ensures AndroidManifest can query the Health Connect provider package.
 * Permissions + rationale intent are handled by `react-native-health-connect` + app.json.
 */
const withHealthConnectQueries = (config) =>
  withAndroidManifest(config, (config) => {
    const manifest = config.modResults.manifest;
    if (!manifest.queries) {
      manifest.queries = [];
    }
    const hasPackage = manifest.queries.some((q) =>
      (q.package || []).some((p) => p.$?.['android:name'] === HEALTH_CONNECT_PACKAGE)
    );
    if (!hasPackage) {
      manifest.queries.push({
        package: [{ $: { 'android:name': HEALTH_CONNECT_PACKAGE } }],
      });
    }
    return config;
  });

module.exports = createRunOncePlugin(
  withHealthConnectQueries,
  'repforge-health-connect-queries',
  '1.0.0'
);
