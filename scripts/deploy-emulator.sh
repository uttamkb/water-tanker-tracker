#!/usr/bin/env bash
set -euo pipefail

# Deploys the debug build to the currently running emulator/device and launches the app.

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOCAL_PROPERTIES="${ROOT_DIR}/local.properties"

if [[ ! -f "${LOCAL_PROPERTIES}" ]]; then
  echo "local.properties missing; cannot locate sdk.dir. Create it with sdk.dir=/path/to/android/sdk"
  exit 1
fi

SDK_DIR="$(grep '^sdk.dir=' "${LOCAL_PROPERTIES}" | head -1 | cut -d'=' -f2-)"
ADB_BIN="${SDK_DIR}/platform-tools/adb"

if [[ -z "${SDK_DIR}" || ! -x "${ADB_BIN}" ]]; then
  echo "Android SDK not found or adb not executable at ${ADB_BIN}"
  exit 1
fi

cd "${ROOT_DIR}"

./gradlew :app:installDebug

"${ADB_BIN}" shell am start -n finsight.apartment.watertracker/com.apartment.watertracker.MainActivity

echo "Deployed and launched app on the connected emulator/device."
