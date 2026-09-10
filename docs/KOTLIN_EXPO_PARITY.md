# Kotlin ↔ Expo parity decision

**Date:** 2026-09-10  
**Verdict:** **Remove the Kotlin / Jetpack Compose Android tree.** Expo `mobile/` is the shipping client.

## Removal bar (core product flows)

| Flow | Expo (`mobile/`) | Kotlin | Notes |
|------|------------------|--------|-------|
| Onboarding + storage consent + local profiles | Yes | Partial (no SAF consent; Room profile) | Expo is the intended local-first model |
| Auth unlock (PIN / biometric / guest) | Yes (SecureStore + Local Auth) | Firebase email + guest only | Product decision: local-only, no Firebase |
| Routines + active session (ghost, rest, swap, plate, diff) | Yes | Yes | Plate UI simpler in Expo; engine present |
| History, heatmap, profile | Yes | Yes | Heatmap simplified (grid vs Canvas anatomy) — acceptable |
| AI ingestion | Yes (Gemini key or simulator) | Simulated only | Expo meets or exceeds Kotlin |
| Health Connect (Android) | Yes (dev / prebuild) | Yes | Soft no-op in Expo Go |
| Export CSV / JSON | Yes | Yes | Import/restore absent in both |

## Acceptable gaps (not blocking removal)

- Anatomy heatmap is a tonnage grid, not front/rear SVG/Canvas body map
- No GitHub-style consistency matrix / deep monthly set archive UI
- Plate math modal uses a default bar (no full bar picker UI)
- “Save as new variation” does not fully apply session edits to the copy
- Import/restore not implemented (also missing in Kotlin)
- Real YouTube transcript/metadata still thin; Gemini uses video id + prompt

## Blocking gaps checked

None of the critical shipping flows are Kotlin-only. Keeping both stacks would duplicate maintenance with no product benefit.

## Follow-up after deletion

- Root README and CI target `mobile/` only
- Run via `cd mobile && npm install && npx expo start` (HC: `expo run:android` / EAS dev client)
