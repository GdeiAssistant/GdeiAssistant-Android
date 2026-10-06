#!/usr/bin/env bash
set -euo pipefail

# Keep the original Gradle status, while collecting diagnostics before the action stops its AVD.
collect_evidence() {
  local test_status=$?
  trap - EXIT
  adb logcat -d -v threadtime > android-emulator-logcat.log 2>&1 || true
  mkdir -p android-ui-evidence
  adb pull /sdcard/Download/GdeiSocialUiEvidence/ android-ui-evidence/ \
    > android-ui-evidence/pull.log 2>&1 || true
  while IFS= read -r -d '' evidence_archive; do
    unzip -o "$evidence_archive" -d "${evidence_archive%/*}" >> android-ui-evidence/pull.log 2>&1 || true
  done < <(find android-ui-evidence -name '*.zip' -print0)
  exit "$test_status"
}
trap collect_evidence EXIT

./gradlew connectedDebugAndroidTest --no-daemon --stacktrace --console=plain \
  -Pandroid.testInstrumentationRunnerArguments.gdeiEmulator=true \
  2>&1 | tee gradle-connected-tests.log
