#!/bin/sh

CURRENT_DIR="$(pwd)"
PROJECT_PATH="$CURRENT_DIR/../.."
SOLUTIONS_PATH="$PROJECT_PATH/solutions/java-solutions"
TESTS_PATH="$PROJECT_PATH/tests"

TESTS_MODULES="info.kgeorgiy.java.advanced"
SOLUTIONS_MODULE="info.kgeorgiy.ja.dunaev"

TESTS_MODULES_PATH="$TESTS_PATH/modules/$TESTS_MODULES"

javadoc -d "$CURRENT_DIR/doc" \
  -private \
  -version \
  -author \
  --module-source-path "$SOLUTIONS_MODULE=$SOLUTIONS_PATH" \
  --module-source-path "$TESTS_MODULES.base=$TESTS_MODULES_PATH.base" \
  --module-source-path "$TESTS_MODULES.walk=$TESTS_MODULES_PATH.walk" \
  --module-source-path "$TESTS_MODULES.arrayset=$TESTS_MODULES_PATH.arrayset" \
  --module-source-path "$TESTS_MODULES.student=$TESTS_MODULES_PATH.student" \
  --module-source-path "$TESTS_MODULES.implementor=$TESTS_MODULES_PATH.implementor" \
  --module-source-path "$TESTS_MODULES.iterative=$TESTS_MODULES_PATH.iterative" \
  --module-path "$TESTS_PATH/lib" \
  --module "$SOLUTIONS_MODULE" \
  --module "$TESTS_MODULES.base" \
  --module "$TESTS_MODULES.walk" \
  --module "$TESTS_MODULES.arrayset" \
  --module "$TESTS_MODULES.student" \
  --module "$TESTS_MODULES.implementor" \
  --module "$TESTS_MODULES.iterative"
