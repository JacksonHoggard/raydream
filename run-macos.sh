#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR_PATH="${SCRIPT_DIR}/target/raydream-jar-with-dependencies.jar"

if [[ ! -f "${JAR_PATH}" ]]; then
  echo "Jar not found at ${JAR_PATH}" >&2
  echo "Build it first with: mvn clean package" >&2
  exit 1
fi

java -XstartOnFirstThread --enable-native-access=ALL-UNNAMED -jar "${JAR_PATH}" "$@"
