#!/bin/sh

CURRENT_DIR="$(dirname "$(readlink -f "$0")")"
PROJECT_PATH="$CURRENT_DIR/.."
SOLUTIONS_PATH="$PROJECT_PATH/java-solutions"
TMP_PATH="$CURRENT_DIR/compiled"
LIBS="$PROJECT_PATH/lib/*"

javac -d "$TMP_PATH" "$SOLUTIONS_PATH/info/kgeorgiy/ja/dunaev/bank/BankTests.java" \
      -cp "$LIBS:$SOLUTIONS_PATH"

cd "$TMP_PATH"
java -cp ".:$LIBS" info.kgeorgiy.ja.dunaev.bank.BankTests
EXIT_CODE=$?
cd ..

rm -rf "$TMP_PATH"
exit $EXIT_CODE
