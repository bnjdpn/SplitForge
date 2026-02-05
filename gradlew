#!/usr/bin/env sh
set -e

DIR="$(cd "$(dirname "$0")" && pwd)"
PROPS="$DIR/gradle/wrapper/gradle-wrapper.properties"

if [ ! -f "$PROPS" ]; then
  echo "Missing gradle-wrapper.properties" >&2
  exit 1
fi

DIST_URL=$(grep -E '^distributionUrl=' "$PROPS" | cut -d'=' -f2- | sed 's#\\##g')
if [ -z "$DIST_URL" ]; then
  echo "distributionUrl not found" >&2
  exit 1
fi

ZIP_NAME=$(basename "$DIST_URL")
DIST_ROOT="$DIR/.gradle-wrapper"
ZIP_PATH="$DIST_ROOT/$ZIP_NAME"
GRADLE_VERSION=$(echo "$ZIP_NAME" | sed -E 's/gradle-([0-9.]+).*/\1/')
INSTALL_DIR="$DIST_ROOT/gradle-$GRADLE_VERSION"

mkdir -p "$DIST_ROOT"

if [ ! -f "$ZIP_PATH" ]; then
  if command -v curl >/dev/null 2>&1; then
    curl -L "$DIST_URL" -o "$ZIP_PATH"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$ZIP_PATH" "$DIST_URL"
  else
    echo "curl or wget required to download Gradle" >&2
    exit 1
  fi
fi

if [ ! -d "$INSTALL_DIR" ]; then
  unzip -q "$ZIP_PATH" -d "$DIST_ROOT"
fi

exec "$INSTALL_DIR/bin/gradle" "$@"
