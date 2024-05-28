#!/bin/sh

CURRENT_DIR="$(dirname "$(readlink -f "$0")")"
PROJECT_PATH="$CURRENT_DIR/../.."
SOLUTIONS_PATH="$PROJECT_PATH/solutions/java-solutions"
TESTS_PATH="$PROJECT_PATH/tests"

TESTS_MODULES="info.kgeorgiy.java.advanced"
SOLUTIONS_MODULE="info.kgeorgiy.ja.dunaev"

TESTS_MODULES_PATH="$TESTS_PATH/modules/$TESTS_MODULES"

MODULES_SOURCES=""
MODULES_NAMES=""
for file in $TESTS_PATH/modules/*; do
  MODULES_SOURCES+=" --module-source-path $(basename $file)=$file"
  MODULES_NAMES+=" --module $(basename $file)"
done

javadoc -d "$CURRENT_DIR/doc" \
  -private \
  -version \
  -author \
  --module-source-path "$SOLUTIONS_MODULE=$SOLUTIONS_PATH" \
  $MODULES_SOURCES \
  --module-path "$TESTS_PATH/lib:$PROJECT_PATH/solutions/lib" \
  --module "$SOLUTIONS_MODULE" \
  $MODULES_NAMES
