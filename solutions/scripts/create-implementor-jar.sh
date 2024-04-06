#!/bin/sh

CURRENT_DIR="$(dirname "$(readlink -f "$0")")"
PROJECT_PATH="$CURRENT_DIR/../.."
SOLUTIONS_PATH="$PROJECT_PATH/solutions/java-solutions"
TMP_PATH="$CURRENT_DIR/compiled"

javac -d "$TMP_PATH" "$SOLUTIONS_PATH/info/kgeorgiy/ja/dunaev/implementor/Implementor.java" \
      -cp "$PROJECT_PATH/tests/modules/info.kgeorgiy.java.advanced.implementor:$SOLUTIONS_PATH"
jar cfm "$CURRENT_DIR/implementor.jar" "$CURRENT_DIR/MANIFEST.MF" -C "$TMP_PATH" .
rm -rf "$TMP_PATH"
