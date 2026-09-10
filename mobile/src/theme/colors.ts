/** RepForge design tokens — Carbon Slate / Forge Amber / Kinetic Lime */
export const colors = {
  carbonSlate: '#0B0D10',
  carbonSlateLight: '#141820',
  carbonSlateSurface: '#1A1F2A',
  carbonSlateCard: '#212733',
  forgeAmber: '#FF6600',
  forgeAmberDark: '#CC5200',
  forgeAmberLight: '#FF8533',
  kineticLime: '#D4FF00',
  kineticLimeDark: '#AACC00',
  textPrimary: '#F0F0F0',
  textSecondary: '#B0B8C8',
  textTertiary: '#6B7588',
  textGhost: '#4A5568',
  errorRed: '#CF6679',
  successGreen: '#81C784',
  warningAmber: '#FFB74D',
  warmup: '#64B5F6',
  dropSet: '#BA68C8',
  failure: '#EF5350',
  progressGreen: '#4CAF50',
  plateauYellow: '#FFC107',
  declineRed: '#F44336',
} as const;

export const spacing = {
  xs: 4,
  sm: 8,
  md: 12,
  lg: 16,
  xl: 20,
  xxl: 24,
  xxxl: 32,
} as const;

/** Sweaty-hands UX: primary touch targets ≥ 48dp */
export const touchTarget = {
  min: 48,
  stepper: 52,
} as const;
