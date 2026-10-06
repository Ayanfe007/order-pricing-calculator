#!/usr/bin/env bash
# Compiles the main sources and the JUnit tests. Needs only a JDK (11+).
set -e
cd "$(dirname "$0")"
mkdir -p out out-test
javac -encoding UTF-8 -d out $(find src/main/java -name '*.java')
javac -encoding UTF-8 -cp "out:lib/junit-platform-console-standalone-1.10.2.jar" \
      -d out-test $(find src/test/java -name '*.java')
echo "Build OK."
echo "  Run the app:   ./run.sh        (or: java -cp out store.ConsoleApp)"
echo "  Run the tests: ./test.sh"
