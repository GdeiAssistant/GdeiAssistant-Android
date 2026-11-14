# Lingnan Editorial Design Tokens & Homepage Notes

`GeneratedDesignTokens.kt` is synced directly from `design/generated/GeneratedDesignTokens.kt` in the main `GdeiAssistant` repository (`design/tokens.json` v1). Run `node design/generate-tokens.mjs` in the main repository before copying updates; never hand-edit the generated token file.

## Theme System & Typography

`Theme.kt`, `Color.kt`, and `CampusAccent.kt` implement the **Warm Editorial Lingnan Academy** design system across light mode ("岭南晨纸" `#F7F6F2` canvas, `#FFFFFF` surface) and dark mode ("珠江夜读" `#0D1311` canvas, `#151E1B` surface), supporting four switchable campus accents (`BANYAN`, `KAPOK`, `HAIZHU`, `BAUHINIA`).

`Shape.kt` defines restrained radii (`small = 6.dp`, `medium = 10.dp`, `large = 14.dp`) and `DesignKit.kt` uses `0.75.dp` hairlines in place of heavy stacked cards. `Type.kt` pairs serif editorial headlines with humanist sans body copy and tabular numeral styles (`tnum`), while respecting system font scaling and the in-app four-step font scale preference.

## Homepage Schedule & Service Directory

`HomeScreen.kt` reads authenticated `schedule`, `card`, `profile`, and `announcement` repositories. Today is resolved in `Asia/Shanghai`, filtering courses by current API week and course week bounds (`minScheduleWeek` / `maxScheduleWeek`) and displaying each course's actual `scheduleLesson` label rather than synthesized clock times.

Missing payloads, unknown current weeks, or incomplete course metadata render an explicit unavailable state instead of an empty-day fallback, and `QuickStatusStrip` binds strictly to real `HomeUiState` fields.

The homepage service grid maps entries to crisp monochrome `HomeFeatureIcons` ImageVectors matching the Lucide icon vocabulary (`1.8` stroke, unfilled rounded caps/joins), falling back to catalog defaults for new routes. Permission notices live in `design/LUCIDE-LICENSE`.

## Local Verification

Run `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug`, `python3 scripts/check_locale_resources.py`, and `python3 scripts/check_hygiene.py` before pushing changes.
