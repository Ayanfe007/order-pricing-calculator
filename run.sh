#!/usr/bin/env bash
# Runs the console application (reads from the terminal, or from stdin when piped).
set -e
cd "$(dirname "$0")"
java -cp out store.ConsoleApp
