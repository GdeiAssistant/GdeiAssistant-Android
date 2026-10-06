#!/usr/bin/env bash
set -euo pipefail

# Keep the original Gradle status, while collecting diagnostics before the action stops its AVD.
collect_evidence() {
  local test_status=$?
  trap - EXIT
  adb logcat -d -v threadtime > android-emulator-logcat.log 2>&1 || true
  mkdir -p android-ui-evidence
  adb pull /sdcard/Android/data/cn.gdeiassistant/files/social-ui-evidence/ android-ui-evidence/ \
    > android-ui-evidence/pull.log 2>&1 || true
  exit "$test_status"
}
trap collect_evidence EXIT

./gradlew connectedDebugAndroidTest --no-daemon --stacktrace --console=plain \
  2>&1 | tee gradle-connected-tests.log
