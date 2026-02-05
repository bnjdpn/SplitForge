#!/usr/bin/env sh
set -e

echo "SplitForge bootstrap"
if ! command -v java >/dev/null 2>&1; then
  echo "Java 17 required. Install from https://adoptium.net" >&2
  exit 1
fi

echo "Java detected: $(java -version 2>&1 | head -n 1)"

echo "Running Gradle sync (this may download Gradle)..."
./gradlew :shared:tasks >/dev/null

echo "Bootstrap complete."
