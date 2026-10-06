#!/usr/bin/env bash
# Runs the JUnit 5 test suite with the bundled console standalone launcher.
set -e
cd "$(dirname "$0")"
java -jar lib/junit-platform-console-standalone-1.10.2.jar execute \
     --class-path "out:out-test" --scan-classpath
