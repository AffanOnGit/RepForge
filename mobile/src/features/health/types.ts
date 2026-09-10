export type HealthConnectAvailability =
  | 'unsupported_platform'
  | 'module_unavailable'
  | 'sdk_unavailable'
  | 'update_required'
  | 'available';

export type HealthConnectStatus = {
  availability: HealthConnectAvailability;
  initialized: boolean;
  hasPermissions: boolean;
  syncEnabled: boolean;
  lastSyncedAtMillis: number | null;
  lastMessage: string | null;
};

export type WorkoutHealthExportInput = {
  title: string;
  startTimeMillis: number;
  endTimeMillis: number;
  caloriesKcal: number;
  sessionId?: string;
};

export type WeightSyncResult = {
  synced: boolean;
  weightKg: number | null;
  message: string;
};

export type WorkoutExportResult = {
  exported: boolean;
  message: string;
};
