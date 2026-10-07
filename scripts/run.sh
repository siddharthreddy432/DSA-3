#!/usr/bin/env bash
set -euo pipefail

# Navigate to project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

# Check java runtime
if ! command -v java &> /dev/null; then
    echo "[ERROR] java runtime not found on PATH."
    exit 1
fi

if [ -f "target/kill-chain-correlation-engine-1.0.0.jar" ]; then
    java -jar "target/kill-chain-correlation-engine-1.0.0.jar" "$@"
    exit $?
fi

if [ -f "target/classes/engine/Main.class" ]; then
    java -cp "target/classes" engine.Main "$@"
    exit $?
fi

echo "[ERROR] Application is not compiled. Please run scripts/build.sh first."
exit 1
